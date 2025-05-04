package com.sougata.natsloadcore.runner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sougata.natsloadcore.config.LoadTestConfig;
import com.sougata.natsloadcore.generator.RateLimitedLoadGenerator;
import com.sougata.natsloadcore.metrics.MetricsCollector;
import com.sougata.natsloadcore.model.LoadTestResult;

public class LoadTestRunner {

    private LoadTestConfig config;

    public LoadTestRunner() {
        // allow no-arg construction
    }

    public LoadTestRunner(LoadTestConfig config) {
        this.config = config;
    }

    public void run(LoadTestConfig config) {
        this.config = config;
        RateLimitedLoadGenerator generator = new RateLimitedLoadGenerator();
        MetricsCollector metricsCollector = generator.runTest(config);

        LoadTestResult result = metricsCollector.getResult(config);

        System.out.println("Load test complete. Results:");

        try {
            System.out.println((new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
