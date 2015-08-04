package za.co.knonchalant.sample.pojo;

import za.co.knonchalant.builder.EType;
import za.co.knonchalant.builder.Name;
import za.co.knonchalant.builder.Type;

public class SampleClass {
    private String textString;
    private int aNumber;

    private String somePath;

    private String someStrings;

    @Name(value = "A string", prompt = "Enter some string here")
    public String getTextString() {
        return textString;
    }

    public void setTextString(String textString) {
        this.textString = textString;
    }

    @Name("An integer")
    public int getaNumber() {
        return aNumber;
    }

    public void setaNumber(int aNumber) {
        this.aNumber = aNumber;
    }

    @Type(EType.PATH)
    public String getSomePath() {
        return somePath;
    }

    public void setSomePath(String somePath) {
        this.somePath = somePath;
    }

    @Type(value = EType.COLLECTION, tag = "range")
    public String getSomeStrings() {
        return someStrings;
    }

    public void setSomeStrings(String someStrings) {
        this.someStrings = someStrings;
    }
}
