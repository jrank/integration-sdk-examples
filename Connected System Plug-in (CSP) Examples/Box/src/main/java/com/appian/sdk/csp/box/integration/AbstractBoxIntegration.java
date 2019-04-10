package com.appian.sdk.csp.box.integration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.appian.sdk.csp.box.BoxService;
import com.appian.sdk.csp.box.LocalizableIntegrationError;
import com.appian.sdk.csp.box.MultiStepIntegrationDesignerDiagnostic;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractBoxIntegration extends AbstractIntegration {

  private BoxService boxService;
  BoxService getService(SimpleConfiguration connectedSystemConfiguration, ExecutionContext executionContext, MultiStepIntegrationDesignerDiagnostic diagnostics) {
    if (this.boxService == null) {
      BoxPlatformConnectedSystem.addRequestDiagnostics(diagnostics.getRequestDiagnostics(), connectedSystemConfiguration, executionContext);

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      this.boxService = new BoxService(conn, diagnostics);
    }
    return boxService;
  }

  IntegrationResponse createSuccessResponse(Map<String,Object> result, ExecutionContext executionContext, MultiStepIntegrationDesignerDiagnostic diagnostic) {
    return super.createSuccessResponse(
      result,
      executionContext,
      diagnostic
    );
  }

  public IntegrationResponse createExceptionResponse(Exception e, ExecutionContext executionContext, MultiStepIntegrationDesignerDiagnostic diagnostic) {
    return super.createExceptionResponse(
      e,
      executionContext,
      diagnostic
    );
  }

  private static final String BOX_ERROR_TITLE = "error.box.title";
  private static final String BOX_ERROR_DETAIL = "error.box.detail";
  private static final String BOX_ERROR_DETAIL_WITH_URL = "error.box.detailWithUrl";
  @Override
  LocalizableIntegrationError createExceptionError(Exception e, MultiStepIntegrationDesignerDiagnostic diagnostic) {
    if (e instanceof BoxAPIException) {

      BoxAPIException ex = (BoxAPIException)e;

      diagnostic.putResponseDiagnostic("Status Code", ex.getResponseCode());
      diagnostic.putResponseDiagnostic("Headers", getFlattenedHeaders(ex.getHeaders()));

      // Create default error, override fields later if more specific values can be extracted from the response
      LocalizableIntegrationError error = new LocalizableIntegrationError();
      error.setTitle(BOX_ERROR_TITLE);
      error.setMessage(ex.getMessage());
      error.setDetail(BOX_ERROR_DETAIL);

      if (ex.getResponse() != null) {
        try {
          ObjectMapper mapper = new ObjectMapper();
          Map<String, Object> errorMap = mapper.readValue(ex.getResponse(), new TypeReference<Map<String, Object>>(){});
          diagnostic.putResponseDiagnostic("Body", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorMap));

          Integer status = (Integer)errorMap.get("status");
          String code = (String)errorMap.get("code");

          // Override message with error from the response body
          String message = (String)errorMap.get("message");
          error.setMessage(message);

          // Check for detailed error messages
          if (errorMap.containsKey("context_info")) {
            Object contextInfoObj = errorMap.get("context_info");
            if (contextInfoObj instanceof String) {
              // Override message with string from context info (non-i18n)
              error.setMessage((String)contextInfoObj);

            } else if (contextInfoObj instanceof Map) {
              // TODO: Check for conflicts? Can also just use message in this case
              Map<String,Object> contextInfo = (Map<String,Object>)contextInfoObj;
              List<Map<String,Object>> errors = (List<Map<String,Object>>)contextInfo.get("errors");
              if (errors != null && errors.size() > 0) {
                // Override message with first returned error message (non-i18n)
                Map<String,Object> detail = errors.get(0);
                String errorReason = (String)detail.get("reason");
                String errorName = (String)detail.get("name");
                String errorMessage = (String)detail.get("message");

                error.setMessage(errorMessage);
              }
            }
          }

          // Check for help url and add to error detail
          String helpUrl = (String)errorMap.get("help_url");
          if (helpUrl != null) {
            error.setDetail(BOX_ERROR_DETAIL_WITH_URL, helpUrl);
          }

        } catch (Exception e2) {
          e2.printStackTrace();
          diagnostic.putResponseDiagnostic("Body", ex.getResponse());
        }
      }

      return error;

    } else {

      return super.createExceptionError(e, diagnostic);

    }
  }

  protected String getBundleBaseName() {
    return "resources";
  }

  protected Map<String, Object> getFlattenedHeaders(Map<String, List<String>> headers) {
    Map<String, Object> flattenedHeaders = new LinkedHashMap<>();
    for (Map.Entry<String,List<String>> entry : headers.entrySet()) {
      flattenedHeaders.put(entry.getKey(), String.join(";", entry.getValue()));
    }
    return flattenedHeaders;
  }
}
