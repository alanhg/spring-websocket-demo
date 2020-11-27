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
        stompClient.subscribe('/topic/sys', function (greeting) {
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
    const xhr = new XMLHttpRequest();
    xhr.open('get', `/broadcast?command=${$('#broadcastCnt').val()}`);
    xhr.send();
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#send").click(function () {
        sendName();
    });
    $("#broadcastBtn").click(function () {
        sendBroadcastCommand();
    });
    $('#topicSubscribeBtn').click(function () {
        const topic = $('#topic').val(); // 订阅某主题
        if (!topic) {
            alert('topic required!');
        }
        stompClient.subscribe(`/topic/${topic}`, function (greeting) {
            $("#topicContent").append(JSON.parse(greeting.body).content);
        });
    });

    $('#sendTopicBtn').click(function () {
        $('sendToTopic')

        const xhr = new XMLHttpRequest();
        xhr.open('get', '/broadcast?command=ttt');
        xhr.send();
    })
});

