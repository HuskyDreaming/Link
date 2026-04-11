package com.huskydreaming.link.common.configuration;

import java.util.Collections;
import java.util.List;

public record ServerConfig(List<String> commands) {

    public static ServerConfig empty() {
        return new ServerConfig(Collections.emptyList());
    }
}