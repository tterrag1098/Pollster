package com.tterrag.chatmux.pollster.objects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value
public class Poll {
    
    int id;
    String title;
    
    @JsonProperty("start_time")
    String startTime;
    String duration;
    @JsonProperty("end_time")
    String endTime;

    List<Option> options;
}
