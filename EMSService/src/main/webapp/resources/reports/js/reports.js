/**
 * 
 */

$(document).ready(function() {

	var options = {
		format : 'dd/MM/yyyy hh:mm:ss',
		defaultDate : true
	};

	$("#starttime,#endtime").datetimepicker(options);
	
	$('#deviceNames').multiselect({
        columns: 2,
        placeholder: 'Select Devices',
        search: true,
        searchOptions: {
            'default': 'Search Devices'
        },
        selectAll: true
    });
	
	$('#memoryMappings').multiselect({
        columns: 2,
        placeholder: 'Select Devices',
        search: true,
        searchOptions: {
            'default': 'Search Devices'
        },
        selectAll: true
    });
	
});
