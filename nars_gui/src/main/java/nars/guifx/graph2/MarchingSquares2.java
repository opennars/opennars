//import java.util.ArrayList;
//
//import static Direction.N;
//import static Direction.S;
//import static Direction.E;
//import static Direction.W;
//
///**
// * A simple implementation of the marching squares algorithm that can identify
// * perimeters in an supplied byte array. The array of data over which this
// * instances of this class operate is not cloned by this class's constructor
// * (for obvious efficiency reasons) and should therefore not be modified while
// * the object is in use. It is expected that the data elements supplied to the
// * algorithm have already been thresholded. The algorithm only distinguishes
// * between zero and non-zero values.
// *
// * @author Tom Gibara
// *
// */
//
//public class MarchingSquares {
//
//    // fields
//
//    private final byte[] data;
//
//    private final int width;
//
//    private final int height;
//
//    // constructors
//
//    /**
//     * Creates a new object that can locate perimeter paths in the supplied
//     * data. The length of the supplied data array must exceed width * height,
//     * with the data elements in row major order and the top-left-hand data
//     * element at index zero.
//     *
//     * @param width
//     *            the width of the data matrix
//     * @param height
//     *            the width of the data matrix
//     * @param data
//     *            the data elements
//     */
//
//    public MarchingSquares(int width, int height, byte[] data) {
//        this.width = width;
//        this.height = height;
//        this.data = data;
//    }
//
//    // accessors
//
//    /**
//     * @return the width of the data matrix over which this object is operating
//     */
//
//    public int getWidth() {
//        return width;
//    }
//
//    /**
//     * @return the width of the data matrix over which this object is operating
//     */
//
//    public int getHeight() {
//        return height;
//    }
//
//    /**
//     * @return the data matrix over which this object is operating
//     */
//
//    public byte[] getData() {
//        return data;
//    }
//
//    // methods
//
//    /**
//     * Finds the perimeter between a set of zero and non-zero values which
//     * begins at the specified data element. If no initial point is known,
//     * consider using the convenience method supplied. The paths returned by
//     * this method are always closed.
//     *
//     * @param initialX
//     *            the column of the data matrix at which to start tracing the
//     *            perimeter
//     * @param initialY
//     *            the row of the data matrix at which to start tracing the
//     *            perimeter
//     *
//     * @return a closed, anti-clockwise path that is a perimeter of between a
//     *         set of zero and non-zero values in the data.
//     * @throws IllegalArgumentException
//     *             if there is no perimeter at the specified initial point.
//     */
//
//    // TODO could be made more efficient by accumulating value during movement
//    public Path identifyPerimeter(int initialX, int initialY) {
//        if (initialX < 0) initialX = 0;
//        if (initialX > width) initialX = width;
//        if (initialY < 0) initialY = 0;
//        if (initialY > height) initialY = height;
//
//        int initialValue = value(initialX, initialY);
//        if (initialValue == 0 || initialValue == 15)
//            throw new IllegalArgumentException(String.format("Supplied initial coordinates (%d, %d) do not lie on a perimeter.", initialX, initialY));
//
//        ArrayList<Direction> directions = new ArrayList<Direction>();
//
//        int x = initialX;
//        int y = initialY;
//        Direction previous = null;
//
//        do {
//            final Direction direction;
//            switch (value(x, y)) {
//                case  1: direction = N; break;
//                case  2: direction = E; break;
//                case  3: direction = E; break;
//                case  4: direction = W; break;
//                case  5: direction = N; break;
//                case  6: direction = previous == N ? W : E; break;
//                case  7: direction = E; break;
//                case  8: direction = S; break;
//                case  9: direction = previous == E ? N : S; break;
//                case 10: direction = S; break;
//                case 11: direction = S; break;
//                case 12: direction = W; break;
//                case 13: direction = N; break;
//                case 14: direction = W; break;
//                default: throw new IllegalStateException();
//            }
//            directions.add(direction);
//            x += direction.screenX;
//            y += direction.screenY; // accomodate change of basis
//            previous = direction;
//        } while (x != initialX || y != initialY);
//
//        return new Path(initialX, -initialY, directions);
//    }
//
//    /**
//     * A convenience method that locates at least one perimeter in the data with
//     * which this object was constructed. If there is no perimeter (ie. if all
//     * elements of the supplied array are identically zero) then null is
//     * returned.
//     *
//     * @return a perimeter path obtained from the data, or null
//     */
//
//    public Path identifyPerimeter() {
//        int size = width * height;
//        for (int i = 0; i < size; i++) {
//            if (data[i] != 0) {
//                return identifyPerimeter(i % width, i / width);
//            }
//        }
//        return null;
//    }
//
//    // private utility methods
//
//    private int value(int x, int y) {
//        int sum = 0;
//        if (isSet(x, y)) sum |= 1;
//        if (isSet(x + 1, y)) sum |= 2;
//        if (isSet(x, y + 1)) sum |= 4;
//        if (isSet(x + 1, y + 1)) sum |= 8;
//        return sum;
//    }
//
//    private boolean isSet(int x, int y) {
//        return x <= 0 || x > width || y <= 0 || y > height ?
//                false :
//                data[(y - 1) * width + (x - 1)] != 0;
//    }
//
//}
