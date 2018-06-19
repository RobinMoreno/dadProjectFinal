function actualiza()
{
	var request = new XMLHttpRequest();
	var url = "http://localhost:8081/api/actualizadatos";
	
	request.onreadystatechange = function(){
		if(request.readyState == 4){
			if(request.status == 200){
				var response = JSON.parse(request.responseText);
				if(response.estadoVentana == 0){
					jQuery("#estado").html("");
						
				}else{
					jQuery("#estado").html("");
				}
				
				jQuery("#temperaturaInt").html(response.temperaturaInt + " ºC");
				jQuery("#temperaturaInt").html(response.temperaturaInt + " ºC");
			}
		}else{
			jQuery("#estado").html("");
			jQuery("#temperaturaInt").html("");
			jQuery("#temperaturaExt").html("");
		}
	};
	request.open("GET", url, true);
	request.send();
}

