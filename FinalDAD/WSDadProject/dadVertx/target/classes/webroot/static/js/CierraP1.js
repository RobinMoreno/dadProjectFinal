function cierraP1(){
	var request = new XMLHttpRequest();
	var url = "/api/gate/1/0";
	
	request.open("POST", url, true);
	
	request.send();

}	

function cierraP1Servo(){
	var request = new XMLHttpRequest();
	var url = "/api/gatec/1/0";
	
	request.open("POST", url, true);
	
	request.send();

}




