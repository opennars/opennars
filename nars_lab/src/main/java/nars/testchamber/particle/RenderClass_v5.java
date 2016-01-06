//// sdurant12
//// 11/14/2012
//package nars.testchamber.particle;
//
//import javolution.util.FastTable;
//import nars.util.data.random.XORShiftRandom;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.awt.image.BufferedImage;
//import java.awt.image.DataBufferInt;
//import java.util.List;
//import java.util.Random;
//
//public class RenderClass_v5 extends JComponent implements MouseListener, MouseMotionListener, KeyListener {
//
//    Random r = new XORShiftRandom();
//    float oldX, oldY;
//    List<Particle> particleAL = new FastTable<>();
//    List<Graviton> gravitonAL = new FastTable<>();
//    private final int WIDTH, HEIGHT;
//    private long lastTime;
//    private boolean pause = false;
//    private boolean emit = false;
//
//    private final BufferedImage particleImage;
//    private final int[] particleRaster;
//    private final BufferedImage blockImage;
//    private final int[] blockRaster;
//    private final int[][] densityArray;
//    private final Block[][] blockArray;
//
//    public RenderClass_v5(int W, int H) {
//
//        addMouseListener(this);
//        addMouseMotionListener(this);
//        addKeyListener(this);
//        setFocusable(true);
//        requestFocusInWindow();
//
//        gravitonAL.add(new Graviton());
//
//        WIDTH = W / 2;
//        HEIGHT = H / 2;
//
//        setBackground(Color.BLACK);
//
//        particleImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
//        particleRaster = ((DataBufferInt) particleImage.getRaster().getDataBuffer()).getData();
//
//
//        blockImage = new BufferedImage(WIDTH / 4, HEIGHT / 4, BufferedImage.TYPE_INT_ARGB);
//        blockRaster = ((DataBufferInt) blockImage.getRaster().getDataBuffer()).getData();
//
//        densityArray = new int[WIDTH / 32 + 1][HEIGHT / 32 + 1];
//
//        blockArray = new Block[WIDTH / 32 + 1][HEIGHT / 32 + 1];
//
//        for (int x_I = 0; x_I < blockArray.length; x_I++) {
//            for (int y_I = 0; y_I < blockArray[1].length; y_I++) {
//                Block b = new Block();
//                b.setBlock(0, 0, 0, 0);
//
//                if (y_I < 9 && y_I > 2 && x_I > 2 && x_I < 11) {
//                    b.type = Block.GRYSTONE;
//                }
//                if (y_I % 2 == 0 && x_I % 2 == 0 && x_I < WIDTH / 32 - 2 && x_I > 2 && y_I < HEIGHT / 32 - 2 && y_I > 2) {
//                    b.type = Block.GRYSTONE;
//                }
//
//                blockArray[x_I][y_I] = b;
//            }
//        }
//
//    }
//
//    private static final float InvSqrt(float x) {
//        return Float.intBitsToFloat(0x5f3759d5 - (Float.floatToIntBits(x) >> 1));
//    }
//
//    public static final int additiveColor(int c1, int c2) {
//        int red = (c1 & 0x00ff0000) + (c2 & 0x00ff0000);
//        int grn = (c1 & 0x0000ff00) + (c2 & 0x0000ff00);
//        int blu = (c1 & 0x000000ff) + (c2 & 0x000000ff);
//        return 0xff000000 + (red > 0x00ff0000 ? 0x00ff0000 : red) + (grn > 0x0000ff00 ? 0x0000ff00 : grn) + (blu > 0x000000ff ? 0x000000ff : blu);
//    }
//
//    public void glow(int light, int x, int y) {
//
//        int lightCutoff = 90;
//        int lightCap = 2048;
//
//        if (light > lightCap) {
//            light = lightCap;
//        }
//
//        if (blockArray[x][y].type == 0) {
//
//            if (blockArray[x][y].light <= light) {
//                blockArray[x][y].light = light;
//            }
//
//            if (light <= lightCutoff || x <= 0 || x >= (WIDTH / 32) || y <= 0 || y >= (HEIGHT / 32)/*if light <= 1 || location invalid*/) {
//        //base case
//                //do nothing
//            } else {
//
//                float lightAttenuation = 0.8f;
//
//                if (light > lightCutoff + blockArray[x + 1][y].light) {
//                    glow((int) (light * lightAttenuation), x + 1, y);
//                }
//
//                if (light > lightCutoff + blockArray[x - 1][y].light) {
//                    glow((int) (light * lightAttenuation), x - 1, y);
//                }
//
//                if (light > lightCutoff + blockArray[x][y + 1].light) {
//                    glow((int) (light * lightAttenuation), x, y + 1);
//                }
//
//                if (light > lightCutoff + blockArray[x][y - 1].light) {
//                    glow((int) (light * lightAttenuation), x, y - 1);
//                }
//
//            }
//        } else {
//            if (light <= lightCutoff || x <= 0 || x >= (WIDTH / 32) || y <= 0 || y >= (HEIGHT / 32)) {
//        //base case
//                //do nothing
//            } else {
//
//                float lightAttenuation = 0.3f;
//
//                if (light > lightCutoff + blockArray[x + 1][y].light) {
//                    glow((int) (light * lightAttenuation), x + 1, y);
//                }
//
//                if (light > lightCutoff + blockArray[x - 1][y].light) {
//                    glow((int) (light * lightAttenuation), x - 1, y);
//                }
//
//                if (light > lightCutoff + blockArray[x][y + 1].light) {
//                    glow((int) (light * lightAttenuation), x, y + 1);
//                }
//
//                if (light > lightCutoff + blockArray[x][y - 1].light) {
//                    glow((int) (light * lightAttenuation), x, y - 1);
//                }
//            }
//        }
//
//    }
//
//    public void emitParticles(int numberSquare) {
//
//        for (int x = 0; x <= numberSquare; x++) {
//            for (int y = 0; y <= numberSquare; y++) {
//
//                Particle p = new Particle();
//
//                float xPos = (oldX + x - numberSquare / 2);
//                float yPos = (oldY + y - numberSquare / 2);
//
//                float Vel = r.nextFloat();
//
//                double angle = r.nextDouble() * Math.PI * 2;
//
//                float xVel = Vel * (float) Math.cos(angle);
//                float yVel = Vel * (float) Math.sin(angle);
//
//                p.pxVel = xVel;
//                p.pyVel = yVel;
//
//                p.setParticle(xPos, yPos, xVel, yVel);
//                if (blockArray[(int) ((xPos) / 32)][(int) ((yPos) / 32)].type == 0) { // if in air then add particle
//                    if (xPos <= WIDTH - 4 && xPos >= 4 && yPos <= HEIGHT - 4 && yPos >= 4) {
//                        densityArray[ (int) ((xPos + 2) / 32)][(int) ((yPos + 2) / 32)] += 2;
//                        particleAL.add(p);
//                    }
//                }
//
//            }
//        }
//    }
//
//    public int makeIntColor(int A, int R, int G, int B) {
//        return (A > 255 ? 0xff000000 : A << 24) + (R > 255 ? 0xff0000 : R << 16) + (G > 255 ? 0xff00 : G << 8) + (B > 255 ? 0xff : B);
//    }
//
//    public void paintBlock(int x, int y) {
//        int light = (int) (0.1 * blockArray[x][y].light);
//        if (light > 200) {
//            light = 200;
//        }
//        if (light < 0) {
//            light = 0;
//        }
//
//        if (blockArray[x][y].type == 0) {
//            for (int x_I = 0; x_I < 8; x_I++) {
//                for (int y_I = 0; y_I < 8; y_I++) {
//
//                    blockRaster[x * 8 + x_I + (y * 8 + y_I) * WIDTH / 4] = makeIntColor(255, light, light / 3, 0);
//
//                }
//            }
//        } else if (blockArray[x][y].type == Block.GRYSTONE) {
//
//            float leftBright = blockArray[x - 1][y].light + 0.5f * blockArray[x - 2][y].light + 0.25f * blockArray[x - 3][y].light + 0.125f * blockArray[x - 1][y - 1].light + 0.125f * blockArray[x - 1][y + 1].light;
//            float rightBright = blockArray[x + 1][y].light + 0.5f * blockArray[x + 2][y].light + 0.25f * blockArray[x + 3][y].light + 0.125f * blockArray[x + 1][y - 1].light + 0.125f * blockArray[x + 1][y + 1].light;
//            float upBright = blockArray[x][y - 1].light + 0.5f * blockArray[x][y - 2].light + 0.25f * blockArray[x][y - 3].light + 0.125f * blockArray[x - 1][y - 1].light + 0.125f * blockArray[x + 1][y - 1].light;
//            float downBright = blockArray[x][y + 1].light + 0.5f * blockArray[x][y + 2].light + 0.25f * blockArray[x][y + 3].light + 0.125f * blockArray[x - 1][y + 1].light + 0.125f * blockArray[x - 1][y + 1].light;
//
//            for (int x_I = 0; x_I < 8; x_I++) {
//                for (int y_I = 0; y_I < 8; y_I++) {
//
//                    int normal = Block.normalIndentMap[y_I][x_I];
//
//                    int bright = 0;
//                    bright += leftBright * ((0xff000000 & normal) >>> 24) / 0xff;
//                    bright += rightBright * ((0x00ff0000 & normal) >> 16) / 0xff;
//                    bright += upBright * ((0x0000ff00 & normal) >> 8) / 0xff;
//                    bright += downBright * (0x000000ff & normal) / 0xff;
//
//                    blockRaster[x * 8 + x_I + (y * 8 + y_I) * WIDTH / 4] = additiveColor(Block.GRYSTONEINDENT_TEX[y_I][x_I], makeIntColor(255, bright / 12, bright / 24, bright / 64));
//                }
//            }
//        }
//    }
//
//    public void tick() {
//
//        float xPos, yPos, xVel, yVel;
//        int age;
//        float ClickToX, ClickToY, InvClickToP;
//        int width = WIDTH;
//        int height = HEIGHT;
//
//        if (emit) {
//            emitParticles(16);
//        }
//
//        for (int x_I = 0, lightWidth = (WIDTH / 32); x_I < lightWidth; x_I++) {  //Draw previous frame's lighting, then clear lightArray
//            for (int y_I = 0, lightHeight = (HEIGHT / 32); y_I < lightHeight; y_I++) {
//                paintBlock(x_I, y_I);
//            }
//        }
//
//        if (!pause) {
//
//            for (int x_I = 0, lightWidth = (WIDTH / 32) + 1; x_I < lightWidth; x_I++) {  //clear lightArray
//                for (int y_I = 0, lightHeight = (HEIGHT / 32) + 1; y_I < lightHeight; y_I++) {
//
//                    blockArray[x_I][y_I].light = (int) (0.9 * blockArray[x_I][y_I].light);
//
//                }
//            }
//
//            for (int I = 0; I < particleRaster.length; I++) { // reset particleRaster
//                particleRaster[I] = 0;
//            }
//
//            for (int particle_I = 0; particle_I < particleAL.size(); particle_I++) {
//                Particle p = particleAL.get(particle_I);
//
//                xPos = p.xPos;
//                yPos = p.yPos;
//
//                float rand = r.nextFloat() * 0.5f + 0.5f;
//
//                xVel = p.xVel * (1 - rand) + p.pxVel * rand;
//                yVel = p.yVel * (1 - rand) + p.pyVel * rand;
//
//                age = p.age;
//
//                p.pxVel = xVel;
//                p.pyVel = yVel;
//
//                if (gravitonAL.isEmpty()) { // if not pulling, slow the particle down
//                    xVel = 0.97f * xVel;
//                    yVel = 0.97f * yVel;
//                } else {
//
//                    for (Graviton v : gravitonAL) { // for every graviton
//
//                        if (v != null) {
//
//                            ClickToX = v.xPos - xPos;
//                            ClickToY = v.yPos - yPos;
//                            float xPull = v.xPull;
//                            float yPull = v.yPull;
//
//                            InvClickToP = InvSqrt((ClickToX * ClickToX + ClickToY * ClickToY));
//
//                            xVel += xPull * ClickToX * InvClickToP;
//                            yVel += yPull * ClickToY * InvClickToP;
//
//                        }
//                    }
//                }
//
//                if (xPos <= width && xPos >= 0 && yPos <= height && yPos >= 0) {
//                    densityArray[ (int) ((xPos + 2) / 32)][(int) ((yPos + 2) / 32)] -= 2;
//                }
//
//                if (xPos <= width - 64 && xPos >= 64 && yPos <= height - 64 && yPos >= 64) { // if visible
//
//                    if (blockArray[ (int) ((xPos + xVel) / 32)][(int) ((yPos + yVel) / 32)].type == 0) { // if no collision
//
//                        xPos += xVel;
//                        yPos += yVel;
//
//                    } else { // if collision
//
//                        if (r.nextFloat() <= 0.1f) {
//                            xPos += 0.5 * xVel;
//                            yPos += 0.5 * yVel;
//                        }
//
//                        float Vel = (float) Math.sqrt(xVel * xVel + yVel * yVel);
//
//                        if (Vel < 1) {
//                            age += 100000;
//                        }
//
//                        if (blockArray[ (int) (xPos / 32)][(int) ((yPos + yVel) / 32)].type != 0) {
//                            xVel = xVel > 0 ? Vel : -Vel;
//                            yVel = r.nextFloat() - 0.5f;
//                        } else {
//                            yPos += yVel;
//                        }
//
//                        if (blockArray[ (int) ((xPos + xVel) / 32)][(int) (yPos / 32)].type != 0) {
//                            yVel = yVel > 0 ? Vel : -Vel;
//                            xVel = r.nextFloat() - 0.5f;
//                        } else {
//                            xPos += xVel;
//                        }
//                    }
//                } else { // if not visible
//                    xPos += xVel;
//                    yPos += yVel;
//                }
//
//                age += 10;
//
//                if ((Math.abs(xVel) + Math.abs(yVel)) <= 0.3) {
//                    age += 100 / (Math.abs(xVel) + Math.abs(yVel));
//                }
//
//                if (xPos <= width - 4 && xPos >= 4 && yPos <= height - 4 && yPos >= 4) { // in canvas
//
//                    for (int xi = -2; xi < 2; xi++) {
//                        for (int yi = -2; yi < 2; yi++) {
//                            particleRaster[(int) (xPos + xi + width * (int) (yPos + yi))] = additiveColor(particleRaster[(int) (xPos + xi + width * (int) (yPos + yi))], 0xff9f1604); // opaque, with blending
//                        }
//                    }
//
//                    if (densityArray[ (int) ((xPos + 2) / 32)][(int) ((yPos + 2) / 32)] < 400) {
//                        age += 400 - densityArray[ (int) ((xPos + 2) / 32)][(int) ((yPos + 2) / 32)];
//                    }
//
//                    if (age > 10000) {
//                        particleAL.remove(particle_I);
//                    } else {
//                        densityArray[ (int) ((xPos + 2) / 32)][(int) ((yPos + 2) / 32)] += 2;
//                    }
//
//                } else { // outside of canvas
//                    age += 200;
//                    if (age > 10000) {
//                        particleAL.remove(particle_I);
//                    }
//                }
//
//                p.setParticle(xPos, yPos, xVel, yVel);
//                p.age = age;
//
//            }
//        }
//
//        for (int x_I = 0, lightWidth = (WIDTH / 32) + 1; x_I < lightWidth; x_I++) {
//            for (int y_I = 0, lightHeight = (HEIGHT / 32) + 1; y_I < lightHeight; y_I++) {
//
//                glow(densityArray[x_I][y_I], x_I, y_I);
//
//            }
//        }
//
//    }
//
//    @Override
//    public void paint(Graphics g) {
//        super.paintComponent(g);
//
//        g.drawImage(blockImage, 0, 0, 2 * WIDTH, 2 * HEIGHT, null);
//
//        g.drawImage(particleImage, 0, 0, 2 * WIDTH, 2 * HEIGHT, null);
//
//        g.setColor(Color.WHITE);
//        g.drawString("Framerate:" + (1000 / (System.currentTimeMillis() - lastTime)), 5, 15);
//        g.drawString("Particles : " + particleAL.size(), 5, 28);
//        g.drawString("Gravity Well : " + gravitonAL.size(), 5, 41);
//
//        for (Graviton v : gravitonAL) {
//            g.fillRect((int) v.xPos * 2, 0, 2, 16);
//            g.fillRect(0, (int) v.yPos * 2, 16, 2);
//        }
//
//        lastTime = System.currentTimeMillis();
//    }
//
//    @Override
//    public void mouseClicked(MouseEvent me) {
//
//        if (SwingUtilities.isRightMouseButton(me)) {
//
//            Block b = new Block();
//            b.setBlock(0, 0, 0, 0);
//            blockArray[(int) (oldX / 32)][(int) (oldY / 32)] = b;
//
//        }
//
//        if (SwingUtilities.isLeftMouseButton(me)) {
//
//            float mouseX = me.getX() / 2;
//            float mouseY = me.getY() / 2;
//
//            Graviton v = new Graviton();
//
//            v.setGraviton(mouseX, mouseY, 0.5f, 0.5f);
//            gravitonAL.add(v);
//        }
//
//    }
//
//    @Override
//    public void mousePressed(MouseEvent me) {
//    }
//
//    @Override
//    public void mouseReleased(MouseEvent me) {
//        if (SwingUtilities.isRightMouseButton(me)) {
//
//            emit = false;
//
//        }
//        if (SwingUtilities.isLeftMouseButton(me)) {
//
//            Graviton v = new Graviton();
//
//            gravitonAL.remove(0);
//
//            gravitonAL.add(0, v);
//
//        }
//    }
//
//    @Override
//    public void mouseEntered(MouseEvent me) {
//        requestFocusInWindow();
//    }
//
//    @Override
//    public void mouseExited(MouseEvent me) {
//    }
//
//    @Override
//    public void mouseDragged(MouseEvent me) {
//
//        float mouseX = me.getX() / 2;
//        float mouseY = me.getY() / 2;
//
//        if (SwingUtilities.isRightMouseButton(me)) {
//
//            emit = true;
//
//        }
//
//        if (SwingUtilities.isLeftMouseButton(me)) {
//
//            gravitonAL.remove(0);
//
//            Graviton v = new Graviton();
//
//            v.setGraviton(mouseX, mouseY, 1, 1);
//
//            gravitonAL.add(0, v);
//
//        }
//
//        oldX = mouseX;
//        oldY = mouseY;
//
//    }
//
//    @Override
//    public void mouseMoved(MouseEvent me) {
//
//        oldX = me.getX() / 2;
//        oldY = me.getY() / 2;
//
//    }
//
//    @Override
//    public void keyTyped(KeyEvent ke) {
//
//        int KeyChar = ke.getKeyChar();
//
//        System.out.println(KeyChar);
//
//        if (KeyChar == 27 /*ESC*/) {
//            pause = !pause;
//        }
//
//        if (KeyChar == 112 /*KeyEvent.VK_P*/) {
//            gravitonAL.clear();
//            gravitonAL.add(new Graviton());
//        }
//
//        if (KeyChar == 99 /*KeyEvent.VK_C*/) {
//            particleAL.clear();
//        }
//
//        if (KeyChar == 120 /*KeyEvent.VK_X*/) {
//            for (Block[] aBlockArray : blockArray) {
//                for (int y_I = 0; y_I < blockArray[1].length; y_I++) {
//                    aBlockArray[y_I].setBlock(0, 0, 0, 0);
//                }
//            }
//        }
//
//    }
//
//    @Override
//    public void keyPressed(KeyEvent ke) {
//    }
//
//    @Override
//    public void keyReleased(KeyEvent ke) {
//    }
// }
