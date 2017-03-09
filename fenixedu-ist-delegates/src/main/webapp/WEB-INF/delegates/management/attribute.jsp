<%--

    Copyright © 2013 Instituto Superior Técnico

    This file is part of FenixEdu IST Delegates.

    FenixEdu IST Delegates is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu IST Delegates is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu IST Delegates.  If not, see <http://www.gnu.org/licenses/>.

--%>
<!DOCTYPE html>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
${portal.toolkit()}
<div>
<div class="page-header">
  <h1><spring:message code="title.delegates.app"/><small><spring:message code="title.delegates.attribute.position"/></small></h1>
</div>

<spring:url var="formActionUrl" value="${action}"/>
<b><spring:message code="label.select.student.for.position"/> <c:out value="${delegatePositionBean.delegateTitle}"/></b><p>
<div>
<form:form modelAttribute="delegatePositionBean" role="form" method="post" action="${formActionUrl}" enctype="multipart/form-data">
	${csrf.field()}
	<form:input type="hidden" class="form-control" id="cycleTypeInput" path="cycleType" value="${delegatePositionBean.cycleType}"/>
	<c:if test="${not empty delegatePositionBean.delegate}">
	<form:input type="hidden" class="form-control" id="delegateInput" path="delegate" value="${delegatePositionBean.delegate.externalId}"/>
    </c:if>
    <c:if test="${not empty delegatePositionBean.degree}">
		<form:input type="hidden" class="form-control" id="degreeInput" path="degree" value="${delegatePositionBean.degree.externalId}"/>
    </c:if>
    <c:if test="${not empty delegatePositionBean.curricularYear}">
		<form:input type="hidden" class="form-control" id="curricularYearInput" path="curricularYear" value="${delegatePositionBean.curricularYear.externalId}"/>
	</c:if>
	<div class="form-group">
		<form:label for="nameInput" path="name"><spring:message code="label.student"/></form:label>
		<input bennu-user-autocomplete class="form-control" name="name" required="required" placeholder="utilizador"/>
<%-- 		<form:input type="text" class="form-control" id="nameInput" path="name" required="required"/> --%>
	</div>
	<button type="submit" class="btn btn-default"><spring:message code="label.submit"/></button>
</form:form>
</div>
</div>