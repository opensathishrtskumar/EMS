$(document).ready(
		function() {

			var options = {
				format : 'dd/MM/yyyy hh:mm:ss',
				defaultDate : true
			};

			$("#starttime,#endtime").datetimepicker(options);

			var memoryMappings = $('#memoryMappings');

			$('#deviceNames').multiselect({
				columns : 2,
				placeholder : 'Select Devices',
				search : true,
				searchOptions : {
					'default' : 'Search Devices'
				},
				selectAll : false,
				onOptionClick : function(element, option) {
					loadMemoryMappings($(option).val());
				}
			});

			function loadMemoryMappings(deviceuniqueid) {
				$.ajax({
					type : "POST",
					url : "getmemorymapping/" + deviceuniqueid,
					success : function(data, status) {
						console.log("Memory mapping loaded for device "
								+ deviceuniqueid + " is " + data);
						$(memoryMappings).empty();
						var options = "";
						$.map(data, function(key, value) {
							options = options + "<option id='" + key + "' value='"
									+ key + "'>" + key + "</option>";
						});
						$(memoryMappings).html(options);
						reloadMemoryMappings();
					}
				});
			}

			function reloadMemoryMappings() {
				$(memoryMappings).multiselect('reload');
			}

			$(memoryMappings).multiselect({
				columns : 2,
				placeholder : 'Select Memory Mappings',
				search : true,
				searchOptions : {
					'default' : 'Search Memory Mappings'
				},
				selectAll : false
			});

			$("#daterangereport").submit(function() {
				console.log("Form submit");
				// Validate form
				var memory = $(memoryMappings).val();

				var validation = true;
				var startTime = $("#reportStartTime").val();
				var endTime = $("#reportEndTime").val();

				if (memory.length == 0) {
					alert("Please select any memory mapping!");
					validation = false;
				} else if (startTime.length == 0) {
					alert("Please select Report start time!");
					validation = false;
				} else if (endTime.length == 0) {
					alert("Please select Report end time!");
					validation = false;
				}

				return validation;
			});

		});
