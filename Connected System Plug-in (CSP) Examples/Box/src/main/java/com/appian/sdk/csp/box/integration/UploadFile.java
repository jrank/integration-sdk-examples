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
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.appian.sdk.csp.box.BoxService;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxUser;

@TemplateId(name="UploadFile")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class UploadFile extends AbstractBoxIntegration {

  public static final int CHUNKED_UPLOAD_MINIMUM = 20000;

//  public static final String USER_TYPE = "userType";
//  public static final String USER_TYPE_APP = "userTypeApp";
//  public static final String USER_TYPE_USER = "userTypeUser";
  public static final String USER_ID = "userId";
  public static final String DOCUMENT = "document";
  public static final String FILE_NAME = "fileName";
  public static final String PARENT_FOLDER_ID = "parentFolderID";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(connectedSystemConfiguration, executionContext);

    List<Choice> connectAsChoices = new LinkedList<>();
    BoxUser currentUser = BoxUser.getCurrentUser(conn);
    connectAsChoices.add(Choice.builder().name("Connected System " + " (" + currentUser.getInfo().getName() + ", " + currentUser.getID() + ")").value(currentUser.getID()).build());
    for (BoxUser.Info userInfo : BoxUser.getAllEnterpriseUsers(conn)) {
      connectAsChoices.add(Choice.builder().name(userInfo.getName() + " (" + userInfo.getID() + ")").value(userInfo.getID()).build());
    }

    SimpleConfiguration config = integrationConfiguration.setProperties(
//      textProperty(USER_TYPE)
//        .label("Connect As")
//        .choices(
//          Choice.builder().name("Application").value(USER_TYPE_APP).build(),
//          Choice.builder().name("User").value(USER_TYPE_USER).build()
//        )
//        .instructionText("Select the type of identity to use")
//        .isRequired(true)
//        .refresh(RefreshPolicy.ALWAYS)
//        .build(),
      textProperty(USER_ID)
        .label("Connect As")
        .choices(connectAsChoices.toArray(new Choice[0]))
        .instructionText("Lists user-owned documents instead of application-owned documents")
        .isRequired(true)
        .isExpressionable(true)
        .build(),
      documentProperty(DOCUMENT)
        .label("Document")
        .instructionText("The document to upload")
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

    if (integrationConfiguration.getValue(USER_ID) == null) {
      integrationConfiguration.setValue(USER_ID, currentUser.getID());
    }

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
    String parentFolderId = integrationConfiguration.getValue(PARENT_FOLDER_ID);

    BoxIntegrationDesignerDiagnostic diagnostic = new BoxIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());

    try {
      BoxService service = getService(connectedSystemConfiguration, executionContext, diagnostic);

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
