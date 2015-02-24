package ca.nengo.ui.models;

/**
 * a Node which creates its own UI type
 */
public interface UIBuilder {

    /** produces a new UI instance for this Node */
    UINeoNode newUI();

}
