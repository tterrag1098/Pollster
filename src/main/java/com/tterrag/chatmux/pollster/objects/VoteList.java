package com.tterrag.chatmux.pollster.objects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Value;

@Value
public class VoteList {
    
    Vote[] votes;
    
    @JsonCreator
    public VoteList(Vote... votes) {
        this.votes = votes;
    }
    
    public VoteList(List<Vote> votes) {
        this.votes = votes.toArray(new Vote[0]);
    }
}