package nars.nal;

import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;

/**
 * Defines the conditions used in an instance of a derivation
 */
public interface Premise {

    Concept getCurrentConcept();

    TermLink getCurrentTermLink();

    TaskLink getCurrentTaskLink();
}
