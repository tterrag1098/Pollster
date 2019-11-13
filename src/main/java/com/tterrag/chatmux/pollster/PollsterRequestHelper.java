package com.tterrag.chatmux.pollster;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.tterrag.chatmux.pollster.objects.Poll;
import com.tterrag.chatmux.util.http.RequestHelper;
import com.tterrag.chatmux.util.reactor.Monos;

import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;


public class PollsterRequestHelper extends RequestHelper {

    protected PollsterRequestHelper() {
        super(new ObjectMapper()
                    .registerModule(new Jdk8Module())
                    .registerModule(new JavaTimeModule())
                    .registerModule(new ParameterNamesModule()),
                System.getProperty("pollster.apiUrl"));
    }

    @Override
    protected void addHeaders(HttpHeaders headers) {
        headers
            .add("Content-Type", "application/json")
            .add("User-Agent", "ChatMux-Pollster/1.0")
            .add("Authorization", "Bearer " + System.getProperty("pollster.authKey"));
    }
    
    @Override
    protected <T> Mono<T> handleResponse(HttpClientResponse resp, ByteBufMono body, JavaType type) {
        if (type.getRawClass() == Response.class) {
            return super.handleResponse(resp, body, type);
        } else {
            return super.<Response<T>>handleResponse(resp, body, TypeFactory.defaultInstance().constructParametricType(Response.class, type))
                    .transform(Monos.mapOptional(Response::getData));
        }
    }
    
    public <T> Mono<Response<T>> getResponse(String endpoint, Class<? extends T> type) {
        return this.get(endpoint, TypeFactory.defaultInstance().constructParametricType(Response.class, type));
    }
    
    public Mono<Poll> getCurrentPoll() {
        return get("/poll/current", Poll.class);
    }
    
    public Mono<Response<Poll>> getCurrentPollResponse() {
        return getResponse("/poll/current", Poll.class);
    }
}
