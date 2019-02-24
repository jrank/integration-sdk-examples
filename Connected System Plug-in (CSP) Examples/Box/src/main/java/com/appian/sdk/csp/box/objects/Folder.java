package com.appian.sdk.csp.box.objects;

public class Folder extends Item {

  private com.box.sdk.BoxFolder.Info info;

  public Folder(com.box.sdk.BoxFolder.Info info) {
    super(info);
    this.info = info;
  }

//  public static Map<String,Object> toMap(Folder folder) {
//    return super.toMap(folder);
//  }
//
//  public static Map<String,Object> toMap(com.box.sdk.Folder.Info info) {
//    Folder folder = new Folder(info);
//    return toMap(folder);
//  }
}
