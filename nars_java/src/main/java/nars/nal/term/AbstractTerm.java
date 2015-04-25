package nars.nal.term;


/**
    * Term is the basic component of Narsese, and the object of processing in NARS.

    * A Term may have an associated Concept containing relations with other Terms.
    * It is not linked in the Term, because a Concept may be forgot while the Term
    * exists. Multiple objects may represent the same Term.
 */
@Deprecated public interface AbstractTerm extends Term {
}
