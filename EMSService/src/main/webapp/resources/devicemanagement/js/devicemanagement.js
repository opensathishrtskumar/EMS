
function validateForm() {
	if($.trim($("#deviceName").val()) == "" || $.trim($("#memoryMapping").val()) == "" || $("#baudRate").val() == "0"
		|| $("#wordLength").val() == "0" || $("#parity").val() == "0" || $("#stopbit").val() == "0" || $("#regmapping").val() == 0
		|| $("#port").val() == "0" || $("#method").val() == "0" || $('input[name=enabled]:checked').val()) {
		return false;
	}
	return true;
}

function getDeviceInfo() {
	var deviceId = $("#DeviceNameOption").val();
	if(deviceId) {
		$.ajax({
			type : "GET",
			url  : "readdevice",
			data : {
				"deviceId" : deviceId
			},
			cache: false,
			success : function(responseData) {
				
				responseData = JSON.parse(responseData);
				$("#deviceUniqueId").val(responseData.deviceId);
				$("#unitId").val(responseData.uniqueId);
				$("#deviceName").val(responseData.deviceName);
				$("#memoryMapping").val(responseData.memoryMapping);
				$("#baudRate").val(responseData.baudRate);
				$("#wordLength").val(responseData.wordLength);
				$("#parity").val(responseData.parity);
				$("#stopbit").val(responseData.stopbit);
				$("#regmapping").val(responseData.registerMapping);
				$("#port").val(responseData.port);
				$("#method").val(responseData.method);
				$("input[name=enabledRadio][value="+responseData.enabled+"]").prop('checked', true);
			},
			error : function(e) {
				alert('error');
			}
		});
	}
}

function updateChanges() {
	
	var deviceId = $("#DeviceNameOption").val();
	if(deviceId) {
		
		if(confirm("Are you sure, you want to Update device ?")) {
			var deviceForm = {};
			deviceForm.deviceId = $("#unitId").val();
			deviceForm.unitId = $("#deviceUniqueId").val();
			deviceForm.deviceName = $("#deviceName").val();
			deviceForm.baudRates = $("#baudRate").val();
			deviceForm.wordLength = $("#wordLength").val();
			deviceForm.parity = $("#parity").val();
			deviceForm.stopBit = $("#stopbit").val();
			deviceForm.memoryMapping = $("#memoryMapping").val();
			deviceForm.enabled = $("input[name=enabledRadio]:checked").val();
			deviceForm.regMapping = $("#regmapping").val();
			deviceForm.port = $("#port").val();
			deviceForm.readMethod = $("#method").val();
			
			$.ajax({
				type : "POST",
				url  : "updatedevice",
				data : {
					"deviceForm" : JSON.stringify(deviceForm)
				},
				cache: false,
				success : function(responseData) {
					if(responseData && responseData == "success") {
						alert("Device updated successfully...!")
						window.location.href = "showdevice";
					} else {
						alert(responseData);
					}
				},
				error : function(e) {
					alert('error');
				}
			});
		}
	} else {
		alert('Please select the device name...');
	}
}

function removeDevice() {
	
	var deviceId = $("#DeviceNameOption").val();
	if(deviceId) {
		
		if(confirm("Are you sure, you want to Remove device ?")) {
			$.ajax({
				type : "POST",
				url  : "removedevice",
				data : {
					"deviceId" : deviceId
				},
				cache: false,
				success : function(responseData) {
					
					if(responseData && responseData == "true") {
						alert("Device deleted successfully...!")
						window.location.href = "showdevice";
					} else {
						alert(responseData);
					}
				},
				error : function(e) {
					alert('error');
				}
			});
		}
	} else {
		alert('Please select the device name...');
	}
}

function saveDevice() {
	
	var deviceForm = {};
	deviceForm.deviceName = $("#deviceName").val();
	deviceForm.unitId = $("#unitId").val();
	deviceForm.baudRates = $("#baudRate").val();
	deviceForm.wordLength = $("#wordLength").val();
	deviceForm.parity = $("#parity").val();
	deviceForm.stopBit = $("#stopbit").val();
	deviceForm.memoryMapping = $("#memoryMapping").val();
	deviceForm.enabled = $("input[name=enabled]:checked").val();
	deviceForm.regMapping = $("#regmapping").val();
	deviceForm.port = $("#port").val();
	deviceForm.readMethod = $("#method").val();
	
	$.ajax({
		type : "POST",
		url  : "savedevice",
		data : {
			"deviceForm" : JSON.stringify(deviceForm)
		},
		cache: false,
		success : function(responseData) {
			if(responseData && responseData == "success") {
				alert("Device saved successfully...!")
				window.location.href = "adddevice";
			} else {
				alert(responseData);
			}
		},
		error : function(e) {
			alert('error');
		}
	});
}