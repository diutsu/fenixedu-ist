/**
 * Copyright © 2013 Instituto Superior Técnico
 *
 * This file is part of FenixEdu IST QUC.
 *
 * FenixEdu IST QUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu IST QUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu IST QUC.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ist.fenixedu.quc.domain;

import java.util.Collection;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.bennu.core.domain.Bennu;

import com.google.common.base.Strings;

public class InquiryResultComment extends InquiryResultComment_Base {

    public InquiryResultComment(InquiryResult questionResult, Person person, ResultPersonCategory personCategory,
            Integer resultOrder) {
        super();
        setGeneralAttributes(person, personCategory, resultOrder);
        setInquiryResult(questionResult);
    }

    public InquiryResultComment(InquiryGlobalComment globalComment, Person person, ResultPersonCategory personCategory,
            Integer resultOrder, String comment) {
        super();
        setGeneralAttributes(person, personCategory, resultOrder);
        setInquiryGlobalComment(globalComment);
        setComment(comment);
    }

    private void setGeneralAttributes(Person person, ResultPersonCategory personCategory, Integer resultOrder) {
        setRootDomainObject(Bennu.getInstance());
        setPerson(person);
        setPersonCategory(personCategory);
        setResultOrder(resultOrder);
    }

    public void delete() {
        setInquiryGlobalComment(null);
        setInquiryResult(null);
        setPerson(null);
        setRootDomainObject(null);
        super.deleteDomainObject();
    }

    public static boolean hasMandatoryCommentsToMakeAsResponsible(Professorship professorship) {
        for (Professorship sibling : professorship.getExecutionCourse().getProfessorshipsSet()) {
            Collection<InquiryResult> inquiryResults = sibling.getInquiryResultsSet();
            for (InquiryResult inquiryResult : inquiryResults) {
                if (inquiryResult.getResultClassification() != null) {
                    if (inquiryResult.getResultClassification().isMandatoryComment()
                            && !inquiryResult.getInquiryQuestion().isResultQuestion(inquiryResult.getExecutionPeriod())) {
                        InquiryResultComment inquiryResultComment =
                                inquiryResult.getInquiryResultComment(sibling.getPerson(), ResultPersonCategory.REGENT);
                        if (inquiryResultComment == null || Strings.isNullOrEmpty(inquiryResultComment.getComment())) {
                            inquiryResultComment =
                                    inquiryResult.getInquiryResultComment(sibling.getPerson(), ResultPersonCategory.TEACHER);
                            if (inquiryResultComment == null || Strings.isNullOrEmpty(inquiryResultComment.getComment())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasMandatoryCommentsToMake(Professorship professorship) {
        Collection<InquiryResult> inquiryResults = professorship.getInquiryResultsSet();
        for (InquiryResult inquiryResult : inquiryResults) {
            if (inquiryResult.getResultClassification() != null) {
                if (inquiryResult.getResultClassification().isMandatoryComment()
                        && !inquiryResult.getInquiryQuestion().isResultQuestion(inquiryResult.getExecutionPeriod())) {
                    InquiryResultComment inquiryResultComment =
                            inquiryResult.getInquiryResultComment(professorship.getPerson(), ResultPersonCategory.TEACHER);
                    if (inquiryResultComment == null || Strings.isNullOrEmpty(inquiryResultComment.getComment())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasMandatoryCommentsToMakeAsRegentInUC(Person person, ExecutionCourse executionCourse) {
        final Collection<InquiryResult> inquiryResults = executionCourse.getInquiryResultsSet();
        for (final InquiryResult inquiryResult : inquiryResults) {
            if (inquiryResult.getResultClassification() != null && inquiryResult.getProfessorship() == null) {
                if (inquiryResult.getResultClassification().isMandatoryComment()
                        && !inquiryResult.getInquiryQuestion().isResultQuestion(executionCourse.getExecutionPeriod())) {
                    InquiryResultComment inquiryResultComment =
                            inquiryResult.getInquiryResultComment(person, ResultPersonCategory.REGENT);
                    if (inquiryResultComment == null) {
                        inquiryResultComment = inquiryResult.getInquiryResultComment(person, ResultPersonCategory.TEACHER);
                        if (inquiryResultComment == null || Strings.isNullOrEmpty(inquiryResultComment.getComment())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
