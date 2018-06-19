function Lecturas()
{
	var request = new XMLHttpRequest();
	var url = "/api/reading/1";
	
	request.open("GET", url, true);
	
	 request.onreadystatechange = function(){
		 if(request.readyState == 4){
			 if(request.status == 200){
				 var response1 = JSON.parse(request.responseText);
	 
				 var VarActualll = response1.temperature;
				 var ActualSmoke = response1.smoke;
				 $( "div#varActual" ).html(VarActualll + " ºC");
				 if(ActualSmoke == 0){
				 $( "div#varSmoke" ).html("No");
				 }else{
				 $( "div#varSmoke" ).html("Si");
				 }
				 
			 }	
		 }
	 };
	
	
	request.send();
}

function actualiza()
{

	var request = new XMLHttpRequest();
	var url = "/api/devices/1";
	
	request.open("GET", url, true);
	
	 request.onreadystatechange = function(){
		 if(request.readyState == 4){
			 if(request.status == 200){
				 var response = JSON.parse(request.responseText);
				 console.log(response);
				 
				 VarName = response.name;
				 VarEstadoP1 = response.state;
				 VarTempP1 = response.temperature;	
				
				 $( "div#name" ).html(VarName); 
				 	 
				 $( "div#varTemp1" ).html(VarTempP1 + " ºC");
				 
				 //image
				 
				 	if (VarEstadoP1 == 0){
					 $( "div#content1" ).empty();
					 $( "div#content1" ).append('<img class="img-responsive" id="image1" src="/media/images/pCerrada.png" alt="Estufa Apagada">');
				 	 $( "div#varEstado1" ).html("Apagada"); 
					 document.getElementById("onBut").disabled = false;
					 document.getElementById("offBut").disabled = true;
				 }else{
					 $( "div#content1" ).empty();
					 $( "div#content1" ).append('<img class="img-responsive" id="image1" src="/media/images/pAbierta.png" alt="Estufa Encendida">');
				 	 $( "div#varEstado1" ).html("Encendida"); 
					 document.getElementById("onBut").disabled = true;
					 document.getElementById("offBut").disabled =false;
				 }
			 }	
		 }
	 };
	
	request.send();
}

function inicializacion(){

actualiza();

window.setTimeout(Lecturas,2500);

Lecturas();
}

function mqttOn()
{
	var request = new XMLHttpRequest();
	var url = "/api/mqtt/turnOn";
	
	request.open("GET", url, true);
	
	 request.onreadystatechange = function(){
		 if(request.readyState == 4){
			 if(request.status == 200){
				 var response = JSON.parse(request.responseText);
	 
			 }	
		 }
	 };
	
	request.send();
	
}

function mqttOff()
{
	var request = new XMLHttpRequest();
	var url = "/api/mqtt/turnOff";
	
	request.open("GET", url, true);
	
	 request.onreadystatechange = function(){
		 if(request.readyState == 4){
			 if(request.status == 200){
				 var response = JSON.parse(request.responseText);
				 
			 }	
		 }
	 };
	
	request.send();
	
}

function clickOn(){
	mqttOn();

	window.setTimeout(actualiza,2500);

	actualiza();
}

function clickOff(){
	mqttOff();

	window.setTimeout(actualiza,2500);

	actualiza();
}



function updateTextInput(val) {
          document.getElementById('textInput').value=val; 
}

function updateSliderInput(val) {
          document.getElementById('sliderInput').value=val; 
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function encender(){
	var request = new XMLHttpRequest();
	var url = "/api/devices/1/1";
	
	request.open("POST", url, true);
	
	request.send();
	
}

function apagar(){
	var request = new XMLHttpRequest();
	var url = "/api/devices/1/0";
	
	request.open("POST", url, true);
	
	request.send();
	
}

function updateTempDeseada(){
	var request = new XMLHttpRequest();
	
	var tempValue = document.getElementById('sliderInput').value;
	
	var url = "/api/devices/temperature/1/";
	
	var url = url + tempValue;
	
	request.open("POST", url, true);
	
	request.send();
	
	window.setTimeout(actualiza,3000);
	
	actualiza();
	
}



