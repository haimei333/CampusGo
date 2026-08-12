package com.campusgo.api.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    /** 一期仅 TEXT；预留 IMAGE */
    private String msgType = "TEXT";

    @NotBlank(message = "消息不能为空")
    @Size(max = 2000, message = "消息不能超过 2000 字")
    private String content;
}
