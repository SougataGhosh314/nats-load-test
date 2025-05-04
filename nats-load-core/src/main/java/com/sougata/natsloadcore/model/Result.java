package com.sougata.natsloadcore.model;

import lombok.Data;

import java.util.Map;

@Data
public class Result {
    private int successCount;
    private int errorCount;
    private float totalLatencyMillis;
    private Map<String, Float> latencyPercentiles;
    private double sustainedThroughput; // per second
    private float averageLatencyInMillis;
    private double throughputDropPercent;
}