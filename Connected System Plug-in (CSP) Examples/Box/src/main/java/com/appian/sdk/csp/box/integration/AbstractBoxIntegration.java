package com.appian.sdk.csp.box.integration;

import java.util.Map;

import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.box.sdk.BoxAPIException;

public abstract class AbstractBoxIntegration extends AbstractIntegration {

  private static final String BOX_ERROR_TITLE = "error.box.title";
  private static final String BOX_ERROR_DETAIL = "error.box.detail";
  @Override
  protected IntegrationError createExceptionError(Exception e, ExecutionContext executionContext, Map<String, Object> responseDiagnostic) {
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
        .detail(BOX_ERROR_DETAIL)
        .build();

    } else {

      return super.createExceptionError(e, executionContext, responseDiagnostic);

    }
  }

  protected String getBundleBaseName() {
    return "resources";
  }


}
