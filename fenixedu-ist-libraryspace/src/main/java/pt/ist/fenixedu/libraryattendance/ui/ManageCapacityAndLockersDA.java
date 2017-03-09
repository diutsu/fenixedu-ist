/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST Library Attendance.
 *
 * FenixEdu IST Library Attendance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST Library Attendance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST Library Attendance.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.libraryattendance.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.space.SpaceUtils;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.academic.ui.struts.action.library.LibraryApplication;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.portal.EntryPoint;
import org.fenixedu.bennu.struts.portal.StrutsFunctionality;
import org.fenixedu.spaces.domain.Information;
import org.fenixedu.spaces.domain.Space;
import org.fenixedu.spaces.domain.SpaceClassification;
import org.fenixedu.spaces.ui.InformationBean;
import org.joda.time.DateTime;

import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixframework.Atomic;

import com.google.common.base.Strings;

@StrutsFunctionality(app = LibraryApplication.class, path = "manage-capacity-and-lockers",
        titleKey = "label.manage.capacity.lockers")
@Mapping(path = "/manageCapacityAndLockers", module = "library")
@Forwards(@Forward(name = "libraryUpdateCapacityAndLockers", path = "/library/operator/libraryUpdateCapacityAndLockers.jsp"))
public class ManageCapacityAndLockersDA extends FenixDispatchAction {

    @EntryPoint
    public ActionForward prepareUpdateCapacityAndLockers(ActionMapping mapping, ActionForm actionForm,
            HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("libraryInformation", new LibraryInformation());
        return mapping.findForward("libraryUpdateCapacityAndLockers");
    }

    public ActionForward selectLibraryToUpdate(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        LibraryInformation libraryInformation = getRenderedObject("libraryInformation");

        Space library = libraryInformation.getLibrary();

        if (library != null) {
            libraryInformation.setCapacity(library.getAllocatableCapacity());
            libraryInformation.setLockers(library.getChildren().size());
        }

        RenderUtils.invalidateViewState();
        request.setAttribute("libraryInformation", libraryInformation);
        return mapping.findForward("libraryUpdateCapacityAndLockers");
    }

    @Atomic
    public ActionForward updateCapacityAndLockers(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        LibraryInformation libraryInformation = getRenderedObject("libraryUpdate");

        Space library = libraryInformation.getLibrary();
        if (!library.isActive()) {
            throw new UnsupportedOperationException("Library not active");
        }
        try {
            setCapacity(library, libraryInformation.getCapacity());
            setLockers(library, libraryInformation.getLockers(), new DateTime());
        } catch (IOException ioecexception){
            throw new UnsupportedOperationException("IO related error with library");
        }
        libraryInformation.setCapacity(library.getAllocatableCapacity());
        libraryInformation.setLockers(library.getChildren().size());

        request.setAttribute("libraryInformation", libraryInformation);
        return mapping.findForward("libraryUpdateCapacityAndLockers");
    }

    public ActionForward handleInvalidCapacityOrLockers(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        LibraryInformation libraryInformation = getRenderedObject("libraryUpdate");
        request.setAttribute("libraryInformation", libraryInformation);
        request.setAttribute("libraryUpdate", libraryInformation);
        return mapping.findForward("libraryUpdateCapacityAndLockers");
    }

    private void setCapacity(Space library, int capacity) throws IOException {
        InformationBean bean = library.bean();
        bean.setAllocatableCapacity(capacity);
        library.bean(bean);
    }

    private void setLockers(Space library, int lockers, DateTime today) throws IOException {
        int highestLocker = 0;
        for (Space relSpace : library.getChildren()) {
            Space space = relSpace;
            int lockerNumber;
            try {
                lockerNumber = Integer.parseInt(space.getName());
                if (lockerNumber > lockers) {
                    final InformationBean bean = space.bean();
                    bean.setValidUntil(today);
                    space.bean(bean);
                } else {
                    setCapacity(space, 1);
                }
                if (lockerNumber > highestLocker) {
                    highestLocker = lockerNumber;
                }
            } catch (NumberFormatException e) {
            }
        }
        if (highestLocker < lockers) {
            for (int i = highestLocker + 1; i <= lockers; i++) {
                final InformationBean bean =
                        Information.builder().name(Strings.padStart(Integer.toString(i), String.valueOf(lockers).length(), '0'))
                                .classification(SpaceClassification.getByName(SpaceUtils.ROOM_SUBDIVISION)).validFrom(today)
                                .allocatableCapacity(1).bean();
                new Space(library, bean);
            }
        }
    }

}
