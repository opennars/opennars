package ptrman.lstmNlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Collection of various training example generators
 */
public class TrainingExampleGenerators {

    public class Tuple {
        public String naturalText;
        public String nalText;
    }

    public interface IGenerator {
        public Tuple generate(Random random);
    }

    // example
    // Thousands of people have since signed the letter, including leading artificial intelligence researchers at Google, Facebook, Microsoft and other industry hubs along with top computer scientists, physicists and philosophers around the world.

    public static class Pattern1 implements IGenerator {
        final String[] signedWhat = {"letter", "card", "paper"};
        final String[] signedByWhom = {"people", "persons", "scientists", "researchers"};
        final String[] researchersAt = {"google", "facebook", "stanford", "microsoft", "ibm", "intel", "amd"};

        public Tuple generate(Random random) {
            final int signedWhatIndex = random.nextInt(signedWhat.length);
            final int signedByWhomIndex = random.nextInt(signedByWhom.length);

            List<String> selectedSignedByWhom = takeNFromWithOverlap(Arrays.asList(researchersAt), 3, random);

            String naturalText = "Thousands of " + signedByWhom[signedByWhomIndex] + " have since signed the " + signedWhat[signedWhatIndex] + ", including leading artificial intelligence researchers at " + getEnumerationEndedWithAnd(selectedSignedByWhom);

            // TODO< build NAL >

            // TODO< build the tuple >

            return null;
        }





    }

    private static String getEnumerationEndedWithAnd(List<String> elements) {
        String result = "";

        for( int i = 0; i < elements.size() - 1; i++ ) {
            final int elementIndex = i;
            result = result + " " + elements.get(elementIndex);
        }

        final int elementIndex = elements.size();
        result = result + " and " + elements.get(elementIndex);

        return result;
    }

    private static<Type> List<Type> takeNFromWithOverlap(final List<Type> list, final int n, Random random) {
        List<Type> result = new ArrayList<>();

        for( int i = 0; i < n; i++ ) {
            result.add(list.get(random.nextInt(list.size())));
        }

        return result;
    }
}
