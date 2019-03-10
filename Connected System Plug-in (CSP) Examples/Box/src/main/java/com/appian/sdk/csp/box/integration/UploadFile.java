package com.appian.sdk.csp.box.integration;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.BoxMultipartRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@TemplateId(name="UploadFile")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class UploadFile extends AbstractBoxIntegration {

  public static final String DOCUMENT = "document";
  public static final String FILE_NAME = "fileName";
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
      documentProperty(DOCUMENT)
        .label("Document")
        .instructionText("The document to upload.")
        .isRequired(true)
        .isExpressionable(true)
        .build(),
      textProperty(FILE_NAME)
        .label("Name")
        .instructionText("The desired name for the file. If blank the name of the document will be used. Box supports file names of 255 characters or less. Names cannot contain non-printable ASCII characters, \"/\" or \"\\\", names with trailing spaces, or the special names '.' and '..'.")
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


    // SDK: Shouldn't be so hard to add properties in different methods
//    List<PropertyDescriptor> properties = integrationConfiguration.getProperties();
//    properties.add(documentProperty(DOCUMENT)
//      .label("Document")
//      .instructionText("The document to upload.")
//      .isRequired(true)
//      .isExpressionable(true)
//      .build()
//    );
//    properties.add(textProperty(FILE_NAME)
//      .label("Name")
//      .instructionText("The desired name for the file. If blank the name of the document will be used. Box supports file names of 255 characters or less. Names cannot contain non-printable ASCII characters, \"/\" or \"\\\", names with trailing spaces, or the special names '.' and '..'.")
//      .isExpressionable(true)
//      .build()
//    );
//    properties.add(textProperty(PARENT_FOLDER_ID)
//      .label("Parent Folder ID")
//      .instructionText("The ID of the parent folder. The root folder of a Box account is always represented by the ID '0'.")
//      .isRequired(true)
//      .isExpressionable(true)
//      .build()
//    );

//    integrationConfiguration.setProperties(properties.toArray(new PropertyDescriptor[]{}));

//    return integrationConfiguration;
  }

  @Override
  protected String getOperationDescription() {
    return "Add a new file. A file object is returned inside of a collection if the ID is valid and if the update is successful.";
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    Map<String, Object> requestDiagnostic = new LinkedHashMap<>();
    Map<String, Object> responseDiagnostic = new LinkedHashMap<>();
    BoxPlatformConnectedSystem.addRequestDiagnostics(requestDiagnostic, connectedSystemConfiguration, executionContext);

    // Get integration inputs
    Document document = integrationConfiguration.getValue(DOCUMENT);
    String fileName = integrationConfiguration.getValue(FILE_NAME);
    if (fileName == null) {
      fileName = document.getFileName();
    }
    String parentFolderId = integrationConfiguration.getValue(PARENT_FOLDER_ID);

    Long executeStart = null;

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Create the request
      URL url = BoxFolder.UPLOAD_FILE_URL.build(conn.getBaseUploadURL());
      String method = "POST";
      BoxMultipartRequest request = new BoxMultipartRequest(conn, url);

      Map<String, Object> parent = new HashMap<>();
      parent.put("id", parentFolderId);
      Map<String, Object> file = new HashMap<>();
      file.put("name", fileName);
      file.put("parent", parent);
      String attributes = new ObjectMapper().writeValueAsString(file);
      request.putField("attributes", attributes);
      // TODO: Use chunked upload for larger files
      request.setFile(document.getInputStream(), fileName);

      // Log the request
      requestDiagnostic.put("URL", url.toString());
      requestDiagnostic.put("Method", method);
      requestDiagnostic.put("Attributes", attributes);
      // TODO: Standardize this string representation (i18n?)
      requestDiagnostic.put("File", "Binary content not shown for document, " + fileName + " (" + document.getFileSize() + ")");

      executeStart = System.currentTimeMillis();

      // Execute the request
      BoxJSONResponse response = (BoxJSONResponse) request.send();

      Long executeEnd = System.currentTimeMillis();

      responseDiagnostic.put("Status Code", response.getResponseCode());
      responseDiagnostic.put("Headers", response.getHeaders());
      responseDiagnostic.put("Body", response.getJSON());

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> result = mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});
      result = ((List<Map<String, Object>>)result.get("entries")).get(0);

      return createSuccessResponse(result, executionContext, executeStart, executeEnd, requestDiagnostic, responseDiagnostic);

    } catch (Exception e) {

      Long executeEnd = System.currentTimeMillis();

      return createExceptionResponse(e, executionContext, executeEnd - executeStart, requestDiagnostic, responseDiagnostic);
    }
  }
}
