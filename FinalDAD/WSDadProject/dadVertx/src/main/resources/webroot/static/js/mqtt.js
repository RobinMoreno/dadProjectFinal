		var mqtt;
		var reconnectTimeout = 2000;
		var host="localhost"; //change this

		var port=1883;
		
		function onFailure(message) {
			console.log("Connection Attempt to Host "+host+"Failed");
			setTimeout(MQTTconnect, reconnectTimeout);
        }
		function onMessageArrived(msg){
		$( "div#RegP1" ).append("Mensaje Recibido" + '<br>');
			out_msg="Message received "+msg.payloadString+"<br>";
			out_msg=out_msg+"Message received Topic "+msg.destinationName;
			
			var messageLine = $('<tr><td class="received">' + msg.payloadString + '</td></tr>');
			$( "div#RegP1" ).append(msg.payloadString + '<br>');
			var objDiv = document.getElementById("RegP1");
			objDiv.scrollTop = objDiv.scrollHeight;
			$chatWindow.append(messageLine);
			
			console.log(out_msg);

		}
		
	 	function onConnect() {
	  // Once a connection has been made, make a subscription and send a message.
	    $( "div#RegP1" ).append("Connected" + '<br>');
		console.log("Connected ");
		mqtt.subscribe("sensor1");
		message = new Paho.MQTT.Message("Hello World");
		$( "div#RegP1" ).append("Hello World" + '<br>');
		message.destinationName = "sensor1";
		mqtt.send(message);
	  }
	  
	  function MQTTconnect() {
	  $( "div#RegP1" ).append("Hello World" + '<br>');
		console.log("connecting to "+ host +" "+ port);
		mqtt = new Paho.MQTT.Client(host,port,"clientjs");
		//document.write("connecting to "+ host);
		var options = {
			timeout: 3,
			onSuccess: onConnect,
			onFailure: onFailure,
			 };
		mqtt.onMessageArrived = onMessageArrived;
		
		mqtt.connect(options); //connect
		}