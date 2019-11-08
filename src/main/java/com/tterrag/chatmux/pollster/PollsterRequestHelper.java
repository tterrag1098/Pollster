package com.tterrag.chatmux.pollster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tterrag.chatmux.util.http.RequestHelper;

import io.netty.handler.codec.http.HttpHeaders;


public class PollsterRequestHelper extends RequestHelper {

    protected PollsterRequestHelper(ObjectMapper mapper) {
        super(mapper, System.getProperty("pollster.apiUrl"));
    }

    @Override
    protected void addHeaders(HttpHeaders headers) {
        headers
            .add("Content-Type", "application/json")
            .add("User-Agent", "ChatMux-Pollster/1.0")
            .add("Authorization", "Bearer " + System.getProperty("pollster.authKey"));
    }
}
