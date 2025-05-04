package com.sougata.natsloadrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sougata.natsloadcore.config.LoadTestConfig;
import com.sougata.natsloadcore.runner.LoadTestRunner;

import java.io.InputStream;

import static com.sougata.natsloadcore.contants.Constants.LOAD_TEST_CONFIG_FILENAME;

public class RunUserCreateTest {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream configStream = RunUserCreateTest.class.getClassLoader().getResourceAsStream(LOAD_TEST_CONFIG_FILENAME);

        if (configStream == null) {
            throw new IllegalStateException("Missing load_test_config.json");
        }

        LoadTestConfig config = mapper.readValue(configStream, LoadTestConfig.class);
        new LoadTestRunner().run(config);
    }
}
