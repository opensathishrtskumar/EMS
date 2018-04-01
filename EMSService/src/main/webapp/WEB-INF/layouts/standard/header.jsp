<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- Change background image -->
<h1><a title="EMS" href="<c:url value="/" />"><img src="<c:url value="/resources/logo-header.png" />" alt="EMS" /></a></h1>
<div id="nav">
	<ul>
		<%-- <c:if test="${account != null}">
		<li><a href="<c:url value="/invite" />">Invite</a></li>
		<li><a href="<c:url value="/events" />">Events</a></li>
		<li><a href="<c:url value="/develop/apps" />">Develop</a></li>
		<li><a href="<c:url value="/settings" />">Settings</a></li>
		<li><a href="<c:url value="/signout" />">Sign Out</a></li>
		</c:if> --%>	
	</ul>
</div>
