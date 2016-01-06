//package nars.checkers;
//
//import nars.NAR;
//import nars.gui.NARSwing;
//import nars.nar.Default;
//import nars.util.java.NALObjects;
//
///**
// * Created by me on 7/17/15.
// */
//public class CheckNARs {
//
//    public static void main(String[] args) throws Exception {
//
//
//        NAR n = new NAR(new Default().realTimeHard(100));
//
//        new NARSwing(n);
//
//        Game g = new NALObjects(n).build("game", Game.class);
//        Board b = new Board(new HumanPlayer("Human1"), new HumanPlayer("Human2"), g);
//        b.playWindow();
//
//    }
//
//
// }
