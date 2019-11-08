package com.tterrag.chatmux.pollster.objects;

import lombok.Value;

@Value
public class Option {

    char key;
    String title;
    int results;
}
