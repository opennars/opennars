package nars.tuprolog.gui.ide;

public interface FileEditArea
{
    /**
     * Set the saved flag for the theory contained in the edit area after
     * the last Save operation.
     *
     * @param flag <code>true</code> if the theory has been modified ,
     * <code>false</code> otherwise.
     */
    public void setSaved(boolean flag);

    /**
     * Check if the theory in the edit area has been modified after the
     * last Save operation.
     *
     * @return <code>true</code> if the theory has been modified,
     * <code>false</code> otherwise.
     */
    public boolean isSaved();

}
