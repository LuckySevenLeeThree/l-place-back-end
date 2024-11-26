package com.lslt.l_place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ChatMessageDTO {
    private String sender;
    private String content;
    private String timestamp;
}
