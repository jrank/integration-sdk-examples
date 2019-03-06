package com.appian.sdk.csp.box.objects;

import java.util.LinkedHashMap;
import java.util.Map;

public class Item {

  private com.box.sdk.BoxItem.Info info;

  public Item(com.box.sdk.BoxItem.Info info) {
    this.info = info;
  }

  public String getType() {
    return info.getType();
  }

  public String getId() {
    return info.getID();
  }

  public String getName() {
    return info.getName();
  }

  public String getDescription() {
    return info.getDescription();
  }

  /*
   * Box "size" is a long and may not fit in an int.
   */
  public String getSize() {
    return String.valueOf(info.getSize());
  }

  /*
   * Box actually returns a "mini-object" for getParent(), and it looks like another API call is incurred to
   * retrieve it. We could create a full folder reference here, but not all details are included so the
   * designer would still need to run an integration to get all fields. This might be an opportunity to use
   * the option to select only certain fields, or to have a toggle for "get parent" in the integration
   * template.
   */
  public String getParentId() {
    com.box.sdk.BoxFolder.Info parent = info.getParent();
    if (parent != null) {
      return parent.getID();
    }
    return null;
  }

  public String getStatus() {
    return info.getItemStatus();
  }

  /*
   * Similar to parent folder, need to determine the best way to deal with related objects.
   */
  public String getOwnerId() {
    com.box.sdk.BoxUser.Info owner = info.getOwnedBy();
    if (owner != null) {
      return owner.getID();
    }
    return null;
  }

  public Map<String,Object> toMap() {
    Map<String,Object> map = new LinkedHashMap<>();

    map.put("type", getType());
    map.put("id", getId());
    map.put("name", getName());
    map.put("description", getDescription());
    map.put("status", getStatus());
    map.put("size", getSize());
    map.put("ownerId", getOwnerId());
    map.put("parentId", getParentId());

    return map;
  }

  public static Map<String,Object> toMap(com.box.sdk.BoxItem.Info info) {
    Item item = new Item(info);
    return item.toMap();
  }
}
