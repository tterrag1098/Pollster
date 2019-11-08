package com.tterrag.chatmux.pollster.objects;

import lombok.Value;

@Value 
public class VoteList {
    
    Vote[] votes;
    
    public VoteList(Vote... votes) {
        this.votes = votes;
    }
}