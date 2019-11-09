package com.tterrag.chatmux.pollster;

import java.util.Locale;

import org.pf4j.Extension;

import com.tterrag.chatmux.api.bridge.ChatMessage;
import com.tterrag.chatmux.api.command.CommandContext;
import com.tterrag.chatmux.api.command.CommandListener;
import com.tterrag.chatmux.pollster.objects.Poll;

import reactor.core.publisher.Mono;

@Extension
public class PollsterCommandListener implements CommandListener {

    @Override
    public <M extends ChatMessage<M>> Mono<?> runCommand(String command, CommandContext<M> ctx) {
        if (command.equals("!pollster")) {
            String[] args = ctx.getSplitArgs();
            if (args.length > 0) {
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "current": 
                        return Pollster.API.getResponse("/poll/current", Poll.class)
                                .flatMap(resp -> resp.getData()
                                        .map(poll -> ctx.reply(poll.formatMessage()))
                                        .orElse(ctx.reply(resp.getMessage())));
                }
            } else {
                return ctx.reply("Available commands: current");
            }
        }
        return Mono.empty();
    }

}
