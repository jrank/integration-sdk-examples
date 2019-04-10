package com.appian.sdk.csp.box.integration;

import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxService;
import com.appian.sdk.csp.box.MultiStepIntegrationDesignerDiagnostic;

@TemplateId(name="GetFolderInfo")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class GetFolderInfo extends AbstractBoxIntegration {

  public static final String FOLDER_ID = "folderID";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration.setProperties(
      textProperty(FOLDER_ID)
        .label("Folder ID")
        .instructionText("The root folder of a Box account is always represented by the ID '0'.")
        .isRequired(true)
        .isExpressionable(true)
        .build()
    );
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    // Get integration inputs
    String folderId = integrationConfiguration.getValue(FOLDER_ID);

    MultiStepIntegrationDesignerDiagnostic diagnostics = new MultiStepIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());

    try {
      BoxService service = getService(connectedSystemConfiguration, executionContext, diagnostics);

      Map<String, Object> result = service.getFolderInfo(folderId);

      return createSuccessResponse(result, executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
