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
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.BoxUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BoxService {

  private BoxDeveloperEditionAPIConnection connection;
  private BoxIntegrationDesignerDiagnostic diagnostic;

  public BoxService(BoxDeveloperEditionAPIConnection connection, BoxIntegrationDesignerDiagnostic diagnostic) {
    this.connection = connection;
    this.diagnostic = diagnostic;
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

    // TODO: Use chunked upload for larger files
  public Map<String, Object> uploadFile(String parentFolderId, InputStream inputStream, String filename, Long fileSize, Long documentId)
    throws IOException {

    URL url = BoxFolder.UPLOAD_FILE_URL.build(this.connection.getBaseUploadURL());
    BoxMultipartRequestWithDiagnostics request = new BoxMultipartRequestWithDiagnostics(this.connection, url);

    Map<String, Object> parent = new HashMap<>();
    parent.put("id", parentFolderId);
    Map<String, Object> file = new HashMap<>();
    file.put("name", filename);
    file.put("parent", parent);
    String attributes = new ObjectMapper().writeValueAsString(file);
    request.putField("attributes", attributes);
    request.setFile(inputStream, filename, fileSize, documentId);

    // Execute the request
    BoxJSONResponse response = (BoxJSONResponse)send(request);

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> result = mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});
    return ((List<Map<String, Object>>)result.get("entries")).get(0);
  }

  public void deleteFile(String fileId) {
    URL url = BoxFile.FILE_URL_TEMPLATE.build(this.connection.getBaseURL(), fileId);
    BoxAPIRequestWithDiagnostics request = new BoxAPIRequestWithDiagnostics(this.connection, url, "DELETE");

    send(request);
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

  protected BoxAPIResponse send(BoxAPIRequest request) {
    if (!this.diagnostic.isEnabled()) {
      // Just send the request if diagnostics are disabled
      return request.send();
    }

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

  protected void addRequestDiagnostics(BoxAPIRequest request) {
    Map<String, Object> requestDiagnostics = this.diagnostic.getRequestDiagnostics();

    requestDiagnostics.put(DIAGNOSTIC_URL, request.getUrl().toString());
    requestDiagnostics.put(DIAGNOSTIC_METHOD, request.getMethod());

    if (request instanceof BoxAPIRequestWithDiagnostics) {
      BoxAPIRequestWithDiagnostics requestWithDiagnostics = (BoxAPIRequestWithDiagnostics)request;
      requestDiagnostics.put(DIAGNOSTIC_HEADERS, getHeadersAsStrings(requestWithDiagnostics.getHeaders()));

    } else if (request instanceof BoxJSONRequestWithDiagnostics) {
      BoxJSONRequestWithDiagnostics requestWithDiagnostics = (BoxJSONRequestWithDiagnostics)request;
      requestDiagnostics.put(DIAGNOSTIC_HEADERS, getHeadersAsStrings(requestWithDiagnostics.getHeaders()));
      if (requestWithDiagnostics.getBodyAsJsonValue() != null) {
        requestDiagnostics.put(DIAGNOSTIC_BODY, requestWithDiagnostics.getBodyAsJsonValue().toString());
      }

    } else if (request instanceof BoxMultipartRequestWithDiagnostics) {
      BoxMultipartRequestWithDiagnostics requestWithDiagnostics = (BoxMultipartRequestWithDiagnostics)request;
      requestDiagnostics.put(DIAGNOSTIC_HEADERS, getHeadersAsStrings(requestWithDiagnostics.getHeaders()));
      if (requestWithDiagnostics.getBodyParts() != null) {
        for (Map.Entry<String, Object> part : requestWithDiagnostics.getBodyParts().entrySet()) {
          requestDiagnostics.put(DIAGNOSTIC_BODY_PART + part.getKey(), part.getValue());
        }
      }
    }
  }

  protected void addResponseDiagnostics(BoxAPIResponse response) {
    Map<String, Object> responseDiagnostic = this.diagnostic.getResponseDiagnostics();

    responseDiagnostic.put(DIAGNOSTIC_STATUS_CODE, response.getResponseCode());
    responseDiagnostic.put(DIAGNOSTIC_HEADERS, response.getHeaders());
    if (response instanceof BoxJSONResponse) {
      responseDiagnostic.put(DIAGNOSTIC_BODY, ((BoxJSONResponse)response).getJSON());
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
