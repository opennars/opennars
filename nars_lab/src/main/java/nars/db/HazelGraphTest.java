package nars.db;

/**
 * Created by me on 6/3/15.
 */
public class HazelGraphTest {


    static final Runnable b = () -> {

        sleep(1000);

        HazelGraph g = new HazelGraph("h");

        System.out.println( "b received: " + g.getNodeSet() );
    };

    static final Runnable a = () -> {

        HazelGraph g = new HazelGraph("h");
        g.addNode("x");

        new Thread(b).start();

    };



    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)  {
        new Thread(a).start();

    }
}
