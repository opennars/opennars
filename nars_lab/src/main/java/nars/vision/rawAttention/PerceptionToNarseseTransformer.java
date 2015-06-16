package nars.vision.rawAttention;

import java.util.List;

/**
 *
 */
public class PerceptionToNarseseTransformer {
    public static String translateToNarsese(final List<Boolean> perception, final String concept) {
        String inside = "";

        for( int i = 0; i < perception.size(); i++ ) {
            if( perception.get(i) ) {
                inside += "{true}";
            }
            else {
                inside += "{false}";
            }

            if( i != perception.size()-1 ) {
                inside += ",";
            }
        }

        return "<" + "(*," + inside + ")" + "-->" + concept + ">" + ". :|:";
    }
}
