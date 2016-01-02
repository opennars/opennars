package nars.ca;// Mirek's Java Cellebration
// http://www.mirekw.com
//
// User interface

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

class MJCellUI extends Frame {
	public MJRules mjr;
	public MJBoard mjb;
	public final MJCell mjc;
	public MJOpen mjo;
	public String sInitGame = "Generations"; // initial game
	public String sInitRule = "Brian's Brain"; // initial rule
	public String sInitPatt = ""; // optional initial pattern
	public boolean bInitPanelLeft = true; // Left panel visible
	public boolean bInitPanelBotm = true; // Bottom panel visible
    private Panel pnlLeft;
	private Panel pnlBotm;
    private Panel pnlPatterns;
	private Panel pnlFav;
    public String sBaseURL;
	public List<String> vDescr; // pattern description
	private Dialog msgDlg; // dialog used for "Info" and "About"
	private MJPatternsList PatDlg; // patterns list dialog
	private MJFavourities FavDlg; // favourite patterns

	// menu
	private final MenuBar mnuBar = new MenuBar();
	private final Menu mnuFile = new Menu("File");
	private final Menu mnuView = new Menu("View");
	private final Menu mnuAnim = new Menu("Animation");
	private final Menu mnuRule = new Menu("Rules");
	private final Menu mnuBord = new Menu("Board");
	private final Menu mnuColo = new Menu("Colors");
	private final Menu mnuHelp = new Menu("Help");
	private final Menu mnuSeed = new Menu("Seed");

	// File
	private final MenuItem itmOpen = new MenuItem("Open pattern...");
	private final MenuItem itmFav = new MenuItem("Favourite patterns...");
	private final MenuItem itmInfo = new MenuItem("General info...  (I)");
	private final MenuItem itmDesc = new MenuItem("Pattern description...  (D)");
	private final MenuItem itmExit = new MenuItem("Exit");

	// Animation
	@SuppressWarnings("HardcodedFileSeparator")
	public final MenuItem itmRunStop = new MenuItem("Start / Stop  (Enter)");
	private final MenuItem itmStep = new MenuItem("Single step  (Space)");
	public final MenuItem itmRewind = new MenuItem("Rewind  (Backspace)");
	@SuppressWarnings("HardcodedFileSeparator")
	public final MenuItem itmSlower = new MenuItem("Run slower  (/)");
	public final MenuItem itmFaster = new MenuItem("Run faster  (*)");
	private final Menu mnuRefreshStep = new Menu("Refresh step");
	private final CheckboxMenuItem itmRefreshStep1 = new CheckboxMenuItem(
			"Refresh each cycle");
	private final CheckboxMenuItem itmRefreshStep10 = new CheckboxMenuItem(
			"Refresh every 10 cycles");
	private final CheckboxMenuItem itmRefreshStep20 = new CheckboxMenuItem(
			"Refresh every 20 cycles");
	private final CheckboxMenuItem itmRefreshStep100 = new CheckboxMenuItem(
			"Refresh every 100 cycles");
	private final CheckboxMenuItem itmRefreshStepPage = new CheckboxMenuItem(
			"Refresh every full page (1D CA)");

	// Rules
	private final MenuItem itmUserRule = new MenuItem("Define own rules...  (?)");
	private final CheckboxMenuItem itmWrap = new CheckboxMenuItem(
			"Wrapping at edges  (W)");

	// View
	private final MenuItem itmRefresh = new MenuItem("Refresh  (F5)");
	private final CheckboxMenuItem itmViewControls = new CheckboxMenuItem(
			"Show control panel", true);
	private final CheckboxMenuItem itmViewSeed = new CheckboxMenuItem(
			"Show seeding panel", true);

	// Board
	private final MenuItem itmRand = new MenuItem("Randomize  (R)");
	private final MenuItem itmSeed = new MenuItem("Seed  (S)");
	private final MenuItem itmClear = new MenuItem("Clear  (C)");
	private final MenuItem itmBoardFit = new MenuItem("Fit pattern  (F)");
	private final CheckboxMenuItem itmGrid = new CheckboxMenuItem("Show grid  (G)");
	private final Menu mnuBoardSize = new Menu("Board size");
	private final MenuItem itmBoardAnySize = new MenuItem("User size");
	private final MenuItem itmBoard80x60 = new MenuItem("Board 80 x 60");
	private final MenuItem itmBoard100x100 = new MenuItem("Board 100 x 100");
	private final MenuItem itmBoard120x120 = new MenuItem("Board 120 x 120");
	private final MenuItem itmBoard160x120 = new MenuItem("Board 160 x 120");
	private final MenuItem itmBoard200x150 = new MenuItem("Board 200 x 150");
	private final MenuItem itmBoard200x200 = new MenuItem("Board 200 x 200");
	private final MenuItem itmBoard320x240 = new MenuItem("Board 320 x 240");
	private final MenuItem itmBoard400x300 = new MenuItem("Board 400 x 300");
	private final MenuItem itmBoard500x500 = new MenuItem("Board 500 x 500");
	private final MenuItem itmBoard800x600 = new MenuItem("Board 800 x 600");
	private final MenuItem itmCellsBigger = new MenuItem("Zoom in  (+)");
	private final MenuItem itmCellsSmaller = new MenuItem("Zoom out  (-)");

	// Colors
	private final MenuItem itmCloStatesCnt = new MenuItem("Count of states...");
	private final MenuItem itmCloCrrState = new MenuItem("Active state...");
	private final MenuItem itmCloNextState = new MenuItem(
			"Activate next state   ( ] )");
	private final MenuItem itmCloPrevState = new MenuItem(
			"Activate previous state   ( [ )");
	private final CheckboxMenuItem itmCloMtdStd = new CheckboxMenuItem(
			"Standard coloring");
	private final CheckboxMenuItem itmCloMtdAlt = new CheckboxMenuItem(
			"Alternate coloring");
	private final CheckboxMenuItem itmCPlMjcStd = new CheckboxMenuItem(
			"Palette 'MJCell Standard'");
	private final CheckboxMenuItem itmCPl8Color = new CheckboxMenuItem(
			"Palette '8 colors'");
	private final CheckboxMenuItem itmCPlRedWht = new CheckboxMenuItem(
			"Palette 'Red & blue'");
	private final CheckboxMenuItem itmCPlBlu = new CheckboxMenuItem(
			"Palette 'Dolphin'");
	private final CheckboxMenuItem itmCPlTst = new CheckboxMenuItem(
			"Palette 'Milky way'");

	// Help
	private final MenuItem itmAbout = new MenuItem("About...  (F1)");

	// Buttons, etc.
	private final Button btnOpen = new Button("Patterns library");
	private final Button btnFav = new Button("Favourities");
	private final Button btnRand = new Button("Rand");
	private final Button btnSeed = new Button("Seed");
	private final Button btnClear = new Button("Clear");
	@SuppressWarnings("HardcodedFileSeparator")
	public final Button btnRunStop = new Button("Start / Stop");
	private final Button btnStep = new Button("Step");
	public final Button btnSlower = new Button("Slower");
	public final Button btnFaster = new Button("Faster");
	private final Button btnUserRule = new Button("?");
	private final Button btnDesc = new Button("d");
	private final Checkbox chkWrap = new Checkbox("Wrap", true); // wrap at edges?
	private final Checkbox chkGrid = new Checkbox("Grid", true); // show grid?
	public final Choice cmbGames = new Choice();
	public final Choice cmbRules = new Choice();
	public final Checkbox chkAdd = new Checkbox("Add", false); // Rand/seed add to
	// existing universe?
	public final Checkbox chkMon = new Checkbox("Mono", false); // Mono, only 1 state?
	public final Checkbox chkUni = new Checkbox("Uni", false); // Uniform colors
	private final Choice cmbRand = new Choice();
	private final Choice cmbSeed = new Choice();
	@SuppressWarnings("HardcodedFileSeparator")
	private final Label lblStates = new Label("1/2");
	private final Label lblRule = new Label("???");
	private final Label lblCycle = new Label("Cycle: 0");
	private final Label lblPopul = new Label("Population: 0    ");
	@SuppressWarnings("HardcodedFileSeparator")
	private final Label lblBoard = new Label("Board: 000x000/00");

	// ----------------------------------------------------------------
	// Constructor
	public MJCellUI(MJCell cMjc) {
		mjc = cMjc; // the calling class
		vDescr = new Vector();
	}

	// ----------------------------------------------------------------
	// Build UI elements
	@SuppressWarnings("HardcodedFileSeparator")
	public void build() {
		sBaseURL = mjc.sBaseURL;

		// Panels
        Panel pnlTop = new Panel();
		pnlLeft = new Panel();
		pnlBotm = new Panel();
        Panel pnlRule = new Panel();
		pnlPatterns = new Panel();
		pnlFav = new Panel();
        Panel pnlSpeed = new Panel();
        Panel pnlWrapGrid = new Panel();
        Panel pnlRun = new Panel();

		setTitle(mjc.getAppletName());
		setLayout(new BorderLayout(1, 1)); // adds nice division lines
		pnlLeft.setLayout(new GridLayout(12, 1)); // vertical layout for 12
		// items
		pnlTop.setBackground(Color.lightGray);
		pnlLeft.setBackground(Color.lightGray);
		pnlBotm.setBackground(Color.lightGray);

		// build the menu
		mnuFile.removeAll();
		mnuView.removeAll();
		mnuAnim.removeAll();
		mnuRule.removeAll();
		mnuBord.removeAll();
		mnuColo.removeAll();
		mnuHelp.removeAll();
		mnuSeed.removeAll();

		mnuFile.add(itmOpen);
		mnuFile.add(itmFav);
		mnuFile.add("-");
		mnuFile.add(itmInfo);
		mnuFile.add(itmDesc);
		mnuFile.add("-");
		mnuFile.add(itmExit);

		mnuView.add(itmRefresh);
		mnuView.add("-");
		mnuView.add(itmGrid);
		mnuView.add("-");
		mnuView.add(itmViewSeed);
		mnuView.add(itmViewControls);

		mnuAnim.add(itmRunStop);
		mnuAnim.add(itmStep);
		mnuAnim.add(itmRewind);
		itmRewind.setEnabled(false);
		mnuAnim.add("-");
		mnuAnim.add(itmSlower);
		mnuAnim.add(itmFaster);
		mnuAnim.add("-");
		mnuAnim.add(mnuRefreshStep);
		mnuRefreshStep.add(itmRefreshStep1);
		mnuRefreshStep.add(itmRefreshStep10);
		mnuRefreshStep.add(itmRefreshStep20);
		mnuRefreshStep.add(itmRefreshStep100);
		mnuRefreshStep.add(itmRefreshStepPage);

		mnuRule.add(itmUserRule);
		mnuRule.add("-");
		mnuRule.add(itmWrap);

		mnuBord.add(itmRand);
		mnuBord.add(itmSeed);
		mnuBord.add(itmClear);
		mnuBord.add("-");
		mnuBord.add(mnuBoardSize); // Board size submenu
		mnuBoardSize.add(itmBoardAnySize);
		mnuBoardSize.add("-");
		mnuBoardSize.add(itmBoard80x60);
		mnuBoardSize.add(itmBoard100x100);
		mnuBoardSize.add(itmBoard120x120);
		mnuBoardSize.add(itmBoard160x120);
		mnuBoardSize.add(itmBoard200x150);
		mnuBoardSize.add(itmBoard200x200);
		mnuBoardSize.add(itmBoard320x240);
		mnuBoardSize.add(itmBoard400x300);
		mnuBoardSize.add(itmBoard500x500);
		mnuBoardSize.add(itmBoard800x600);
		mnuBord.add("-");
		mnuBord.add(itmBoardFit); // fit the pattern
		mnuBord.add(itmCellsBigger); // Zoom in
		mnuBord.add(itmCellsSmaller); // Zoom out

		mnuColo.add(itmCloStatesCnt); // count of states
		mnuColo.add("-");
		mnuColo.add(itmCloCrrState); // active state
		mnuColo.add(itmCloNextState); // activate next state
		mnuColo.add(itmCloPrevState); // activate previous state
		mnuColo.add("-");
		mnuColo.add(itmCloMtdStd); // standard coloring
		mnuColo.add(itmCloMtdAlt); // alternate coloring
		mnuColo.add("-");
		mnuColo.add(itmCPlMjcStd); // "MJCell Standard"
		mnuColo.add(itmCPl8Color); // "8 colors"
		mnuColo.add(itmCPlRedWht); // "Red & blue"
		mnuColo.add(itmCPlBlu); // "Dolphin"
		mnuColo.add(itmCPlTst); // "Milky way"

		mnuHelp.add(itmAbout);

		mnuBar.add(mnuFile);
		mnuBar.add(mnuView);
		mnuBar.add(mnuAnim);
		mnuBar.add(mnuRule);
		mnuBar.add(mnuBord);
		mnuBar.add(mnuColo);
		mnuBar.add(mnuHelp);
		setMenuBar(mnuBar);

		// add games
		mjr = new MJRules();
		cmbGames.removeAll();
		cmbGames.addItem(MJRules.GAME_GENE_Name); // Generations
		cmbGames.addItem(MJRules.GAME_LIFE_Name); // Life
		cmbGames.addItem(MJRules.GAME_WLIF_Name); // Weighted Life
		cmbGames.addItem(MJRules.GAME_VOTE_Name); // Vote
		cmbGames.addItem(MJRules.GAME_RTBL_Name); // Rules table
		cmbGames.addItem(MJRules.GAME_CYCL_Name); // Cyclic CA
		cmbGames.addItem(MJRules.GAME_1DTO_Name); // 1D totalistic
		cmbGames.addItem(MJRules.GAME_1DBI_Name); // 1D binary
		cmbGames.addItem(MJRules.GAME_NMBI_Name); // Neumann binary
		cmbGames.addItem(MJRules.GAME_GEBI_Name); // General binary
		cmbGames.addItem(MJRules.GAME_LGTL_Name); // Larger than Life
		cmbGames.addItem(MJRules.GAME_MARG_Name); // Margolus
		cmbGames.addItem(MJRules.GAME_USER_Name); // User DLL

		// left panel
		pnlLeft.add(cmbGames);
		pnlLeft.add(cmbRules);

		pnlRule.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		pnlRule.add(btnUserRule);
		pnlRule.add(lblRule);
		pnlLeft.add(pnlRule);

		pnlPatterns.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
		pnlPatterns.add(btnDesc);
		pnlPatterns.add(btnOpen);
		pnlLeft.add(pnlPatterns);

		pnlFav.add(btnFav);
		pnlLeft.add(pnlFav);

		pnlRun.setLayout(new GridLayout(1, 2)); // horizontal layout
		pnlRun.add(btnRunStop);
		pnlRun.add(btnStep);
		pnlLeft.add(pnlRun);

		pnlSpeed.setLayout(new GridLayout(1, 2)); // horizontal layout
		pnlSpeed.add(btnSlower);
		pnlSpeed.add(btnFaster);
		pnlLeft.add(pnlSpeed);

		pnlLeft.add(lblCycle);
		pnlLeft.add(lblPopul);
		pnlLeft.add(lblStates);
		pnlLeft.add(lblBoard);
		pnlWrapGrid.setLayout(new GridLayout(1, 2)); // horizontal layout
		pnlWrapGrid.add(chkWrap);
		pnlWrapGrid.add(chkGrid);
		pnlLeft.add(pnlWrapGrid);

		// bottom panel
		pnlBotm.add(chkAdd);
		pnlBotm.add(chkMon);
		pnlBotm.add(chkUni);
		cmbRand.addItem("5%");
		cmbRand.addItem("10%");
		cmbRand.addItem("15%");
		cmbRand.addItem("20%");
		cmbRand.addItem("25%");
		cmbRand.addItem("30%");
		cmbRand.addItem("40%");
		cmbRand.addItem("50%");
		cmbRand.addItem("60%");
		cmbRand.addItem("70%");
		cmbRand.addItem("80%");
		cmbRand.addItem("90%");
		cmbRand.addItem("100%");
		cmbRand.select("20%");
		pnlBotm.add(cmbRand);
		pnlBotm.add(btnRand);
		pnlBotm.add(new Label(""));
		cmbSeed.addItem("BLK 1x1");
		cmbSeed.addItem("BLK 1x2");
		cmbSeed.addItem("BLK 2x2");
		cmbSeed.addItem("BLK 5x5");
		cmbSeed.addItem("BLK 10x1");
		cmbSeed.addItem("BLK 10x10");
		cmbSeed.addItem("BLK 20x20");
		cmbSeed.addItem("BLK 30x10");
		cmbSeed.addItem("BLK 50x50");
		//cmbSeed.addItem("CIR 10"); QQtodo
		cmbSeed.addItem("FRM 10x10");
		cmbSeed.addItem("FRM 30x30");
		cmbSeed.addItem("FRM 50x50");
		cmbSeed.addItem("FRM 80x80");
		cmbSeed.select("BLK 5x5");
		pnlBotm.add(cmbSeed);
		pnlBotm.add(btnSeed);
		pnlBotm.add(new Label(""));
		pnlBotm.add(btnClear);

		// add panels and the board
		setSize(560, 430);
		pnlLeft.setVisible(bInitPanelLeft);
		itmViewControls.setState(bInitPanelLeft);
		pnlBotm.setVisible(bInitPanelBotm);
		itmViewSeed.setState(bInitPanelBotm);
		add("North", pnlTop);
		add("West", pnlLeft);
		add("South", pnlBotm);
		mjb = new MJBoard(this);
		add("Center", mjb);

		// final initialization
		mjo = new MJOpen(this, mjb);
		SetWrapping(true);
		SetGridVisibility(true);
		SetRefreshStep(1); // refresh every 1 cycle
		SetColoringMethod(1); // standard coloring
		mjb.SetStatesCount(9); // count of states (for games with no history)
		SetColorPalette("MJCell Standard");
		PatDlg = new MJPatternsList(new Frame(""), this);
		FavDlg = new MJFavourities(new Frame(""), this);

		// initial rule
		cmbGames.select(sInitGame);
		InitRules();
		cmbRules.select(sInitRule);
		SendActiveRule();

		// create some random cells in case no initial pattern is loaded
		mjb.Randomize(cmbRand.getSelectedItem(), mjb.RAND_ALL);

		// initial pattern
		if (!sInitPatt.isEmpty()) {
			mjo.OpenFile(cmbGames.getSelectedItem() + '/'
					+ cmbRules.getSelectedItem() + '/' + sInitPatt);
		}

		// prepare the label for the rule definition
		int iLen = lblRule.getText().length();
		if (iLen < 20) {
			// a trick - I set a long text so that the label length
			// is big enough to show rules with names longer than initial BB
			String str = "";
			iLen = 20 - iLen;
			while (iLen-- > 0)
				str = str + ' ';
			lblRule.setText(lblRule.getText() + str);
		}
	}

	// ----------------------------------------------------------------
	// first-time initialization
	public void Init() {
		cmbGames.transferFocus();
		cmbRules.transferFocus();
		btnRunStop.setFont(new Font(btnRunStop.getFont().getName(), Font.BOLD,
				btnRunStop.getFont().getSize()));
	}

	// ----------------------------------------------------------------
	// Resize size-sensitive controls
	@Override
	public void paint(Graphics g) {
		btnUserRule.setBounds(0, 0, 20, 20);
		btnDesc.setBounds(0, 0, 20, 20);
		btnOpen.setBounds(21, 0, pnlPatterns.getSize().width - 21, 20);
		btnFav.setBounds(0, 0, pnlFav.getSize().width, 20);
	}

	// ----------------------------------------------------------------
	// A game was selected, fill the rules combo
	public void InitRules() {
		int i, iGame;
		String sGameName = cmbGames.getSelectedItem();

		cmbRules.removeAll();
		iGame = mjr.GetGameIndex(sGameName);
		if (iGame >= 0) {
			for (i = 0; i < mjr.Rules[iGame].size(); i++)
				cmbRules.addItem(((CARule) mjr.Rules[iGame].elementAt(i)).name);
		}
		SendActiveRule(); // activate also the rule
	}

	// ----------------------------------------------------------------
	// Activate the given game
	public void ActivateGame(String sGame) {
		cmbGames.select(mjr.GetGameName(mjr.GetGameIndex(sGame)));
		InitRules();
	}

	public void ActivateGame(int iGame) {
		if ((iGame >= MJRules.GAME_LIFE) && (iGame <= MJRules.GAME_LAST)) {
			ActivateGame(mjr.GetGameName(iGame));
		}
	}

	// ----------------------------------------------------------------
	// Activate the given rule
	public void ActivateRule(String sRule) {
		cmbRules.select(sRule);
		SendActiveRule();
	}

	// ----------------------------------------------------------------
	// Interprete the rule from the combo box and send it to the board
	public void SendActiveRule() {
		int i, iGame;
		String sRuleName = cmbRules.getSelectedItem();
		String sGameName = cmbGames.getSelectedItem();
		String sRuleDef = "No rule";

		mjb.stop();
		iGame = mjr.GetGameIndex(sGameName);
		sRuleDef = mjr.GetRuleDef(sGameName, sRuleName);
		SendRule(iGame, sRuleName, sRuleDef);

		PatDlg.InitList(); // refresh the patterns list
	}

	// ----------------------------------------------------------------
	// send the specified rule to the board
	public void SendRule(int iGame, String sRuleName, String sRuleDef) {
		mjb.SetRule(iGame, sRuleName, sRuleDef);
		if (sRuleDef.length() > 20)
			sRuleDef = sRuleDef.substring(0, 17) + "...";
		lblRule.setText(sRuleDef); // show rule definition
		UpdateUI(); // update dynamic elements of the UI
	}

	// ----------------------------------------------------------------
	// handle events we are interested in
	@Override
	public boolean handleEvent(Event e) {
		if (e.id == Event.WINDOW_DESTROY) {
			mjb.stop();

			PatDlg.show(false);
			PatDlg.removeAll();

			FavDlg.show(false);
			FavDlg.removeAll();

			hide();
			removeAll();
			return true;
		}
		return super.handleEvent(e);
	}

	// ----------------------------------------------------------------
	// button / menu item selected
	@Override
	public boolean action(Event e, Object arg) {
		int i, j;
		//noinspection IfStatementWithTooManyBranches
		if ((e.target == btnRunStop) || (e.target == itmRunStop)) {
			if (mjb.caThread != null)
				mjb.stop();
			else
				mjb.start();
		} else if ((e.target == btnStep) || (e.target == itmStep)) {
			mjb.SingleStep();
		} else if (e.target == itmRewind) {
			mjb.RestoreBackup();
		} else if (e.target == itmRefreshStep1) {
			SetRefreshStep(1);
		} else if (e.target == itmRefreshStep10) {
			SetRefreshStep(10);
		} else if (e.target == itmRefreshStep20) {
			SetRefreshStep(20);
		} else if (e.target == itmRefreshStep100) {
			SetRefreshStep(100);
		} else if (e.target == itmRefreshStepPage) {
			SetRefreshStep(-1);
		} else if ((e.target == btnOpen) || (e.target == itmOpen)) // patterns
		// list
		{
			PatDlg.show(true);
			PatDlg.requestFocus();
		} else if ((e.target == btnFav) || (e.target == itmFav))// favourite
		// patterns
		{
			FavDlg.show(true);
			FavDlg.requestFocus();
		} else if ((e.target == btnSlower) || (e.target == itmSlower)) {
			RunSlower();
		} else if ((e.target == btnFaster) || (e.target == itmFaster)) {
			RunFaster();
		} else if ((e.target == btnRand) || (e.target == itmRand)) {
			mjb.Randomize(cmbRand.getSelectedItem(), mjb.RAND_VIEW);
		} else if ((e.target == btnSeed) || (e.target == itmSeed)) {
			mjb.Seed(cmbSeed.getSelectedItem());
		} else if ((e.target == chkWrap) || (e.target == itmWrap)) {
			SetWrapping(!mjb.WrapAtEdges);
		} else if ((e.target == chkGrid) || (e.target == itmGrid)) {
			SetGridVisibility(!mjb.DrawGrid);
		} else if (e.target == itmRefresh) {
			mjb.RedrawBoard(true);
		} else if (e.target == itmViewControls) {
			pnlLeft.setVisible(!pnlLeft.isVisible());
			doLayout();
			pnlLeft.doLayout();
		} else if (e.target == itmViewSeed) {
			pnlBotm.setVisible(!pnlBotm.isVisible());
			doLayout();
			pnlBotm.doLayout();
		} else if ((e.target == btnClear) || (e.target == itmClear)) {
			mjb.Clear(true);
		} else if (e.target == itmExit) {
			mjb.stop();
			hide();
			removeAll();
		} else if (e.target == cmbGames) // one of games selected
		{
			InitRules();
		} else if (e.target == cmbRules) // one of rules selected
		{
			SendActiveRule();
		} else if ((e.target == chkMon) || (e.target == chkUni)) {
			UpdateRandomizingUI();
		} else if (e.target == itmBoardFit) // fit the pattern
			mjb.Fit(true);
		else if (e.target == itmCellsBigger) // Zoom in
			mjb.CellsBigger();
		else if (e.target == itmCellsSmaller) // Zoom out
			mjb.CellsSmaller();
		else if (e.target == itmBoard80x60)
			mjb.InitBoard(80, 60, mjb.CellSize);
		else if (e.target == itmBoard100x100)
			mjb.InitBoard(100, 100, mjb.CellSize);
		else if (e.target == itmBoard120x120)
			mjb.InitBoard(120, 120, mjb.CellSize);
		else if (e.target == itmBoard160x120)
			mjb.InitBoard(160, 120, mjb.CellSize);
		else if (e.target == itmBoard200x150)
			mjb.InitBoard(200, 150, mjb.CellSize);
		else if (e.target == itmBoard200x200)
			mjb.InitBoard(200, 200, mjb.CellSize);
		else if (e.target == itmBoard320x240)
			mjb.InitBoard(320, 240, mjb.CellSize);
		else if (e.target == itmBoard400x300)
			mjb.InitBoard(400, 300, mjb.CellSize);
		else if (e.target == itmBoard500x500)
			mjb.InitBoard(500, 500, mjb.CellSize);
		else if (e.target == itmBoard800x600)
			mjb.InitBoard(800, 600, mjb.CellSize);
		else if (e.target == itmBoardAnySize)
			InputBoardSize();
		else if (e.target == itmCloStatesCnt)
			InputCountOfStates();
		else if (e.target == itmCloCrrState)
			InputActiveState();
		else if (e.target == itmCloNextState)
			mjb.SetCrrState(mjb.CrrState + 1);
		else if (e.target == itmCloPrevState)
			mjb.SetCrrState(mjb.CrrState - 1);
		else if (e.target == itmCloMtdStd) // standard coloring
			SetColoringMethod(1);
		else if (e.target == itmCloMtdAlt) // alternate coloring
			SetColoringMethod(2);
		else if (e.target == itmCPlMjcStd) // "MJCell Standard"
			SetColorPalette("MJCell Standard");
		else if (e.target == itmCPl8Color) // "8 colors"
			SetColorPalette("8 colors");
		else if (e.target == itmCPlRedWht) // "Red & blue"
			SetColorPalette("Red & blue");
		else if (e.target == itmCPlBlu) // "Dolphin"
			SetColorPalette("Dolphin");
		else if (e.target == itmCPlTst) // "Milky way"
			SetColorPalette("Milky way");
		else if (e.target == itmAbout) {
			DialogAbout();
		} else if (e.target == itmInfo) // show Info
		{
			DialogInfo();
		} else if ((e.target == itmDesc) || (e.target == btnDesc)) // show
		// pattern
		// description
		{
			DialogDesc();
		} else if ((e.target == btnUserRule) || (e.target == itmUserRule)) {
			DefineUserRules();
		}

		UpdateUI(); // update dynamic elements of the UI
		return true;
	}

	// ----------------------------------------------------------------
	// Hot keys handling
	@Override
	@SuppressWarnings("HardcodedFileSeparator")
	public boolean keyDown(Event evt, int key) {
		boolean retVal = false; // event handled?
		switch (key) {
		case Event.F1: // help/about
			DialogAbout();
			retVal = true;
			break;

		case Event.F5: // refresh
			mjb.RedrawBoard(true);
			break;

		case Event.ENTER: // run/stop
			if (mjb.caThread != null)
				mjb.stop();
			else
				mjb.start();
			retVal = true;
			break;

		case Event.LEFT:
			mjb.Pan(-10, 0);
			retVal = true;
			break;

		case Event.RIGHT:
			mjb.Pan(10, 0);
			retVal = true;
			break;

		case Event.UP:
			mjb.Pan(0, -10);
			retVal = true;
			break;

		case Event.DOWN:
			mjb.Pan(0, 10);
			retVal = true;
			break;

		case Event.BACK_SPACE:
			mjb.RestoreBackup();
			break;

		default:
			switch ((char) key) {
			case ' ':
				mjb.SingleStep();
				retVal = true;
				break;

			case 'i':
			case 'I':
				DialogInfo();
				retVal = true;
				break;

			case 'd':
			case 'D':
				DialogDesc();
				retVal = true;
				break;

			case 'f':
			case 'F':
				mjb.Fit(true);
				retVal = true;
				break;

			case 'c':
			case 'C':
				mjb.Clear(true);
				retVal = true;
				break;

			case 'r':
			case 'R':
				mjb.Randomize(cmbRand.getSelectedItem(), mjb.RAND_VIEW);
				retVal = true;
				break;

			case 's':
			case 'S':
				mjb.Seed(cmbSeed.getSelectedItem());
				retVal = true;
				break;

			case 'a':
			case 'A':
				chkAdd.setState(!chkAdd.getState());
				retVal = true;
				break;

			case 'w':
			case 'W':
				SetWrapping(!mjb.WrapAtEdges);
				retVal = true;
				break;

			case 'g':
			case 'G':
				SetGridVisibility(!mjb.DrawGrid);
				retVal = true;
				break;

			case '+': // zoom in - bigger cells
				mjb.CellsBigger();
				break;

			case '-': // zoom out - smaller cells
				mjb.CellsSmaller();
				break;

			case '/': // slower
				RunSlower();
				break;

			case '*': // faster
				RunFaster();
				break;

			case '?': // user rules
				DefineUserRules();
				break;

			case ']': // activate next state
				mjb.SetCrrState(mjb.CrrState + 1);
				break;

			case '[': // activate previous state
				mjb.SetCrrState(mjb.CrrState - 1);
				break;
			}
		}
		UpdateUI(); // update the user interface
		return retVal;
	}

	// ----------------------------------------------------------------
	// Allow to define custom rules
	private void DefineUserRules() {
		// input the user rule
		InputBox ib = new InputBox(new Frame(""), mjb.RuleDef, "User rules",
				" Enter your own rules (refer to the rules lexicon for syntax):");
		requestFocus();
		if (ib.isAccepted) {
			String sGameName = mjr.GetGameName(mjb.CrrGame); // correct the rule
			String sRuleDef = ib.txtFld.getText();
			sRuleDef = mjr.CorrectRuleDef(sGameName, sRuleDef);

			// if the rule is new, add it as a user rule
			String sRuleName = mjr.GetRuleName(sGameName, sRuleDef);
			if (sRuleName.isEmpty()) // no such rule yet, add it
			{
				cmbRules.select(MJRules.S_USERRULE); // activate "User rule"
				// item
				SendRule(mjb.CrrGame, MJRules.S_USERRULE, sRuleDef);
				// store the user rule
				mjr.Rules[mjb.CrrGame].addElement(new CARule(
						MJRules.S_USERRULE, sRuleDef));
			} else // one of existing rules
			{
				cmbRules.select(sRuleName);
				SendRule(mjb.CrrGame, sRuleName, sRuleDef);
			}
			mjb.SetStatesCount(mjb.StatesCount); // recreate the palette
		}
		ib.dispose();
	}

	// ----------------------------------------------------------------
	// Set the wrapping state, update UI
	public void SetWrapping(boolean fOn) {
		mjb.WrapAtEdges = fOn;
		chkWrap.setState(fOn);
		itmWrap.setState(fOn);
	}

	// ----------------------------------------------------------------
	// Set the grid visibility state, update UI
	private void SetGridVisibility(boolean fOn) {
		mjb.DrawGrid = fOn;
		UpdateGridUI();
		mjb.RedrawBoard(true);
	}

	// ----------------------------------------------------------------
	// Set and update the RefreshStep user interface
	private void SetRefreshStep(int i) {
		itmRefreshStep1.setState(i == 1);
		itmRefreshStep10.setState(i == 10);
		itmRefreshStep20.setState(i == 20);
		itmRefreshStep100.setState(i == 100);
		itmRefreshStepPage.setState(i == -1);
		mjb.RefreshStep = i;
	}

	// ----------------------------------------------------------------
	// Input count of states
	private void InputCountOfStates() {
		String sDefault = String.valueOf(mjb.StatesCount);
		String sRange = "2.." + (MJBoard.MAX_CLO + 1);
		InputBox ib = new InputBox(new Frame(""), sDefault, "Count of states",
				"Input the count of states (" + sRange + "):");
		requestFocus();
		if (ib.isAccepted) {
			int iTmp;
			String sRetVal = ib.txtFld.getText();
			try {
				iTmp = Integer.valueOf(sRetVal);
				mjb.SetStatesCount(iTmp);
			} catch (Exception e) {
            }
		}
		ib.dispose();
	}

	// ----------------------------------------------------------------
	// Input the active state
	private void InputActiveState() {
		String sDefault = String.valueOf(mjb.CrrState);
		String sRange = "0.." + (mjb.StatesCount - 1);
		InputBox ib = new InputBox(new Frame(""), sDefault, "Active state",
				"Input the active state (" + sRange + "):");
		requestFocus();
		if (ib.isAccepted) {
			int iTmp;
			String sRetVal = ib.txtFld.getText();
			try {
				iTmp = Integer.valueOf(sRetVal);
				mjb.SetCrrState(iTmp);
			} catch (Exception e) {
            }
		}
		ib.dispose();
	}

	// ----------------------------------------------------------------
	// Input the board size
	private void InputBoardSize() {
		String sDefault = String.valueOf(mjb.UnivSize.x) + 'x'
				+ String.valueOf(mjb.UnivSize.y);
		String sMax = "max. " + MJBoard.MAX_X + 'x'
				+ String.valueOf(MJBoard.MAX_Y);
		InputBox ib = new InputBox(new Frame(""), sDefault, "Board size",
				"Input the new board size (" + sMax + "):");
		requestFocus();
		if (ib.isAccepted) {
			Point iSize = mjb.UnivSize;
			String sRetVal = ib.txtFld.getText(); // "999x999"
			try {
				//noinspection UseOfStringTokenizer
				StringTokenizer st = new StringTokenizer(sRetVal, " .,;x-",
						false);
				if (st.hasMoreTokens()) {
					String sTok = st.nextToken();
					iSize.x = Integer.valueOf(sTok);
					if (st.hasMoreTokens()) {
						sTok = st.nextToken();
						iSize.y = Integer.valueOf(sTok);
					}
				}
				mjb.SetBoardSize(iSize.x, iSize.y);
			} catch (Exception e) {
            }
		}
		ib.dispose();
	}

	// ----------------------------------------------------------------
	// Update the states/colors UI logic
	@SuppressWarnings("HardcodedFileSeparator")
	public void UpdateColorsUI() {
		itmCloMtdStd.setState(mjb.ColoringMethod == 1); // standard coloring
		itmCloMtdAlt.setState(mjb.ColoringMethod == 2); // alternate coloring
		itmCloStatesCnt.setLabel("Count of states... ("
				+ mjb.StatesCount + ')');
		itmCloCrrState.setLabel("Active state... ("
				+ mjb.CrrState + ')');
		lblStates.setText("States: " + Integer.toString(mjb.CrrState) + '/'
				+ Integer.toString(mjb.StatesCount));

		boolean fSttCntEna = false;
		switch (mjb.CrrGame) {
		case MJRules.GAME_LIFE: // Standard Conway-like game
		case MJRules.GAME_VOTE: // Vote for life
		case MJRules.GAME_SPEC: // Special rules
		case MJRules.GAME_1DBI: // 1D binary
			fSttCntEna = true;
			break;
		case MJRules.GAME_GENE: // Generations
		case MJRules.GAME_RTBL: // Rules table
		case MJRules.GAME_CYCL: // Cyclic CA
		case MJRules.GAME_NMBI: // Neumann binary
			fSttCntEna = false;
			break;
		case MJRules.GAME_WLIF: // Weighted life
			fSttCntEna = !mjb.RWLife.isHist;
			break;
		case MJRules.GAME_GEBI: // General binary
			fSttCntEna = !mjb.RGenBin.isHist;
			break;
		case MJRules.GAME_LGTL: // Larger than Life
			fSttCntEna = !mjb.RLgtL.isHist;
			break;
		case MJRules.GAME_MARG: // Margolus
			fSttCntEna = !mjb.RMarg.isHist;
			break;
		case MJRules.GAME_USER: // User DLL
			fSttCntEna = !mjb.RUser.isHist;
			break;
		case MJRules.GAME_1DTO: // 1-D CA totalistic
			fSttCntEna = !mjb.R1DTo.isHist;
			break;
		}
		itmCloStatesCnt.setEnabled(fSttCntEna);
		itmCloMtdStd.setEnabled(fSttCntEna);
		itmCloMtdAlt.setEnabled(fSttCntEna);
		itmCloNextState.setEnabled(mjb.CrrState < mjb.StatesCount - 1);
		itmCloPrevState.setEnabled(mjb.CrrState > 0);
	}

	// ----------------------------------------------------------------
	// update the randomizing controls logic
	public void UpdateRandomizingUI() {
		if (chkMon.getState()) {
			chkUni.setEnabled(false);
			cmbRand.setEnabled(true);
		} else {
			chkUni.setEnabled(true);
			if (chkUni.getState()) {
				cmbRand.setEnabled(false);
			} else {
				cmbRand.setEnabled(true);
			}
		}
	}

	// ----------------------------------------------------------------
	// update the grid UI
	public void UpdateGridUI() {
		chkGrid.setState(mjb.DrawGrid);
		itmGrid.setState(chkGrid.getState());

		chkGrid.setEnabled((mjb.CellSize > 4));
		itmGrid.setEnabled(chkGrid.isEnabled());
	}

	// ----------------------------------------------------------------
	// Set one of two coloring methods:
	//  1 - standard coloring
	//  2 - alternate coloring
	public void SetColoringMethod(int mtd) {
		if ((mtd != 1) && (mtd != 2))
			mtd = 1; // standard coloring
		mjb.ColoringMethod = mtd;
		UpdateColorsUI();
	}

	// ----------------------------------------------------------------
	// Activate the given color palette
	public void SetColorPalette(String sPalNam) {
		mjb.mjPal.PalName = sPalNam;
		mjb.SetStatesCount(mjb.StatesCount); // force activation
		mjb.RedrawBoard(true); // redraw everything

		itmCPlMjcStd.setState("MJCell Standard".equalsIgnoreCase(sPalNam));
		itmCPl8Color.setState("8 colors".equalsIgnoreCase(sPalNam));
		itmCPlRedWht.setState("Red & blue".equalsIgnoreCase(sPalNam));
		itmCPlBlu.setState("Dolphin".equalsIgnoreCase(sPalNam));
		itmCPlTst.setState("Milky way".equalsIgnoreCase(sPalNam));
	}

	// ----------------------------------------------------------------
	// Show the 'About...' dialog
	public void DialogAbout() {
		msgDlg = new Dialog(this, "About MJCell");
		msgDlg.setSize(360, 340);

		Button btnOk = new Button("   Close   ");
		TextArea ta = new TextArea(mjc.getAppletInfo());
		ta.appendText("\n\nSystem details");
		ta.appendText("\nBase URL: " + sBaseURL);
		ta.appendText("\nJava vendor: " + System.getProperty("java.vendor"));
		ta.appendText("\nJava version: " + System.getProperty("java.version"));
		ta.appendText("\nOS: " + System.getProperty("os.name") + ", v."
				+ System.getProperty("os.version"));
		Panel btnPnl = new Panel();
		ta.setEditable(false);
		btnPnl.setBackground(Color.lightGray);
		btnPnl.add(btnOk);
		msgDlg.add(ta, BorderLayout.CENTER);
		msgDlg.add(btnPnl, BorderLayout.SOUTH);
		btnOk.addActionListener(e -> msgDlg.dispose());
		msgDlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				msgDlg.dispose();
			}
		});
		msgDlg.setModal(true);
		msgDlg.show();
	}

	// ----------------------------------------------------------------
	// Show the 'Info...' dialog
	public void DialogInfo() {
		boolean fOldRun = mjb.caThread != null;

		// stop while the dialog in open
		mjb.stop();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}

		msgDlg = new Dialog(this, "Info");
		msgDlg.setSize(300, 300);

		Button btnOk = new Button("   Close   ");
		TextArea ta = new TextArea();
		Panel btnPnl = new Panel();
		btnPnl.setBackground(Color.lightGray);
		btnPnl.add(btnOk);
		ta.setEditable(false);
		ta.append("Rule family: " + mjr.GetGameName(mjb.CrrGame) + '\n');
		ta.append("Rule name: " + mjb.RuleName + '\n');
		ta.append("Rule definition: " + mjb.RuleDef + '\n');
		ta.append("Count of states: " + mjb.StatesCount + '\n');
		ta.append("Color palette: " + mjb.mjPal.PalName + '\n');
		ta.append("\n");
		ta.append("Board: " + mjb.UnivSize.x + 'x'
				+ String.valueOf(mjb.UnivSize.y) + '\n');
		ta.append("Cell size: " + mjb.CellSize + '\n');
		ta.append("1D current line: " + mjb.i1DLastRow + '\n');
		ta.append("\n");
		ta.append("Speed: " + Integer.toString(mjb.AnimDelay) + '\n');
		ta.append("Cycle: " + Integer.toString(mjb.Cycle) + '\n');
		ta.append("Population: " + Integer.toString(mjb.Population) + '\n');

		double dTmp = 100.0 * mjb.Population
				/ (mjb.UnivSize.x * mjb.UnivSize.y);
		dTmp = (Math.round(dTmp * 100.0) / 100.0);
		ta.append("Density: " + Double.toString(dTmp) + "%\n");
		ta.append("\nDistribution:\n");
		for (int i = 0; i < mjb.StatesCount; i++) {
			ta.append("State " + Integer.toString(i) + ": "
					+ Integer.toString(mjb.Populations[i]) + '\n');
		}

		msgDlg.add(ta, BorderLayout.CENTER);
		msgDlg.add(btnPnl, BorderLayout.SOUTH);
		btnOk.addActionListener(e -> {
            msgDlg.dispose();
            if (fOldRun)
                mjb.start();
        });
		msgDlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				msgDlg.dispose();
				if (fOldRun)
					mjb.start();
			}
		});
		msgDlg.setModal(true);
		msgDlg.show();
	}

	// ----------------------------------------------------------------
	// show pattern description
	public void DialogDesc() {
		boolean fOldRun = mjb.caThread != null;

		// stop while the dialog in open
		mjb.stop();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}

		msgDlg = new Dialog(this, "Pattern description");
		msgDlg.setSize(350, 300);

		Button btnOk = new Button("   Close   ");
		TextArea ta = new TextArea();
		Panel btnPnl = new Panel();
		btnPnl.setBackground(Color.lightGray);
		btnPnl.add(btnOk);
		ta.setEditable(false);

		if (!vDescr.isEmpty())
			for (String aVDescr : vDescr) ta.append(aVDescr + '\n');
		else
			ta.append("\n No description");

		msgDlg.add(ta, BorderLayout.CENTER);
		msgDlg.add(btnPnl, BorderLayout.SOUTH);
		btnOk.addActionListener(e -> {
            msgDlg.dispose();
            if (fOldRun)
                mjb.start();
        });
		msgDlg.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				msgDlg.dispose();
				if (fOldRun)
					mjb.start();
			}
		});
		msgDlg.setModal(true);
		msgDlg.show();
	}

	// ----------------------------------------------------------------
	// Faster animation
	public void RunFaster() {
		if (mjb.AnimDelay <= 50)
			setAnimDelay(0);
		else if (mjb.AnimDelay <= 100)
			setAnimDelay(50);
		else
			setAnimDelay(mjb.AnimDelay - 100);
	}

	// ----------------------------------------------------------------
	// Faster animation
	public void RunSlower() {
		if (mjb.AnimDelay < 50)
			setAnimDelay(50);
		else if (mjb.AnimDelay < 100)
			setAnimDelay(100);
		else
			setAnimDelay(mjb.AnimDelay + 100);
	}

	// ----------------------------------------------------------------
	// Set the animation delay, 0..1000
	public void setAnimDelay(int newDelay) {
		mjb.setAnimDelay(newDelay);
		btnSlower.enable(mjb.AnimDelay < 1000);
		itmSlower.enable(mjb.AnimDelay < 1000);
		btnFaster.enable(mjb.AnimDelay > 0);
		itmFaster.enable(mjb.AnimDelay > 0);
	}

	// ----------------------------------------------------------------
	// update dynamic elements of the UI
	@SuppressWarnings("HardcodedFileSeparator")
	public void UpdateUI() {
		lblCycle.setText("Cycle: " + Integer.toString(mjb.Cycle));
		lblPopul.setText("Population: " + Integer.toString(mjb.Population)
				+ ' ');
		lblBoard.setText("Board: " + Integer.toString(mjb.UnivSize.x) + 'x'
				+ Integer.toString(mjb.UnivSize.y) + '/'
				+ Integer.toString(mjb.CellSize));
	}

	// ----------------------------------------------------------------
	// applet info
	public String getAppletInfo() {
		return mjc.getAppletInfo();
	}
	// ----------------------------------------------------------------

}