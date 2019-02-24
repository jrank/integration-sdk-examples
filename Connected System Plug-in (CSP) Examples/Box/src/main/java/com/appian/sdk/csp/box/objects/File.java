package com.appian.sdk.csp.box.objects;

public class File extends Item {

  private com.box.sdk.BoxFile.Info info;

  public File(com.box.sdk.BoxFile.Info info) {
    super(info);
    this.info = info;
  }

//  public static Map<String,Object> toMap(File file) {
//    Map<String,Object> map = new LinkedHashMap<>();
//
//    map.put("id", file.getId());
//    map.put("name", file.getName());
//    map.put("description", file.getDescription());
//    map.put("status", file.getStatus());
//    map.put("size", file.getSize());
//    map.put("ownerId", file.getOwnerId());
//    map.put("parentId", file.getParentId());
//
//    return map;
//  }
//
//  public static Map<String,Object> toMap(com.box.sdk.File.Info info) {
//    File file = new File(info);
//    return toMap(file);
//  }
}
