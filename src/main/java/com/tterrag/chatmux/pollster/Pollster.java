package com.tterrag.chatmux.pollster;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import com.tterrag.chatmux.api.bridge.ChatMessage;
import com.tterrag.chatmux.api.bridge.ChatService;
import com.tterrag.chatmux.pollster.objects.Vote;
import com.tterrag.chatmux.pollster.objects.VoteList;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

@Slf4j
public class Pollster {
    
    public static final PollsterRequestHelper API = new PollsterRequestHelper();
    
    private static final Set<ChatMessage<?>> MESSAGE_CACHE = Collections.newSetFromMap(new WeakHashMap<>());
    
    private final UnicastProcessor<Vote> voteBuffer = UnicastProcessor.create();
    
    private final Flux<boolean[]> results;
    
    public Pollster() {
        results = voteBuffer.bufferTimeout(10, Duration.ofSeconds(30))
            .doOnNext(votes -> log.info("Publishing {} votes: {}", votes.size(), votes))
            .doOnTerminate(() -> log.error("Vote publisher terminated!"))
            .flatMap(votes -> API.post("/poll/vote", new VoteList(votes), boolean[].class)
                    .doOnError(t -> log.error("Error publishing votes:", t))
                    .onErrorResume($ -> Mono.empty())
                    .doOnNext(content -> log.info("Response: {}", content)))
            .share();
        
        results.subscribe();
    }
    
    public Mono<Void> onMessage(ChatMessage<?> msg) {
        if (MESSAGE_CACHE.add(msg)) {
            if (msg.getContent().startsWith("!vote")) {
                return Mono.just(msg.getContent().replace("!vote", "").trim())
                        .map(option -> new Vote(msg.getUser(), option))
                        .doOnNext(vote -> log.info("Buffering vote: {}", vote))
                        .doOnNext(vote -> voteBuffer.onNext(vote))
                        .then();
            }
        }
        return Mono.empty();
    }

    private static class Message implements ChatMessage<Message> {

        @Override
        public ChatService<Message> getService() {
            return null;
        }

        @Override
        public String getChannel() {
            return "tterrag";
        }

        @Override
        public String getChannelId() {
            return "tterrag";
        }

        @Override
        public String getUser() {
            return "tterrag";
        }

        @Override
        public String getContent() {
            return "!vote A";
        }

        @Override
        public String getAvatar() {
            return null;
        }

        @Override
        public Mono<Void> delete() {
            return Mono.empty();
        }

        @Override
        public Mono<Void> kick() {
            return Mono.empty();
        }

        @Override
        public Mono<Void> ban() {
            return Mono.empty();
        }
    }
    
    public static void main(String[] args) {
        Pollster pollster = new Pollster();
        pollster.onMessage(new Message()).block();
        pollster.results.doOnNext(res -> log.info("Vote results: {}", res)).blockFirst();
    }
}
