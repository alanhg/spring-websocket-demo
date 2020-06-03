package com.example.messagingstompwebsocket;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SendMsg implements Serializable {
    // 发送消息的用户id
    private String uid;

    // 接收消息的用户id
    @NotNull(message = "未选择用户")
    private String toUid;

    // 发送的文本消息
    @NotNull(message = "消息不能为空")
    private String content;
}
