package com.sougata.natsloadrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sougata.natsloadcore.config.LoadTestConfig;
import com.sougata.natsloadcore.runner.LoadTestRunner;

import java.io.InputStream;

public class RunUserCreateTest {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream configStream = RunUserCreateTest.class.getClassLoader().getResourceAsStream("sample-config.json");

        if (configStream == null) {
            throw new IllegalStateException("Missing sample-config.json");
        }

        LoadTestConfig config = mapper.readValue(configStream, LoadTestConfig.class);
        new LoadTestRunner().run(config);
    }
}
