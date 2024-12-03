package com.lslt.l_place.controller;

import com.lslt.l_place.dto.CursorDTO;
import com.lslt.l_place.dto.CursorRemoveDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CursorController {


    private static final Logger log = LoggerFactory.getLogger(CursorController.class);

    @MessageMapping("/cursors")
    @SendTo("/topic/cursors")
    public CursorDTO processCursor(CursorDTO cursorDTO) {
        return cursorDTO;
    }

    @MessageMapping("/cursors/remove")
    @SendTo("/topic/cursors/remove")
    public CursorRemoveDTO removeCursor(CursorRemoveDTO cursorDTO) {
        log.info(cursorDTO.getUsername());
        return cursorDTO; // 필요에 따라 null 또는 다른 메시지를 반환할 수 있습니다.
    }
}
