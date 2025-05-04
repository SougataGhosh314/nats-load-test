package com.sougata.natsloadcore.config;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class LoadTestConfig {
    private String testName;
    private int durationSeconds;
    private int targetTps;
    private String testClientClass;
    private Map<String, String> clientConfig;
    private List<String> headersToTrack;
}
