package za.co.knonchalant.builder.converters;

/**
 * Base converter providing tag functions.
 */
public abstract class BaseConverter<T> implements IValueFieldConverter<T> {
    private String tag;

    protected String getTag() {
        return tag;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * @return true if the tag has been set.
     */
    protected boolean isTagSet() {
        return tag != null && !tag.isEmpty();
    }
}
