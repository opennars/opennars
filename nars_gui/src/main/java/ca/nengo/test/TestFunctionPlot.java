package ca.nengo.test;

import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.NengrowPanel;
import ca.nengo.ui.model.widget.SliderNode;


public class TestFunctionPlot {

    public static NetworkImpl newFunctionApproximationDemo() throws StructuralException {
        NetworkImpl network = new DefaultNetwork("Function Approximation");

        network.addNode( new SliderNode("Detail Level", 8f, 3, 24f));
        //network.addNode(new JuRLsFunctionApproximator("Fourier Approximator Test"));
        //network.addNode(new FunctionPlot("Function Plot"));
        return network;
    }


    public static void main(String[] args) throws StructuralException {

        new NengrowPanel(newFunctionApproximationDemo()).newWindow(800,800);
    }
}
