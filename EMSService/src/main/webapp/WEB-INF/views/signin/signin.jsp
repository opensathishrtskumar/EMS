<%@ page session="false"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:if test="${not empty message}">
	<div class="${message.type.cssClass}">${message.text}</div>
</c:if>

<form id="signin" action="<c:url value="/signin/authenticate" />"
	method="post">
	<div class="formInfo">
		<h2>EMS Sign In</h2>
		<c:if test="${not empty param['error']}">
			<c:set var="error" scope="page" value="${param['error']}" />
			<c:choose>
				<c:when test="${error == 1} }">
					<div class="error">Your sign in information was incorrect.
						Please try again.</div>
				</c:when>
				<c:otherwise>
					<!-- Add messages if required -->
				</c:otherwise>
			</c:choose>
		</c:if>
	</div>
	<fieldset>
		<label for="login">Username or Email</label> <input id="login"
			name="j_username" type="text" size="25" autocorrect="off"
			autocapitalize="off"
			<c:if test="${not empty signinErrorMessage}">value="${SPRING_SECURITY_LAST_USERNAME}"</c:if> />
		<label for="password">Password</label> <input id="password"
			name="j_password" type="password" size="25" />
	</fieldset>
	<p>
		<button type="submit">Sign In</button>
	</p>
	<!-- Enable Forgot password later -->
	<%-- <p><a href="<c:url value="/reset" />">Forgot your password?</a></p> --%>
</form>
