<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<h2>Reports</h2>

<c:if test="${not empty message}">
	<div class="${message.type.cssClass}">${message.text}</div>
</c:if>

<form:form id="daterangereport" action="/ems/reports/daterange" method="post" modelAttribute="reportForm">
	<div class="formInfo">
  		<h2>Select Report Criteria</h2>
  		<s:bind path="*">
  		</s:bind>
	</div>
		<fieldset>
		<form:label path="firstName">First Name <form:errors path="firstName" cssClass="error" /></form:label>
		<form:input path="firstName" />
		<form:label path="lastName">Last Name <form:errors path="lastName" cssClass="error" /></form:label>
		<form:input path="lastName" />
		<form:label path="email">Email (never shared, used for correspondence) <form:errors path="email" cssClass="error" /></form:label>
		<form:input path="email" />	
		<form:label path="confirmEmail">Confirm Email <form:errors path="confirmEmail" cssClass="error" /></form:label>
		<form:input path="confirmEmail" />	
		<form:label path="password">Password (at least 6 characters) <form:errors path="password" cssClass="error" /></form:label>
		<form:password path="password" />
		<form:label path="gender">Gender</form:label>
		<form:select path="gender">
			<form:option value="MALE" label="Male" />
			<form:option value="FEMALE" label="Female" />
		</form:select>
		<!-- TODO only one error message that considers all 3 fields -->	
		<form:label path="month">Birthday (never shared, used to display age) <form:errors path="month" cssClass="error" /></form:label>		
		<div class="multiple">
			<form:select path="month">
				<form:option value="">Month</form:option>
				<form:option value="1">January</form:option>
				<form:option value="2">February</form:option>
				<form:option value="3">March</form:option>
			</form:select>
			<form:select path="day">
				<form:option value="">Day</form:option>		
				<form:option value="1" />
				<form:option value="2" />
				<form:option value="3" />
				<form:option value="4" />
			</form:select>
			<form:select path="year">
				<form:option value="">Year</form:option>
				<form:option value="2010" />
				<form:option value="2009" />
			</form:select>
		</div>		
	</fieldset>
	<p><button type="submit">Download</button></p>
</form:form>