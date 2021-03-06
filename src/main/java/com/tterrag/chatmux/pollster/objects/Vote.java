package com.tterrag.chatmux.pollster.objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor = @__({@JsonCreator}))
public class Vote {
    
    String user;
    String key;
}