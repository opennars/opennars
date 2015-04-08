//package ca.nengo.ui.model.plot;
//
//import ca.nengo.model.SimulationException;
//import ca.nengo.model.impl.AbstractNode;
//import ca.nengo.model.impl.ObjectTarget;
//import ca.nengo.ui.lib.world.PaintContext;
//import ca.nengo.ui.lib.world.piccolo.object.BoundsHandle;
//import ca.nengo.ui.model.UIBuilder;
//import ca.nengo.ui.model.UINeoNode;
//import ca.nengo.ui.model.icon.EmptyIcon;
//import ca.nengo.util.ScriptGenException;
//import jurls.core.approximation.ParameterizedFunction;
//
//import java.awt.*;
//import java.util.HashMap;
//
///**
//* Created by me on 2/24/15.
//*/
//public class FunctionPlot extends AbstractNode implements UIBuilder {
//
//    private final ObjectTarget<ParameterizedFunction[]> in;
//
//    public FunctionPlot(String name) {
//        super(name);
//
//        setInputs(in = new ObjectTarget(this, "Signal",ParameterizedFunction[].class));
//
//
//    }
//
//    public class FunctionPlotUI extends UINeoNode<FunctionPlot> {
//
//
//        int numPoints = 128;
//        private int[][] xPoints;
//        private int[][] yPoints;
//
//        public FunctionPlotUI() {
//            super(FunctionPlot.this);
//
//
//            BoundsHandle.addBoundsHandlesTo(this);
//            setIcon(new EmptyIcon(this));
//
//            setSelected(true);
//
//            setBounds(0,0,250,150);
//        }
//
//        @Override
//        public void paint(PaintContext paintContext) {
//            super.paint(paintContext);
//
//
//            ParameterizedFunction[] funcs = in.get();
//            if (funcs!=null) {
//                if (xPoints == null || xPoints.length!=funcs.length) {
//                    xPoints = new int[funcs.length][];
//                    yPoints = new int[funcs.length][];
//                }
//
//                Graphics2D g = paintContext.getGraphics();
//
//                int w = (int)(getBounds().getWidth());
//                int h = (int)(getBounds().getHeight());
//
//                g.setColor(Color.black);
//                g.fillRect(0, 0, w, h);
//
//                for (int i = 0; i < funcs.length; i++)
//                    paintFunction(g, i, funcs[i]);
//            }
//
//        }
//
//        private void paintFunction(Graphics2D g, int f,  ParameterizedFunction parameterizedFunction) {
//            if (parameterizedFunction == null) return;
//
//
//            int w = (int)(getBounds().getWidth());
//            int h = (int)(getBounds().getHeight());
//
//
//            if (f == 0) {
//                g.setColor(Color.GREEN);
//            }
//            else {
//                g.setColor(Color.YELLOW);
//            }
//
//            if (xPoints[f] == null || xPoints[f].length != numPoints) {
//                xPoints[f] = new int[numPoints];
//                yPoints[f] = new int[numPoints];
//            }
//
//            for (int i = 0; i < numPoints; i++) {
//                double x = ((double)i/numPoints) * 1.0;
//                double y = parameterizedFunction.value(x);
//                x = ((double)i/numPoints)* w;
//                y = (-y * h/2 + h/2);
//                xPoints[f][i] = (int)x;
//                yPoints[f][i] = (int)y;
//            }
//
//            g.drawPolyline(xPoints[f], yPoints[f], xPoints[f].length);
//
//        }
//
//        @Override
//        public String getTypeName() {
//            return "FunctionPlot";
//        }
//    }
//
//    @Override
//    public void run(float startTime, float endTime) throws SimulationException {
//
//
//    }
//
//
//    @Override
//    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
//        return null;
//    }
//
//    @Override
//    public void reset(boolean randomize) {
//
//    }
//
//    @Override
//    public UINeoNode newUI(double width, double height) {
//        return new FunctionPlotUI();
//    }
//}
