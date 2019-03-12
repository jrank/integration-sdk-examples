package com.appian.sdk.csp.box;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxDateFormat;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.BoxMultipartRequest;

public class BoxMultipartRequestWithDiagnostics extends BoxMultipartRequest {

  private BoxIntegrationDesignerDiagnostic diagnostic;
  private Map<String, Object> parts;

  // TODO: To support multiple requests, add a diagnostic prefix
  public BoxMultipartRequestWithDiagnostics(BoxAPIConnection api, URL url, BoxIntegrationDesignerDiagnostic diagnostic) {
    super(api, url);

    this.diagnostic = diagnostic;
    this.parts = new HashMap<>();
  }

  @Override
  public BoxAPIResponse send() {
    if (!this.diagnostic.isEnabled()) {
      // Just use the base class if diagnostics are disabled
      return super.send();
    }

    Long executionStartTime = System.currentTimeMillis();

    try {

      addRequestDiagnostics();

      BoxJSONResponse response = (BoxJSONResponse)super.send();

      addResponseDiagnostics(response);

      return response;

    } finally {

      this.diagnostic.addExecutionTime(System.currentTimeMillis() - executionStartTime);

    }
  }

  @Override
  public void putField(String key, String value) {
    StringBuilder part = new StringBuilder();
    part.append("Content-Disposition: form-data; name=\"" + key + "\"\r\n");
    part.append("\r\n");
    part.append(value);
    this.parts.put(key, part.toString());
    super.putField(key, value);
  }

  @Override
  public void putField(String key, Date value) {
    this.putField(key, BoxDateFormat.format(value));
  }

  public void setFile(Document document) throws UnsupportedEncodingException {
    StringBuilder part = new StringBuilder();
    part.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + URLEncoder.encode(document.getFileName(), "UTF-8") + "\"\r\n");
    part.append("Content-Type: application/octet-stream\r\n");
    part.append("\r\n");
    part.append("<File content not shown - " + document.getFileName() + " (ID: " + document.getId() + ")>");
    this.parts.put("file", part.toString());
    super.setFile(document.getInputStream(), document.getFileName(), document.getFileSize());
  }

  private static final String URL = "URL";
  private static final String METHOD = "Method";
  private static final String HEADERS = "Headers";
  private static final String BODY_PART = "Body Part: ";
  private static final String BODY = "Body";
  private static final String STATUS_CODE = "URL";

  protected void addRequestDiagnostics() {
    Map<String, Object> requestDiagnostics = this.diagnostic.getRequestDiagnostics();

    requestDiagnostics.put(URL, this.getUrl().toString());
    requestDiagnostics.put(METHOD, this.getMethod());
    requestDiagnostics.put(HEADERS, getHeadersAsStrings(this.getHeaders()));
//    requestDiagnostics.put(PARTS, this.parts);
    for (Map.Entry<String, Object> part : this.parts.entrySet()) {
      requestDiagnostics.put(BODY_PART + part.getKey(), part.getValue());
    }
  }

  protected void addResponseDiagnostics(BoxJSONResponse response) {
    Map<String, Object> responseDiagnostics = this.diagnostic.getResponseDiagnostics();

    responseDiagnostics.put(STATUS_CODE, response.getResponseCode());
    responseDiagnostics.put(HEADERS, response.getHeaders());
    responseDiagnostics.put(BODY, response.getJSON());
  }

  private Map<String, String> getHeadersAsStrings(List<RequestHeader> headers) {
    Map<String, String> stringHeaders = new LinkedHashMap<>();
    for (RequestHeader header : headers) {
      stringHeaders.put(header.getKey(), header.getValue());
    }
    return stringHeaders;
  }
}
