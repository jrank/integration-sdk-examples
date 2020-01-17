package com.appian.sdk.csp.box;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.box.sdk.BoxItem;
import com.eclipsesource.json.JsonObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Tester {

  public static void main(String[] args) throws Exception {
    String json = "{\n" + "    \"total_count\": 24,\n" + "    \"entries\": [\n" + "        {\n" +
      "            \"type\": \"folder\",\n" + "            \"id\": \"192429928\",\n" +
      "            \"sequence_id\": \"1\",\n" + "            \"etag\": \"1\",\n" +
      "            \"name\": \"Stephen Curry Three Pointers\"\n" + "        },\n" + "        {\n" +
      "            \"type\": \"file\",\n" + "            \"id\": \"818853862\",\n" +
      "            \"sequence_id\": \"0\",\n" + "            \"etag\": \"0\",\n" +
      "            \"name\": \"Warriors.jpg\"\n" + "        }\n" + "    ],\n" + "    \"offset\": 0,\n" +
      "    \"limit\": 2,\n" + "    \"order\": [\n" + "        {\n" + "            \"by\": \"type\",\n" +
      "            \"direction\": \"ASC\"\n" + "        },\n" + "        {\n" +
      "            \"by\": \"name\",\n" + "            \"direction\": \"ASC\"\n" + "        }\n" + "    ]\n" +
      "}";

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});

    Long totalCount = Long.valueOf(map.get("total_count").toString());
    Long actualStartIndex = Long.valueOf(map.get("offset").toString());
    Long actualBatchSize = Long.valueOf(map.get("limit").toString());

    Collection<Map<String, Object>> items = Arrays.asList((Map<String, Object>[])map.get("entries"));

    return;
  }
}
