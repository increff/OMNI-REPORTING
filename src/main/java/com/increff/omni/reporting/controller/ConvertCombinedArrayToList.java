package com.increff.omni.reporting.controller;

import com.hazelcast.org.json.JSONArray;
import com.hazelcast.org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertCombinedArrayToList {
    public static List<Map<String, String>> convertCombinedArrayToList(JSONArray combinedArray) {

        // Convert JSONArray to List<Map<String, String>> using org.json library
        List<Map<String, String>> resultList = new ArrayList<>();
        
        for (int i = 0; i < combinedArray.length(); i++) {
            JSONObject jsonObject = combinedArray.getJSONObject(i);
            
            // Convert each JSONObject to a Map
            Map<String, String> map = new HashMap<>();
            for (String key : jsonObject.keySet()) {
                map.put(key, jsonObject.getString(key));
            }
            
            // Add the Map to the result list
            resultList.add(map);
        }
        
        // Print the result list

        System.out.println("convertCombinedArrayToList " + resultList);
        return resultList;
    }
}
