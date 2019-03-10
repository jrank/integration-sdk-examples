package com.appian.sdk.csp.box.integration;

import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.box.sdk.BoxAPIException;

public abstract class AbstractBoxIntegration extends AbstractIntegration {

  public static final String OPERATION_DESCRIPTION = "operationDescription";

  @Override
  protected SimpleConfiguration getHeaderConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    SimpleConfiguration config = integrationConfiguration.setProperties(
      // SDK: Operation description should be shown somewhere by default, even better in the create dialog!
      textProperty(OPERATION_DESCRIPTION)
        .isReadOnly(true)
        .build()
    );

    // SDK: Would like to set this fixed, default value when creating the property
    config.setValue(OPERATION_DESCRIPTION, getOperationDescription());

    return config;
  }

  protected abstract String getOperationDescription();

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
