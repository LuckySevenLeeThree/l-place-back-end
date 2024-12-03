package com.lslt.l_place.controller;

import com.lslt.l_place.dto.CursorDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CursorController {
    @MessageMapping("/cursors")
    @SendTo("/topic/cursors")
    public CursorDTO processCursor(CursorDTO cursorDTO) {
        return cursorDTO;
    }
}
