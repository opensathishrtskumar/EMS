<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<h2>Device Management</h2>

<ul id="connectedAccounts" class="listings">
	<li class="listing">
		<a href="<c:url value="/ems/devicemanagement/showdevice" />">Show Devices</a><br><br>
		<a href="<c:url value="/ems/devicemanagement/adddevice" />">Add Device</a><br><br>
		<%-- <a href="<c:url value="/ems/devicemanagement/adddevicetest" />">Add Device Test</a><br><br> --%>
	</li>
</ul>