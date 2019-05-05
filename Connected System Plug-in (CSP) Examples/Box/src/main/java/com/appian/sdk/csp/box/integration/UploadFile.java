package com.appian.sdk.csp.box.integration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Choice;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.configuration.RefreshPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.appian.sdk.csp.box.BoxService;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

@TemplateId(name="UploadFile")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class UploadFile extends AbstractBoxIntegration {

  public static final int CHUNKED_UPLOAD_MINIMUM = 20000;

  public static final String DOCUMENT = "document";
  public static final String FILE_NAME = "fileName";
  public static final String FOLDER_ID = "folderId";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    SimpleConfiguration config = integrationConfiguration.setProperties(
      boxUserIdProperty(connectedSystemConfiguration),
      documentProperty(DOCUMENT)
        .label("Document")
        .instructionText("The document to upload")
        .isRequired(true)
        .isExpressionable(true)
        .build(),
      textProperty(FILE_NAME)
        .label("File Name")
        .instructionText("The desired name for the file. If blank the name of the document will be used. Box supports file names of 255 characters or less. Names cannot contain non-printable ASCII characters, \"/\" or \"\\\", names with trailing spaces, or the special names '.' and '..'.")
        .isExpressionable(true)
        .build(),
      getBoxFolderBrowserPropertyBuilder(connectedSystemConfiguration, integrationConfiguration, FOLDER_ID)
        .label("Destination Folder")
        .instructionText("The destination folder for the file")
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
    Document document = integrationConfiguration.getValue(DOCUMENT);
    String fileName = integrationConfiguration.getValue(FILE_NAME);
    if (fileName == null) {
      fileName = document.getFileName();
    }
    Integer fileSize = document.getFileSize();
    String parentFolderId = integrationConfiguration.getValue(FOLDER_ID);

    BoxIntegrationDesignerDiagnostic diagnostic = new BoxIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());

    try {
      BoxService service = getService(connectedSystemConfiguration, integrationConfiguration, diagnostic);

      service.canUpload(parentFolderId, fileName, fileSize);

      Map<String,Object> result;
      // CHeck file size to determine whether to use chunked upload
      if (fileSize > CHUNKED_UPLOAD_MINIMUM) {
        result = service.uploadLargeFile(parentFolderId, fileName, fileSize, document.getId(), document.getInputStream());
      } else {
        result = service.uploadFile(parentFolderId, fileName, document.getId(), document.getInputStream());
      }

      return createSuccessResponse(result, executionContext, diagnostic);

    } catch (Exception e) {
      return createExceptionResponse(e, executionContext, diagnostic);
    }
  }
}
