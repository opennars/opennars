package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// The MJCell rules

// 29/6/2000, Modification of Neumann binary rules by T. Suzudo

import java.util.Vector;

public class MJRules {
	public static final String S_USERRULE = "User rule";

	public static final int GAME_LIFE = 0; // Standard Conway-like game
	public static final int GAME_GENE = 1; // Generations
	public static final int GAME_WLIF = 2; // Weighted life
	public static final int GAME_VOTE = 3; // Vote for life
	public static final int GAME_RTBL = 4; // Rules table
	public static final int GAME_CYCL = 5; // Cyclic CA
	public static final int GAME_1DTO = 6; // 1-D CA totalistic
	public static final int GAME_1DBI = 7; // 1-D CA binary
	public static final int GAME_NMBI = 8; // Neumann binary
	public static final int GAME_GEBI = 9; // General binary
	public static final int GAME_LGTL = 10; // Larger than Life
	public static final int GAME_MARG = 11; // Margolus neighbourhood
	public static final int GAME_USER = 12; // User DLL
	public static final int GAME_SPEC = 13; // Special rules
	public static final int GAME_LAST = 13; // upper boud

	public static final int NGHTYP_MOOR = 1; // Moore neighbourhood
	public static final int NGHTYP_NEUM = 2; // von Neumann neighbourhood

	public static final int GAMTYP_1D = 1; // 1-dimensional
	public static final int GAMTYP_2D = 2; // 2-dimensional

	public static final String GAME_LIFE_Name = "Life";
	public static final String GAME_GENE_Name = "Generations";
	public static final String GAME_WLIF_Name = "Weighted Life";
	public static final String GAME_VOTE_Name = "Vote for Life";
	public static final String GAME_RTBL_Name = "Rules table";
	public static final String GAME_CYCL_Name = "Cyclic CA";
	public static final String GAME_1DTO_Name = "1-D totalistic";
	public static final String GAME_1DBI_Name = "1-D binary";
	public static final String GAME_NMBI_Name = "Neumann binary";
	public static final String GAME_GEBI_Name = "General binary";
	public static final String GAME_LGTL_Name = "Larger than Life";
	public static final String GAME_MARG_Name = "Margolus";
	public static final String GAME_USER_Name = "User DLL";
	public static final String GAME_SPEC_Name = "Special rules";

	public static final String GAME_LIFE_Abbr = "LIFE"; // Standard Conway-like game
	public static final String GAME_GENE_Abbr = "GENE"; // Generations
	public static final String GAME_WLIF_Abbr = "WLIF"; // Weighted Life
	public static final String GAME_VOTE_Abbr = "VOTE"; // Vote
	public static final String GAME_RTBL_Abbr = "RTBL"; // Rules table
	public static final String GAME_CYCL_Abbr = "CYCL"; // Cyclic CA
	public static final String GAME_1DTO_Abbr = "1DTO"; // 1-D totalistic CA
	public static final String GAME_1DBI_Abbr = "1DBI"; // 1-D binary CA
	public static final String GAME_NMBI_Abbr = "NMBI"; // Neumann binary
	public static final String GAME_GEBI_Abbr = "GEBI"; // General binary
	public static final String GAME_LGTL_Abbr = "LGTL"; // Larger than Life
	public static final String GAME_MARG_Abbr = "MARG"; // Margolus
	public static final String GAME_USER_Abbr = "USER"; // DLL
	public static final String GAME_SPEC_Abbr = "SPEC"; // Special rules

	// definitions of rules
	public static final int NAM = 0; // rule name
	public static final int RUL = 1; // rule definitioon

	public final Vector[] Rules = new Vector[GAME_LAST + 1]; // array of rules

	// ----------------------------------------------------------------
	// Initialize all rules
	public MJRules() {
		for (int i = 0; i <= GAME_LAST; i++) {
			Rules[i] = new Vector();
		}
		AddRules();
	}

	// ----------------------------------------------------------------
	// Fill the vector with all available rules
	@SuppressWarnings("HardcodedFileSeparator")
	private void AddRules() {
		MJTools mjT;
		Vector vLines;
		int i = -1, iGame = -1;
		String sBff, sNam, sDef;

		vLines = new Vector();
		mjT = new MJTools();
		if (mjT.LoadResTextFile("rul.txt", vLines)) // load the file with rules
		{
			for (i = 0; i < vLines.size(); i++) {
				sBff = ((String) vLines.elementAt(i)).trim();
				if ((!sBff.isEmpty())
						&& !((String) vLines.elementAt(i)).startsWith("//")) {
					if (sBff.length() > 0 && sBff.charAt(0) == '#') // next family of rules
					{
						iGame = GetGameIndex(sBff.substring(1));
					} else // next rule
					{
						if (iGame >= 0) {
							int whereSep = sBff.indexOf('|');
							if (whereSep > 0) {
								sNam = sBff.substring(0, whereSep); // part before '|'
								sDef = sBff.substring(whereSep + 1); // part after '|'
								sNam = sNam.trim();
								sDef = sDef.trim();
								Rules[iGame].addElement(new CARule(sNam, sDef));
							}
						}
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------
	// Get the index of the given game
	public int GetGameIndex(String sGameName) {
		int iGame = -1;
		//noinspection IfStatementWithTooManyBranches
		if ((sGameName.compareTo(GAME_GENE_Name) == 0) // Generations
				|| (sGameName.compareTo(GAME_GENE_Abbr) == 0))
			iGame = GAME_GENE;
		else if ((sGameName.compareTo(GAME_LIFE_Name) == 0) // Life
				|| (sGameName.compareTo(GAME_LIFE_Abbr) == 0))
			iGame = GAME_LIFE;
		else if ((sGameName.compareTo(GAME_WLIF_Name) == 0) // Weighted Life
				|| (sGameName.compareTo(GAME_WLIF_Abbr) == 0))
			iGame = GAME_WLIF;
		else if ((sGameName.compareTo(GAME_VOTE_Name) == 0) // Vote
				|| (sGameName.compareTo(GAME_VOTE_Abbr) == 0))
			iGame = GAME_VOTE;
		else if ((sGameName.compareTo(GAME_RTBL_Name) == 0) // Rules table
				|| (sGameName.compareTo(GAME_RTBL_Abbr) == 0))
			iGame = GAME_RTBL;
		else if ((sGameName.compareTo(GAME_CYCL_Name) == 0) // Cyclic CA
				|| (sGameName.compareTo(GAME_CYCL_Abbr) == 0))
			iGame = GAME_CYCL;
		else if ((sGameName.compareTo(GAME_1DTO_Name) == 0) // 1D totalistic
				|| (sGameName.compareTo(GAME_1DTO_Abbr) == 0))
			iGame = GAME_1DTO;
		else if ((sGameName.compareTo(GAME_1DBI_Name) == 0) // 1D binary
				|| (sGameName.compareTo(GAME_1DBI_Abbr) == 0))
			iGame = GAME_1DBI;
		else if ((sGameName.compareTo(GAME_NMBI_Name) == 0) // Neumann binary
				|| (sGameName.compareTo(GAME_NMBI_Abbr) == 0))
			iGame = GAME_NMBI;
		else if ((sGameName.compareTo(GAME_GEBI_Name) == 0) // General binary
				|| (sGameName.compareTo(GAME_GEBI_Abbr) == 0))
			iGame = GAME_GEBI;
		else if ((sGameName.compareTo(GAME_LGTL_Name) == 0) // Larger than Life
				|| (sGameName.compareTo(GAME_LGTL_Abbr) == 0))
			iGame = GAME_LGTL;
		else if ((sGameName.compareTo(GAME_MARG_Name) == 0) // Margolus
				|| (sGameName.compareTo(GAME_MARG_Abbr) == 0))
			iGame = GAME_MARG;
		else if ((sGameName.compareTo(GAME_USER_Name) == 0) // User DLL
				|| (sGameName.compareTo(GAME_USER_Abbr) == 0))
			iGame = GAME_USER;
		else if ((sGameName.compareTo(GAME_SPEC_Name) == 0) // Special rules
				|| (sGameName.compareTo(GAME_SPEC_Abbr) == 0))
			iGame = GAME_SPEC;
		return iGame;
	}

	// ----------------------------------------------------------------
	// Return the name of the given game
	public String GetGameName(int iGame) {
		String sRetVal;
		switch (iGame) {
		case GAME_LIFE:
			sRetVal = GAME_LIFE_Name;
			break; // Life
		case GAME_GENE:
			sRetVal = GAME_GENE_Name;
			break; // Generations
		case GAME_WLIF:
			sRetVal = GAME_WLIF_Name;
			break; // Weighted life
		case GAME_VOTE:
			sRetVal = GAME_VOTE_Name;
			break; // Vote for life
		case GAME_RTBL:
			sRetVal = GAME_RTBL_Name;
			break; // Rules table
		case GAME_CYCL:
			sRetVal = GAME_CYCL_Name;
			break; // Cyclic CA
		case GAME_1DTO:
			sRetVal = GAME_1DTO_Name;
			break; // 1D totalistic
		case GAME_1DBI:
			sRetVal = GAME_1DBI_Name;
			break; // 1D binary
		case GAME_NMBI:
			sRetVal = GAME_NMBI_Name;
			break; // Neumann binary
		case GAME_GEBI:
			sRetVal = GAME_GEBI_Name;
			break; // General binary
		case GAME_LGTL:
			sRetVal = GAME_LGTL_Name;
			break; // Larger than Life
		case GAME_MARG:
			sRetVal = GAME_MARG_Name;
			break; // Margolus
		case GAME_USER:
			sRetVal = GAME_USER_Name;
			break; // User DLL
		case GAME_SPEC:
			sRetVal = GAME_SPEC_Name;
			break; // Special rules
		default:
			sRetVal = "???";
			break;
		}
		return sRetVal;
	}

	// ----------------------------------------------------------------
	// Check if the dpecifoeg game index is valid
	public boolean IsGameIdxValid(int iGame) {
		return (iGame >= GAME_LIFE) && (iGame <= GAME_LAST);
	}

	// ----------------------------------------------------------------
	// Get the definition of the specified rule
	public String GetRuleDef(String sGameName, String sRuleName) {
		String sRuleDef = "";
		int i, iGame;

		iGame = GetGameIndex(sGameName);
		if (iGame >= 0) {
			for (i = 0; i < Rules[iGame].size(); i++) {
				if (sRuleName
						.compareTo(((CARule) Rules[iGame].elementAt(i)).name) == 0) {
					sRuleDef = ((CARule) Rules[iGame].elementAt(i)).def;
					break;
				}
			}
		}

		return sRuleDef;
	}

	// ----------------------------------------------------------------
	// Get the name of the specified rule definition
	public String GetRuleName(String sGameName, String sRuleDef) {
		String sRuleName = "";
		int i, iGame = -1;

		iGame = GetGameIndex(sGameName);
		if (iGame >= 0) {
			for (i = 0; i < Rules[iGame].size(); i++) {
				if (sRuleDef
						.compareTo(((CARule) Rules[iGame].elementAt(i)).def) == 0) {
					sRuleName = ((CARule) Rules[iGame].elementAt(i)).name;
					break;
				}
			}
		}
		return sRuleName;
	}

	// ----------------------------------------------------------------
	// Correct the definition of the given rule
	public String CorrectRuleDef(String sGameName, String sRuleDef) {
		sRuleDef = sRuleDef.trim();
		int iGame = GetGameIndex(sGameName);

		switch (iGame) {
		case MJRules.GAME_LIFE: // Standard Conway-like game
			RuleLife RLife = new RuleLife();
			RLife.InitFromString(sRuleDef);
			sRuleDef = RLife.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_GENE: // Generations
			RuleGene RGene = new RuleGene();
			RGene.InitFromString(sRuleDef);
			sRuleDef = RGene.GetAsString(); // get possibly corrected rules string
			break;

		case MJRules.GAME_VOTE: // Vote for life
			RuleVote RVote = new RuleVote();
			RVote.InitFromString(sRuleDef);
			sRuleDef = RVote.GetAsString(); // get possibly corrected rules string
			break;

		case MJRules.GAME_WLIF: // Weighted life
			RuleWLife RWLife = new RuleWLife();
			RWLife.InitFromString(sRuleDef);
			sRuleDef = RWLife.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_RTBL: // Rules table
			RuleRTab RRtab = new RuleRTab();
			RRtab.InitFromString(sRuleDef);
			sRuleDef = RRtab.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_CYCL: // Cyclic CA
			RuleCyclic RCyclic = new RuleCyclic();
			RCyclic.InitFromString(sRuleDef);
			sRuleDef = RCyclic.GetAsString(); // get possibly corrected rules string
			break;

		case MJRules.GAME_1DTO: // 1D totalistic
			Rule1DTotal R1DTo = new Rule1DTotal();
			R1DTo.InitFromString(sRuleDef);
			sRuleDef = R1DTo.GetAsString(); // get possibly corrected rules string
			break;

		case MJRules.GAME_1DBI: // 1D binary
			Rule1DBin R1DBin = new Rule1DBin();
			R1DBin.InitFromString(sRuleDef);
			sRuleDef = R1DBin.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_NMBI:
			RuleNeumBin RNeumBin = new RuleNeumBin();
			RNeumBin.InitFromString(sRuleDef);
			sRuleDef = RNeumBin.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_GEBI: // General binary
			RuleGenBin RGenBin = new RuleGenBin();
			RGenBin.InitFromString(sRuleDef);
			sRuleDef = RGenBin.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_LGTL:
			RuleLgtL RLgtL = new RuleLgtL();
			RLgtL.InitFromString(sRuleDef);
			sRuleDef = RLgtL.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_MARG:
			RuleMarg RMarg = new RuleMarg();
			RMarg.InitFromString(sRuleDef);
			sRuleDef = RMarg.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_USER:
			RuleUser RUser = new RuleUser();
			RUser.InitFromString(sRuleDef);
			sRuleDef = RUser.GetAsString(); // get possibly corrected rules string
			break;
		case MJRules.GAME_SPEC:
			break;
		}
		return sRuleDef;
	}
	// ----------------------------------------------------------------
	// ----------------------------------------------------------------
}