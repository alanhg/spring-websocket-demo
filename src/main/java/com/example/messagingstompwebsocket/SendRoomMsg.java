package com.example.messagingstompwebsocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendRoomMsg<T> implements Serializable {
    // 发送的文本消息
    @NotNull(message = "消息不能为空")
    private T content;
    private String room;
    private String uid;
}
