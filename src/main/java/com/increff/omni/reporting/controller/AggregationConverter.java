package com.increff.omni.reporting.controller;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AggregationConverter {

    public static List<Document> convertAggregationQuery(String queryString) {
        List<Document> stages = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\{[^{}]*?\\}");
        Matcher matcher = pattern.matcher(queryString);

        while (matcher.find()) {
            String stageString = matcher.group();
            Document stage = Document.parse(stageString);
            stages.add(stage);
        }

        return stages;
    }

    public static void main(String[] args) {

    }
}