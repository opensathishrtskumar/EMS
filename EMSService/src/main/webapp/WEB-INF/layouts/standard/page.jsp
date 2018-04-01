<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html>
<html>
<head>
	<title><tiles:insertAttribute name="title" defaultValue="EMS" /></title>
	<meta http-equiv="content-type" content="text/html;charset=utf-8" />	
	<link rel="stylesheet" href="<c:url value="/resources/page.css" />" type="text/css" media="screen" />
	<%-- <tiles:useAttribute id="styles" name="styles" classname="java.util.List" ignore="true" /> --%>
	<c:forEach var="style" items="${styles}">
	<link rel="stylesheet" href="<c:url value="/resources/${style}" />" type="text/css" media="all" />
	</c:forEach>
	<c:forEach var="meta" items="${metadata}">
	<meta name="${meta.key}" content="${meta.value}"/> 
	</c:forEach>
	<script type="text/javascript" src="<c:url value="/resources/jquery/3.3.1/jquery-3.3.1.min.js" />"></script>
	<script type="text/javascript" src="<c:url value="/resources/jquery-cookie/1.0/jquery-cookie.js" />"></script>
</head>
<body>
  	<div id="header">
		<tiles:insertAttribute name="header" />
	</div>
	<div id="content-container">
		<div id="content">
			<tiles:insertAttribute name="content" />
		</div>
		<div id="footer">
			<tiles:insertAttribute name="footer" />
		</div>
	</div>
	<%-- <tiles:useAttribute id="scripts" name="scripts" classname="java.util.List" ignore="true" /> --%>
	<c:forEach var="script" items="${scripts}">
		<script type="text/javascript" src="<c:url value="/resources/${script}" />"></script>	
	</c:forEach>
	<script type="text/javascript">
		$.cookie('ems.timeZoneOffset', new Date().getTimezoneOffset() * 60000);
	</script>
</body>
</html>