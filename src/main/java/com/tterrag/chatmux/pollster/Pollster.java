package com.tterrag.chatmux.pollster;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.pf4j.Extension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tterrag.chatmux.api.bridge.ChatChannel;
import com.tterrag.chatmux.api.bridge.ChatMessage;
import com.tterrag.chatmux.api.bridge.ChatService;
import com.tterrag.chatmux.api.wiretap.WiretapPlugin;
import com.tterrag.chatmux.pollster.objects.Vote;
import com.tterrag.chatmux.pollster.objects.VoteList;
import com.tterrag.chatmux.util.http.RequestHelper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Extension
@Slf4j
public class Pollster implements WiretapPlugin {
    
    private static final RequestHelper API = new PollsterRequestHelper(new ObjectMapper());
    
    private static final Set<ChatMessage<?>> MESSAGE_CACHE = Collections.newSetFromMap(new WeakHashMap<>());
    
    @Override
    public <M extends ChatMessage<M>> Mono<Void> onMessage(M msg, ChatChannel<M> from, ChatChannel<?> to) {
        if (!MESSAGE_CACHE.add(msg)) {
            System.out.println(msg + " (" + msg.getClass().getSimpleName() + " @ " + System.identityHashCode(msg) + ")");
            if (msg.getContent().startsWith("!vote")) {
                String vote = msg.getContent().replace("!vote", "").trim();
                return API.post("/poll/vote", new VoteList(new Vote(msg.getUser(), vote)), JsonNode.class)
                        .doOnNext(content -> log.info("Response: {}", content))
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
        new Pollster().onMessage(new Message(), new ChatChannel<Message>() {

            @Override
            public String getName() {
                return "discord";
            }

            @Override
            public ChatService<Message> getService() {
                return null;
            }

            @Override
            public Flux<Message> connect() {
                throw new UnsupportedOperationException();
            }
            
        }, null).block();
    }
}
