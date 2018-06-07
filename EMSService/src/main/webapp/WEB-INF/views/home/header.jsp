<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<h1>
	<a title="EMS" href="<c:url value="/" />"><img
		src="<c:url value="/resources/logo-header.png" />" alt="EMS" /></a>
</h1>
<div id="nav">
	<ul>
		<li><a href="<c:url value="/ems/homeSignedIn" />">Dashboard</a></li>
		<li><a href="<c:url value="/ems/devicemanagement" />">Device Mangement</a></li>
		<li><a href="<c:url value="/ems/reports" />">Reports</a></li>
		<li><a href="<c:url value="/ems/settings" />">Settings</a></li>
		<li><a href="<c:url value="/signout" />">Sign out</a></li>
	</ul>
</div>