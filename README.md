# NATS Load Testing Utility

A modular and configurable load testing utility for benchmarking NATS-based services or clients. It allows you to simulate sustained traffic at varying transaction-per-second (TPS) levels and gather detailed latency and error metrics — ideal for testing scalability and responsiveness of NATS consumers or services.

---

## ✨ Features

### 🔧 `nats-load-core`
The core module provides reusable components for building load test runners:

- **Configurable load simulation**
    - Adjustable TPS (transactions per second)
    - Fixed test duration
    - Rate-limited generator using precise microsecond scheduling

- **Correlation and response tracking**
    - Tracks request/response round-trips using unique correlation IDs
    - Measures and collects latency metrics per response

- **Latency and error metrics**
    - Uses [HdrHistogram](http://hdrhistogram.org/) for high-resolution latency tracking
    - Records:
        - Total requests
        - Error count
        - Missing responses
        - Average, min, max, and percentile latencies (e.g., 50th, 95th, 99th)

- **Minimal implementation requirement**
    - You just need to implement a `LoadTestClient` that can send messages and handle responses.
    - Your client gets injected with config, metrics, and a correlation tracker.

---

### 🚀 `nats-load-runner`
An example runner module built using the core components.

Includes:

- A sample `UserCreateTestClient` implementation that publishes messages to a NATS subject and tracks acknowledgments.
- `RunUserCreateTest` — a self-contained `main()` class that reads a JSON config and runs the test.
- Easy to extend with your own client class by changing the `testClientClass` field in the config.

---

## 📦 Usage

### 1. Define a `LoadTestClient`

Implement the following interface in your own test module:

```java
public interface LoadTestClient {
    void sendNext();   // Called repeatedly at the configured TPS
    void onResponse(String correlationId, long sentTimestamp); // Call this when response arrives
    void close();      // Optional cleanup
}
```

 - You get:
Access to the config object
A CorrelationTracker to register each sent request
A MetricsCollector to report errors and latency

2. Write a main() method
   Use the LoadTestRunner to wire everything together:

```java

public class RunMyTest {
    public static void main(String[] args) throws Exception {
        LoadTestConfig config = loadConfig("my-config.json");
        new LoadTestRunner().run(config);
    }

    private static LoadTestConfig loadConfig(String name) {
        InputStream in = RunMyTest.class.getClassLoader().getResourceAsStream(name);
        return new ObjectMapper().readValue(in, LoadTestConfig.class);
    }
}
```

Provide a JSON config

```json
{
  "testName": "user-create-test",
  "durationSeconds": 10,
  "targetTps": 1000,
  "testClientClass": "com.sougata.natsloadrunner.client.UserCreateTestClient",
  "clientConfig": {
    "requestSubject": "user.create",
    "responseSubject": "user.response",
    "natsUrl": "nats://localhost:4222"
  },
  "headersToTrack": [
    "correlationId",
    "creationTs"
  ]
}


```

Sample Metrics Output

```json

{
  "loadTestConfig" : {
    "testName" : "user-create-test",
    "durationSeconds" : 10,
    "targetTps" : 100,
    "testClientClass" : "com.sougata.natsloadrunner.client.UserCreateTestClient",
    "clientConfig" : {
      "requestSubject" : "user.create",
      "responseSubject" : "user.response",
      "natsUrl" : "nats://localhost:4222"
    },
    "headersToTrack" : [ "correlationId", "creationTs" ]
  },
  "result" : {
    "successCount" : 1001,
    "errorCount" : 1,
    "totalLatencyMillis" : 1205.79,
    "latencyPercentiles" : {
      "p50" : 1.17,
      "p60" : 1.237,
      "p75" : 1.333,
      "p90" : 1.498,
      "p95" : 1.594,
      "p99" : 2.159,
      "p99.9" : 5.811
    },
    "sustainedThroughput" : 100.1,
    "averageLatencyInMillis" : 1.2045854,
    "throughputDropPercent" : 0.0
  }
}

```

Project Structure

nats-load-core/
├── LoadTestRunner.java
├── RateLimitedLoadGenerator.java
├── MetricsCollector.java
├── CorrelationTracker.java
├── LoadTestClient.java
├── LoadTestConfig.java

nats-load-runner/
├── RunUserCreateTest.java
├── UserCreateTestClient.java
└── resources/sample-config.json

🧩 Extending the Utility

To test your own NATS-based microservice or consumer:
Implement LoadTestClient to publish your domain-specific messages.
Subscribe and process responses (or simulate acknowledgments).
Register correlation IDs and call onResponse(...) to record round-trip latency.
Plug your class name into the JSON config and run the test!

✅ Requirements

Java 17+
Gradle or Maven
NATS Java client
(Optional) Docker for running a local NATS server


🧪 Tip: Tuning for High TPS

Use async publishing for higher throughput.
Preallocate buffers to reduce GC overhead.
Monitor system CPU and GC metrics to identify bottlenecks.
Latency increases as you approach system limits — analyze carefully!