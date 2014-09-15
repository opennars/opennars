package nars.prolog.util;

public class Sleep {
    static public void main(String[] args) throws Exception {
        Thread.sleep(Integer.parseInt(args[0]));
        System.exit(0);
    }
}
