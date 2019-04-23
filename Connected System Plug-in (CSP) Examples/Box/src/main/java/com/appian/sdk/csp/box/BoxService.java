package com.appian.sdk.csp.box;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxCollaborator;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.BoxResource;
import com.box.sdk.BoxUser;
import com.box.sdk.LargeFileUpload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BoxService {

  private BoxDeveloperEditionAPIConnection connection;
  private BoxIntegrationDesignerDiagnostic diagnostic;

  public BoxService(BoxDeveloperEditionAPIConnection connection, BoxIntegrationDesignerDiagnostic diagnostic) {
    this.connection = connection;
    this.diagnostic = diagnostic;
  }

  public void canUpload(String parentFolderId, String fileName, Integer fileSize) {
    if (diagnostic.isEnabled()) {
      Map<String,Object> requestDiagnostic = new LinkedHashMap<>();
      requestDiagnostic.put("parentFolderId", parentFolderId);
      requestDiagnostic.put("name", fileName);
      requestDiagnostic.put("fileSize", fileSize);
      diagnostic.putRequestDiagnostic("BoxFolder - Can Upload", requestDiagnostic);
    }

    long startTime = System.currentTimeMillis();
    try {
      BoxFolder folder = new BoxFolder(connection, parentFolderId);
      folder.canUpload(fileName, fileSize);

      if (diagnostic.isEnabled()) {
        diagnostic.putResponseDiagnostic("BoxFolder - Can Upload", "(Void)");
      }
    } finally {
      diagnostic.addExecutionTime(System.currentTimeMillis() - startTime);
    }
  }

  public Map<String,Object> uploadFile(String parentFolderId, String fileName, Integer documentId, InputStream inputStream) {
    if (diagnostic.isEnabled()) {
      Map<String,Object> requestDiagnostic = new LinkedHashMap<>();
      requestDiagnostic.put("parentFolderId", parentFolderId);
      requestDiagnostic.put("fileContent", "[Document: " + documentId + "]");
      requestDiagnostic.put("name", fileName);
      diagnostic.putRequestDiagnostic("BoxFolder - Upload File", requestDiagnostic);
    }

    long startTime = System.currentTimeMillis();
    try {
      BoxFolder folder = new BoxFolder(connection, parentFolderId);
      BoxFile.Info info = folder.uploadFile(inputStream, fileName);
      Map<String,Object> result = boxFileInfoToMap(info, false);

      if (diagnostic.isEnabled()) {
        diagnostic.putResponseDiagnostic("BoxFolder - Upload File", result);
      }

      return result;

    } finally {
      diagnostic.addExecutionTime(System.currentTimeMillis() - startTime);
    }
  }

  public Map<String,Object> uploadLargeFile(String parentFolderId, String fileName, Integer fileSize, Integer documentId, InputStream inputStream)
    throws IOException, InterruptedException {
    if (diagnostic.isEnabled()) {
      Map<String,Object> requestDiagnostic = new LinkedHashMap<>();
      requestDiagnostic.put("parentFolderId", parentFolderId);
      requestDiagnostic.put("inputStream", "[Document: " + documentId + "]");
      requestDiagnostic.put("fileName", fileName);
      requestDiagnostic.put("fileSize", fileSize);
      diagnostic.putRequestDiagnostic("BoxFolder - Upload Large File", requestDiagnostic);
    }

    long startTime = System.currentTimeMillis();
    try {
      BoxFolder folder = new BoxFolder(connection, parentFolderId);
      BoxFile.Info info = folder.uploadLargeFile(inputStream, fileName, fileSize);
      Map<String,Object> result = boxFileInfoToMap(info, false);

      if (diagnostic.isEnabled()) {
        diagnostic.putResponseDiagnostic("BoxFolder - Upload Large File", result);
      }

      return result;

    } finally {
      diagnostic.addExecutionTime(System.currentTimeMillis() - startTime);
    }
  }

  protected Map<String,Object> boxResourceInfoToMap(BoxResource.Info info) {
    if (info == null) {
      return null;
    }

    Map<String,Object> result = new LinkedHashMap<>();

    result.put("id", info.getID());

    return result;
  }

  protected Map<String,Object> boxItemInfoToMap(BoxItem.Info info, boolean isMini) {
    if (info == null) {
      return null;
    }

    Map<String,Object> result = boxResourceInfoToMap(info);

    result.put("name", info.getName());
    if (!isMini) {
      result.put("description", info.getDescription());
      result.put("createdAt", info.getCreatedAt());
      result.put("createdBy", boxUserInfoToMap(info.getCreatedBy(), true));
      result.put("modifiedAt", info.getModifiedAt());
      result.put("modifiedBy", boxUserInfoToMap(info.getModifiedBy(), true));
      result.put("itemStatus", info.getItemStatus());
      result.put("ownedBy", boxUserInfoToMap(info.getOwnedBy(), true));
      result.put("parent", boxItemInfoToMap(info.getParent(), true));
    }

    return result;
  }

  protected Map<String,Object> boxFileInfoToMap(BoxFile.Info info, boolean isMini) {
    if (info == null) {
      return null;
    }

    Map<String,Object> result = boxItemInfoToMap(info, isMini);

    result.put("extension", info.getExtension());

    return result;
  }

  protected Map<String,Object> boxCollaboratorInfoToMap(BoxCollaborator.Info info, boolean isMini) {
    if (info == null) {
      return null;
    }

    Map<String,Object> result = boxResourceInfoToMap(info);

    result.put("name", info.getName());
    result.put("login", info.getLogin());
    if (!isMini) {
      result.put("createdAt", info.getCreatedAt());
      result.put("modifiedAt", info.getModifiedAt());
    }

    return result;
  }

  protected Map<String,Object> boxUserInfoToMap(BoxUser.Info info, boolean isMini) {
    if (info == null) {
      return null;
    }

    Map<String,Object> result = boxCollaboratorInfoToMap(info, isMini);

    if (!isMini) {
      result.put("status", String.valueOf(info.getStatus()));
      // TODO: Are there other important user fields?
    }

    return result;
  }

  public Map<String, Object> createFolder(String parentFolderId, String folderName) throws IOException {
    URL url = BoxFolder.CREATE_FOLDER_URL.build(this.connection.getBaseURL());
    BoxJSONRequestWithDiagnostics request = new BoxJSONRequestWithDiagnostics(this.connection, url, "POST");

    Map<String, Object> parent = new HashMap<>();
    parent.put("id", parentFolderId);
    Map<String, Object> folder = new HashMap<>();
    folder.put("name", folderName);
    folder.put("parent", parent);
    String body = new ObjectMapper().writeValueAsString(folder);
    request.setBody(body);

    BoxJSONResponse response = (BoxJSONResponse)send(request);

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});
  }

  public Map<String, Object> getFolderInfo(String folderId) throws IOException {
    URL url = BoxFolder.FOLDER_INFO_URL_TEMPLATE.build(this.connection.getBaseURL(), folderId);
    BoxJSONRequestWithDiagnostics request = new BoxJSONRequestWithDiagnostics(this.connection, url, "GET");
    BoxJSONResponse response = (BoxJSONResponse)send(request);

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(response.getJSON(), new TypeReference<Map<java.lang.String,java.lang.Object>>(){});
  }

  public Map<String, Object> getFolderItems(String folderId, Integer offset, Integer limit) throws IOException {
    // TODO: Should paging management go in service or in integration?
    String query = "?offset=" + offset + "&limit=" + limit;

    URL url = BoxFolder.GET_ITEMS_URL.buildWithQuery(this.connection.getBaseURL(), query, folderId);
    BoxJSONRequestWithDiagnostics request = new BoxJSONRequestWithDiagnostics(this.connection, url, "GET");
    BoxJSONResponse response = (BoxJSONResponse)send(request);

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});
  }

  public Map<String, Object> uploadFile(String parentFolderId, InputStream inputStream, String fileName, Long fileSize, Long documentId)
    throws IOException {

    URL url = BoxFolder.UPLOAD_FILE_URL.build(this.connection.getBaseUploadURL());
    BoxMultipartRequestWithDiagnostics request = new BoxMultipartRequestWithDiagnostics(this.connection, url);

    Map<String, Object> parent = new HashMap<>();
    parent.put("id", parentFolderId);
    Map<String, Object> file = new HashMap<>();
    file.put("name", fileName);
    file.put("parent", parent);
    String attributes = new ObjectMapper().writeValueAsString(file);
    request.putField("attributes", attributes);
    request.setFile(inputStream, fileName, fileSize, documentId);

    // Execute the request
    BoxJSONResponse response = (BoxJSONResponse)send(request);

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> result = mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});
    return ((List<Map<String, Object>>)result.get("entries")).get(0);
  }

  public Map<String, Object> uploadLargeFile(String parentFolderId, InputStream inputStream, String fileName, Long fileSize, Long documentId)
    throws IOException, InterruptedException {

    URL url = BoxFolder.UPLOAD_SESSION_URL_TEMPLATE.build(this.connection.getBaseUploadURL());

    // Chunked upload is more complicated and not worth deconstructing to individual requests for diagnostics
    // Add request diagnostics manually
    if (this.diagnostic.isEnabled()) {
      // Distinguish this request from any prior request (eg: pre-flight check)
//      this.diagnostic.nextStep();

//      this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_URL, url.toString());
//      this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_METHOD, "POST");
      this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_CHUNKED_UPLOAD_FILE, "<File content not shown - " + fileName + " (ID: " + documentId + ", Size (bytes): " + fileSize + ")>");
    }

    BoxFile.Info info = (new LargeFileUpload()).upload(this.connection, parentFolderId, inputStream, url, fileName, fileSize);

    // Add response diagnostics manually
    if (this.diagnostic.isEnabled()) {
      this.diagnostic.putResponseDiagnostic("Info", info.toString());
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("type", info.getType());
    result.put("id", info.getID());
    result.put("name", info.getName());
//    ObjectMapper mapper = new ObjectMapper();
//    Map<String, Object> result = mapper.convertValue(info, new TypeReference<Map<String, Object>>() {});

    return result;
/*
  Dictionary
    file_version: Dictionary
        sha1: "514fd5edb57a71e7904da1b7ebb391c8e34b1123"
        id: "457154470362"
        type: "file_version"
    parent: Dictionary
        sequence_id: null (Null)
        name: "All Files"
        etag: null (Null)
        id: "0"
        type: "folder"
    description: null (Text)
    created_at: "2019-04-02T05:34:13-07:00"
    content_created_at: "2019-04-02T05:34:13-07:00"
    owned_by: Dictionary
        name: "Jacob Rank"
        id: "2246041444"
        type: "user"
        login: "jacob.rank@appian.com"
    type: "file"
    item_status: "active"
    created_by: Dictionary
        name: "Jacob Rank"
        id: "2246041444"
        type: "user"
        login: "jacob.rank@appian.com"
    trashed_at: null (Text)
    sha1: "514fd5edb57a71e7904da1b7ebb391c8e34b1123"
    size: 168
    sequence_id: "0"
    name: "wrgv13"
    purged_at: null (Text)
    modified_by: Dictionary
        name: "Jacob Rank"
        id: "2246041444"
        type: "user"
        login: "jacob.rank@appian.com"
    shared_link: null (Text)
    etag: "0"
    content_modified_at: "2019-04-02T05:34:13-07:00"
    id: "432407922762"
    modified_at: "2019-04-02T05:34:13-07:00"
    path_collection: Dictionary
        entries: List of Dictionary: 1 item
            Dictionary
                sequence_id: null (Null)
                name: "All Files"
                etag: null (Null)
                id: "0"
                type: "folder"
        total_count: 1
*/
  }

  public void preflightCheck(String parentFolderId, String filename, Long fileSize)
    throws IOException {

    URL url = BoxFolder.UPLOAD_FILE_URL.build(this.connection.getBaseURL());
    BoxJSONRequestWithDiagnostics request = new BoxJSONRequestWithDiagnostics(this.connection, url, "OPTIONS");

    Map<String, Object> parent = new HashMap<>();
    parent.put("id", parentFolderId);
    Map<String, Object> file = new HashMap<>();
    file.put("name", filename);
    file.put("parent", parent);
    file.put("size", fileSize);
    String body = new ObjectMapper().writeValueAsString(file);
    request.setBody(body);

    // Execute the request
    send(request);
  }

  public void deleteFile(String fileId) {
    URL url = BoxFile.FILE_URL_TEMPLATE.build(this.connection.getBaseURL(), fileId);
    BoxAPIRequestWithDiagnostics request = new BoxAPIRequestWithDiagnostics(this.connection, url, "DELETE");

    send(request);
  }

  public Map<String, Object> getUser(String userId) throws IOException {
    URL url = BoxUser.USER_URL_TEMPLATE.build(this.connection.getBaseURL(), userId);
    BoxJSONRequestWithDiagnostics request = new BoxJSONRequestWithDiagnostics(this.connection, url, "GET");
    BoxJSONResponse response = (BoxJSONResponse)request.send();

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});
  }

  public Map<String, Object> getEnterpriseUsers() throws IOException {
    // TODO: Should paging management go in service or in integration?
    Long offset = 1L;
    Long limit = 100L;
    String query = "?offset=" + offset + "&limit=" + limit;

    URL url = BoxUser.USERS_URL_TEMPLATE.buildWithQuery(this.connection.getBaseURL(), query);
    BoxJSONRequestWithDiagnostics request = new BoxJSONRequestWithDiagnostics(this.connection, url, "GET");
    BoxJSONResponse response = (BoxJSONResponse)send(request);

    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});
  }

  BoxAPIResponse send(BoxAPIRequest request) {
    if (!this.diagnostic.isEnabled()) {
      // Just send the request if diagnostics are disabled
      return request.send();
    }

//    this.diagnostic.nextStep();

    Long executionStartTime = System.currentTimeMillis();

    try {

      addRequestDiagnostics(request);

      BoxAPIResponse response = request.send();

      addResponseDiagnostics(response);

      return response;

    } finally {

      this.diagnostic.addExecutionTime(System.currentTimeMillis() - executionStartTime);

    }
  }

  private static final String DIAGNOSTIC_URL = "URL";
  private static final String DIAGNOSTIC_METHOD = "Method";
  private static final String DIAGNOSTIC_BODY = "Body";
  private static final String DIAGNOSTIC_HEADERS = "Headers";
  private static final String DIAGNOSTIC_STATUS_CODE = "Status Code";
  private static final String DIAGNOSTIC_BODY_PART = "Body Part: ";
  private static final String DIAGNOSTIC_CHUNKED_UPLOAD_FILE = "File (Chunked Upload)";

  void addRequestDiagnostics(BoxAPIRequest request) {
    this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_URL, request.getUrl().toString());
    this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_METHOD, request.getMethod());

    if (request instanceof BoxAPIRequestWithDiagnostics) {
      BoxAPIRequestWithDiagnostics requestWithDiagnostics = (BoxAPIRequestWithDiagnostics)request;
      this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_HEADERS, getHeadersAsStrings(requestWithDiagnostics.getHeaders()));

    } else if (request instanceof BoxJSONRequestWithDiagnostics) {
      BoxJSONRequestWithDiagnostics requestWithDiagnostics = (BoxJSONRequestWithDiagnostics)request;
      this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_HEADERS, getHeadersAsStrings(requestWithDiagnostics.getHeaders()));
      if (requestWithDiagnostics.getBodyAsJsonValue() != null) {
        this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_BODY, requestWithDiagnostics.getBodyAsJsonValue().toString());
      }

    } else if (request instanceof BoxMultipartRequestWithDiagnostics) {
      BoxMultipartRequestWithDiagnostics requestWithDiagnostics = (BoxMultipartRequestWithDiagnostics)request;
      this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_HEADERS, getHeadersAsStrings(requestWithDiagnostics.getHeaders()));
      if (requestWithDiagnostics.getBodyParts() != null) {
        for (Map.Entry<String, Object> part : requestWithDiagnostics.getBodyParts().entrySet()) {
          this.diagnostic.putRequestDiagnostic(DIAGNOSTIC_BODY_PART + part.getKey(), part.getValue());
        }
      }
    }
  }

  void addResponseDiagnostics(BoxAPIResponse response) {
    this.diagnostic.putResponseDiagnostic(DIAGNOSTIC_STATUS_CODE, response.getResponseCode());
    this.diagnostic.putResponseDiagnostic(DIAGNOSTIC_HEADERS, response.getHeaders());
    if (response instanceof BoxJSONResponse) {
      this.diagnostic.putResponseDiagnostic(DIAGNOSTIC_BODY, ((BoxJSONResponse)response).getJSON());
    }
  }

  private Map<String, String> getHeadersAsStrings(List<BoxAPIRequest.RequestHeader> headers) {
    Map<String, String> stringHeaders = new LinkedHashMap<>();
    for (BoxAPIRequest.RequestHeader header : headers) {
      stringHeaders.put(header.getKey(), header.getValue());
    }
    return stringHeaders;
  }


}
