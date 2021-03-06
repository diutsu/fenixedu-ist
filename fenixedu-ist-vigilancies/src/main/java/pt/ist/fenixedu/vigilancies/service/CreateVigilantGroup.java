/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Vigilancies.
 *
 * FenixEdu IST Vigilancies is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Vigilancies is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Vigilancies.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.vigilancies.service;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.joda.time.DateTime;

import pt.ist.fenixedu.vigilancies.domain.VigilantGroup;
import pt.ist.fenixframework.Atomic;

public class CreateVigilantGroup {

    @Atomic
    public static void run(String name, Unit unit, String convokeStrategy, String contactEmail, String rulesLink,
            DateTime beginFirstAllowedPeriod, DateTime endFirstAllowedPeriod, DateTime beginSecondAllowedPeriod,
            DateTime endSecondAllowedPeriod) {

        VigilantGroup vigilantGroup = new VigilantGroup();
        vigilantGroup.setName(name);
        vigilantGroup.setUnit(unit);
        vigilantGroup.setContactEmail(contactEmail);
        vigilantGroup.setRulesLink(rulesLink);
        vigilantGroup.setConvokeStrategy(convokeStrategy);
        vigilantGroup.setBeginOfFirstPeriodForUnavailablePeriods(beginFirstAllowedPeriod);
        vigilantGroup.setEndOfFirstPeriodForUnavailablePeriods(endFirstAllowedPeriod);
        vigilantGroup.setBeginOfSecondPeriodForUnavailablePeriods(beginSecondAllowedPeriod);
        vigilantGroup.setEndOfSecondPeriodForUnavailablePeriods(endSecondAllowedPeriod);
        ExecutionYear currentYear = ExecutionYear.readCurrentExecutionYear();
        vigilantGroup.setExecutionYear(currentYear);

    }

}