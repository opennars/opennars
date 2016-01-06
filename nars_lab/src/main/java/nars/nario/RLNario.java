//package nars.nario;
//
//import nars.Global;
//import nars.NAR;
//import nars.Video;
//import nars.gui.NARSwing;
//import nars.nar.Default;
//import nars.rl.*;
//import nars.rl.example.QVis;
//import nars.term.Atom;
//
///**
// * Created by me on 4/26/15.
// */
//public class RLNario extends NARio  {
//
//    private QVis mi;
//    private QLAgent agent;
//
//    public RLNario(NAR nar, Perception... p) {
//        super(nar);
//
//        Global.DEBUG = true;
//        float fps = 30f;
//        gameRate = 1.0f / fps;
//
//        /*this.agent = new QLAgent(nar, "A", "<I --> G>", this, p);
//
//        agent.ql.brain.setEpsilon(0.15);
//
//        mi = new QVis(agent);*/
//
//
//        Video.themeInvert();
//        NARSwing s = new NARSwing(nar);
//        s.setSpeed(0.5f);
//
//    }
//
//    @Override
//    @Deprecated synchronized public double[] observe() {
//
//
//        o.clear();
//
//        o.addAll(dx/6.0, dy/6.0); //higher resolution velocity
//        o.addAll(dx/12.0, dy/12.0); //coarse velocity
//
//        for (boolean b : mario.keys)
//            o.add(b ? 1 : -1);
//
//
//        //o.addAll(radar);
//
//        //Image screen = ((LevelScene) scene).layer.image;
//
//
//
//        return o.toArray();
//    }
//
//    @Override
//    protected void input(String sensed) {
//        //System.out.println(sensed);
//        nar.input(sensed);
//    }
//
//
//    @Override
//    public void frame() {
//        super.frame();
//        mi.frame();
//    }
//
//    public static void main(String[] args) {
//
//
//        NAR nar = new NAR(new Default(768, 32, 4));
//        nar.memory.setSelf(Atom.the("I"));
//
//        nar.param.duration.set(4);
//        nar.setCyclesPerFrame(memoryCyclesPerFrame);
//
//        nar.param.outputVolume.set(0);
//        nar.param.executionThreshold.set(0.55);
//        nar.param.shortTermMemoryHistory.set(3);
//
//        RLNario rl = new RLNario(nar,
//                new RawPerception("r", 0.1f),
//                //new RawPerception.BipolarDirectPerception("r", 0.7f),
//                new HaiSOMPerception("s", 3, 0.1f),
//                new AEPerception("a", 0.1f, 3, 2).setLearningRate(0.005).setSigmoid(false)
//                //new AEPerception("b", 0.8f, 4).setLearningRate(0.08).setSigmoid(false)
//        );
//
//        //rl.agent.setQLFactor(0.45f, 0.85f);
//        //rl.agent.setInputGain(0.6f);
//
//        //rl.agent.add(new ShapePerception(rl.getScreenImage()));
//
//        /*rl.agent.add( new ImagePerception("i1", rl.getLevelImage()) {
//
//        });*/
//
//    }
//
// }
