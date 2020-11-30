var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    if ($('#uuid').val() === '') {
        alert('uuid required!');
        return;
    }
    var socket = new SockJS('/websocket?uid=' + $('#uuid').val());
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function () {
        setConnected(true);
        console.log('系统订阅开启');
        stompClient.subscribe('/topic/public', function (greeting) {
            let obj = $('#broadcastHistory');
            obj.text(obj.text() + '\r\n' + greeting.body);
        });
    });

    const _transportClose = socket._transportClose;
    socket._transportClose = function (code, reason) {
        if (this._transport && this._transport.close) {
            this._transport.close();
        }
        _transportClose.call(this, code, reason);
    }
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendBroadcastCommand() {
    stompClient.send(`/app/broadcast`, {}, JSON.stringify({
        content: $('#broadcastCnt').val()
    }));
}

$(function () {
    let roomSubscription, roomId;
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#broadcastBtn").click(function () {
        sendBroadcastCommand();
    });
    $("#roomBtn").click(function () {
        roomId = $('#roomId').val();
        roomSubscription = stompClient.subscribe(`/topic/${roomId}`, function (greeting) {
            const cnt = $("#roomHistory").html() + `<hr>roomId:${roomId} : ${greeting.body}`;
            $("#roomHistory").html(cnt);
        });
    });
    $("#roomLeaveBtn").click(function () {
        stompClient.unsubscribe(roomSubscription.id, {
            simpDestination: roomId
        });
    });

    /**
     * 指定房间发送消息
     */
    $("#roomMessageBtn").click(function () {
        const roomId = $('#roomId').val();
        if (!roomId) {
            alert('roomId required!');
        }
        stompClient.send(`/app/send2Room/${roomId}`, {}, JSON.stringify({
            content: $('#roomMessage').val()
        }));
    });
});

