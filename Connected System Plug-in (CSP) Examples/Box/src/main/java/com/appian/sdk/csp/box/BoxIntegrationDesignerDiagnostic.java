package com.appian.sdk.csp.box;

import java.util.LinkedHashMap;
import java.util.Map;

public class BoxIntegrationDesignerDiagnostic {

  private Boolean isEnabled;
  private Map<String, Object> requestDiagnostics;
  private Map<String, Object> responseDiagnostics;
  private Long totalExecutionTime;

  public BoxIntegrationDesignerDiagnostic(Boolean isEnabled) {
    this.isEnabled = isEnabled;
    this.requestDiagnostics = new LinkedHashMap<>();
    this.responseDiagnostics = new LinkedHashMap<>();
    this.totalExecutionTime = 0L;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void putRequestDiagnostic(String key, Object value) {
    this.requestDiagnostics.put(getDiagnosticKey(key), value);
  }

  public void putResponseDiagnostic(String key, Object value) {
    this.responseDiagnostics.put(getDiagnosticKey(key), value);
  }

  private String getDiagnosticKey(String key) {
    return key;
  }

  public Map<String,Object> getRequestDiagnostics() {
    return this.requestDiagnostics;
  }

  public Map<String,Object> getResponseDiagnostics() {
    return this.responseDiagnostics;
  }

  public Long getTotalExecutionTime() {
    return this.totalExecutionTime;
  }

  public void addExecutionTime(Long executionTime) {
    this.totalExecutionTime += executionTime;
  }
}
