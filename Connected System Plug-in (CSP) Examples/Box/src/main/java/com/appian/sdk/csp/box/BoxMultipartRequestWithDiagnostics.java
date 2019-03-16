package com.appian.sdk.csp.box;

import java.io.InputStream;
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
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxDateFormat;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.BoxMultipartRequest;

public class BoxMultipartRequestWithDiagnostics extends BoxMultipartRequest {

  private Map<String, Object> parts;

  public BoxMultipartRequestWithDiagnostics(BoxAPIConnection api, URL url) {
    super(api, url);

    this.parts = new HashMap<>();
  }

  @Override
  public List<RequestHeader> getHeaders() {
    return super.getHeaders();
  }

  public Map<String,Object> getBodyParts() {
    return this.parts;
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

  public void setFile(InputStream inputStream, String filename, Long fileSize, Long documentId) throws UnsupportedEncodingException {
    StringBuilder part = new StringBuilder();
    part.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"\r\n");
    part.append("Content-Type: application/octet-stream\r\n");
    part.append("\r\n");
    part.append("<File content not shown - " + filename + " (ID: " + documentId + ")>");
    this.parts.put("file", part.toString());
    super.setFile(inputStream, filename, fileSize);
  }
}
