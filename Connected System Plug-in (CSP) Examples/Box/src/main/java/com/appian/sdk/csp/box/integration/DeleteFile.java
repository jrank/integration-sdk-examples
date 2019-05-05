package com.appian.sdk.csp.box.integration;

import java.util.LinkedHashMap;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxService;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;

@TemplateId(name="DeleteFile")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class DeleteFile extends AbstractBoxIntegration {

  public static final String FILE_ID = "fileId";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    SimpleConfiguration config = integrationConfiguration.setProperties(
      textProperty(FILE_ID)
        .label("File ID")
        .instructionText("The file ID to delete")
        .isRequired(true)
        .isExpressionable(true)
        .build()
    );

    return config;
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    // Get integration inputs
    String fileId = integrationConfiguration.getValue(FILE_ID);

    BoxIntegrationDesignerDiagnostic diagnostics = new BoxIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());

    try {
      BoxService service = getService(connectedSystemConfiguration, integrationConfiguration, diagnostics);

      service.deleteFile(fileId);

      return createSuccessResponse(new LinkedHashMap<>(), executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
