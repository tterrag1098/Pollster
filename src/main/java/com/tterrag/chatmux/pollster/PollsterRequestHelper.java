package com.tterrag.chatmux.pollster;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.tterrag.chatmux.pollster.objects.AcceptedVotes;
import com.tterrag.chatmux.pollster.objects.Poll;
import com.tterrag.chatmux.pollster.objects.Vote;
import com.tterrag.chatmux.pollster.objects.VoteList;
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
    
    protected <T> Mono<T> get(String endpoint, String key, Class<T> type) {
        return get(endpoint, JsonNode.class).map(n -> n.get(key)).map(n -> mapper.convertValue(n, type));
    }
    
    public Mono<Poll> getCurrentPoll() {
        return get("/polls/current", "poll", Poll.class)
                .onErrorResume(IOException.class, $ -> Mono.empty());
    }
    
    public Mono<AcceptedVotes> vote(int id, Vote... votes) {
        return vote(id, Arrays.asList(votes));
    }
    
    public Mono<AcceptedVotes> vote(int id, List<Vote> votes) {
        return post("/polls/vote/" + id, new VoteList(votes), AcceptedVotes.class);
    }
}
