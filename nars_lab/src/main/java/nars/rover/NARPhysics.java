//package nars.rover;
//
//import nars.NAR;
//
//import java.awt.event.KeyEvent;
//import java.util.concurrent.Future;
//
//
//
//public class NARPhysics<P extends PhysicsModel> extends NARGame  {
//    public final P model;
//    public final PhysicsRun phy;
//    private Future<?> phyCycle;
//
//    public NARPhysics(NAR nar, float simulationRate, P model) {
//        super(nar);
//        this.model = model;
//        this.phy = new PhysicsRun(simulationRate, model) {
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                NARPhysics.this.keyPressed(e);
//            }
//
//
//        };
//
//    }
//    public void keyPressed(KeyEvent e) { }
//
//    @Override
//    public void start(float fps, int cyclesPerFrame) {
//        phy.controller.setFrameRate((int)fps);
//        super.start(fps, cyclesPerFrame);
//    }
//
//    public P getModel() { return model; }
//
//
//    @Override
//    public void stop() {
//        super.stop();
//    }
//
//
//    @Override
//    public void init() {
//    }
//
//    @Override
//    public void frame() {
//
//
////        if (phy!=null) {
////
////            //wait for previous cycle to finish if it hasnt
////            if (phyCycle!=null) {
////                try {
////                    phyCycle.get();
////                } catch (Exception ex) {
////                    Logger.getLogger(NARPhysics.class.getName()).log(Level.SEVERE, null, ex);
////                }
////            }
////
////            phyCycle = exe.submit(this);
////        }
//    }
//
//
//
//}
