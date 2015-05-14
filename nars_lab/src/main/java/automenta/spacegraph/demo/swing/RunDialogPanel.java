///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.dialog.step.MultiMessageStep;
//import automenta.netention.demo.Demo;
//import automenta.netention.demo.Demo;
//import automenta.spacegraph.swing.SwingWindow;
//import automenta.netention.swing.widget.DialogPanel;
//import javax.swing.JPanel;
//
///**
// *
// * @author seh
// */
//public class RunDialogPanel implements Demo {
//
//    public static void main(String[] args) {
//        new SwingWindow(new RunDialogPanel().newPanel(), 400, 800, true);
//    }
//
//    @Override
//    public String getName() {
//        return "Dialog Demo 1";
//    }
//
//    @Override
//    public String getDescription() {
//        return "..";
//    }
//
//    @Override
//    public JPanel newPanel() {
//        //MessageStep b = new MessageStep("ABC");
//        //MessageStep a = new MessageStep("ABC", "Next", b);
//
//        MultiMessageStep c = new MultiMessageStep(new String[] {
//            "abc",
//            "xyz",
//            "123",
//            "456"
//        }, null);
//
//        return new DialogPanel(c);
//    }
//}
