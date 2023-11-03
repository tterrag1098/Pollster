package com.tterrag.chatmux.pollster.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor = @__({@JsonCreator}))
@JsonIgnoreProperties(ignoreUnknown = true)
public class Option {

    char key;
    String title;
    int results;
}
