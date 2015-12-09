package nars.truth;

import nars.Symbols;
import nars.util.Texts;

import java.io.Serializable;

/** abstract typed Truth  */
public interface MetaTruth<T> extends Cloneable, Serializable {

    T value();

    void setValue(T v);

    //TODO add appendString(sb, decimals)
    default StringBuilder appendString(StringBuilder sb) {
        String vs = value().toString();
        int decimals = 2;
        sb.ensureCapacity(3 + (2 + decimals) + vs.length() );
        return sb
                .append(Symbols.TRUTH_VALUE_MARK)
                .append(vs)
                .append(Symbols.VALUE_SEPARATOR)
                .append(Texts.n(getConfidence(), decimals))
                .append(Symbols.TRUTH_VALUE_MARK);
    }

    /**
     * Get the confidence value
     *
     * @return The confidence value
     */
    float getConfidence();



    /** TODO move this to a MutableTruth interface to separate a read-only impl */
    void setConfidence(float c);



    default CharSequence toCharSequence() {
        StringBuilder sb =  new StringBuilder();
        return appendString(sb);
    }

}
