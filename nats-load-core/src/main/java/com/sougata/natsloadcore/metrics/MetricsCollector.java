package com.sougata.natsloadcore.metrics;

import com.sougata.natsloadcore.config.LoadTestConfig;
import com.sougata.natsloadcore.model.LoadTestResult;
import com.sougata.natsloadcore.model.Result;
import org.HdrHistogram.ConcurrentHistogram;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class MetricsCollector {

    private final LongAdder totalLatencyMicros = new LongAdder();
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();
    private final ConcurrentHistogram histogram = new ConcurrentHistogram(3);


    public void recordSuccess(long latencyMicros) {
        totalLatencyMicros.add(latencyMicros);
        successCount.incrementAndGet();
        histogram.recordValue(latencyMicros);
    }

    public void recordError() {
        errorCount.incrementAndGet();
    }

    public void recordMissingResponses(int count) {
        errorCount.addAndGet(count);
    }

    public LoadTestResult getResult(LoadTestConfig loadTestConfig) {
        int expectedTotalRequests = loadTestConfig.getTargetTps() * loadTestConfig.getDurationSeconds();

        LoadTestResult loadTestResult = new LoadTestResult();
        loadTestResult.setLoadTestConfig(loadTestConfig);

        int success = successCount.get();
        int error = errorCount.get();
        long totalLatency = totalLatencyMicros.sum();

        float totalLatencyInMillis = (float) totalLatency / 1000; // to millis

        Result testResult = new Result();

        testResult.setSuccessCount(success);
        testResult.setErrorCount(error);
        testResult.setTotalLatencyMillis(totalLatencyInMillis);

        testResult.setAverageLatencyInMillis(success == 0 ? 0 : totalLatencyInMillis/success);
        testResult.setSustainedThroughput(success / (double) loadTestConfig.getDurationSeconds());
        testResult.setLatencyPercentiles(computePercentiles());

        double dropPercent = 100.0 * (expectedTotalRequests - success) / expectedTotalRequests;
        testResult.setThroughputDropPercent(Math.max(0, dropPercent));

        loadTestResult.setResult(testResult);
        return loadTestResult;
    }

    private Map<String, Float> computePercentiles() {
        Map<String, Float> result = new LinkedHashMap<>();
        int[] percentiles = {50, 60, 75, 90, 95, 99, 99_9};

        for (int p : percentiles) {
            double valueMicros = histogram.getValueAtPercentile(p);
            result.put("p" + (p == 99_9 ? "99.9" : p), (float) (valueMicros / 1000.0)); // to millis
        }

        return result;
    }
}
