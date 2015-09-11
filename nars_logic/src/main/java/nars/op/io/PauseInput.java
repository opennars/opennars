//package nars.op.io;
//
//import nars.nal.nal8.ImmediateOperator;
//import nars.nal.nal8.Operation;
//import nars.task.Task;
//
///**
// * Input perception command to queue 'stepLater' cycles in Memory
// * TODO wrap as Operator
// */
//public class PauseInput extends ImmediateOperator {
//
//    public static final PauseInput the = new PauseInput();
//
//    protected PauseInput() {
//        super();
//    }
//
//    public static Task pause(int cycles) {
//        return the.newTask(Integer.toString(cycles));
//    }
//
//    @Override
//    public void accept(Operation o) {
//        //HACK natural number atom should not need quotes
//        String cs = o.args()[0].toString();
//        if (cs.startsWith("\"") && cs.endsWith("\""))
//            cs = cs.substring(1, cs.length()-1);
//
//        int cycles = Integer.parseInt( cs );
//        o.getMemory().think(cycles);
//    }
//}
