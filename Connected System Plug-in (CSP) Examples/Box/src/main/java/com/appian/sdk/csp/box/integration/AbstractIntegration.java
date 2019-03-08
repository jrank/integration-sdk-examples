package com.appian.sdk.csp.box.integration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.box.sdk.BoxAPIException;

public abstract class AbstractIntegration extends SimpleIntegrationTemplate {

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

  private static final String BOX_ERROR_TITLE = "error.box.title";
  protected IntegrationError createBoxExceptionError(Exception e, ExecutionContext executionContext, Map<String, Object> responseDiagnostic) {
    if (e instanceof BoxAPIException) {

      BoxAPIException ex = (BoxAPIException)e;
      responseDiagnostic.put("Status Code", ex.getResponseCode());
      //      for (Map.Entry<String, java.util.List<String>> header : ex.getHeaders().entrySet()) {
      //        responseDiagnostic.put(header.getKey(), header.getValue());
      //      }
      responseDiagnostic.put("Headers", ex.getHeaders());
      responseDiagnostic.put("Body", ex.getResponse());

      return IntegrationError.builder()
        .title(BOX_ERROR_TITLE)
        .message(ex.getMessage())
        .detail(DEFAULT_ERROR_DETAIL)
        .build();

    } else {

      return createExceptionError(e, executionContext, responseDiagnostic);

    }
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
