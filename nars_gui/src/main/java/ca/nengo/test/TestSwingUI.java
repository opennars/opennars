//package ca.nengo.test;
//
//import automenta.vivisect.swing.Terminal;
//import ca.nengo.model.SimulationException;
//import ca.nengo.model.impl.AbstractNode;
//import ca.nengo.model.impl.NetworkImpl;
//import ca.nengo.ui.Nengrow;
//import ca.nengo.ui.model.UIBuilder;
//import ca.nengo.ui.model.UINeoNode;
//import ca.nengo.ui.model.icon.EmptyIcon;
//import ca.nengo.ui.model.math.JuRLsFunctionApproximator;
//import ca.nengo.ui.model.node.UINetwork;
//import ca.nengo.ui.model.plot.FunctionPlot;
//import ca.nengo.util.ScriptGenException;
//import org.piccolo2d.event.PInputEvent;
//import org.piccolo2d.event.PInputEventListener;
//import org.piccolo2d.extras.pswing.PSwing;
//
//import javax.swing.*;
//import java.awt.event.*;
//import java.util.HashMap;
//
///**
// * Created by me on 2/24/15.
// */
//public class TestSwingUI extends Nengrow {
//
//
//    long time = 0;
//
//    public static void main(String[] args) {
//        new TestSwingUI();
//    }
//
//    @Override
//    public void init() throws Exception {
//        NetworkImpl network = new NetworkImpl();
//        network.addNode(new JuRLsFunctionApproximator("Approximator"));
//        network.addNode(new FunctionPlot("plot1"));
//
//
////        HumanoidFacePanel h;
////        network.addNode(new SwingNode("Humanoid", h = new HumanoidFacePanel(400,400)));
//
//
//        network.addNode(new SwingNode("Terminal", new Terminal().view));
//        //network.addNode(new SwingNode("Button", new JButton("Button")));
//
//        //network.addNode(new SwingNode("NSlider", new NSlider(0.25f, 0, 1)));
//
////        NAR nn = new NAR(new Default());
////        network.addNode(new SwingNode("NAR", new TextInputPanel(nn)));
//
//
//        UINetwork networkUI = (UINetwork) addNodeModel(network);
//
//        networkUI.doubleClicked();
//
//        network.run(0, 0);
//
//
//        new Timer(10, new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    float dt = 0.001f; //myStepSize
//                    network.run(time, time + dt);
//                    time += dt;
//                } catch (SimulationException e1) {
//                    e1.printStackTrace();
//                }
//                //cycle();
//            }
//        }).start();
//
//    }
//
//    public static class SwingNode<J extends JComponent> extends AbstractNode implements UIBuilder {
//
//        private final J component;
//        private SwingNodeUI ui;
//
//        public SwingNode(String name, J j) {
//            super(name);
//            this.component = j;
//
//
//        }
//
//        @Override
//        public void run(float startTime, float endTime) throws SimulationException {
//
//        }
//
//
//        @Override
//        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
//            return null;
//        }
//
//        @Override
//        public void reset(boolean randomize) {
//
//        }
//
//        @Override
//        public UINeoNode newUI() {
//            if (this.ui==null)
//                this.ui = new SwingNodeUI();
//            return ui;
//        }
//
//        private class SwingNodeUI extends UINeoNode {
//
//            public SwingNodeUI() {
//                super(SwingNode.this);
//
//
//                //BoundsHandle.addBoundsHandlesTo(this);
//
//                setIcon(new EmptyIcon(this));
//                setBounds(0, 0, 150, 150);
//
//                setSelectable(true);
//
//                //setSelected(true);
//
//
//                PSwing pp = new PSwing(component);
//
//                component.revalidate();
//                component.grabFocus();
//
//
//                //pp.setScale(1f);
//                //pp.validateFullPaint();
//
//                getPiccolo().addChild(pp);
//                pp.raiseToTop();
//                pp.updateBounds();
//
//
//                pp.setScale(0.5);
//                PInputEventListener x;
//                addInputEventListener(x = new PInputEventListener() {
//
//                    @Override
//                    public void processEvent(PInputEvent pInputEvent, int i) {
//                        InputEvent ie = pInputEvent.getSourceSwingEvent();
//                        if (pInputEvent.isKeyEvent()) {
//                            int c = pInputEvent.getKeyCode();
//
//                            for (KeyListener k :component.getKeyListeners()) {
//                                k.keyTyped((KeyEvent) pInputEvent.getSourceSwingEvent());
//                            }
//                            pp.repaint();
//                        }
//                        else if (ie != null && !ie.isConsumed()) {
//                            ie.setSource(component);
//                            component.dispatchEvent(ie);
//                            ie.consume();
//                            pp.repaint();
//                        }
//                        pInputEvent.setHandled(true);
//                    }
//                });
////pp.addInputEventListener();
//                //getPiccolo().addChild(pp);
//
//            }
//
//
//            @Override
//            public String getTypeName() {
//                return "SwingNode";
//            }
//        }
//
//
//    }
//}
