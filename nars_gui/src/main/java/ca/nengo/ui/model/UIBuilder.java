package ca.nengo.ui.model;

/**
 * a Node which creates its own UI type
 */
public interface UIBuilder {

    /** produces a new UI instance for this Node */
    UINeoNode newUI(double width, double height);

}
