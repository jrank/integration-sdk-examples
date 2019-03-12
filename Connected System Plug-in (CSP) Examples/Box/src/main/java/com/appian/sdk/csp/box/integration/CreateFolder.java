package com.appian.sdk.csp.box.integration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.BoxJSONRequestWithDiagnostics;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxJSONResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@TemplateId(name="CreateFolder")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class CreateFolder extends AbstractBoxIntegration {

  public static final String FOLDER_NAME = "folderName";
  public static final String PARENT_FOLDER_ID = "parentFolderID";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    SimpleConfiguration config = integrationConfiguration.setProperties(
      // SDK: Operation description should be shown somewhere by default, even better in the create dialog!
      textProperty(OPERATION_DESCRIPTION)
        .isReadOnly(true)
        .build(),
      textProperty(FOLDER_NAME)
        .label("Name")
        .instructionText("The desired name for the folder. Box supports folder names of 255 characters or less. Names cannot contain non-printable ASCII characters, \"/\" or \"\\\", names with trailing spaces, or the special names '.' and '..'.")
        .isRequired(true)
        .isExpressionable(true)
        .build(),
      textProperty(PARENT_FOLDER_ID)
        .label("Parent Folder ID")
        .instructionText("The ID of the parent folder. The root folder of a Box account is always represented by the ID '0'.")
        .isRequired(true)
        .isExpressionable(true)
        .build()
    );

    // SDK: Would like to set this fixed, default value when creating the property
    config.setValue(OPERATION_DESCRIPTION, getOperationDescription());

    return config;
  }

  @Override
  protected String getOperationDescription() {
    return "Create a new folder. A full folder object is returned if the parent folder ID is valid and if no name collisions occur.";
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    // TODO: Move to abstract base class?
    BoxIntegrationDesignerDiagnostic diagnostics = new BoxIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());
    BoxPlatformConnectedSystem.addRequestDiagnostics(diagnostics.getRequestDiagnostics(), connectedSystemConfiguration, executionContext);

    // Get integration inputs
    String folderName = integrationConfiguration.getValue(FOLDER_NAME);
    String parentFolderId = integrationConfiguration.getValue(PARENT_FOLDER_ID);

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Create the request
      URL url = BoxFolder.CREATE_FOLDER_URL.build(conn.getBaseURL());
      BoxJSONRequestWithDiagnostics request = new BoxJSONRequestWithDiagnostics(conn, url, "POST", diagnostics);

      Map<String, Object> parent = new HashMap<>();
      parent.put("id", parentFolderId);
      Map<String, Object> folder = new HashMap<>();
      folder.put("name", folderName);
      folder.put("parent", parent);
      String body = new ObjectMapper().writeValueAsString(folder);
      request.setBody(body);

      // Execute the request
      BoxJSONResponse response = (BoxJSONResponse) request.send();

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> result = mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});

      return createSuccessResponse(result, executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
