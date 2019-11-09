package com.tterrag.chatmux.pollster.objects;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
@RequiredArgsConstructor(onConstructor = @__({@JsonCreator}))
public class Poll {
    
    int id;
    String title;
    
    @JsonProperty("start_time")
    Instant startTime;
    Duration duration;
    @JsonProperty("end_time")
    Instant endTime;

    List<Option> options;
}
