package nars.sokoban;

import automenta.vivisect.swing.NWindow;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * "Sokoban 1.0, Written by Yossie Silverman."
 * @author Yossie Silverman  http://www.blacksteel.com/Sokoban/Sokoban.java
 */
public class Sokoban extends Applet {

	String[] levels = {

			"M^^^^#####" +
					"M^^^^#   #" +
					"M^^^^#$  #" +
					"M^^###  $##" +
					"M^^#  $ $ #" +
					"M### # ## #^^^######" +
					"M#   # ## #####  ..#" +
					"M# $  $          ..#" +
					"M##### ### #@##  ..#" +
					"M^^^^#     #########" +
					"M^^^^#######",

			"M############" +
					"M#..  #     ###" +
					"M#..  # $  $  #" +
					"M#..  #$####  #" +
					"M#..    @ ##  #" +
					"M#..  # #  $ ##" +
					"M###### ##$ $ #" +
					"M^^# $  $ $ $ #" +
					"M^^#    #     #" +
					"M^^############",

			"M^^^^^^^^########" +
					"M^^^^^^^^#     @#" +
					"M^^^^^^^^# $#$ ##" +
					"M^^^^^^^^# $  $#" +
					"M^^^^^^^^##$ $ #" +
					"M######### $ # ###" +
					"M#....  ## $  $  #" +
					"M##...    $  $   #" +
					"M#....  ##########" +
					"M########M",

			"M^^^^^^^^^^^########" +
					"M^^^^^^^^^^^#  ....#" +
					"M############  ....#" +
					"M#    #  $ $   ....#" +
					"M# $$$#$  $ #  ....#" +
					"M#  $     $ #  ....#" +
					"M# $$ #$ $ $########" +
					"M#  $ #     #" +
					"M## #########" +
					"M#    #    ##" +
					"M#     $   ##" +
					"M#  $$#$$  @#" +
					"M#    #    ##" +
					"M###########",

			"M^^^^^^^^#####" +
					"M^^^^^^^^#   #####" +
					"M^^^^^^^^# #$##  #" +
					"M^^^^^^^^#     $ #" +
					"M######### ###   #" +
					"M#....  ## $  $###" +
					"M#....    $ $$ ##" +
					"M#....  ##$  $ @#" +
					"M#########  $  ##" +
					"M^^^^^^^^# $ $  #" +
					"M^^^^^^^^### ## #" +
					"M^^^^^^^^^^#    #" +
					"M^^^^^^^^^^######",

			"M######^^###" +
					"M#..  #^##@##" +
					"M#..  ###   #" +
					"M#..     $$ #" +
					"M#..  # # $ #" +
					"M#..### # $ #" +
					"M#### $ #$  #" +
					"M^^^#  $# $ #" +
					"M^^^# $  $  #" +
					"M^^^#  ##   #" +
					"M^^^#########",

			"M^^^^^^^#####" +
					"M^#######   ##" +
					"M## # @## $$ #" +
					"M#    $      #" +
					"M#  $  ###   #" +
					"M### #####$###" +
					"M# $  ### ..#" +
					"M# $ $ $ ...#" +
					"M#    ###...#" +
					"M# $$ #^#...#" +
					"M#  ###^#####" +
					"M####",

			"M^^^^^^^^^^#######" +
					"M^^^^^^^^^^#  ...#" +
					"M^^^^^^#####  ...#" +
					"M^^^^^^#      . .#" +
					"M^^^^^^#  ##  ...#" +
					"M^^^^^^## ##  ...#" +
					"M^^^^^### ########" +
					"M^^^^^# $$$ ##" +
					"M^#####  $ $ #####" +
					"M##   #$ $   #   #" +
					"M#@ $  $    $  $ #" +
					"M###### $$ $ #####" +
					"M^^^^^#      #" +
					"M^^^^^########",

			"M^###^^#############" +
					"M##@####       #   #" +
					"M# $$   $$  $ $ ...#" +
					"M#  $$$#    $  #...#" +
					"M# $   # $$ $$ #...#" +
					"M###   #  $    #...#" +
					"M#     # $ $ $ #...#" +
					"M#    ###### ###...#" +
					"M## #  #  $ $  #...#" +
					"M#  ## # $$ $ $##..#" +
					"M# ..# #  $      #.#" +
					"M# ..# # $$$ $$$ #.#" +
					"M##### #       # #.#" +
					"M^^^^# ######### #.#" +
					"M^^^^#           #.#" +
					"M^^^^###############",

			"M^^^^^^^^^^####" +
					"M^^^^^####^#  #" +
					"M^^^### @###$ #" +
					"M^^##      $  #" +
					"M^##  $ $$## ##" +
					"M^#  #$##     #" +
					"M^# # $ $$ # ###" +
					"M^#   $ #  # $ #####" +
					"M####    #  $$ #   #" +
					"M#### ## $         #" +
					"M#.    ###  ########" +
					"M#.. ..#^####" +
					"M#...#.#" +
					"M#.....#" +
					"M#######",

			"M^^####" +
					"M^^#  ###########" +
					"M^^#    $   $ $ #" +
					"M^^# $# $ #  $  #" +
					"M^^#  $ $  #    #" +
					"M### $# #  #### #" +
					"M#@#$ $ $  ##   #" +
					"M#    $ #$#   # #" +
					"M#   $    $ $ $ #" +
					"M^####  #########" +
					"M^^#      #" +
					"M^^#      #" +
					"M^^#......#" +
					"M^^#......#" +
					"M^^#......#" +
					"M^^########",

			"M################" +
					"M#              #" +
					"M# # ######     #" +
					"M# #  $ $ $ $#  #" +
					"M# #   $@$   ## ##" +
					"M# #  $ $ $###...#" +
					"M# #   $ $  ##...#" +
					"M# ###$$$ $ ##...#" +
					"M#     # ## ##...#" +
					"M#####   ## ##...#" +
					"M^^^^#####     ###" +
					"M^^^^^^^^#     #" +
					"M^^^^^^^^#######",

			"M^^^#########" +
					"M^^##   ##  #####" +
					"M###     #  #    ###" +
					"M#  $ #$ #  #  ... #" +
					"M# # $#@$## # #.#. #" +
					"M#  # #$  #    . . #" +
					"M# $    $ # # #.#. #" +
					"M#   ##  ##$ $ . . #" +
					"M# $ #   #  #$#.#. #" +
					"M## $  $   $  $... #" +
					"M^#$ ######    ##  #" +
					"M^#  #^^^^##########" +
					"M^####",

			"M^^^^^^^#######" +
					"M^#######     #" +
					"M^#     # $@$ #" +
					"M^#$$ #   #########" +
					"M^# ###......##   #" +
					"M^#   $......## # #" +
					"M^# ###......     #" +
					"M##   #### ### #$##" +
					"M#  #$   #  $  # #" +
					"M#  $ $$$  # $## #" +
					"M#   $ $ ###$$ # #" +
					"M#####     $   # #" +
					"M^^^^### ###   # #" +
					"M^^^^^^#     #   #" +
					"M^^^^^^########  #" +
					"M^^^^^^^^^^^^^####",

			"M^^^^#######" +
					"M^^^#   #  #" +
					"M^^^#  $   #" +
					"M^### #$   ####" +
					"M^#  $  ##$   #" +
					"M^#  # @ $ # $#" +
					"M^#  #      $ ####" +
					"M^## ####$##     #" +
					"M^# $#.....# #   #" +
					"M^#  $..**. $# ###" +
					"M##  #.....#   #" +
					"M#   ### #######" +
					"M# $$  #  #" +
					"M#  #     #" +
					"M######   #" +
					"M^^^^^#####",

			"M#####" +
					"M#   ##" +
					"M#    #^^####" +
					"M# $  ####  #" +
					"M#  $$ $   $#" +
					"M###@ #$    ##" +
					"M^#  ##  $ $ ##" +
					"M^# $  ## ## .#" +
					"M^#  #$##$  #.#" +
					"M^###   $..##.#" +
					"M^^#    #.*...#" +
					"M^^# $$ #.....#" +
					"M^^#  #########" +
					"M^^#  #" +
					"M^^####",

			"M^^^##########" +
					"M^^^#..  #   #" +
					"M^^^#..      #" +
					"M^^^#..  #  ####" +
					"M^^#######  #  ##" +
					"M^^#            #" +
					"M^^#  #  ##  #  #" +
					"M#### ##  #### ##" +
					"M#  $  ##### #  #" +
					"M# # $  $  # $  #" +
					"M# @$  $   #   ##" +
					"M#### ## #######" +
					"M^^^#    #" +
					"M^^^######",

			"M^^^^^###########" +
					"M^^^^^#  .  #   #" +
					"M^^^^^# #.    @ #" +
					"M^##### ##..# ####" +
					"M##  # ..###     ###" +
					"M# $ #...   $ #  $ #" +
					"M#    .. ##  ## ## #" +
					"M####$##$# $ #   # #" +
					"M^^## #    #$ $$ # #" +
					"M^^#  $ # #  # $## #" +
					"M^^#               #" +
					"M^^#  ###########  #" +
					"M^^####^^^^^^^^^####",

			"M^^######" +
					"M^^#   @####" +
					"M##### $   #" +
					"M#   ##    ####" +
					"M# $ #  ##    #" +
					"M# $ #  ##### #" +
					"M## $  $    # #" +
					"M## $ $ ### # #" +
					"M## #  $  # # #" +
					"M## # #$#   # #" +
					"M## ###   # # ######" +
					"M#  $  #### # #....#" +
					"M#    $    $   ..#.#" +
					"M####$  $# $   ....#" +
					"M#       #  ## ....#" +
					"M###################",

			"M^^^^##########" +
					"M#####        ####" +
					"M#     #   $  #@ #" +
					"M# #######$####  ###" +
					"M# #    ## #  #$ ..#" +
					"M# # $     #  #  #.#" +
					"M# # $  #     #$ ..#" +
					"M# #  ### ##     #.#" +
					"M# ###  #  #  #$ ..#" +
					"M# #    #  ####  #.#" +
					"M# #$   $  $  #$ ..#" +
					"M#    $ # $ $ #  #.#" +
					"M#### $###    #$ ..#" +
					"M^^^#    $$ ###....#" +
					"M^^^#      ##^######" +
					"M^^^########"

	};
	
	static final char wall = '#';
	static final char floor = ' ';
	static final char me = '@';
	static final char megoal = '&';
	static final char occupied = '*';
	static final char dollar = '$';
	static final char cr = 'M';
	static final char blank = '^';
	static final char goal = '.';
	Image[] tiles = new Image[128];
	
	AudioClip buzz, wow;
	
	char[] level;
	int currlevel, w, h, push, move;
	int lastcount, pos1, pos2, pos3;
	Rectangle lastrect;
	boolean uc;

	char[] savelevel;
	int savecurrlevel, savew, saveh, savepush, savemove;
	boolean gamesaved = false;
	
	Font font = new Font("Helvetica", Font.PLAIN, 12);
	Font fontb = new Font("Helvetica", Font.BOLD, 12);

        @SuppressWarnings("HardcodedFileSeparator")
		public Sokoban() throws HeadlessException, IOException {
                    //buzz = getAudioClip(getDocumentBase(), "buzz.au");
                    //wow = getAudioClip(getDocumentBase(), "wow.au");
                    MediaTracker tracker = new MediaTracker(this);
                    Image j = ImageIO.read(new File("nars_lab/nars/sokoban/Sokoban.gif"));
                    tracker.addImage(j,0);
                    try { tracker.waitForAll(); } catch (InterruptedException e) {}
                    String tile = "# @$.&*";
                    for (int i = 0; i < tile.length(); i++) {
                            tiles[tile.charAt(i)] = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
                            Graphics g = tiles[tile.charAt(i)].getGraphics();
                            g.drawImage(j, -i*16, 0, this);
                    }
                    j.flush();
                    newLevel(0);
                    requestFocus();
        }
	
	
        
		
	@Override
	public void paint(Graphics g) {
		update(g);
	}
	
	@Override
	public synchronized void update(Graphics g) {
		Dimension d = size();
		if (d.width * d.height == 0) return; // supposedly this can happen!
		Rectangle r = g.getClipRect();

		if (r.x < 72) { // only do this if necessary!
			g.setColor(Color.lightGray);
			g.fillRect(0, 0, d.width, d.height);
			g.setFont(fontb);
			g.setColor(Color.blue);
			g.drawString("Sokoban",0,16);
			g.setFont(font);
			g.setColor(Color.black);
			String[] help = {"h=Left", "j=Down", "k=Up", "l=Right", " (or Arrows)",
					"H,J,K,L=", " FastMove", "u=Undo", "A=Restart", "S=Save", "R=Restore",
					"+=UpLevel", "-=DownLevel"};
			for (int i = 0; i < help.length; i++)
				g.drawString(help[i], 0, 80 + 16 * i);
			g.setFont(fontb);
			g.drawString("Level:", 0, 32);
			g.drawString("Move:", 0, 48);
			g.drawString("Push:", 0, 64);
			drawStatus(g);
		}

		int y = -16 + h, x = -16 + w;
		for (char aLevel : level)
			if (aLevel == cr) {
				x = -16 + w;
				y += 16;
			} else {
				x += 16;
				if (aLevel == blank) continue;
				if (r.inside(x, y)) // only draw the images necessary for move!
					g.drawImage(tiles[aLevel], x, y, this);
			}
	}
	
	public void drawStatus(Graphics g) {
		g.setColor(Color.lightGray);
		g.fillRect(40, 16, 32, 48);
		g.setColor(Color.black);
		g.setFont(font);
		g.drawString("" + (currlevel + 1), 40, 32);
		g.drawString("" + move, 40, 48);
		g.drawString("" + push, 40, 64);
	}
	
	public void drawMove() {
		Graphics g = getGraphics();
		drawStatus(g); // order is important, since update munges the clipRect
		repaint(lastrect.x, lastrect.y, lastrect.width, lastrect.height);
	}
		
	@Override
	public boolean keyDown(Event e, int key) {
		uc = false;
		switch (e.key) {
			case 'H': uc = true;
			case 'h': case Event.LEFT: movearound(-1, 0); break;
			case 'L': uc = true;
			case 'l': case Event.RIGHT: movearound(1, 0); break;
			case 'K': uc = true;
			case 'k': case Event.UP: movearound(0, -1); break;
			case 'J': uc = true;
			case 'j': case Event.DOWN: movearound(0, 1); break;
			case '+':
			case '-': currlevel += e.key == '+' ? 1 : -1;
						if (currlevel < 0) currlevel = 0;
						else if (currlevel == levels.length) currlevel = levels.length - 1;
			case 'A': newLevel(currlevel); repaint(); break;
			case 'u': undomove(); break;
			case 'S': saveGame(); break;
			case 'R': if (gamesaved) restoreGame(); break;
		}
		return true;
	}
	
	public void newLevel(int l) {
		currlevel = l; push = 0; move = 0;
		w = 0; h = 0; level = levels[currlevel].toCharArray();
		lastcount = 0;
		int W = 0;
		for (char aLevel : level)
			if (aLevel == cr) {
				if (W > w) w = W;
				W = 0;
				h++;
			} else W++;
		Dimension d = size();
		w = 72 + (d.width - 72 - 16 * w) / 2; h = (d.height - 16 * h) / 2;
	}
	
	public void restoreGame() {
		currlevel = savecurrlevel;
		w = savew; h = saveh; push = savepush; move = savemove;
		level = savelevel; gamesaved = false;
		repaint();
	}
	
	public void saveGame() {
		savecurrlevel = currlevel;
		savew = w; saveh = h; savepush = push; savemove = move;
		savelevel = new char[level.length];
		System.arraycopy(level ,0, savelevel, 0, level.length);
		gamesaved = true;
	}
	
	public int	moveone(int pos, int x, int y, int dx, int dy) {
		int i;
		if (dx != 0) return pos + dx;
		if (dy == -1) for (i = pos - x - 2; level[i] != cr; i--);
		else for (i = pos + 1; level[i] != cr; i++);
		return i + x + 1;
	}
	
	public void movearound(int dx, int dy) {
		do {
			int x = 0, y = -1, savepos1 = pos1, savepos2 = pos2, savepos3 = pos3;
			for (pos1 = 0; pos1 < level.length; pos1++)
				if (level[pos1] == cr) { x = 0; y++; }
				else if ((level[pos1] != me) && (level[pos1] != megoal)) x++; else break;
			pos2 = moveone(pos1, x, y, dx, dy);
			int count = 0;
			if (level[pos2] == floor || level[pos2] == goal) count = 1;
			else {
				if (uc) { lastcount = 1; pos1 = savepos1; pos2 = savepos2; break; }
				if (level[pos2] == dollar || level[pos2] == occupied) {
					pos3 = moveone(pos2, x, y, dx, dy);
					if (level[pos3] == floor || level[pos3] == goal) count = 2;
				}
			}
			if (count > 0) {
				level[pos1] = level[pos1] == me ? floor : goal;
				level[pos2] = level[pos2] == floor || level[pos2] == dollar ? me : megoal;
				move++;
				if (count > 1) {
					level[pos3] = level[pos3] == floor ? dollar : occupied;
					push++;
				}
				lastcount = count;
				int xo = x + dx * count, yo = y + dy * count;
				lastrect = new Rectangle(w + Math.min(xo, x) * 16, h + Math.min(yo, y) * 16,
					(Math.abs(xo - x) + 1) * 16, (Math.abs(yo - y) + 1) * 16);
				drawMove();
				boolean b = true;
				for (char aLevel : level) if (aLevel == dollar) b = false;
				if (b) {
					wow.play();
					try { Thread.sleep(2000); } catch (InterruptedException e) {}
					newLevel(currlevel + 1);
					repaint();
				}
			} else {
				pos1 = savepos1; pos2 = savepos2; pos3 = savepos3;
				buzz.play();
			}
		} while (uc);
	}
	
	public void undomove() {
		if (lastcount > 0) {
			level[pos1] = level[pos1] == floor ? me : megoal;
			move--;
			if (lastcount > 1) {
				level[pos2] = level[pos2] == me ? dollar : occupied;
				level[pos3] = level[pos3] == dollar ? floor : goal;
				push--;
			} else level[pos2] = level[pos2] == me ? floor : goal;
			lastcount = 0;
			drawMove();
		}
	}
			
	@Override
	public String getAppletInfo() {
		return "Sokoban 1.0, Written by Yossie Silverman.";
	}
        
        public static void main(String[] args) throws Exception {
            NWindow w = new NWindow("Sokoban", new Sokoban());
            w.setSize(500,500);
            w.setVisible(true);
        }
}