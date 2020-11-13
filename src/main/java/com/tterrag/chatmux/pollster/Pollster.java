package com.tterrag.chatmux.pollster;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.tterrag.chatmux.api.bridge.ChatMessage;
import com.tterrag.chatmux.api.bridge.ChatService;
import com.tterrag.chatmux.pollster.objects.AcceptedVotes;
import com.tterrag.chatmux.pollster.objects.Poll;
import com.tterrag.chatmux.pollster.objects.Vote;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class Pollster {
    
    public static final PollsterRequestHelper API = new PollsterRequestHelper();
    
    private static final Set<ChatMessage<?>> MESSAGE_CACHE = Collections.newSetFromMap(new WeakHashMap<>());
    
    private final UnicastProcessor<Vote> voteBuffer = UnicastProcessor.create();
    
    private final Flux<AcceptedVotes> results;
    
    public Pollster() {
        results = voteBuffer.bufferTimeout(10, Duration.ofSeconds(30))
            .doOnNext(votes -> log.info("Publishing {} votes: {}", votes.size(), votes))
            .doOnTerminate(() -> log.error("Vote publisher terminated!"))
            .flatMap(votes -> getCurrentPoll(true)
                    .flatMap(poll -> API.vote(poll.getId(), votes))
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
                        .map(option -> new Vote(msg.getUserId(), option))
                        .doOnNext(vote -> log.info("Buffering vote: {}", vote))
                        .doOnNext(vote -> voteBuffer.onNext(vote))
                        .then();
            }
        }
        return Mono.empty();
    }
    
    private AtomicReference<Poll> currentPoll = new AtomicReference<>();
    private final Scheduler pollLookupThread = Schedulers.newSingle(r -> new Thread(r, "Current Poll Lookup"));
    
    Mono<Poll> getCurrentPoll(boolean update) {
        return Mono.fromSupplier(() -> {
            synchronized (currentPoll) {
                Poll current = currentPoll.get();
                if (update || current == null || current.getEndTime().isBefore(Instant.now())) {
                    return API.getCurrentPoll()
                            .doOnNext(currentPoll::set)
                            .switchIfEmpty(Mono.fromRunnable(() -> currentPoll.set(null)))
                            .block();
                } else {
                    return current;
                }
            }          // Allow blocking
        }).subscribeOn(pollLookupThread);
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
        public String getUserId() {
            return "140245257416736769";
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
