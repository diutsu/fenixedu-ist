/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST CMS Components.
 *
 * FenixEdu IST CMS Components is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST CMS Components is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST CMS Components.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.cmscomponents.domain.unit;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static org.fenixedu.cms.domain.component.Component.forType;

import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.domain.Menu;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.cms.domain.Site;
import org.fenixedu.commons.i18n.LocalizedString;

import pt.ist.fenixedu.cmscomponents.domain.unit.components.Organization;
import pt.ist.fenixedu.cmscomponents.domain.unit.components.SubUnits;
import pt.ist.fenixedu.cmscomponents.domain.unit.components.UnitCourses;
import pt.ist.fenixedu.cmscomponents.domain.unit.components.UnitEmployees;
import pt.ist.fenixedu.cmscomponents.domain.unit.components.UnitReserachersComponent;
import pt.ist.fenixedu.cmscomponents.domain.unit.components.UnitTeachersComponent;

/**
 * Created by borgez on 24-11-2014.
 */
public class ScientificAreaListener {
    private static final String BUNDLE = "resources.FenixEduLearningResources";
    private static final LocalizedString MENU_TITLE = getLocalizedString("resources.FenixEduLearningResources", "label.menu");
    private static final LocalizedString TITLE_EMPLOYEES = getLocalizedString(BUNDLE, "department.staff");
    private static final LocalizedString TITLE_TEACHERS = getLocalizedString(BUNDLE, "department.faculty");
    private static final LocalizedString TITLE_SUBUNITS = getLocalizedString(BUNDLE, "researchUnit.subunits");
    private static final LocalizedString TITLE_ORGANIZATION = getLocalizedString(BUNDLE, "researchUnit.organization");
    private static final LocalizedString TITLE_PUBLICATIONS = getLocalizedString(BUNDLE, "department.publications");
    private static final LocalizedString TITLE_COURES = getLocalizedString(BUNDLE, "department.courses");

    public static Site create(Unit scientificArea) {
        final Site newSite = UnitSiteBuilder.getInstance().create(scientificArea);
        final Menu menu = new Menu(newSite, MENU_TITLE);
        createDefaultContents(newSite, menu, Authenticate.getUser());
        return newSite;
    }

    public static void createDefaultContents(Site site, Menu menu, User user) {
        UnitsListener.createDefaultContents(site, menu, user);

        Page.create(site, menu, null, TITLE_EMPLOYEES, true, "employeesByArea", user, forType(UnitEmployees.class));
        Page.create(site, menu, null, TITLE_TEACHERS, true, "departmentFaculty", user, forType(UnitTeachersComponent.class));
        Page.create(site, menu, null, TITLE_SUBUNITS, true, "subunits", user, forType(SubUnits.class));
        Page.create(site, menu, null, TITLE_ORGANIZATION, true, "unitOrganization", user, forType(Organization.class));
        Page.create(site, menu, null, TITLE_PUBLICATIONS, true, "researcherSection", user,
                forType(UnitReserachersComponent.class));
        Page.create(site, menu, null, TITLE_COURES, true, "departmentCourses", user, forType(UnitCourses.class));

    }
}
