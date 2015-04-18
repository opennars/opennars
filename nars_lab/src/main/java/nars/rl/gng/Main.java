//package nars.rl.gng;
//
//import org.xml.sax.SAXException;
//
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class Main {
//
//    private List<Node> nodes;
//    private List<Connection> connections;
//
//    public Main() {
//        nodes = new ArrayList<Node>();
//        connections = new ArrayList<Connection>();
//    }
//
//    public List<Node> getNodes() {
//        return nodes;
//    }
//
//    public void setNodes(List<Node> nodes) {
//        this.nodes = nodes;
//    }
//
//    public List<Connection> getConnections() {
//        return connections;
//    }
//
//    public void setConnections(List<Connection> connections) {
//        this.connections = connections;
//    }
//
//    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
//        NeuralGasNet network = new NeuralGasNet(new Config("resources/config.xml"));
//        network.learn(new Dataset());
//
//        System.out.println("Learning finished");
//        for (Node node : network.nodes)
//        {
//            System.out.println(node.getWeights()[0]+ " " + node.getWeights()[1] + " " + node.getWeights()[2]);
//        }
//
////        Main main = new Main();
////
////        Random random = new Random();
////
////        Node node1 = new Node();
////        node1.setWeights(new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()});
////        node1.setLocalError(0);
////
////        Node node2 = new Node();
////        node2.setWeights(new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()});
////        node2.setLocalError(0);
////
////        main.getNodes().add(node1);
////        main.getNodes().add(node2);
////
////        double[] x = {random.nextDouble(), random.nextDouble(), random.nextDouble()};
////        Node closestNode = main.getNodes().get(0);
////        Node secondClosestNode = main.getNodes().get(0);
////        double min = Double.MAX_VALUE;
////        for (Node node : main.getNodes()) {
////            if (main.distance(x, node.getWeights()) < min) {
////                secondClosestNode = closestNode;
////                closestNode = node;
////            }
////        }
////
////        closestNode.setLocalError(closestNode.getLocalError() + main.distance(x, closestNode.getWeights()));
////
////
////        System.out.println();
//    }
//
//    public double distance(double[] x, double[] y) {
//        double coord1Diff = Math.pow(x[0] - y[0], 2);
//        double coord2Diff = Math.pow(x[1] - y[1], 2);
//        double coord3Diff = Math.pow(x[2] - y[2], 2);
//        return Math.sqrt(coord1Diff + coord2Diff + coord3Diff);
//    }
//}
