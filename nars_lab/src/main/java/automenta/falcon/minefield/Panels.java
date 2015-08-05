//package automenta.falcon.minefield;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.image.BufferedImage;
//import java.io.File;
//
//class MazePalette {
//    public static final int AV_Color_Num = 8;
//    public static final Color[] AV_Color = {Color.black, Color.blue, Color.magenta, Color.green, Color.yellow, Color.cyan, Color.pink, Color.white};
//    public static int AV_Color_Index = 0;
//}
//
//class MazePanel extends JPanel implements ActionListener {
//    final int size = 16;
//    final int MAXSTEP = 500;
//    private final int agent_num;
//    private int[][] current;
//    private int[] target;
//    private int[] bearing;
//    private int[] currentBearing;
//    private int[] targetBearing;
//    private double[] sonar;
//    private double range;
//    private int[][] mines;
//    private int[][][] path;
//    private int[] numStep;
//    private int maxStep;
//
//    /**
//     * Background image.
//     *
//     * @author J.J.
//     */
//    private BufferedImage bgImage;
//    /**
//     * Bome image.
//     *
//     * @author J.J.
//     */
//    private Image bombIcon;
//    /**
//     * Tank image.
//     *
//     * @author J.J.
//     */
//    private Image[] tankIcon;
//    /**
//     * Target image.
//     *
//     * @author J.J.
//     */
//    private Image targetIcon;
//    /**
//     * popup menu for bgimage seleciton.
//     *
//     * @author Jin Jun
//     */
//    private JPopupMenu popup;
//
//    public MazePanel(Maze m) {
//        agent_num = 1;
//        init_MP(m);
//    }
//
//    public MazePanel(int agt, Maze m) {
//        agent_num = agt;
//        init_MP(m);
//        doRefresh(m);
//    }
//
//    public void init_MP(Maze m) {
//        // initiate components first.
//        initComponents();
//
//        int agt;
//
//        numStep = new int[agent_num];
//        current = new int[agent_num][];
//        currentBearing = new int[agent_num];
//        for (agt = 0; agt < agent_num; agt++)
//            current[agt] = new int[2];
//        m.getCurrent(current);
//
//        path = new int[MAXSTEP][agent_num][2];
//
//        target = new int[2];
//        target = m.getTarget();
//
//        mines = new int[size][size];
//        for (int i = 0; i < size; i++)
//            for (int j = 0; j < size; j++)
//                mines[i][j] = m.getMines(i, j);
//
//        /**
//         * load icons
//         */
//        loadImageIcons();
//
//        /**
//         * load background
//         */
//        /*File bgFile = new File("./images/background.jpg");
//
//        if (bgFile.canRead()) {
//            loadBackgroundImage(bgFile);
//        } else {
//            System.out.println("Cannot find default background image.");
//        }*/
//    }
//
//    /**
//     * Load icons for objects in the maze.
//     *
//     * @author J.J.
//     */
//    private void loadImageIcons() {
//        bombIcon = new ImageIcon("./images/bomb.png", "Bomb icon").getImage();
//        tankIcon = new Image[8];
//        for (int i = 0; i < 8; i++) {
//            tankIcon[i] = new ImageIcon("./images/tank" + i + ".png",
//                    "Tank icon").getImage();
//        }
//        targetIcon = new ImageIcon("./images/target.png", "Target icon").getImage();
//    }
//
//    /**
//     * Method to initiate components.
//     *
//     * @author J.J.
//     */
//    private void initComponents() {
//        popup = new JPopupMenu();
//        JMenuItem bgMenuItem = new JMenuItem("Change background image");
//        bgMenuItem.addActionListener(this);
//        popup.add(bgMenuItem);
//
//        this.addMouseListener(new PopupListener());
//    }
//
//    /**
//     * Method to load background image.
//     *
//     * @author J.J.
//     */
//    private void loadBackgroundImage(File file) {
//
//        try {
//            bgImage = javax.imageio.ImageIO.read(file);
//
//        } catch (Exception e) {
//            System.out.println("Load background failed.");
//        }
//    }
//
//    /**
//     * Method to draw background image.
//     *
//     * @author J.J.
//     */
//    private void drawBackgroundImage(Graphics g) {
//        if (this.bgImage == null) return;
//
//        float factor = 0;
//
//        g.drawImage(this.bgImage, 0, 0, this.getSize().width, this.getSize().height, this);
////        ((Graphics2D)g).drawImage(bgImage, null,0,0);
//    }
//
//    public void doRefresh(Maze m) {
//
//        m.getCurrent(current);
//        for (int k = 0; k < agent_num; k++)
//            currentBearing[k] = m.getCurrentBearing(k);
//
//        target = m.getTarget();
//        for (int i = 0; i < size; i++)
//            for (int j = 0; j < size; j++)
//                mines[i][j] = m.getMines(i, j);
//
//        for (int a = 0; a < agent_num; a++)
//            numStep[a] = 0;
//        repaint();
//    }
//
//    public void setCurrent(Maze m) {
//        int k;
//
//        m.getCurrent(current);
//        for (k = 0; k < agent_num; k++)
//            currentBearing[k] = m.getCurrentBearing(k);
//        if (MazeRun.graphic) repaint();
//    }
//
//    public void setCurrentPath(Maze m, int[][] pos, int step)
//            throws ArrayIndexOutOfBoundsException {
//        int agt;
//        try {
//            for (agt = 0; agt < agent_num; agt++) {
//                path[step][agt] = pos[agt];
//                numStep[agt] = step;
//            }
//            if (MazeRun.graphic) repaint();
//        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println("path array index out of bound:" + e.getMessage());
//        }
//    }
//
//    protected void paintComponent(Graphics g) {
//        int a;
//        Color c;
//
//        super.paintComponent(g);
//        drawBackgroundImage(g);
//
//        int radius = getWidth() / size / 2;
//        int radius_path = 0;
//
//        /**
//         * drawing obstacle
//         *
//         * documented by J.J.
//         */
//        g.setColor(Color.red);
//        for (int i = 0; i < size; i++)
//            for (int j = 0; j < size; j++)
//                if (mines[i][j] == 1)
//                    g.drawImage(bombIcon,
//                            (int) ((getWidth() / size) * (i + 0.5)) - radius,
//                            (int) ((getHeight() / size) * (j + 0.5)) - radius,
//                            radius * 2, radius * 2, this);
//
//        /**
//         * drawing mine
//         */
//        g.setColor(Color.orange);
//        g.drawImage(targetIcon,
//                (int) ((getWidth() / size) * (target[0] + 0.5)) - radius,
//                (int) ((getHeight() / size) * (target[1] + 0.5)) - radius,
//                2 * radius, 2 * radius, this);
//
//        radius *= 0.75;
//
//        for (a = 0; a < agent_num; a++) {
//            c = MazePalette.AV_Color[a % MazePalette.AV_Color_Num];
//            g.setColor(c);
//            /**
//             * drawing tank
//             */
//            if (currentBearing[a] % 2 == 0) {
//                g.drawImage(tankIcon[currentBearing[a]],
//                        (int) ((getWidth() / size) * (current[a][0] + 0.5)) - radius,
//                        (int) ((getHeight() / size) * (current[a][1] + 0.5)) - radius,
//                        2 * radius, 2 * radius, this);
//            } else {
//                g.drawImage(tankIcon[currentBearing[a]],
//                        (int) ((getWidth() / size) * (current[a][0] + 0.5)) - radius,
//                        (int) ((getHeight() / size) * (current[a][1] + 0.5)) - radius,
//                        (int) (2.828 * radius), (int) (2.828 * radius), this);
//            }
//
//
//// 			for the purpose of tracking the actions of agents
//            if (!MazeRun.Track)
//                continue;
//
//            for (int i = 0; i < numStep[a]; i++) {
//                radius_path = radius * (i + 1) / numStep[a];
//                if ((path[i][a][0] == 0) && (path[i][a][1] == 0)) {
//                    g.setColor(Color.red);
//                    radius_path *= 2;
//                } else
//                    g.setColor(c);
//
//                g.fillRect((int) ((getWidth() / size) * (path[i][a][0] + 0.5)) - radius_path / 2,
//                        (int) ((getHeight() / size) * (path[i][a][1] + 0.5)) - radius_path / 2,
//                        radius_path, radius_path);
//
//                if ((path[i + 1][a][0] == 0) && (path[i + 1][a][1] == 0)) {
//                    g.setColor(Color.red);
//                } else
//                    g.setColor(c);
//
//                if (path[i + 1][a][0] != 0 || path[i + 1][a][1] != 0)
//                    g.drawLine((int) ((getWidth() / size) * (path[i][a][0] + 0.5)),
//                            (int) ((getHeight() / size) * (path[i][a][1] + 0.5)),
//                            (int) ((getWidth() / size) * (path[i + 1][a][0] + 0.5)),
//                            (int) ((getHeight() / size) * (path[i + 1][a][1] + 0.5)));
//            }
//        }
//    }
//
//    public void actionPerformed(ActionEvent e) {
//        JFileChooser fc = new JFileChooser();
//
//        fc.showOpenDialog(this);
//
//        File bgFile = fc.getSelectedFile();
//
//        if (bgFile == null) return;
//
//        loadBackgroundImage(bgFile);
//        if (MazeRun.graphic) repaint();
//    }
//
//    class PopupListener extends MouseAdapter {
//        public void mousePressed(MouseEvent e) {
//            maybeShowPopup(e);
//        }
//
//        public void mouseReleased(MouseEvent e) {
//            maybeShowPopup(e);
//        }
//
//        private void maybeShowPopup(MouseEvent e) {
//            if (e.isPopupTrigger()) {
//                if (popup == null) return;
//
//                popup.show(e.getComponent(),
//                        e.getX(), e.getY());
//            }
//        }
//    }
//}
//
//class SonarPanel extends JPanel {
//    private final double[] sonar;
//    private final int numSonar = 5;
//    private final Color fore_color;
//    private final boolean sonar_mode;
//    private int bearing;
//    private double range;
//    private int radius;
//    private int agent;
//
//    public SonarPanel(int agt, boolean mode, Color c, Maze m) {
//        agent = agt;
//        sonar_mode = mode;
//        fore_color = c;
//        sonar = new double[numSonar];
//        if (sonar_mode)
//            m.getSonar(agt, sonar);
//        else
//            m.getAVSonar(agt, sonar);
//    }
//
//    public void setSonar(Maze m) {
//        agent = 0;
//        if (sonar_mode)
//            m.getSonar(agent, sonar);
//        else
//            m.getAVSonar(agent, sonar);
//        if (MazeRun.graphic) repaint();
//    }
//
//    public boolean get_sonar_mode() {
//        return (sonar_mode);
//    }
//
//    public void setSonar(int agt, Maze m) {
//        agent = agt;
//        if (sonar_mode)
//            m.getSonar(agt, sonar);
//        else
//            m.getAVSonar(agt, sonar);
//        if (MazeRun.graphic) repaint();
//    }
//
//    protected void paintComponent(Graphics g) {
//        int r;
//
//        super.paintComponent(g);
//
//        g.setColor(Color.black);
//        for (int i = 0; i < numSonar; i++)
//            g.drawRect((getWidth() / numSonar) * i, getHeight() / 2 - getWidth() / numSonar / 2, getWidth() / numSonar, getWidth() / numSonar);
//
//        g.setColor(fore_color);
//        r = Math.min(getHeight() / 2, getWidth() / (2 * numSonar));
//        for (int i = 0; i < numSonar; i++) {
//            radius = (int) (sonar[i] * r);
//            g.fillOval((getWidth() / numSonar) * i + getWidth() / (2 * numSonar) - radius, getHeight() / 2 - radius, radius * 2, radius * 2);
//        }
//    }
//}
//
//class BearingPanel extends JPanel {
//    private final double range;
//    private int currentBearing;
//    private int targetBearing;
//    private int radius;
//    private int agent;
//
//
//    public BearingPanel(int agt, Maze m) {
//        agent = agt;
//        currentBearing = m.getCurrentBearing(agt);
//        targetBearing = m.getTargetBearing(agt);
//        range = m.getRange(agt);
//
//    }
//
//    public void setBearing(Maze m) {
//        agent = 0;
//        currentBearing = m.getCurrentBearing(0);
//        targetBearing = m.getTargetBearing(0);
//        if (MazeRun.graphic) repaint();
//    }
//
//    public void setBearing(int agt, Maze m) {
//        agent = agt;
//        currentBearing = m.getCurrentBearing(agt);
//        targetBearing = m.getTargetBearing(agt);
//        if (MazeRun.graphic) repaint();
//    }
//
//    protected void paintComponent(Graphics g) {
//
//        super.paintComponent(g);
//
//        g.setColor(Color.blue);
//        radius = Math.min(getHeight() / 2, getWidth() / 4);
//        g.fillOval(getWidth() / 4 - radius, getHeight() / 2 - radius, radius * 2, radius * 2);
//        g.setColor(Color.magenta);
//        int cx = getWidth() / 4;
//        int cy = getHeight() / 2;
//
//        switch (currentBearing) {
//            case 0:
//                g.drawLine(cx, cy, cx, cy - radius);
//                g.drawLine(cx - 1, cy, cx - 1, cy - radius + 1);
//                g.drawLine(cx + 1, cy, cx + 1, cy - radius + 1);
//                break;
//            case 1:
//                g.drawLine(cx, cy, (int) (cx + radius / Math.sqrt(2.0)), (int) (cy - radius / Math.sqrt(2.0)));
//                g.drawLine(cx - 1, cy - 1, (int) (cx + radius / Math.sqrt(2.0)) - 1, (int) (cy - radius / Math.sqrt(2.0)) - 1);
//                g.drawLine(cx + 1, cy + 1, (int) (cx + radius / Math.sqrt(2.0)) + 1, (int) (cy - radius / Math.sqrt(2.0)) + 1);
//                break;
//            case 2:
//                g.drawLine(cx, cy, cx + radius, cy);
//                g.drawLine(cx, cy - 1, cx + radius - 1, cy - 1);
//                g.drawLine(cx, cy + 1, cx + radius - 1, cy + 1);
//                break;
//            case 3:
//                g.drawLine(cx, cy, (int) (cx + radius / Math.sqrt(2.0)), (int) (cy + radius / Math.sqrt(2.0)));
//                g.drawLine(cx + 1, cy - 1, (int) (cx + radius / Math.sqrt(2.0)) + 1, (int) (cy + radius / Math.sqrt(2.0)) - 1);
//                g.drawLine(cx - 1, cy + 1, (int) (cx + radius / Math.sqrt(2.0)) - 1, (int) (cy + radius / Math.sqrt(2.0)) + 1);
//                break;
//            case 4:
//                g.drawLine(cx, cy, cx, cy + radius);
//                g.drawLine(cx - 1, cy, cx - 1, cy + radius - 1);
//                g.drawLine(cx + 1, cy, cx + 1, cy + radius - 1);
//                break;
//            case 5:
//                g.drawLine(cx, cy, (int) (cx - radius / Math.sqrt(2.0)), (int) (cy + radius / Math.sqrt(2.0)));
//                g.drawLine(cx + 1, cy + 1, (int) (cx - radius / Math.sqrt(2.0)) + 1, (int) (cy + radius / Math.sqrt(2.0)) + 1);
//                g.drawLine(cx - 1, cy - 1, (int) (cx - radius / Math.sqrt(2.0)) - 1, (int) (cy + radius / Math.sqrt(2.0)) - 1);
//                break;
//            case 6:
//                g.drawLine(cx, cy, cx - radius, cy);
//                g.drawLine(cx, cy - 1, cx - radius + 1, cy - 1);
//                g.drawLine(cx, cy + 1, cx - radius + 1, cy + 1);
//                break;
//            case 7:
//                g.drawLine(cx, cy, (int) (cx - radius / Math.sqrt(2.0)), (int) (cy - radius / Math.sqrt(2.0)));
//                g.drawLine(cx + 1, cy - 1, (int) (cx - radius / Math.sqrt(2.0)) + 1, (int) (cy - radius / Math.sqrt(2.0)) - 1);
//                g.drawLine(cx - 1, cy + 1, (int) (cx - radius / Math.sqrt(2.0)) - 1, (int) (cy - radius / Math.sqrt(2.0)) + 1);
//                break;
//        }
//
//        g.setColor(Color.blue);
//        radius = getHeight() / 2;
//        g.fillOval((getWidth() * 3) / 4 - radius, getHeight() / 2 - radius, radius * 2, radius * 2);
//        g.setColor(Color.magenta);
//        cx = (getWidth() * 3) / 4;
//        cy = getHeight() / 2;
//
//        switch (targetBearing) {
//            case 0:
//                g.drawLine(cx, cy, cx, cy - radius);
//                g.drawLine(cx - 1, cy, cx - 1, cy - radius + 1);
//                g.drawLine(cx + 1, cy, cx + 1, cy - radius + 1);
//                break;
//            case 1:
//                g.drawLine(cx, cy, (int) (cx + radius / Math.sqrt(2.0)), (int) (cy - radius / Math.sqrt(2.0)));
//                g.drawLine(cx - 1, cy - 1, (int) (cx + radius / Math.sqrt(2.0)) - 1, (int) (cy - radius / Math.sqrt(2.0)) - 1);
//                g.drawLine(cx + 1, cy + 1, (int) (cx + radius / Math.sqrt(2.0)) + 1, (int) (cy - radius / Math.sqrt(2.0)) + 1);
//                break;
//            case 2:
//                g.drawLine(cx, cy, cx + radius, cy);
//                g.drawLine(cx, cy - 1, cx + radius - 1, cy - 1);
//                g.drawLine(cx, cy + 1, cx + radius - 1, cy + 1);
//                break;
//            case 3:
//                g.drawLine(cx, cy, (int) (cx + radius / Math.sqrt(2.0)), (int) (cy + radius / Math.sqrt(2.0)));
//                g.drawLine(cx + 1, cy - 1, (int) (cx + radius / Math.sqrt(2.0)) + 1, (int) (cy + radius / Math.sqrt(2.0)) - 1);
//                g.drawLine(cx - 1, cy + 1, (int) (cx + radius / Math.sqrt(2.0)) - 1, (int) (cy + radius / Math.sqrt(2.0)) + 1);
//                break;
//            case 4:
//                g.drawLine(cx, cy, cx, cy + radius);
//                g.drawLine(cx - 1, cy, cx - 1, cy + radius - 1);
//                g.drawLine(cx + 1, cy, cx + 1, cy + radius - 1);
//                break;
//            case 5:
//                g.drawLine(cx, cy, (int) (cx - radius / Math.sqrt(2.0)), (int) (cy + radius / Math.sqrt(2.0)));
//                g.drawLine(cx + 1, cy + 1, (int) (cx - radius / Math.sqrt(2.0)) + 1, (int) (cy + radius / Math.sqrt(2.0)) + 1);
//                g.drawLine(cx - 1, cy - 1, (int) (cx - radius / Math.sqrt(2.0)) - 1, (int) (cy + radius / Math.sqrt(2.0)) - 1);
//                break;
//            case 6:
//                g.drawLine(cx, cy, cx - radius, cy);
//                g.drawLine(cx, cy - 1, cx - radius + 1, cy - 1);
//                g.drawLine(cx, cy + 1, cx - radius + 1, cy + 1);
//                break;
//            case 7:
//                g.drawLine(cx, cy, (int) (cx - radius / Math.sqrt(2.0)), (int) (cy - radius / Math.sqrt(2.0)));
//                g.drawLine(cx + 1, cy - 1, (int) (cx - radius / Math.sqrt(2.0)) + 1, (int) (cy - radius / Math.sqrt(2.0)) - 1);
//                g.drawLine(cx - 1, cy + 1, (int) (cx - radius / Math.sqrt(2.0)) - 1, (int) (cy - radius / Math.sqrt(2.0)) + 1);
//                break;
//        }
//    }
//}
