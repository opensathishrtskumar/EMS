<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<c:if test="${not empty message}">
	
	<c:if test="${message > 0}">
		<div class="success">Password Changed Successfully</div>
	</c:if>
	<c:if test="${message == 0}">
		<div class="error">Password updation failed, Try later</div>
	</c:if>
	
</c:if>

<form:form id="resetPassword" method="post"
	modelAttribute="changePasswordForm">
	<div class="formInfo">
		<h2>Reset password</h2>
	</div>
	<fieldset>
		<form:label path="oldPassword">Current Password<form:errors
				path="oldPassword" cssClass="error" />
		</form:label>
		<form:password path="oldPassword" />

		<form:label path="password">Password (at least 6 characters) <form:errors
				path="password" cssClass="error" />
		</form:label>
		<form:password path="password" />
		<form:label path="confirmPassword">Confirm Password<form:errors
				path="confirmPassword" cssClass="error" /></form:label>
		<form:password path="confirmPassword" />
		<input type="hidden" value="${token}" />
	</fieldset>
	<p>
		<button type="submit">Reset</button>
	</p>
</form:form>