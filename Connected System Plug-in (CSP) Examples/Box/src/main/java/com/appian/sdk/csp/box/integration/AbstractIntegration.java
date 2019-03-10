package com.appian.sdk.csp.box.integration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;

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

  protected SimpleConfiguration getHeaderConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration;
  }

  protected SimpleConfiguration getMainConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration;
  }

  protected SimpleConfiguration getFooterConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration;
  }

  protected IntegrationResponse createSuccessResponse(
    Map<String,Object> result,
    ExecutionContext executionContext,
    Long executeStart,
    Long executeEnd,
    Map<String,Object> requestDiagnostic,
    Map<String,Object> responseDiagnostic) {

    IntegrationDesignerDiagnostic diagnostic = null;
    if (executionContext.isDiagnosticsEnabled()) {
      diagnostic = IntegrationDesignerDiagnostic.builder()
        .addExecutionTimeDiagnostic(executeEnd - executeStart)
        .addRequestDiagnostic(requestDiagnostic)
        .addResponseDiagnostic(responseDiagnostic)
        .build();
    }

    return IntegrationResponse
      .forSuccess(result)
      .withDiagnostic(diagnostic)
      .build();
  }

  protected IntegrationResponse createExceptionResponse(Exception e, ExecutionContext executionContext, Long executionTime, Map<String, Object> requestDiagnostic, Map<String, Object> responseDiagnostic) {
    IntegrationError unlocalizedError = createExceptionError(e, executionContext, responseDiagnostic);

    IntegrationDesignerDiagnostic diagnostic = null;
    if (executionContext.isDiagnosticsEnabled()) {
      diagnostic = IntegrationDesignerDiagnostic.builder()
        .addRequestDiagnostic(requestDiagnostic)
        .addResponseDiagnostic(responseDiagnostic)
        .addExecutionTimeDiagnostic(executionTime)
        .addErrorDiagnostic(getLocalizedError(getDesignerBundle(executionContext), unlocalizedError))
        .build();
    }

    return IntegrationResponse
      .forError(getLocalizedError(getExecutionBundle(executionContext), unlocalizedError))
      .withDiagnostic(diagnostic)
      .build();
  }

  private static final String DEFAULT_ERROR_TITLE = "error.default.title";
  private static final String DEFAULT_ERROR_DETAIL = "error.default.detail";
  private static final String EXCEPTION_STACKTRACE = "Exception";
  protected IntegrationError createExceptionError(Exception e, ExecutionContext executionContext, Map<String, Object> responseDiagnostic) {
    if (executionContext.isDiagnosticsEnabled()) {
      // Add the exception stacktrace to the response diagnostics
      StringWriter stackTrace = new StringWriter();
      e.printStackTrace(new PrintWriter(stackTrace));
      responseDiagnostic.put(EXCEPTION_STACKTRACE, stackTrace.toString());
    }

    return IntegrationError.builder()
      .title(DEFAULT_ERROR_TITLE)
      .message(e.getMessage())
      .detail(DEFAULT_ERROR_DETAIL)
      .build();
  }

  protected IntegrationError getLocalizedError(ResourceBundle bundle, IntegrationError unlocalizedError) {
    return IntegrationError.builder()
      .title(getLocalizedString(bundle, unlocalizedError.getTitle()))
      .message(getLocalizedString(bundle, unlocalizedError.getMessage()))
      .detail(getLocalizedString(bundle, unlocalizedError.getDetail()))
      .build();
  }

  protected String getLocalizedString(ResourceBundle bundle, String keyOrValue) {
    if (bundle.containsKey(keyOrValue)) {
      return bundle.getString(keyOrValue);
    }
    return keyOrValue;
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


  private Map<String, Object> requestDiagnostic;
  private Map<String, Object> responseDiagnostic;

  protected Map<String, Object> getRequestDiagnostic() {
    if (this.requestDiagnostic == null) {
      this.requestDiagnostic = new LinkedHashMap<>();
    }
    return this.requestDiagnostic;
  }

  protected Map<String, Object> getResponseDiagnostic() {
    if (this.responseDiagnostic == null) {
      this.responseDiagnostic = new LinkedHashMap<>();
    }
    return this.responseDiagnostic;
  }

  protected void addRequestDiagnostic(String key, Object value) {
    getRequestDiagnostic().put(key, value);
  }

  protected void addResponseDiagnostic(String key, Object value) {
    getResponseDiagnostic().put(key, value);
  }


}
