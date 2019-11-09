package com.tterrag.chatmux.pollster.objects;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class VoteList {
    
    Vote[] votes;
    
    @JsonCreator
    public VoteList(Vote... votes) {
        this.votes = votes;
    }
}