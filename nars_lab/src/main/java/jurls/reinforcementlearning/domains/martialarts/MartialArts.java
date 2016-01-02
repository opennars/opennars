/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains.martialarts;

import jurls.reinforcementlearning.domains.RLEnvironment;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thorsten
 */
public class MartialArts implements RLEnvironment {

    private final World world = new World();
    private final Renderer renderer = new Renderer(world);
    private final Random random = new Random();

    private enum PseudoAIState {

        START, WALKAWAY
    }

    private PseudoAIState pseudoAIState = PseudoAIState.START;

    @SuppressWarnings("HardcodedFileSeparator")
    public MartialArts() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setSize(200, 100);
        f.setLayout(new GridLayout(0, 1));
        JLabel l = new JLabel();
        JProgressBar pb = new JProgressBar();
        f.add(l);
        f.add(pb);
        f.setVisible(true);

        l.setText("loading Images");
        pb.setIndeterminate(true);
        loadImages("businessman", world.opponentPlayer);
        loadImages("worker", world.rlPlayer);
        l.setText("computing players");
        computePlayer(world.opponentPlayer);
        computePlayer(world.rlPlayer);

        world.opponentPlayer.x = 150;
        world.rlPlayer.x = 650;
        world.rlPlayer.mirror = true;

        f.setVisible(false);

        try {
            world.background = ImageIO.read(getClass().getResource(
                    "/nars_lab_x/main/java/jurls/reinforcementlearning/domains/martialarts/images/"
                    + "backgroundimg1.png"
            ));
        } catch (IOException ex) {
            Logger.getLogger(MartialArts.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void computePlayer(Player player) {
        int[] pixel = new int[1];
        int w = 0;
        int h = 0;

        for (int i = 0; i < player.moves.length; ++i) {
            for (int j = 0; j < player.moves[i].images.length; ++j) {
                Move move = player.moves[i];
                BufferedImage im = move.images[j];
                if (im.getWidth() > w) {
                    w = im.getWidth();
                }
                if (im.getHeight() > h) {
                    h = im.getHeight();
                }
                WritableRaster r = im.getAlphaRaster();
                for (int x = 0; x < im.getWidth(); ++x) {
                    for (int y = 0; y < im.getHeight(); ++y) {
                        r.getPixel(x, y, pixel);
                        if (pixel[0] == 1) {
                            if (x > player.maxX) {
                                player.maxX = x;
                                move.hitX = x;
                                move.hitY = y;
                                move.hitIndex = j;
                            }
                            if (y > player.maxY) {
                                player.maxY = y;
                            }
                            if (x < player.minX) {
                                player.minX = x;
                            }
                            if (y < player.minY) {
                                player.minY = y;
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("HardcodedFileSeparator")
    private void loadImages(String prefix, Player player) {
        int i = 0;

        ArrayList<Move> moves = new ArrayList<>();

        for (int k = 0; k < 2; ++k) {
            Move m = new Move();
            ArrayList<BufferedImage> ims = new ArrayList<>();
            for (int j = 0; j < 5; ++j) {
                try {
                    BufferedImage im = ImageIO.read(getClass().getResource(
                            "/nars_lab_x/main/java/jurls/reinforcementlearning/domains/martialarts/images/"
                            + prefix + String.format("%04d", i) + ".png"
                    ));
                    ims.add(im);
                } catch (Exception ex) {
                    Logger.getLogger(MartialArts.class.getName()).log(Level.SEVERE, null, ex);
                }
                ++i;
            }
            m.images = ims.toArray(new BufferedImage[ims.size()]);
            moves.add(m);
        }
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                Move m = new Move();
                ArrayList<BufferedImage> ims = new ArrayList<>();
                for (int j = 0; j < 10; ++j) {
                    BufferedImage im = ImageIO.read(getClass().getResource(
                            "/nars_lab_x/main/java/jurls/reinforcementlearning/domains/martialarts/images/"
                            + prefix + String.format("%04d", i) + ".png"
                    ));
                    ims.add(im);
                    ++i;
                }
                m.images = ims.toArray(new BufferedImage[ims.size()]);
                moves.add(m);
            }
        } catch (Exception ex) {
        }

        player.moves = moves.toArray(new Move[moves.size()]);
    }

    @Override
    public double[] observe() {
        return new double[]{
            world.opponentPlayer.x,
            world.opponentPlayer.moveImageIndex,
            world.opponentPlayer.moveIndex,
            world.opponentPlayer.mirror ? 1 : 0,
            world.rlPlayer.x,
            world.rlPlayer.moveImageIndex,
            world.rlPlayer.moveIndex,
            world.rlPlayer.mirror ? 1 : 0
        };
    }

    @Override
    public double getReward() {
        return reward(1, world.rlPlayer, world.opponentPlayer)
                + reward(-1, world.opponentPlayer, world.rlPlayer);
    }

    @Override
    public boolean takeAction(int action) {
        takeAction(action, world.rlPlayer);
        return true;
    }

    private void takeAction(int action, Player player) {
        if (player.moveIndex < 0) {
            player.moveIndex = action;
            player.moveImageIndex = 0;
        }
    }

    @Override
    public void frame() {
        animate(world.opponentPlayer);
        animate(world.rlPlayer);

        int dx = world.rlPlayer.x - world.opponentPlayer.x;

        switch (pseudoAIState) {
            case START:
                //noinspection IfStatementWithTooManyBranches
                if (Math.abs(dx) < 50
                        && world.rlPlayer.x < world.width / 2
                        && world.opponentPlayer.mirror) {
                    takeAction(0, world.opponentPlayer);
                } else if (Math.abs(dx) < 50
                        && world.rlPlayer.x < world.width / 2
                        && !world.opponentPlayer.mirror) {
                    pseudoAIState = PseudoAIState.WALKAWAY;
                } else if (Math.abs(dx) < 50
                        && world.rlPlayer.x > world.width / 2
                        && !world.opponentPlayer.mirror) {
                    takeAction(0, world.opponentPlayer);
                } else if (Math.abs(dx) < 50
                        && world.rlPlayer.x > world.width / 2
                        && world.opponentPlayer.mirror) {
                    pseudoAIState = PseudoAIState.WALKAWAY;
                } else if (world.rlPlayer.x < world.opponentPlayer.x
                        && !world.opponentPlayer.mirror || world.rlPlayer.x > world.opponentPlayer.x
                        && world.opponentPlayer.mirror) {
                    takeAction(0, world.opponentPlayer);
                } else {
                    int m = (int) Math.round(Math.random() * 4 * (world.opponentPlayer.moves.length - 1)) + 1;
                    if (m < world.opponentPlayer.moves.length) {
                        takeAction(m, world.opponentPlayer);
                    }else if(Math.random() < 0.2){
                        takeAction(1, world.opponentPlayer);
                    }
                }
                break;
            case WALKAWAY:
                if (Math.abs(dx) < 400
                        && world.opponentPlayer.x > 160
                        && world.opponentPlayer.x < 640) {
                    takeAction(1, world.opponentPlayer);
                } else {
                    pseudoAIState = PseudoAIState.START;
                }
                break;
        }

    }

    private int reward(int factor, Player player, Player opponent) {
        if (player.moveIndex >= 2) {
            Move move = player.moves[player.moveIndex];
            if (player.moveImageIndex == move.hitIndex) {
                renderer.clearCollisionBuffer();
                renderer.renderPlayer(opponent);
                Point2D src = new Point2D.Double(move.hitX, move.hitY);
                Point2D dst = new Point2D.Double();
                AffineTransform t = new AffineTransform(renderer.getTransform());
                t.concatenate(renderer.computePlayerTransform(player));
                t.transform(src, dst);
                Hit h = new Hit(
                        (int) dst.getX(),
                        (int) dst.getY(),
                        factor * player.moveImageIndex
                );
                if (h.x >= 0 && h.x < world.width
                        && h.y >= 0 && h.y < world.height) {
                    int rgb = renderer.getCollisionBufferRGB(h.x, h.y) & 0xffffff;
                    if (rgb != 0) {
                        world.hits.add(h);
                        return h.reward;
                    }
                }
            }
        }

        return 0;
    }

    private void animate(Player player) {
        if (player.moveIndex >= 0) {
            player.moveImageIndex++;
            if (player.moveImageIndex >= player.moves[player.moveIndex].images.length) {
                player.moveIndex = -1;
            }
            if (player.moveIndex == 1) {
                player.x += (player.mirror ? -1 : 1) * 10;
                if (player.x < 150) {
                    player.x = 150;
                }
                if (player.x > world.width - 150) {
                    player.x = world.width - 150;
                }
            }
            if (player.moveIndex == 0) {
                if (player.moveImageIndex == 2) {
                    player.mirror = !player.mirror;
                }
            }
        }

        Iterator<Hit> i = world.hits.iterator();
        while (i.hasNext()) {
            Hit h = i.next();
            h.progress++;
            if (h.progress > 20) {
                i.remove();
            }
        }
    }

    @Override
    public Component component() {
        return renderer;
    }

    @Override
    public int numActions() {
        return world.rlPlayer.moves.length;
    }

}
