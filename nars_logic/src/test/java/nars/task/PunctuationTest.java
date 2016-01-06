package nars.task;

public class PunctuationTest {
	/*
	 * <sseehh_> ok i should make a test which tests all these cases directly
	 * <sseehh_> by directly i mean a programmatic input/output test of the
	 * reasoner in isolation <sseehh_> not involving any cycles etc
	 */

	/*
	 * <patham9_> it depends what is task and what belief <patham9_> task "?" +
	 * belief "." = result "." <patham9_> task "." + belief "?" = impossible,
	 * belief has "." as punctuation <patham9_> task "@" + belief "!" = result
	 * "!" <patham9_> (desire to be precise but in inference it will be called
	 * belief) <patham9_> task "!" + belief "@" = impossible, desire has "!" as
	 * puncutation <patham9_> "." & ".", "?" and "?", "@" and "@", "!", "!" are
	 * trivial, punctuation of result will be equal <patham9_> task "?" belief
	 * "@" = impossible <patham9_> task "@" belief "?" = impossible <patham9_>
	 * task "?" belief "!" = invalid (i think so) <patham9_> task "@" belief "."
	 * = invalid (i think so) <patham9_> i hope this were all cases, you may add
	 * a comment of them somewhere <patham9_> punctuation rules <- (keyword in
	 * case i will search for this once the IRC log search is ready)
	 */
}
