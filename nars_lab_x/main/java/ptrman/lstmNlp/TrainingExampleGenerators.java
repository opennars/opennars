package ptrman.lstmNlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Collection of various training example generators
 */
public class TrainingExampleGenerators {
    public static class Tuple {
        public final String naturalText;
        public final String nalText;

        public Tuple(final String naturalText, final String nalText) {
            this.naturalText = naturalText;
            this.nalText = nalText;
        }
    }

    public interface IGenerator {
        public Tuple generate(Random random);
    }

    /**

     Thousands of people have since signed the letter, including leading artificial intelligence researchers at Google, Facebook, Microsoft and other industry hubs along with top computer scientists, physicists and philosophers around the world.
     --------------------------------------------------------------------------------------------------------------------------------------

     <(*, "Thousands", "people") --> count-1-of-2 >
     <(*, "people", "have since", "signed", "letter")--> done-1-2-action-3 >
     <(*, "including", "people", "leading", "artificial intelligence researchers") --> 1-2-3-in-4 >
     <(*, "people", "Google") --> at>
     ....



     */

    public static class Pattern1 implements IGenerator {
        final String[] signedWhat = {"letter", "card", "paper"};
        final String[] signedByWhom = {"people", "persons", "scientists", "researchers"};
        final String[] researchersAt = {"google", "facebook", "stanford", "microsoft", "ibm", "intel", "amd"};

        public Tuple generate(Random random) {
            final int signedWhatIndex = random.nextInt(signedWhat.length);
            final int signedByWhomIndex = random.nextInt(signedByWhom.length);

            List<String> selectedresearchersAt = takeNFromWithOverlap(Arrays.asList(researchersAt), 1+random.nextInt(4), random);

            String selectedSignedByWhom = signedByWhom[signedByWhomIndex];
            String selectedSignedWhat = signedWhat[signedWhatIndex];

            String naturalText = "Thousands of " + selectedSignedByWhom + " have since signed the " + selectedSignedWhat + ", including leading artificial intelligence researchers at " + getEnumerationEndedWithAnd(selectedresearchersAt);

            List<String> rulesText = new ArrayList<>();

            rulesText.add("<(*, \"Thousands\", \"" + selectedSignedByWhom + "\") --> count-1-of-2 >");
            rulesText.add("<(*, "+ selectedSignedByWhom +", \"have since\", \"signed\", \"" + selectedSignedWhat + "\")--> done-1-2-action-3 >");
            rulesText.add("<(*, \"including\", \"" + selectedSignedByWhom + "\", \"leading\", \"artificial intelligence researchers\") --> 1-2-3-in-4 >");


            for( final String at : selectedresearchersAt ) {
                rulesText.add("<(\"" + selectedSignedByWhom +  "\", \"" + at + "\") --> at>");
            }

            String nalText = "";
            nalText += buildGroup(rulesText);

            return new Tuple(naturalText, nalText);
        }
    }

    private static String getEnumerationEndedWithAnd(List<String> elements) {
        String result = "";

        for( int i = 0; i < elements.size() - 1; i++ ) {
            final int elementIndex = i;
            result += result + " " + elements.get(elementIndex);
        }

        if( elements.size() > 1) {
            final int elementIndex = elements.size()-1;
            result += " and " + elements.get(elementIndex);
        }
        else {
            final int elementIndex = elements.size()-1;
            result += "" + elements.get(elementIndex);
        }

        return result;
    }

    private static<Type> List<Type> takeNFromWithOverlap(final List<Type> list, final int n, Random random) {
        List<Type> result = new ArrayList<>();

        for( int i = 0; i < n; i++ ) {
            result.add(list.get(random.nextInt(list.size())));
        }

        return result;
    }

    private static String buildGroup(final List<String> members) {
        String result = "(&&,";

        for( int i = 0; i < members.size(); i++ ) {
            result += members.get(i);

            if( i != members.size() - 1 ) {
                result += ",";
            }
        }

        result += ")";

        return result;
    }
}
