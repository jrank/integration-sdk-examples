package com.appian.sdk.csp.box.objects;

import java.util.LinkedHashMap;
import java.util.Map;

public class User {

  private com.box.sdk.BoxUser.Info info;

  public User(com.box.sdk.BoxUser.Info info) {
    this.info = info;
  }

  public String getId() {
    return info.getID();
  }

  public String getName() {
    return info.getName();
  }

  public String getLogin() {
    return info.getLogin();
  }

  public Map<String,Object> toMap() {
    Map<String,Object> map = new LinkedHashMap<>();

    map.put("id", getId());
    map.put("name", getName());
    map.put("login", getLogin());

    return map;
  }

  public static Map<String,Object> toMap(com.box.sdk.BoxUser.Info info) {
    User user = new User(info);
    return user.toMap();
  }
}
