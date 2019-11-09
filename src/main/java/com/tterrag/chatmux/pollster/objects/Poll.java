package com.tterrag.chatmux.pollster.objects;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor = @__({@JsonCreator}))
@JsonIgnoreProperties(ignoreUnknown = true)
public class Poll {
    
    int id;
    String title;
    
    @JsonProperty("start_time")
    Instant startTime;
    @JsonProperty("interpreted_duration")
    Duration duration;
    @JsonProperty("end_time")
    Instant endTime;

    List<Option> options;
    
    public String formatMessage() {
        StringBuilder ret = new StringBuilder(
                title + " (#" + id + ")\n"
                        + "From: " + startTime + " to " + endTime + " (" + duration + ")\n\n"
                        + "Choices:\n");
        for (Option option : options) {
            ret.append("  ").append(option.getTitle()).append(" (").append(option.getKey()).append("): ").append(option.getResults()).append(" votes\n");
        }
        return ret.toString();
    }
}
