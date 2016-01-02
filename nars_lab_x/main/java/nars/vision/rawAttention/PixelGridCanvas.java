package nars.vision.rawAttention;

/**
 * Canvas which shows big pixels and the pixels which are "seen" by nars
 */
public class PixelGridCanvas extends Canvas {
    public PerceptionDescriptor perceptionDescriptor;

    public int pixelSize = 1;

    public void paint(Graphics graphics) {
        final int width = perceptionDescriptor.pixelMap.length;
        final int height = perceptionDescriptor.pixelMap[0].length;

        for( int mapX = 0; mapX < width; mapX++ ) {
            for( int mapY = 0; mapY < height; mapY++ ) {
                int x = mapX * pixelSize;
                int y = mapY * pixelSize;

                if( perceptionDescriptor.pixelMap[mapX][mapY] ) {
                    graphics.setColor(Color.white);
                }
                else {
                    graphics.setColor(Color.black);
                }
                graphics.fillRect(x, y, pixelSize, pixelSize);

                graphics.setColor(Color.red);

                if( perceptionDescriptor.cachedPerceptionMap != null && perceptionDescriptor.cachedPerceptionMap[mapX][mapY] ) {
                    graphics.drawRect(x, y, pixelSize, pixelSize);
                }
            }
        }

        graphics.dispose();
    }

    public void refresh() {
        repaint();
    }
}
