package nars.nal;

import nars.nal.concept.Concept;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;

/**
 * Defines the conditions used in an instance of a derivation
 */
public interface Premise {

    Concept getCurrentConcept();

    TermLink getCurrentTermLink();

    TaskLink getCurrentTaskLink();
}
