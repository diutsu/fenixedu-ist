/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Core.
 *
 * FenixEdu Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.teacher.ui.struts.action.departmentAdmOffice;

import javax.servlet.http.HttpServletRequest;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.ui.struts.action.departmentAdmOffice.DepartmentAdmOfficeApp.DefineExpectationPeriods;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;

import pt.ist.fenixedu.teacher.domain.TeacherPersonalExpectationPeriod;
import pt.ist.fenixedu.teacher.domain.TeacherPersonalExpectationsVisualizationPeriod;

@Mapping(module = "departmentAdmOffice", path = "/teacherPersonalExpectationsVisualizationPeriod",
        functionality = DefineExpectationPeriods.class)
@Forwards({
        @Forward(
                name = "showDefinitionPeriod",
                path = "/departmentAdmOffice/teacher/teacherPersonalExpectationsManagement/showTeacherPersonalExpectationsVisualizationPeriod.jsp"),
        @Forward(
                name = "editDefinitionPeriod",
                path = "/departmentAdmOffice/teacher/teacherPersonalExpectationsManagement/editTeacherPersonalExpectationsVisualizationPeriod.jsp") })
public class TeacherPersonalExpectationsVisualizationPeriodDA extends TeacherPersonalExpectationsDefinitionPeriodDA {

    @Override
    protected void readAndSetPeriod(HttpServletRequest request, ExecutionYear year) {
        if (year != null) {
            TeacherPersonalExpectationsVisualizationPeriod teacherExpectationDefinitionPeriod =
                    TeacherPersonalExpectationPeriod.getTeacherPersonalExpectationsVisualizationPeriodByExecutionYear(
                            getDepartment(request), year);
            request.setAttribute("period", teacherExpectationDefinitionPeriod);
        }
    }
}