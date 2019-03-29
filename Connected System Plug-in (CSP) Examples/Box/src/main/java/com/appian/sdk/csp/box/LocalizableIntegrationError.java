package com.appian.sdk.csp.box;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.appian.connectedsystems.templateframework.sdk.IntegrationError;

public class LocalizableIntegrationError {

  String titleKey;
  Object[] titleArguments;
  String messageKey;
  Object[] messageArguments;
  String detailKey;
  Object[] detailArguments;

  public void setTitle(String key, Object... arguments) {
    this.titleKey = key;
    this.titleArguments = arguments;
  }

  public void setMessage(String key, Object... arguments) {
    this.messageKey = key;
    this.messageArguments = arguments;
  }

  public void setDetail(String key, Object... arguments) {
    this.detailKey = key;
    this.detailArguments = arguments;
  }

  public IntegrationError localize(ResourceBundle bundle) {
    return IntegrationError.builder()
      .title(getFormattedString(bundle, titleKey, titleArguments))
      .message(getFormattedString(bundle, messageKey, messageArguments))
      .detail(getFormattedString(bundle, detailKey, detailArguments))
      .build();
  }

  protected String getFormattedString(ResourceBundle bundle, String keyOrValue, Object... arguments) {
    if (keyOrValue != null && bundle.containsKey(keyOrValue)) {
      return MessageFormat.format(bundle.getString(keyOrValue), arguments);
    }
    return keyOrValue;
  }
}
