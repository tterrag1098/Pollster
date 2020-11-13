package com.tterrag.chatmux.pollster;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.pf4j.Extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tterrag.chatmux.api.bridge.ChatMessage;
import com.tterrag.chatmux.api.bridge.ChatService;
import com.tterrag.chatmux.api.bridge.Connectable;
import com.tterrag.chatmux.api.command.CommandContext;
import com.tterrag.chatmux.api.command.CommandListener;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Extension
public class PollsterCommandListener implements CommandListener {
    
    private final Pollster pollster = new Pollster();
    
    private final Set<String> connectedChannels = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private ChatService<?> service;
    private Set<String> admins;
    
    @Override
    public Mono<?> onServiceAvailable(Connectable connectable) {
        return readChannels(connectable);
    }
    
    @Override
    public void setAdmins(ChatService<?> service, Set<String> admins) {
        this.service = service;
        this.admins = Collections.unmodifiableSet(admins);
    }

    @Override
    public <M extends ChatMessage<M>> Mono<?> runCommand(String command, CommandContext<M> ctx) {
        if (ctx.getService() == this.service && this.admins.contains(ctx.getUserId()) && command.equals("!pollster")) {
            String[] args = ctx.getSplitArgs();
            if (args.length > 0) {
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "current": 
                        return pollster.getCurrentPoll(true)
                                .flatMap(poll -> ctx.reply(poll.formatMessage()))
                                .switchIfEmpty(ctx.reply("No poll active"));
                    case "listen":
                        Flux<? extends ChatMessage<?>> source;
                        if (args.length > 1) {
                            connectedChannels.add(args[1]);
                            source = ctx.connect(args[1]);
                        } else {
                            connectedChannels.add(ctx.getService().getName() + "/" + ctx.getChannelId());
                            source = ctx.connect();
                        }
                        saveChannels();
                        return source.flatMap(pollster::onMessage).then();
                }
            } else {
                return ctx.reply("Available commands: current, listen");
            }
        }
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> canHandle(ChatService<?> service, String command, String args) {
        return Mono.fromSupplier(() -> service == this.service && command.equals("!pollster"));
    }
    
    private void saveChannels() {
        try {
            new ObjectMapper().writeValue(new File("pollster.json"), connectedChannels);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Mono<Void> readChannels(Connectable connectable) {
        try {
            File file = new File("pollster.json");
            if (file.exists()) {
                Set<String> allLinks = new ObjectMapper().readValue(new File("pollster.json"), new TypeReference<Set<String>>() {});
                return Flux.fromIterable(allLinks)
                        .doOnNext(connectedChannels::add)
                        .flatMap(connectable::connect)
                        .flatMap(pollster::onMessage)
                        .then();
            }
            return Mono.empty();
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
