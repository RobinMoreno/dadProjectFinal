var wsocket;
var serviceLocation = "ws://192.168.56.1:8090/gate";
var $chatWindow;

function onMessageReceived(evt) {

	var messageLine = $('<tr><td class="received">' + evt.data + '</td></tr>');
	$( "div#RegP1" ).append(evt.data + '<br>');
	var objDiv = document.getElementById("RegP1");
	objDiv.scrollTop = objDiv.scrollHeight;
	$chatWindow.append(messageLine);

}

function connectToChatserver() {

	wsocket = new WebSocket(serviceLocation);
	wsocket.onmessage = onMessageReceived;
}




