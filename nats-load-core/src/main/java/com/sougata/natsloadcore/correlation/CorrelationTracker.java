package com.sougata.natsloadcore.correlation;

import java.util.concurrent.ConcurrentHashMap;

public class CorrelationTracker {

    private final ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<>();

    public void track(String correlationId) {
        startTimes.put(correlationId, System.nanoTime());
    }

    public long markCompleteAndReturnLatency(String correlationId) {
        Long startTime = startTimes.remove(correlationId);
        if (startTime == null) {
            return -1;
        }
        return (System.nanoTime() - startTime) / 1000; // return micros
    }

    public int getOutstandingCount() {
        return startTimes.size();
    }
}
