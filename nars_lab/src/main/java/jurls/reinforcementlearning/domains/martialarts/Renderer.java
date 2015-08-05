/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains.martialarts;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thorsten
 */
public class Renderer extends JComponent {

    private final World world;
    private final BufferedImage bufferedImage;
    private final Graphics2D graphics2D;
    private final BufferedImage collisionBufferedImage;
    private final Graphics2D collisionGraphics2D;

    public Renderer(World world) {
        this.world = world;
        bufferedImage = new BufferedImage(
                world.width,
                world.height,
                BufferedImage.TYPE_3BYTE_BGR
        );
        graphics2D = (Graphics2D) bufferedImage.getGraphics();
        collisionBufferedImage = new BufferedImage(
                world.width,
                world.height,
                BufferedImage.TYPE_3BYTE_BGR
        );
        collisionGraphics2D = (Graphics2D) collisionBufferedImage.getGraphics();
    }

    public int getCollisionBufferRGB(int x, int y) {
        return collisionBufferedImage.getRGB(x, y);
    }

    public void clearCollisionBuffer() {
        collisionGraphics2D.setColor(Color.black);
        collisionGraphics2D.fillRect(0, 0,
                collisionBufferedImage.getWidth(),
                collisionBufferedImage.getHeight()
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        graphics2D.drawImage(world.background, 0, 0, world.width, world.height, null);
        renderPlayer(graphics2D, world.opponentPlayer);
        renderPlayer(graphics2D, world.rlPlayer);
        List<Hit> hits = new ArrayList<>(world.hits);
        for (Hit h : hits) {
            if (h.reward > 0) {
                graphics2D.setColor(Color.green);
            } else {
                graphics2D.setColor(Color.red);
            }
            int r = h.progress;
            graphics2D.fillArc(h.x - r, h.y - r, 2 * r, 2 * r, 0, 360);
        }
        g.drawImage(bufferedImage, 0, 0, getWidth(), getHeight(), null);
    }

    public AffineTransform getTransform() {
        return graphics2D.getTransform();
    }

    public AffineTransform computePlayerTransform(Player player) {
        AffineTransform t = new AffineTransform();
        t.translate(player.x, 0);
        if (player.mirror) {
            t.scale(-1, 1);
        }
        t.translate(-150, 0);
        t.scale(
                300.0 / (player.maxX - player.minX),
                (double) world.height / (player.maxY - player.minY)
        );
        t.translate(-player.minX, -player.minY);
        return t;
    }

    public void renderPlayer(Player player) {
        renderPlayer(collisionGraphics2D, player);
    }

    private void renderPlayer(Graphics2D g, Player player) {
        int a = 0;
        int b = 0;

        if (player.moveIndex >= 0) {
            a = player.moveIndex;
            b = player.moveImageIndex;
        }

        g.drawImage(
                player.moves[a].images[b],
                computePlayerTransform(player),
                null
        );
    }
}
