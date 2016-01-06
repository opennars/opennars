//package nars.nario;
//
//import jurls.reinforcementlearning.domains.RLEnvironment;
//import nars.NAR;
//import nars.rl.Perception;
//import nars.rl.QLAgent;
//import nars.task.Task;
//import nars.term.Term;
//
//import java.awt.image.BufferedImage;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Supplier;
//
///**
// * Created by me on 5/20/15.
// */
//abstract public class ImagePerception implements Perception {
//
//    private final Supplier<BufferedImage> source;
//    private final String id;
//
//    public ImagePerception(String id, Supplier<BufferedImage> source) {
//        this.source = source;
//        this.id = id;
//    }
//
//    @Override
//    public void init(RLEnvironment env, QLAgent agent) {
//
//
//    }
//
//    @Override
//    public Iterable<Task> perceive(NAR nar, double[] input, double t) {
//        BufferedImage i = source.get();
//        float[] im = process(i);
//
//
//        if (im == null) return null;
//
//        List<Task> p = new ArrayList();
//        return p;
//    }
//
//    protected abstract float[] process(BufferedImage i);
//
//    @Override
//    public boolean isState(Term t) {
//        return false;
//    }
// }
