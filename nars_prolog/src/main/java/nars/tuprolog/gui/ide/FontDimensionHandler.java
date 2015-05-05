package nars.tuprolog.gui.ide;

public interface FontDimensionHandler {

    /**
     * Increment the font dimension of the IDE's editor 
     */
    public void incFontDimension();

    /**
     * Increment the font dimension of the IDE's editor 
     */
    public void decFontDimension();

    public void setFontDimension(int dimension);

    public int getFontDimension();

    
}
