package nars.nal;

import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Task;

/**
 * Defines the conditions used in an instance of a derivation
 */
public interface Premise {

    Concept getConcept();

    TermLink getTermLink();

    TaskLink getTaskLink();

    Task getBelief();
}
