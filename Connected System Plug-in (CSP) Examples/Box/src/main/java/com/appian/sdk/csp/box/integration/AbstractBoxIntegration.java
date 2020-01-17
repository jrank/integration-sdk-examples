package com.appian.sdk.csp.box.integration;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.configuration.Choice;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.configuration.RefreshPolicy;
import com.appian.connectedsystems.templateframework.sdk.configuration.TextPropertyDescriptor;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.appian.sdk.csp.box.BoxService;
import com.appian.sdk.csp.box.LocalizableIntegrationError;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractBoxIntegration extends AbstractIntegration {

  public TextPropertyDescriptor.TextPropertyDescriptorBuilder getBoxFolderBrowserPropertyBuilder(SimpleConfiguration connectedSystemConfiguration, SimpleConfiguration integrationConfiguration, PropertyPath updatedPropertyPath, String key) {
    List<Choice> folderChoicesList;

    if (integrationConfiguration.getProperty(key) != null || !new PropertyPath(key).equals(updatedPropertyPath)) {
      // If the property exists and hasn't been update by the designer, reuse existing choices
      folderChoicesList = ((TextPropertyDescriptor)integrationConfiguration.getProperty(key)).getChoices();

    } else {
      // No current property exists, or the property has been updated by the designer
      folderChoicesList = new LinkedList<>();
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(connectedSystemConfiguration);
      String folderId = integrationConfiguration.getValue(key);
      if (folderId == null) {
        // Default to the root folder
        folderId = BoxFolder.getRootFolder(conn).getID();
        integrationConfiguration.setValue(key, folderId);
      }

      BoxFolder folder = new BoxFolder(conn, folderId);
      BoxFolder.Info folderInfo = folder.getInfo();

      // Add the current selection's parent folder
      if (folderInfo.getParent() != null) {
        folderChoicesList.add(Choice.builder()
          .name("< " + folderInfo.getParent().getName() + " (" + folderInfo.getParent().getID() + ")")
          .value(folderInfo.getParent().getID())
          .build()
        );
      }

      // Add the current selection
      folderChoicesList.add(Choice.builder()
        .name(folderInfo.getName() + " (" + folderInfo.getID() + ")")
        .value(folderInfo.getID())
        .build()
      );

      // Add the current selection's child folders
      Iterable<BoxItem.Info> childItems = folder.getChildren("name");
      for (BoxItem.Info childInfo : childItems) {
        if ("folder".equals(childInfo.getType())) {
          folderChoicesList.add(Choice.builder()
            .name(folderInfo.getName() + " > " + childInfo.getName() + " (" + childInfo.getID() + ")")
            .value(childInfo.getID())
            .build()
          );
        }
      }
    }

    return textProperty(key)
      .choices(folderChoicesList.toArray(new Choice[0]))
      .refresh(RefreshPolicy.ALWAYS);
  }

  private BoxService boxService;
  BoxService getService(SimpleConfiguration connectedSystemConfiguration, SimpleConfiguration integrationConfiguration, BoxIntegrationDesignerDiagnostic diagnostic) {
    if (this.boxService == null) {
      BoxPlatformConnectedSystem.addRequestDiagnostic(diagnostic.getRequestDiagnostics(), connectedSystemConfiguration, integrationConfiguration);

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(connectedSystemConfiguration);

      this.boxService = new BoxService(conn, diagnostic);
    }
    return boxService;
  }

  IntegrationResponse createSuccessResponse(Map<String,Object> result, ExecutionContext executionContext, BoxIntegrationDesignerDiagnostic diagnostic) {
    return super.createSuccessResponse(
      result,
      executionContext,
      diagnostic
    );
  }

  public IntegrationResponse createExceptionResponse(Exception e, ExecutionContext executionContext, BoxIntegrationDesignerDiagnostic diagnostic) {
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
  LocalizableIntegrationError createExceptionError(Exception e, BoxIntegrationDesignerDiagnostic diagnostic) {
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
