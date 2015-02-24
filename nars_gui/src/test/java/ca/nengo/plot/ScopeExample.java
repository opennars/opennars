///*
// * Created on 11-Feb-08
// */
//package ca.nengo.plot;
//
//import ca.nengo.math.Function;
//import ca.nengo.math.FunctionBasis;
//import ca.nengo.math.impl.FunctionBasisImpl;
//import ca.nengo.math.impl.GaussianPDF;
//import ca.nengo.math.impl.PostfixFunction;
//import ca.nengo.math.impl.SigmoidFunction;
//import ca.nengo.model.Network;
//import ca.nengo.model.SimulationException;
//import ca.nengo.model.StructuralException;
//import ca.nengo.model.Units;
//import ca.nengo.model.impl.FunctionInput;
//import ca.nengo.model.impl.NetworkImpl;
//import ca.nengo.util.Probe;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import java.awt.*;
//import java.io.IOException;
//
////import java.awt.Color;
////import java.awt.geom.*;
////import java.awt.event.WindowAdapter;
////import java.awt.event.WindowEvent;
////import java.lang.reflect.*;
////import java.util.*;
////import javax.swing.SwingUtilities;
////import javax.swing.JPanel;
////import org.jfree.chart.ChartFactory;
////import org.jfree.chart.ChartPanel;
////import org.jfree.chart.JFreeChart;
////import org.jfree.chart.plot.*;
////import org.jfree.chart.renderer.xy.*;
////import org.jfree.data.Range;
////import org.jfree.data.xy.XYSeries;
////import org.jfree.data.xy.XYSeriesCollection;
////import ca.nengo.util.*;
////import ca.nengo.plot.Plotter;
//
///**
// * Example data for scope display.
// *
// * @author Bryan Tripp
// */
//public class ScopeExample {
//
//	public static void main(String[] args) throws Exception {
//
//		try {
//			Network network = new NetworkImpl();
//
//			//this "function input" is Probeable ...
//			Function[] funcs = new Function[2];
//			for(int i=0; i<funcs.length; ++i) {
//				funcs[i] = new PostfixFunction("sin(x0)^"+(i+1), 1);
//			}
//			String name = "functions of time";
//			FunctionInput fi = new FunctionInput(name, funcs, Units.uAcm2);
//			network.addNode(fi);
//
//			//we can add a probe to it and run the simulator ...
//			Probe p = network.getSimulator().addProbe(name, FunctionInput.STATE_NAME, true);
//			network.run(0, 10);
//
//			JFrame frame = makeAnimPlotFrame("vector plot");
//			frame.setLayout(new BorderLayout());
//			Scope scope = new Scope(p);
//			frame.getContentPane().add(scope.getGraphPanel(), BorderLayout.CENTER);
//			frame.getContentPane().add(scope.getCtrlPanel(), BorderLayout.SOUTH);
//			//frame.setSize(900, 600);
//			frame.pack();
//			frame.setLocationRelativeTo(null);
//
//			//... and plot the probed data from the simulation ...
//			//Plotter.plot(p.getData(), "function output");
//
//			//now here are a couple of function bases ...
//			Function g1 = new GaussianPDF(0, 1);
//			Function g2 = new GaussianPDF(0.5f, 1);
//			FunctionBasis gaussianBasis = new FunctionBasisImpl(new Function[]{g1, g2});
//
//			//here is a plot of the probed vector X the gaussian basis (value at time 4.5s) ...
//			gaussianBasis.setCoefficients(p.getData().getValues()[4500]);
//			//Plotter.plot(gaussianBasis, -3, .001f, 3, "gaussian basis plot");
//
//
//			Function s1 = new SigmoidFunction(0, 1, 0, 1);
//			Function s2 = new SigmoidFunction(0.5f, -1, 0, 1);
//			FunctionBasis sigmoidBasis = new FunctionBasisImpl(new Function[]{s1, s2});
//
//			//here is a plot of the probed vector X the sigmoid basis (value at time 0.5s) ...
//			sigmoidBasis.setCoefficients(p.getData().getValues()[500]);
//			//Plotter.plot(sigmoidBasis, -3, .001f, 3, "sigmoid basis plot");
//
//		} catch (StructuralException e) {
//			e.printStackTrace();
//		} catch (SimulationException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static JFrame makeAnimPlotFrame(String title) throws IOException {
//		JFrame r = new JFrame(title);
//		Image image = ImageIO.read(ScopeExample.class.getClassLoader().getResource("ca/nengo/plot/spikepattern-grey.png"));
//		r.setIconImage(image);
//        r.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        r.setVisible(true);
//        return r;
//	}
//}
