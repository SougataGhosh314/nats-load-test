package com.sougata.natsloadcore.correlation;

public class CorrelationResult {
    private long latencyMillis;
    private boolean error;

    public CorrelationResult(long latencyMillis, boolean error) {
        this.latencyMillis = latencyMillis;
        this.error = error;
    }

    public long getLatencyMillis() { return latencyMillis; }
    public boolean isError() { return error; }
}
