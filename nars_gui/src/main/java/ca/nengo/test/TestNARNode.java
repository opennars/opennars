package ca.nengo.test;

import automenta.vivisect.Video;
import ca.nengo.model.AgentNode;
import ca.nengo.model.Network;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.NengrowPanel;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.model.plot.LinePlot;
import ca.nengo.ui.model.plot.StringView;
import ca.nengo.ui.model.widget.PadNode;
import ca.nengo.ui.model.widget.SliderNode;
import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.NARReaction;
import nars.gui.NARSwing;
import nars.io.out.Output;
import nars.nar.Default;
import nars.task.Task;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SynchOperator;
import nars.nal.nal8.operator.TermFunction;
import nars.term.Atom;
import nars.term.Term;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by me on 3/3/15.
 */
public class TestNARNode  {



    public static void main(String[] args) throws StructuralException {
        new NengrowPanel(newAgentNodeDemo()).newWindow(800, 600);
    }

    public static Network newAgentNodeDemo() throws StructuralException {
        NetworkImpl network = new DefaultNetwork<>();

        NAR nar = new NAR(new Default());
        NARNode an = new NARNode("NARBot1", nar);
        Video.themeInvert();
        new NARSwing(nar);

        //an.setMovementBounds(new Rectangle2D.Double(0, 0, 500.01, 500.01));
        network.addNode(an);


        network.addNode(new StringView("Text1"));
        network.addNode(new LinePlot("Plot1"));
        network.addNode(new SliderNode("A", 0, 0, 1f));
        network.addNode(new SliderNode("B", 0, 0, 50f));
        network.addNode(new PadNode("XY", 2, 0, 8, 4, 0, 8));


        return network;
    }


    public static class NARNode extends AgentNode {

        public final NAR nar;

        public NARNode(String name, NAR nar) {
            super(name);
            this.nar = nar;
            nar.memory.setSelf(Atom.the(name));
            nar.param.outputVolume.set(25);
            initOperators();

            new NARReaction(nar, Output.DefaultOutputEvents) {

                final float speakThreshold = 0.9f;

                @Override
                public void event(Class event, Object[] args) {
                    if (event == Events.OUT.class) {
                        Task t= (Task)args[0];
                        if (t.getPriority() > speakThreshold)
                            say(t.sentence.toString());
                    }

                }
            };
        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {
            super.run(startTime, endTime);

            nar.frame(1); //TODO scale # cycles to actual time elapsed
        }

        protected void initOperators() {

            //access to world objects
            nar.on(new SynchOperator("object") {

                @Override
                protected List<Task> execute(Operation operation, Memory memory) {
                    return null;
                }
            });

            /*nar.addPlugin(new SynchronousSentenceFunction("near") {


                @Override
                protected Collection<Sentence> function(Memory memory, Term[] x) {
                    if (x.length > 1) {
                        Term obj =
                    }
                    return null;
                }

            });*/

            nar.on(new TermFunction("see") {


                @Override
                public Term function(Term[] x) {


                    Collection<WorldObject> intersects;

                    if (x.length == 0) {
                        intersects = ui.intersecting(null);
                    } else {
                        Term t = x[0];
                        String st = t.toString();

                        double ww = Math.max(ui.getWidth(), ui.getHeight());
                        double dx = 0, dy = 0;
                        double heading = Double.NaN;
                        switch (st) {
                            case "front":
                                heading = getHeading();
                                break;
                            case "back":
                                heading = -getHeading();
                                break;
                            case "left":
                                heading = getHeading() - Math.PI / 2;
                                break;
                            case "right":
                                heading = getHeading() + Math.PI / 2;
                                break;
                            default:
                                dx = dy = 0;
                                break;
                        }

                        if (Double.isFinite(heading)) {
                            dx = ww * Math.cos(heading);
                            dy = ww * Math.sin(heading);
                        }
                        intersects = ui.intersecting(null, dx, dy);
                    }

                    intersects.add(ui.getNetworkParent());

                    Set<Term> t = new HashSet(intersects.size());

                    for (WorldObject w : intersects) {
                        if (w == NARNode.this.ui) continue;
                        String ww = w.name().trim();
                        if (ww.isEmpty()) {
                            if (Global.DEBUG)
                                System.err.println("Warning: " + w + " (" + w.getClass() + ") has empty name");
                            continue;
                        }
                        t.add(Atom.quote(ww));
                    }

                    return Inheritance.make(SetExt.make(t), Atom.the("intersects"));
                }


            });

            nar.on(new SynchOperator("move") {

                @Override
                protected List<Task> execute(Operation operation, Memory memory) {

                    double dx = 64;
                    boolean error = true;
                    Term[] args = operation.argArray();
                    if (args.length > 1) {
                        Term param = args[0];
                        String ps = param.toString();

                        error = false;
                        double d = 0;
                        switch (ps) {
                            case "front":
                                d = +dx;
                                break;
                            case "back":
                                d = -dx;
                                break;
                            default:
                                error = true;
                        }
                        if (!error) {
                            forward(d);
                        }
                    }

                    return null;
                }
            });
            nar.on(new SynchOperator("turn") {

                @Override
                protected List<Task> execute(Operation operation, Memory memory) {
                    double dA = Math.PI / 4; //radians

                    Term[] args = operation.argArray();
                    boolean error = true;
                    if (args.length > 1) {
                        Term param = args[0];
                        String ps = param.toString();

                        error = false;
                        switch (ps) {
                            case "left":
                                rotate(-dA);
                                break;
                            case "right":
                                rotate(+dA);
                                break;
                            case "north":
                                heading(-Math.PI / 2);
                                break;
                            case "south":
                                heading(Math.PI / 2);
                                break;
                            case "east":
                                heading(0);
                                break;
                            case "west":
                                heading(2 * Math.PI / 2);
                                break;
                            default:
                                error = true;
                        }
                    }

                    if (error) {
                        //...
                    }

                    return null;
                }
            });

            nar.input(
                    "see(#objects)! \n 10\n see(front, #objects)!\n 20 \n move(front)! \n" +
                            " 20 \n" +
                            " move(back)! \n 20 \n move(left)! \n 20 \n 20 \n turn(left)! \n 20 \n turn(right)!\n" +
                            "20\n turn(north)! \n 20 \n turn(south)! \n"
            );
        }



    }
//
//    @Override
//    public void init() throws Exception {
//
//
//        UINetwork networkUI = (UINetwork) addNodeModel(newAgentNodeDemo());
//        networkUI.doubleClicked();
//
//
//        new Timer(100, new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    float dt = getSimulationDT();
//                    networkUI.node().run(time, time + dt);
//                    time += dt;
//
//                } catch (SimulationException e1) {
//                    e1.printStackTrace();
//                }
//                //cycle();
//            }
//        }).start();
//
//    }

}
