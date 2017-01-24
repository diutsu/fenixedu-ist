package pt.ist.fenixedu.cmscomponents.domain.executionCourse;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.domain.Menu;
import org.fenixedu.cms.domain.Page;
import org.fenixedu.commons.i18n.LocalizedString;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;
import static org.fenixedu.cms.domain.component.Component.forType;

/**
 * Created by diutsu on 20/01/17.
 */
public class ExecutionCourseQUCListener {
    public static final String BUNDLE = "resources.FenixEduQucResources";
    public static final LocalizedString QUC_TITLE = getLocalizedString(BUNDLE, "link.coordinator.QUCResults");
    
    public static void createQUCComponent(ExecutionCourse executionCourse){
        Menu menu = executionCourse.getSite().getSystemMenu();
        Page.create(executionCourse.getSite(), menu, null, QUC_TITLE, true, "QUC", Authenticate.getUser(),
                forType(ExecutionCourseQUCComponent.class));
    }
}
