package com.sougata.natsloadcore.generator;

import com.sougata.natsloadcore.config.LoadTestConfig;
import com.sougata.natsloadcore.correlation.CorrelationTracker;
import com.sougata.natsloadcore.metrics.MetricsCollector;
import com.sougata.natsloadcore.client.LoadTestClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimitedLoadGenerator {

    public MetricsCollector runTest(LoadTestConfig config) {
        MetricsCollector metricsCollector = new MetricsCollector();
        CorrelationTracker correlationTracker = new CorrelationTracker();

        LoadTestClient client = createClient(config, metricsCollector, correlationTracker);

        int tps = config.getTargetTps();
        long intervalMicros = 1000000L / tps;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                client.sendNext();
            } catch (Exception e) {
                metricsCollector.recordError();
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, intervalMicros, TimeUnit.MICROSECONDS);

        // Schedule a delayed shutdown after test duration
        scheduleShutdown(scheduler, config.getDurationSeconds() + 1, client);

        // Wait for termination with a timeout (e.g. +10 seconds buffer)
        try {
            if (!scheduler.awaitTermination(config.getDurationSeconds() + 5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow(); // Force if not terminated
                client.close();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            client.close();
            Thread.currentThread().interrupt();
        }

        int missing = correlationTracker.getOutstandingCount();
        metricsCollector.recordMissingResponses(missing);

        return metricsCollector;
    }

    private void scheduleShutdown(ScheduledExecutorService executor, int delaySeconds, LoadTestClient client) {
        executor.schedule(() -> {
            executor.shutdown();
            client.close();
        }, delaySeconds, TimeUnit.SECONDS);
    }


    private LoadTestClient createClient(LoadTestConfig config, MetricsCollector metricsCollector, CorrelationTracker correlationTracker) {
        try {
            Class<?> clazz = Class.forName(config.getTestClientClass());
            return (LoadTestClient) clazz.getConstructor(LoadTestConfig.class, MetricsCollector.class, CorrelationTracker.class)
                    .newInstance(config, metricsCollector, correlationTracker);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate LoadTestClient", e);
        }
    }
}
