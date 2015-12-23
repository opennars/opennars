package nars.guifx.graph2.layout;

import com.gs.collections.impl.list.mutable.primitive.FloatArrayList;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.source.SpaceGrapher;

/**
 * Created by me on 10/7/15.
 */
public class Hilbert extends Linear {


    private FloatArrayList poly;

    @Override
    public void run(SpaceGrapher graph, int iterations) {

        int m = graph.getVertices().size();

        int order = 2;
        do {
            
            //lastPoint = new Point( nwOfWindow );
            lastPoint[0] = lastPoint[1] = 0;
            poly = update(order, NORTH, EAST, SOUTH, WEST);
            //2: 16, 3: 64, 4: 256

            order++;

        } while (poly.size() < m*2);

        //nwOfWindow = p;

        //int  windowSize = Math.min( d.width-nwOfWindow.x, d.height-nwOfWindow.y ) - 30;//was 10



        super.run(graph, iterations);
    }

    static final int MAX_CACHED_ORDER = 8;
    static final FloatArrayList[] hilbertOrders = new FloatArrayList[MAX_CACHED_ORDER];

    private FloatArrayList update(int order, int north, int east, int south, int west) {


        //caching
        if ((order > MAX_CACHED_ORDER) || (order < 2)) {
            throw new RuntimeException("invalid hilbert curve order");
        }
        //

        scaleFactor = 1; //(int) ((1.0) / curveSize(order));


        if (hilbertOrders[order] == null)
            hilbertOrders[order] = hilbert(order, north, east, south, west, new FloatArrayList());

        return hilbertOrders[order];
    }

    @Override
    public void setPosition(TermNode v, int i, int max) {
        FloatArrayList poly = this.poly;

        if ( i*2 >= poly.size() )
            return;

        double x = poly.get(i*2);
        double y = poly.get(i*2+1);
        double scale = 200;


        v.move(x*scale, y*scale, 0.5, 0.01);
    }


    //http://www2.cs.uidaho.edu/~bruceb/cs127/code/Hilbert/Hilbert.java

//    public static class Hilbert
//    {
        static final int  NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;

        float[] lastPoint = new float[2];
        float scaleFactor;




        int curveSize( int ord ) {
            //return (int)(Math.pow(2.0, (double)ord)) - 1;
            return power(2, ord) - 1;
        }

        void move( int d )        {
            switch( d )
            {
                case NORTH:
                    lastPoint[1] -= scaleFactor;
                    break;
                case EAST:
                    lastPoint[0] += scaleFactor;
                    break;
                case SOUTH:
                    lastPoint[1] += scaleFactor;
                    break;
                case WEST:
                    lastPoint[0] -= scaleFactor;
                    break;
            }
        }

        FloatArrayList hilbert(
                int  order,
                int  facing,
                int  right,
                int  behind,
                int  left,
                FloatArrayList poly)
        {

            if( order == 0 ) {
                poly.add(lastPoint[0]);
                poly.add(lastPoint[1]);
            }
            else            {
                hilbert( order-1, left, behind, right, facing, poly );
                move( right );
                hilbert( order-1, facing, right, behind, left, poly );
                move( behind );
                hilbert( order-1, facing, right, behind, left, poly );
                move( left );
                hilbert( order-1, right, facing, left, behind, poly );
            }
            return poly;
        }

        /**  Divide and conquer power algorithm  */
        static int power( int k, int n )
        {
            if( n == 0 )
                return 1;
            else
            {
                int  t = power( k, n/2 );
                return (n % 2) == 0 ? t * t : k * t * t;
            }
        }
}
