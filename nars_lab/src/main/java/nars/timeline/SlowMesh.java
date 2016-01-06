///*
// * Here comes the text of your license
// * Each line should be prefixed with  * 
// */
//package nars.timeline;
//
//import automenta.vivisect.swing.NWindow;
//import nars.core.NAR;
//import nars.build.Neuromorphic;
//import nars.gui.NARSwing;
//import nars.gui.output.graph.ConceptGraphCanvas2;
//import nars.gui.output.graph.ProcessingGraphPanel;
//
///**
// * Testing NARS sub-symbolic architecture by demonstrating precision control of 
// * the dynamics of a stable topology of concepts  */
//public class SlowMesh {    
//    
//    //creates timeslices of a (w,h) mesh, linking each component's successive timeslice by a sequential conjunction
//    public SlowMesh(NAR n, int w, int h, int timeslices) {
//        
//    }
//    
//    public SlowMesh(NAR n, int w, int h) {
//        this(n, w, h, 1);
//        
//        for (int x = 0; x < w; x++) {
//            for (int y = 0; y < h; y++) {
//                if (x > 0)
//                    n.addInput(simlink(x, y, x-1, 0));
//                if (y > 0)
//                    n.addInput(simlink(x, y, 0, y-1));
//                if (x < w-1)
//                    n.addInput(simlink(x, y, x+1, 0));
//                if (y < h-1)
//                    n.addInput(simlink(x, y, 0, y+1));
//            }
//        }
//    }
//    
//    public String pointName(int x, int y) {
//        return "(*," + x + "," + y + ")";
//    }
//    public String simlink(int x1, int y1, int x2, int y2) {
//        //return "<" + pointName(x1, y1) + " <-> " + pointName(x2, y2) + ">.";
//        //return "<" + pointName(x1, y1) + " <=> " + pointName(x2, y2) + ">.";
//        //return "<(*," + pointName(x1, y1) + "," + pointName(x2, y2) + ") --> e>.";
//        //return "<" + pointName(x1, y1) + " <|> " + pointName(x2, y2) + ">.";
//        //return "(&&," + pointName(x1, y1) + "," + pointName(x2, y2) + ").";
//        return "(&&," + pointName(x1, y1) + "," + pointName(x2, y2) + ").";
//    }
//    
//    public static void main(String args[]) {
//        NAR n = NAR.build(new Neuromorphic(4));
//        
//        new SlowMesh(n, 3, 3);
//        
//        new NARSwing(n);
//        ConceptGraphCanvas2 cv;
//        new NWindow("Concept Graph 2", new ProcessingGraphPanel(n, 
//                cv = new ConceptGraphCanvas2(n))).show(500, 500);
//        cv.setTaskLinks(false);
//    }
// }
