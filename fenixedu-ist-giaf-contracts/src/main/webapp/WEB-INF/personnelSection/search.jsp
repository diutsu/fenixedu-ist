<%--

    Copyright © 2013 Instituto Superior Técnico

    This file is part of FenixEdu IST GIAF Contracts.

    FenixEdu IST GIAF Contracts is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu IST GIAF Contracts is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu IST GIAF Contracts.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="pt.ist.fenixedu.contracts.domain.LegacyRoleUtils"%>
<%@page import="pt.ist.fenixWebFramework.servlets.filters.contentRewrite.GenericChecksumRewriter"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>



<div class="page-header">
	<h1>
		<spring:message code="title.personnelSection" />
		<small><spring:message code="label.search" /></small>
	</h1>
</div>

<form:form role="form" modelAttribute="search" method="GET" class="form-horizontal">
	${csrf.field()}
	<div class="form-group">
		<label for="username" class="col-sm-2 control-label">${fr:message('resources.ApplicationResources', 'label.username')}:</label>
		<div class="col-sm-10">
			<input  id="username" name="username" class="form-control col-sm-11 user-search" value="<c:out value='${search.username}'/>"/>
		</div>
	</div>
	<div class="form-group">
		<label for="name" class="col-sm-2 control-label">${fr:message('resources.ApplicationResources', 'label.person.name')}</label>
		<div class="col-sm-10">
			<input id="name" name="name" class="form-control col-sm-11 user-search" value="<c:out value='${search.name}'/>"/>
		</div>
	</div>
	<div class="form-group">
		<label for="documentIdNumber" class="col-sm-2 control-label">${fr:message('resources.ApplicationResources', 'label.person.identificationDocumentNumber')}</label>
		<div class="col-sm-10">
			<input id="documentIdNumber" name="documentIdNumber" class="form-control col-sm-11 user-search" value="<c:out value='${search.documentIdNumber}'/>"/>
		</div>
	</div>
	<div class="form-group">
		<label for="email" class="col-sm-2 control-label">${fr:message('resources.ApplicationResources', 'label.person.email')}</label>
		<div class="col-sm-10">
			<input id="email" name="email" class="form-control col-sm-11 user-search" value="<c:out value='${search.email}'/>"/>
		</div>
	</div>
	
	<div class="form-group">
		<label for="employeeNumber" class="col-sm-2 control-label">${fr:message('resources.ApplicationResources', 'label.number')}:</label>
		<div class="col-sm-10">
			<input id="employeeNumber" name="employeeNumber" class="form-control col-sm-11 user-search" value="<c:out value='${search.employeeNumber}'/>"/>
		</div>
	</div>
	<div class="form-group">
		<div class="col-sm-push-2 col-sm-10">
			<button type="submit" class="btn btn-default" id="search"><spring:message code="label.search" /></button>
		</div>				
	</div>
</form:form>

<spring:url var="createUrl" value="/personnelSection/createEmployee"></spring:url>

<c:choose>
	<c:when test="${searchResult == null}">
	</c:when>
	<c:when test="${empty searchResult}">
		<spring:message code="label.result.empty" ></spring:message>
	</c:when>
	<c:otherwise>
		<table class="table table-condensed">
			<thead>
				<tr>
					<th>${fr:message('resources.ApplicationResources', 'label.person.photo')}</th>
					<th>${fr:message('resources.ApplicationResources', 'label.username')}</th>
					<th>${fr:message('resources.ApplicationResources', 'label.number')}</th>
					<th>${fr:message('resources.ApplicationResources', 'label.person.name')}</th>
					<th>${fr:message('resources.ApplicationResources', 'label.person.identificationDocumentNumber')}</th>
					<th>${fr:message('resources.ApplicationResources', 'label.person.email')}</th>
					<th/>
					<th/>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="person" items="${searchResult}">
					<c:set var="profile" value="${person.profile}"/>
					<tr>
						<td>
    						<c:if test="${profile.user != null}">
                                <img src="${profile.avatarUrl}" alt="<c:out value='${profile.fullName}'/>" /></td>
    						</c:if>
						</td>
						<td><c:out value="${person.username}"/></td>
						<td><c:out value="${person.employee.employeeNumber}"/></td>  
						<td><c:out value="${profile.fullName}"/></td>
						<td><c:out value="${person.documentIdNumber}"/></td>
						<td<c:out value=">${person.emailForSendingEmails}"/></td>
						<td>
						  <c:if test="${person.user != null}">
						      <c:out value="${pt.ist.fenixedu.contracts.domain.LegacyRoleUtils.mainRolesStr(person.user)}" />
						  </c:if>
						</td>
						<c:choose>
							<c:when test="${person.employee == null}">
								<td><a href="#" data-toggle="modal" data-target="#create-contract-dialog<c:out value='${person.username}'/>"><span class="glyphicon glyphicon-edit"></span> <spring:message code="label.create.contract"/></a></td>
								<div class="modal fade" id="create-contract-dialog<c:out value='${person.username}'/>">
									<div class="modal-dialog">
										<div class="modal-content">
											<div class="modal-header">
												<button type="button" class="close" data-dismiss="modal">
													<span aria-hidden="true">&times;</span><span class="sr-only">Close</span>
												</button>
												<h4 class="modal-title">
													<spring:message code="label.create.contract"/>
												</h4>
											</div>
											<div class="modal-body">
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.username')}:</dt><dd><c:out value="${person.username}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.name')}:</dt><dd><c:out value="${person.name}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.email')}</dt><dd><c:out value="${person.emailForSendingEmails}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.identification')}:</dt><dd><c:out value="${person.documentIdNumber}"/> (<c:out value="${person.idDocumentType.localizedName}"/>)</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.contributorNumber')}</dt><dd><c:out value="${person.socialSecurityNumber}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.maritalStatus')}</dt><dd><c:out value="${person.maritalStatus.localizedName}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.birth')}</dt><dd><c:out value="${person.dateOfBirthYearMonthDay}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.country')}:</dt><dd><c:out value="${person.country.name}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.countryOfBirth')}</dt><dd><c:out value="${person.countryOfBirth.name}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.addressParish')}</dt><dd><c:out value="${person.parishOfBirth}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.addressMunicipality')}</dt><dd><c:out value="${person.districtSubdivisionOfBirth}"/>&nbsp;</dd></dl>
												<dl class="dl-horizontal"><dt>${fr:message('resources.ApplicationResources', 'label.person.addressDistrict')}</dt><dd><c:out value="${person.districtOfBirth}"/>&nbsp;</dd></dl>
											</div>
											<div class="modal-footer">
												 <form:form role="form" method="GET" action="${createUrl}/${person.externalId}" modelAttribute="search">
													 ${csrf.field()}
													 <input type="hidden" name="username" value="<c:out value='${search.username}'/>"/>
													 <input type="hidden" name="name" value="<c:out value='${search.name}'/>"/>
													 <input type="hidden" name="documentIdNumber" value="<c:out value='${search.documentIdNumber}'/>"/>
													 <input type="hidden" name="email" value="<c:out value='${search.email}'/>"/>
													 <input type="hidden" name="employeeNumber" value="<c:out value='${search.employeeNumber}'/>"/>
													<button type="submit" class="btn btn-primary"><spring:message code="button.createEmployeeNumber" /></button>
												</form:form>
												<button type="button" class="btn btn-default" data-dismiss="modal">${fr:message('resources.ApplicationResources', 'button.cancel')}</button>
											</div>
										</div>
									</div>
								</div>
							</c:when>
							<c:otherwise>
								<bean:define id="url" type="java.lang.String"><%=request.getContextPath()%>/professionalInformation.do?method=showProfessionalData&personId=${person.externalId}</bean:define>
								<td><a href="<%=url+"&_request_checksum_="+ GenericChecksumRewriter.calculateChecksum(url, session)%>"><spring:message code="link.personnelSection.showProfessionalInformation" /></a></td>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
				</tbody>
			</table>
		</c:otherwise>		
	</c:choose>