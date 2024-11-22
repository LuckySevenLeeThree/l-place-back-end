package com.lslt.l_place.service;

import com.lslt.l_place.dto.ChatMessageDTO;

public interface ChatService {
    void sendMessage(ChatMessageDTO message);
}
