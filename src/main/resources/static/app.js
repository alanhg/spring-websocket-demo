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
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });

        stompClient.subscribe('/topic/sys', function (greeting) {
            console.log(greeting);
            $('#broadcastContent').text(greeting.body);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

function sendBroadcastCommand() {
    const xhr = new XMLHttpRequest();
    xhr.open('get', '/broadcast?command=ttt');
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
    $("#broadcast").click(function () {
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

