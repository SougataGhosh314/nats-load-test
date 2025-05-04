package com.sougata.natsloadcore.model;

import com.sougata.natsloadcore.config.LoadTestConfig;
import lombok.Data;

@Data
public class LoadTestResult {
    LoadTestConfig loadTestConfig;
    Result result;
}