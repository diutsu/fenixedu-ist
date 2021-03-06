/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST GIAF Contracts.
 *
 * FenixEdu IST GIAF Contracts is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST GIAF Contracts is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST GIAF Contracts.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.contracts.domain.accessControl;

import java.util.stream.Stream;

import org.fenixedu.academic.util.Bundle;
import org.fenixedu.bennu.core.annotation.GroupOperator;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.GroupStrategy;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.joda.time.DateTime;

import pt.ist.fenixedu.contracts.domain.Employee;
import pt.ist.fenixedu.contracts.domain.util.CategoryType;

@GroupOperator("activeResearchers")
public class ActiveResearchers extends GroupStrategy {
    private static final long serialVersionUID = -6648971466827719165L;

    @Override
    public String getPresentationName() {
        return BundleUtil.getString(Bundle.GROUP, "label.name.ActiveResearchersGroup");
    }

    @Override
    public Stream<User> getMembers() {
        return Bennu.getInstance().getEmployeesSet().stream().filter(ActiveResearchers::isResearcher)
                .map(employee -> employee.getPerson().getUser());
    }

    @Override
    public Stream<User> getMembers(DateTime when) {
        return getMembers();
    }

    @Override
    public boolean isMember(User user) {
        return user != null && user.getPerson() != null && user.getPerson().getEmployee() != null
                && isResearcher(user.getPerson().getEmployee());
    }

    @Override
    public boolean isMember(User user, DateTime when) {
        return isMember(user);
    }

    protected static boolean isResearcher(Employee employee) {
        return (employee.getPerson().getPersonProfessionalData() != null ? employee.getPerson().getPersonProfessionalData()
                .getCurrentPersonContractSituationByCategoryType(CategoryType.RESEARCHER) : null) != null;
    }
}
