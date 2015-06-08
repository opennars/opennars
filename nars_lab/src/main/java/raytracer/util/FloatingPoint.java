package raytracer.util;

import nars.util.data.random.XORShiftRandom;

/**
 * Diese Klasse stellt zus�tzliche Routinen f�r Gleitkommazahlen bereit.
 * 
 * @author Mathias Kosch
 *
 */
public class FloatingPoint
{
    /** Toleranz f�r Gleitkommazahlen vom Typ Double. */
    protected final static double DOUBLE_TOLERANCY = Double.longBitsToDouble(0x3D90000000000000L);

    final static XORShiftRandom random = new XORShiftRandom();
    
    /**
     * Vergleicht zwei Gleitkommazahlen unter Beachtung der Rundungsungenauigkeit.<br>
     * Beide Zahlen sind gleich, wenn sie entweder gleich sind oder die Differenz
     * innerhalb der zugestandenen Rundungsungenauigkeit liegt.
     *
     * @param f1 Zahl 1.
     * @param f2 Zahl 2.
     * @return Ergebnis des Vergleichs.<br>
     *         <code>-1</code>. falls <code>f1 &lt; f2</code>.
     *         <code>+1</code>. falls <code>f1 &gt; f2</code>.
     *         <code>0</code>. falls <code>f1 == f2</code>.
     */
    public static int compareTolerated(double f1, double f2)
    {
        // Pr�fen, ob die Differenz innerhalb der Toleranz liegt:
        double f = f1-f2;
        if ((f >= -DOUBLE_TOLERANCY) && (f <= DOUBLE_TOLERANCY))
            return 0;
        
        long bits1 = Double.doubleToLongBits(f1);
        long bits2 = Double.doubleToLongBits(f2);
        
        // Pr�fen, ob die Exponenten gleich sind und die Differenz der Mantissen
        // innerhalb der Toleranz liegt:
        if (((bits1 ^ bits2) & 0x7FF0000000000000L) == 0)
        {
            // Differenz der Mantissen berechnen:
            f = Double.longBitsToDouble((bits1 & 0x800FFFFFFFFFFFFFL) | 0x3FF0000000000000L);
            f -= Double.longBitsToDouble((bits2 & 0x800FFFFFFFFFFFFFL) | 0x3FF0000000000000L);
            
            if ((f >= -DOUBLE_TOLERANCY) && (f <= DOUBLE_TOLERANCY))
                return 0;
        }
        
        // Ungleiche Exponenten:
        return (f1 < f2) ? -1 : 1;
    }
    
    
    /**
     * Ermittelt den minimalen Wert, der zu einem <code>float</code>-Wert noch
     * dazu addiert werden kann, sodass die Summe vom Ausgangswert verschieden
     * ist.
     * 
     * @param value <code>float</code>-Wert, zu dem der minimal m�gliche Summand
     *        bestimmt wird.
     * @return Minimaler Wert, sodass <code>value + WERT != value</code>.
     */
    public static float floatEpsilon(float value)
    {
        int bits = Float.floatToIntBits(value);
        int exponent = (bits >> 23) & 0xFF;
        
        exponent -= 23;
        if (exponent > 0)
            bits = exponent << 23;
        else
        {
            bits = 0x00400000 >> -exponent;
            if (bits == 0)
                bits = 1;
        }
        
        return Float.intBitsToFloat(bits);
    }

    public static double nextRandom() {
        return random.nextDouble();
    }
}