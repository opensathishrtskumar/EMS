<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>

<h2>Reports</h2>


<ul id="connectedAccounts" class="listings">
	<li class="listing">
		<a href="<c:url value="/ems/reports/daterange" />">Date Range Reports</a>	
	</li>
	
</ul>
