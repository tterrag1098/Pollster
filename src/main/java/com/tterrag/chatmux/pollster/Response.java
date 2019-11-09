package com.tterrag.chatmux.pollster;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
@RequiredArgsConstructor(onConstructor = @__({@JsonCreator}))
public class Response<T> {
    
    @JsonProperty("data")
    Optional<T> data;

    @JsonProperty("message")
    String message;
}
