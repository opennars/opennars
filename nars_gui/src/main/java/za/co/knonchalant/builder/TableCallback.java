package za.co.knonchalant.builder;

/**
 * Callback for a table action handling the action on the table object and specifying its name.
 */
public interface TableCallback<T> {
    /**
     * Handle the action that must be performed on the object.
     *
     * @param object the object stored in the row
     */
    void handle(T object);

    /**
     * @return the name of the action being performed that will appear on the button.
     */
    String getActionName();
}
