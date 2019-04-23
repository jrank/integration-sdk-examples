package com.appian.sdk.csp.box.integration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.ResourceBundle;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.LocalizableIntegrationError;

public abstract class AbstractIntegration extends SimpleIntegrationTemplate {

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    integrationConfiguration = getHeaderConfiguration(integrationConfiguration, connectedSystemConfiguration, propertyPath, executionContext);

    integrationConfiguration = getMainConfiguration(integrationConfiguration, connectedSystemConfiguration, propertyPath, executionContext);

    integrationConfiguration = getFooterConfiguration(integrationConfiguration, connectedSystemConfiguration, propertyPath, executionContext);

    return integrationConfiguration;
  }

  SimpleConfiguration getHeaderConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration;
  }

  SimpleConfiguration getMainConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration;
  }

  SimpleConfiguration getFooterConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration;
  }

  IntegrationResponse createSuccessResponse(
    Map<String,Object> result,
    ExecutionContext executionContext,
    BoxIntegrationDesignerDiagnostic diagnostic) {

    IntegrationDesignerDiagnostic responseDiagnostic = null;
    if (diagnostic.isEnabled()) {
      responseDiagnostic = IntegrationDesignerDiagnostic.builder()
        .addExecutionTimeDiagnostic(diagnostic.getTotalExecutionTime())
        .addRequestDiagnostic(diagnostic.getRequestDiagnostics())
        .addResponseDiagnostic(diagnostic.getResponseDiagnostics())
        .build();
    }

    return IntegrationResponse
      .forSuccess(result)
      .withDiagnostic(responseDiagnostic)
      .build();
  }

  IntegrationResponse createExceptionResponse(Exception e,
    ExecutionContext executionContext,
    BoxIntegrationDesignerDiagnostic diagnostic) {
    LocalizableIntegrationError error = createExceptionError(e, diagnostic);

    IntegrationDesignerDiagnostic responseDiagnostic = null;
    if (diagnostic.isEnabled()) {
      responseDiagnostic = IntegrationDesignerDiagnostic.builder()
        .addExecutionTimeDiagnostic(diagnostic.getTotalExecutionTime())
        .addRequestDiagnostic(diagnostic.getRequestDiagnostics())
        .addResponseDiagnostic(diagnostic.getResponseDiagnostics())
        .addErrorDiagnostic(error.localize(getDesignerBundle(executionContext)))
        .build();
    }

    e.printStackTrace();

    return IntegrationResponse
      .forError(error.localize(getExecutionBundle(executionContext)))
      .withDiagnostic(responseDiagnostic)
      .build();
  }

  private static final String DEFAULT_ERROR_TITLE = "error.default.title";
  private static final String DEFAULT_ERROR_DETAIL = "error.default.detail";
  private static final String EXCEPTION_STACKTRACE = "Exception";
  LocalizableIntegrationError createExceptionError(Exception e, BoxIntegrationDesignerDiagnostic diagnostic) {
    if (diagnostic.isEnabled()) {
      // Add the exception stacktrace to the response diagnostics
      StringWriter stackTrace = new StringWriter();
      e.printStackTrace(new PrintWriter(stackTrace));
      diagnostic.putResponseDiagnostic(EXCEPTION_STACKTRACE, stackTrace.toString());
    }

    LocalizableIntegrationError error = new LocalizableIntegrationError();
    error.setTitle(DEFAULT_ERROR_TITLE);
    error.setMessage(e.getMessage());
    error.setDetail(DEFAULT_ERROR_DETAIL);

    return error;
  }

  protected abstract String getBundleBaseName();

  private ResourceBundle designerBundle;
  protected ResourceBundle getDesignerBundle(ExecutionContext executionContext) {
    if (this.designerBundle == null) {
      this.designerBundle = ResourceBundle.getBundle(getBundleBaseName(), executionContext.getDesignerLocale());
    }
    return this.designerBundle;
  }

  private ResourceBundle executionBundle;
  protected ResourceBundle getExecutionBundle(ExecutionContext executionContext) {
    if (this.executionBundle == null) {
      this.executionBundle = ResourceBundle.getBundle(getBundleBaseName(), executionContext.getExecutionLocale());
    }
    return this.executionBundle;
  }
}
