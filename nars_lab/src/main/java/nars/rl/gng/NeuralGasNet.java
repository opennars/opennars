package nars.rl.gng;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * from: https://github.com/scadgek/NeuralGas
 * TODO use a graph for incidence structures to avoid some loops
 */
public class NeuralGasNet extends SimpleDirectedGraph<Node,Connection> {
    private final int dimension;

    private int iteration;


    private int maxNodes;
    private int lambda;
    private int maxAge;
    private double alpha;
    private double beta;
    private double epsW;
    private double epsN;

    public int getLambda() {
        return lambda;
    }

    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setEpsN(double epsN) {
        this.epsN = epsN;
    }

    public void setEpsW(double epsW) {
        this.epsW = epsW;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    public double getEpsW() {
        return epsW;
    }

    public double getEpsN() {
        return epsN;
    }



    public NeuralGasNet(int dimension, int maxNodes) {

        super(Connection.class);

        this.iteration = 0;
        this.dimension = dimension;
        this.maxNodes = maxNodes;

        //default values
        setLambda(5);
        setMaxAge(15);
        setAlpha(0.5);
        setBeta(0.0005);
        setEpsW(0.05);
        setEpsN(0.0006);



        for (int i = 0; i < 2; i++) {
            addVertex(new Node(i, dimension).randomizeUniform(-1.0, 1.0));
        }


//        pw = new PrintWriter("resources/output.txt");
//
//        for (Node node : nodes)
//        {
//            pw.println(node.getWeights()[0] + " " + node.getWeights()[1] + " " + node.getWeights()[2] + " black");
//        }
//        pw.println("*");
    }

    public Node closest(double[] x) {
        //find closest nodes
        double minDist = Double.POSITIVE_INFINITY;
        Node closest = null;
        for (Node node : vertexSet()) {
            if (node.getDistanceSq(x) < minDist)
                closest = node;
        }

        return closest;
    }

    public Node learn(double[] x) {

        //find closest nodes
        double minDist = Double.POSITIVE_INFINITY;
        double minDist2 = Double.POSITIVE_INFINITY;
        double maxDist = Double.NEGATIVE_INFINITY;
        Node closest = null;
        Node nextClosestNode = null;
        Node furthest = null;
        for (Node node : vertexSet()) {
            double dd = node.getDistanceSq(x);

            if (dd > maxDist) {
                furthest = node;
                maxDist = dd;
            }
            if (dd < minDist) {
                closest = node;
                minDist = dd;
            }

        }
        for (Node node : vertexSet()) {
            double dd = node.getDistanceSq(x); //TODO cache this localDist
            if (node != closest && (dd < minDist2)) {
                nextClosestNode = node;
                minDist2 = dd;
            }
        }


        if (closest == null || nextClosestNode == null) {
            throw new RuntimeException("closest=" + closest + ", nextClosest=" + nextClosestNode);
        }

        //update local error of the "winner"
        closest.setLocalError(closest.getLocalError() + closest.getDistance(x));

        //update weights for "winner"
        closest.update(getEpsW(), x);

        //update weights for "winner"'s neighbours
        for (Connection connection : edgeSet()) {
            Node toUpdate = null;
            if (connection.from == closest) {
                toUpdate = connection.from;
            } else if (connection.to == closest) {
                toUpdate = connection.to;
            }

            if (toUpdate != null) {
                toUpdate.update(getEpsN(), x);
                connection.setAge(connection.getAge() + 1);
            }
        }



        //set connection between "winners" to age zero
        addEdge(new Connection(closest, nextClosestNode));
        addEdge(new Connection(nextClosestNode, closest));

        //remove connections with age > maxAge
        List<Connection> toRemove = new ArrayList(1);
        for (Connection c : edgeSet()) {
            if (c.getAge() > getMaxAge()) {
                toRemove.add(c);
            }
        }
        removeAllEdges(toRemove);

        int nextID;
        if (vertexSet().size() >= maxNodes - 1) {
            nextID = furthest.id;
            removeVertex(furthest);
        }
        else {
            nextID = vertexSet().size();
        }

        //if iteration is lambda
        if (iteration != 0 && iteration % getLambda() == 0) {

            //find node with maximal local error
            double maxError = Double.NEGATIVE_INFINITY;
            Node maxErrorNode = null;
            for (Node node : vertexSet()) {
                if (node.getLocalError() > maxError) {
                    maxErrorNode = node;
                    break;
                }
            }

            //find max error neighbour of the mentioned node
            maxError = Double.NEGATIVE_INFINITY;
            Node maxErrorNeighbour = null;
            for (Connection connection : edgeSet()) {

                if (connection.to == maxErrorNode) {
                    if (connection.from.getLocalError() > maxError) {
                        maxErrorNeighbour = connection.from;
                    }
                } else if (connection.from == maxErrorNode) {
                    if (connection.to.getLocalError() > maxError) {
                        maxErrorNeighbour = connection.to;
                    }
                }
            }
            final Node mENeighbor = maxErrorNeighbour;
            final Node mENode = maxErrorNeighbour;

            //remove connection between them
            removeEdge(mENode, maxErrorNeighbour);
            removeEdge(maxErrorNeighbour, mENode);



            if (maxErrorNeighbour == null || maxErrorNode == null) {
                throw new RuntimeException("SOMETHING WENT WRONG");
            }

            //create node between errorest nodes
            Node newNode = new Node(nextID, maxErrorNode, maxErrorNeighbour);
            addVertex(newNode);

            //create connections between them
            addEdge(new Connection(maxErrorNode, newNode));
            addEdge(new Connection(newNode, maxErrorNode));

            //update errors of the error nodes
            maxErrorNode.setLocalError(maxErrorNode.getLocalError() * getAlpha());
            maxErrorNeighbour.setLocalError(maxErrorNeighbour.getLocalError() * getAlpha());
        }

        //update errors of the nodes
        for (Node node : vertexSet()) {
            node.setLocalError(node.getLocalError() - node.getLocalError() * getBeta());

            System.out.println("  "+ node);
        }

//            //save positions
//            for (Node node : nodes)
//            {
//                pw.println(node.getWeights()[0] + " " + node.getWeights()[1] + " " + node.getWeights()[2] + " black");
//            }
//            pw.println("*");


        //System.out.println("Iteration: " + iteration++);

        //pw.close();

        iteration++;

        return closest;
    }

    private void addEdge(Connection connection) {
        addEdge(connection.from, connection.to, connection);
    }
}
