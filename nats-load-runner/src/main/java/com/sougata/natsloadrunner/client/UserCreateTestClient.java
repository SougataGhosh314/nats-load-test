package com.sougata.natsloadrunner.client;

import com.sougata.natscore.model.PayloadHeader;
import com.sougata.natscore.model.PayloadWrapper;
import com.sougata.natsloadcore.client.LoadTestClient;
import com.sougata.natsloadcore.config.LoadTestConfig;
import com.sougata.natsloadcore.correlation.CorrelationTracker;
import com.sougata.natsloadcore.metrics.MetricsCollector;
import com.sougata.userprotos.UserRequest;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.impl.Headers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class UserCreateTestClient implements LoadTestClient {

    private Connection natsConnection;
    private String requestSubject;
    private String responseSubject;
    private MetricsCollector metricsCollector;
    private CorrelationTracker correlationTracker;
    private AtomicInteger requestCounter = new AtomicInteger(0);

    public UserCreateTestClient(LoadTestConfig config, MetricsCollector metricsCollector, CorrelationTracker correlationTracker) {
        try {
            this.metricsCollector = metricsCollector;
            this.correlationTracker = correlationTracker;

            this.requestSubject = config.getClientConfig().get("requestSubject");
            this.responseSubject = config.getClientConfig().get("responseSubject");

            String natsUrl = config.getClientConfig().getOrDefault("natsUrl", "nats://localhost:4222");
            this.natsConnection = Nats.connect(natsUrl);

            Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
                try {
                    String correlationId = msg.getHeaders().getFirst(PayloadHeader.CORRELATION_ID.getKey());
                    long latencyMicros = correlationTracker.markCompleteAndReturnLatency(correlationId);
                    if (latencyMicros >= 0) {
                        metricsCollector.recordSuccess(latencyMicros);
                    } else {
                        metricsCollector.recordError();
                    }
                } catch (Exception e) {
                    metricsCollector.recordError();
                }
            });

            dispatcher.subscribe(responseSubject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize UserCreateTestClient", e);
        }
    }

    @Override
    public void sendNext() {
        try {
            int requestId = requestCounter.incrementAndGet();

            String correlationId = UUID.randomUUID().toString();
            long creationTimeMicros = System.nanoTime() / 1000;

            UserRequest request = UserRequest.newBuilder()
                    .putBody("name", "test-user-" + requestId)
                    .build();

            PayloadWrapper<byte[]> payloadWrapper = PayloadWrapper.<byte[]>newBuilder()
                    .setCorrelationId(correlationId)
                    .setCreationTimestamp(String.valueOf(creationTimeMicros))
                    .setPayload(request.toByteArray())
                    .setPayloadType(UserRequest.class.getName())
                    .build();

            correlationTracker.track(correlationId);
            natsConnection.publish(requestSubject, toHeaders(payloadWrapper), payloadWrapper.getPayload());
        } catch (Exception e) {
            metricsCollector.recordError();
        }
    }

    private Headers toHeaders(PayloadWrapper<byte[]> wrapper) {
        Headers headers = new Headers();
        wrapper.getPayloadHeaders().forEach((k, v) -> headers.add(k.getKey(), v));
        return headers;
    }
}
