//TODO: Make distributions for size

package ca.nengo.neural.neuron.impl;

import ca.nengo.math.Function;
import ca.nengo.math.impl.DefaultFunctionInterpreter;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.model.*;
import ca.nengo.model.impl.GroupImpl;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.impl.NEFGroupFactoryImpl;
import ca.nengo.neural.nef.impl.NEFGroupImpl;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.neural.neuron.SynapticIntegrator;
import ca.nengo.neural.neuron.impl.RateFunctionSpikeGenerator.PoiraziDendriteSigmoidFactory;
import ca.nengo.util.MU;

import java.util.Random;

/**
 * Non Linear Network
 * This network is a model of Pyramidal Cells found in the central nervous system
 * These cells contain an active dendritic tree with functional computation occuring
 * within the dendrites themselves. The implementation chosen involves creating a
 * network of Ensembles(dendrites and cell bodies) such that one ensemble of
 * "dendrites" projects to a specific termination in the "soma" ensemble with
 * weights chosen in such a way that only one node of the soma is given an input
 * from a specific dendritic branch.
 *
 * @author Albert Mallia
 */
public class PyramidalNetwork extends DefaultNetwork
{

    private static final long serialVersionUID = 1L;
    private boolean oneDimTerminations; //Whether or not each termination in the network can handle more than one dimension initially
    private int dim; //dimensions of the network
    private int somaDim = 1; //The soma is set to one dimension if no function is computed at the dendrites
    private int size;//number of non linear "neurons"
    private int subUnitNum; //number of dendrites per non linear neuron
    private float[][] somaEncoders; //encoders of the somatic ensemble
    private NEFGroup[] Dendrites;//dendritic trees
    private NEFGroup transfer; //transfer ensemble to send values from outside the network to each dendritic tree in the network
    private NEFGroup soma; //cell bodies
    private float[][] myRange;
    private float[] myRadii;
    private IndicatorPDF myDendriteCount; //Range of subunit(dendrite) sizes
    private float mySubDifference; //Difference between high and low of subunit distribution
    private DefaultFunctionInterpreter interpreter;
    private final int ranges = 15; //range for random scale values in dendrite ensembles (this is an arbitrary number, appropriate scales have not yet been found)
    private boolean LIFDendrites = false;
    private boolean spikingLIFDendrites = false;
    private final String myName;
    private String myDendriteFunction; //function to be computer at the dendrites
    private String myConnectedOrigin; //name of the origin being connected to the soma (X is default unless a function is computed at the dendrites)

    //TODO: distribution for dendrites

    /**
     * @param name Name of the network
     * @param dim Dimensions of the network
     * @param size Number of pyramidal neurons in the network
     * @param dendriteRange Range of dendrites per neuron
     * @param f function to be calculated at the dendrites
     * @param oneDim whether or not terminations to the network are unidimensional or multidimensional
     * @param LIFDendrites Use LIF dendrites?
     * @param spikingLIFDendrites Use spiking LIF dendrites?
     * @throws StructuralException if name isn't unique
     */
    public PyramidalNetwork(String name, int dim, int size, IndicatorPDF dendriteRange, String f, boolean oneDim,
            boolean LIFDendrites, boolean spikingLIFDendrites) throws StructuralException {
        myName = name;
        setName(myName);
        this.dim = dim;
        this.size = size;
        oneDimTerminations = oneDim;
        myDendriteCount = dendriteRange;
        int i = 0;
        myRadii = new float[this.dim];

        //calculates the difference between subunit high and low
        //while this is done in the IndicatorPDF class, the difference cannot be accessed
        mySubDifference = dendriteRange.getHigh() - dendriteRange.getLow();

        myRange = new float[this.size][1];
        interpreter = new DefaultFunctionInterpreter();

        //if a function string is sent in, connections will be made from a function computing origin to the soma

        //By default, dendrites are always set to rate mode
        //However, the user can specify whether or not to change these to fire in spiking mode
        //This is mainly for testing and comparison purposes, and can be removed
        this.LIFDendrites = LIFDendrites;
        this.spikingLIFDendrites = spikingLIFDendrites;

        if (!(f.isEmpty())) {
            this.myDendriteFunction = f;

            while (i < this.dim) {
                this.myRadii[i] = 1; //(float) Math.sqrt(this.dim);
                i = i + 1;
            }
            //otherwise, X is used, and this network is likely to be a communication channel
        } else	{
            this.myConnectedOrigin = "X";
        }

        makeNetwork();
    }

    /**
     * @param name Name of the network
     * @param dim Dimensions of the network
     * @param size Number of pyramidal neurons in the network
     * @param dendriteRange Range of dendrites per neuron
     * @param f function to be calculated at the dendrites
     * @param oneDim whether or not terminations to the network are unidimensional or multidimensional
     * @throws StructuralException if name is taken
     */
    public PyramidalNetwork(String name, int dim, int size, IndicatorPDF dendriteRange,
            String f,boolean oneDim) throws StructuralException {
        this(name,dim,size,dendriteRange,f,oneDim,false,false);
    }

    /**
     * @param name Name of the network
     * @param dim Dimensions of the network
     * @param size Number of pyramidal neurons in the network
     * @param dendriteRange Range of dendrites per neuron
     * @param f function to be calculated at the dendrites
     * @throws StructuralException if name isn't unique
     */
    public PyramidalNetwork(String name, int dim, int size, IndicatorPDF dendriteRange, String f) throws StructuralException {
        this(name,dim,size,dendriteRange,f,false);
    }

    /**
     * @param name Name of the network
     * @param dim Dimensions of the network
     * @param size Number of pyramidal neurons in the network
     * @param dendriteRange Range of dendrites per neuron
     * @throws StructuralException if name isn't unique
     */
    public PyramidalNetwork(String name, int dim, int size, IndicatorPDF dendriteRange) throws StructuralException {
        this(name,dim,size,dendriteRange,"");
    }

    /**
     * Gives a default subunit size of 100
     *
     * @param name Name of the network
     * @param dim Dimensions of the network
     * @param size Number of pyramidal neurons in the network
     * @throws StructuralException if name isn't unique
     */
    public PyramidalNetwork(String name, int dim, int size) throws StructuralException {
        this(name,dim,size,new IndicatorPDF(100,100));
    }

    /**
     * Gives a default number of 20 neurons and 100 dendrites per neuron
     *
     * @param name Name of the network
     * @param dim Dimensions of the network
     * @throws StructuralException if name isn't unique
     */
    public PyramidalNetwork(String name, int dim) throws StructuralException {
        this(name, dim,20);
    }

    /**
     * Gives a default of 1 dimension
     *
     * @param name Name of the network
     * @throws StructuralException if name isn't unique
     *
     */
    public PyramidalNetwork(String name) throws StructuralException {
        this(name, 1);
    }

    /**
     * Default constructor
     *
     * @throws StructuralException if name isn't unique
     */
    public PyramidalNetwork() throws StructuralException {
        this("P1");
    }

    /**
     * Creates nodes and calls methods to make all origins, terminations, and projections
     *
     * @throws StructuralException if name isn't unique
     */
    public void makeNetwork() throws StructuralException {
        createDendrites();

        //if the function at the dendrites is specified, a new origin is created on each dendritic tree
        if (!(this.myDendriteFunction == null)) {
            createFunctionOriginDendrites();
        }

        createSoma();
        createTerminations();
        createTransferEnsemble(); //creates direct mode ensemble which passes all input to the dendrites
        connect();
        expose();
        addNodes();
    }

    /**
     * Mainly used for testing purposes when trying to find proper scale values
     *
     * @param index index number of dendritic tree
     * @return Dendritic ensemble at given index number
     */
    public NEFGroup getDendrites(int index) {
        return this.Dendrites[index];
    }

    /**
     * Adds dendrites, soma, and transfer ensembles to the network
     *
     * @throws StructuralException on addNode if it throws
     */
    private void addNodes() throws StructuralException {
        int i = 0;

        this.addNode(this.soma);
        //this.addNode(this.input);
        this.addNode(this.transfer);

        //adds all of the dendrite nodes
        while(i < this.size) {
            this.addNode(this.Dendrites[i]);
            i = i +1;
        }
    }


    public String name() {
        return myName;
    }

    /**
     * Creates an origin at the dendrite level with a user specified function
     * The value calculated at the dendrites is then transferred to the soma ensemble
     * @throws StructuralException if decoded origin already exists
     */
    public void createFunctionOriginDendrites() throws StructuralException {
        this.interpreter = new DefaultFunctionInterpreter();

        int i = 0;
        Function[] f = new Function[1];
        f[0] = this.interpreter.parse(this.myDendriteFunction, this.dim);
        this.myConnectedOrigin = "function"; //changes connected origin name to be the newly created origin as opposed to X

        //creates the function origin on all dendritic trees
        while (i < this.size) {
            this.Dendrites[i].addDecodedOrigin("function",f,Neuron.AXON);
            i = i + 1;
        }
    }


    /**
     * For testing
     *
     * @param index dendrite ensemble for which range is being returned
     * @return the range of scale values for a particular dendrite ensemble
     */
    public float getRange(int index) {
        return this.myRange[index][0];
    }


    /**
     * Gets the scale values for a particular dendritic ensemble
     *
     * @param index index number for dendritic ensemble
     * @return returns the scale value for each node in the ensemble
     */
    public float[] getScales(int index) {
        int i = 0;
        float[] scales = new float[this.subUnitNum];
        Node[] n = this.Dendrites[index].getNodes();
        while(i < this.subUnitNum) {
            scales[i] = ((SpikingNeuron)n[i]).getScale();
            i = i + 1;
        }

        return scales;
    }

    /**
     *
     * @return Exposed network origin X (from the soma)
     * @throws StructuralException if origin doesn't exist
     */
    public NSource getOrigin() throws StructuralException {
        return this.getSource("X");
    }


    /**
     * Sets the encoders for a dendrite ensemble to be +-1/sqrt(dimensions).
     * The encoders are set to be the diagonals of a unit cube, and then normalized.
     * This is done instead of the typical method of selecting encoders from random sampling within a unit sphere.
     * This method is only called when a function is being computed at the dendrites.
     *
     * @param size Size of the dendritic tree being created
     * @return encoder vector with each dimension set as (+-1/sqrt(# of dimensions))
     */
    private float[][] setDendriteEncoders(float size) {
        float[][] e = new float[(int) size][this.dim];
        int i = 0;
        int j = 0;
        int m = 1; //multiplier number
        Random r = new Random();//multiplier random
        float denom = (float) Math.sqrt(this.dim);


        while (i < size) 	{
            while (j < this.dim) {
                if (r.nextInt(2) == 0) {
                    m = -1;
                } else {
                    m = 1;
                }

                e[i][j] = m/denom;

                j = j+1;
            }
            j = 0;
            i = i + 1;
        }

        return e;
    }

    /**
     * Creates dendritic trees with a single multidimensional termination
     *
     * @throws StructuralException
     */
    private void createDendrites() throws StructuralException {
        NEFGroup[] e = new NEFGroup[this.size];
        NEFGroupFactoryImpl f = new NEFGroupFactoryImpl();
        NodeFactory g = null;
        int i = 0;
        float[][] w = MU.I(this.dim); //Identity Matrix transform for the termination on each dendritic ensemble
        Random rand = new Random(); //random number generator to select scales for dendrite trees
        Random sizeRand = new Random();//Random number generator to select subunit numbers

        int newR; //new range from which to choose dendrite scales from
        int newSize; //new size chosen from subUnitRange

        if (this.LIFDendrites == true) {
            g = new LIFNeuronFactory();
        } else {
            //According to Poirazi et al (2003), a pyramidal neuron's dendrites can be represented as sigmoid subunits capable of spiking
            //The function of the sigmoid subunits is assumed to be the same for each subunit and is...
            //... s(n) = 1/(1+exp((3.6-n)/0.20) + 0.30n + 0.0114n^2)
            g = new PoiraziDendriteFactory();
        }

        f.setNodeFactory(g);

        //make ensemble and add termination
        while (i < this.size) {

            //if the high and low values are the same, then there is no need to randomly pick a number
            if (this.mySubDifference == 0) {
                newSize = (int)this.myDendriteCount.getLow();

                //if a range is given, a new size is chosen for each dendritic ensemble between the two values specified
            } else {
                //while this can be done in the class IndicatorPDF, it returns a float[] and is incompatible with the make function of NEFEnsembleFactoryImpl
                newSize = (int) (this.myDendriteCount.getLow() + sizeRand.nextInt((int)(this.mySubDifference + 1)));
            }

            if (this.LIFDendrites == false) {

                //select a new range of dendrite scales
                //the range is selected at random from the ranges value. This represents the range of scales for the dendritic tree as a whole
                //then, each dendrite gets a random scale number chosen from this new range
                newR = rand.nextInt(this.ranges);

                //ensures that the range to choose from is always greater than 0 in order to avoid an exception
                if(newR == 0) {
                    newR = 1;
                }

                this.myRange[i][0] = newR;
                ((PoiraziDendriteFactory)g).changeRange(newR);
            }

            e[i] = f.make("Dendrites" + i, newSize, this.dim);
            e[i].addDecodedTermination("dinput" + i, w , 0.007f, false);

            //if a function is being computed, the radius and encoders are different than they normally would be
            if (this.myConnectedOrigin != "X") {
                ((NEFGroupImpl)e[i]).setEncoders(this.setDendriteEncoders(newSize));
                ((NEFGroupImpl)e[i]).setRadii(this.myRadii);
            }

            if (this.LIFDendrites == true && this.spikingLIFDendrites == true) {
                e[i].setMode(SimulationMode.DEFAULT);
            } else {
                e[i].setMode(SimulationMode.RATE); //since the function used is a rate function, all dendrite trees are set to rate mode
            }

            i = i+1;
        }

        //set parameters
        //TODO: tauRC must be 0.002, tauRef must be 0.002, maxRate can be taken out, intercept -1,1
        this.Dendrites = e;
    }

    /**
     * Creates a transfer ensemble inside the network
     * this ensemble is composed of one direct mode neuron which takes in all inputs arriving to the network,
     * represents them in a multidimensional origin, and transfers the values to each dendritic ensemble in the network
     * @throws StructuralException
     */
    private void createTransferEnsemble() throws StructuralException
    {
        NEFGroup e;
        NEFGroupFactoryImpl f = new NEFGroupFactoryImpl();
        NodeFactory g = new LIFNeuronFactory();
        int i = 0;
        int currentDim = 0;
        float[][][] w = new float[this.dim][this.dim][1];
        f.setNodeFactory(g);

        e = f.make("Transfer", 1, this.dim);

        //when terminations at the network level are multidimensional, a single multidimensional termination is needed at the level of ...
        //... the tranfer ensemble. Otherwise, single dimensional terminations are needed, each storing their value in a different...
        //...dimension of the transfer ensemble
        if(this.oneDimTerminations == true)
        {
            //creates a weight matrix that takes values from each input and stores them in a different dimension of the ensemble
            while (currentDim<this.dim)
            {

                while (i<this.dim)
                {
                    if (i == currentDim)
                    {
                        w[currentDim][i][0] = 1;
                    }
                    else
                    {
                        w[currentDim][i][0] = 0;
                    }

                    i = i + 1;
                }

                e.addDecodedTermination("i" + currentDim, w[currentDim], 0.007f, false);
                currentDim = currentDim + 1;
                i = 0;
            }
        }
        else //if only one termination is used for multiple dimensions, all that is needed is an identity matrix for the transform
        {
            e.addDecodedTermination("input", MU.I(this.dim), 0.007f, false);
        }
        e.setMode(SimulationMode.DIRECT);
        this.transfer = e;
    }

    /**
     * Adds a standard decoded termination to the network
     * 
     * @param name Name of the termination
     * @param transform Weight matrix for the termination
     * @param tauPSC PSC time constant
     * @param modulatory Modulatory?
     * @throws StructuralException if termination already exists
     */
    public void addDecodedTermination(String name, float[][] transform, float tauPSC, boolean modulatory) throws StructuralException {
        this.transfer.addDecodedTermination(name,transform,tauPSC,modulatory);
        this.exposeTermination(this.transfer.getTarget(name), name);
    }

    /**
     * Adds a one dimension termination to the network
     * This allows the user to specify which dimension the input value should be stored in as opposed to sending in a weight matrix to do so
     * A multiplier transform is also expected
     * @param name Name of the termination
     * @param dimension Dimension for input to be stored in
     * @param transform Transform for input value
     * @throws StructuralException if termination exists
     */
    public void addOneDimTermination(String name, int dimension, float transform) throws StructuralException {
        int i = 0;
        float[][] w = new float[this.dim][1];

        while (i< this.dim)
        {
            if (i == (dimension-1))
            {
                w[i][0] = 1 * transform;

            }
            else
            {
                w[i][0] = 0;
            }
            i = i + 1;
        }

        //adds and exposes the termination on the transfer ensemble
        this.transfer.addDecodedTermination(name,w,0.007f,false);
        this.exposeTermination(this.transfer.getTarget(name), name);

    }

    /**Default one dimension termination with no transform
     * Sets a default transform of 1
     * @param name Name of the termination
     * @param dimension Dimension input values are to be stored in
     * @throws StructuralException if termination exists
     */
    public void addOneDimTermination(String name, int dimension) throws StructuralException
    {
        addOneDimTermination(name,dimension,1);
    }

    /**creates one termination on the soma ensemble for each node in the ensemble
     *the weights are created in such a way that each dendritic tree outputs to only one specific node in the soma ensemble
     */
    private void createTerminations() throws StructuralException
    {
        int i = 0;

        while (i<this.size)
        {
            ((GroupImpl)this.soma).addTarget("d" + i, solveEncoders(i), (float) 0.007, false);
            i = i +1;
        }

    }

    /**creates encoders such that each dendrite ensemble will only project to one neuron of the "soma" ensemble
     * this is done by setting all encoder values to 0 unless weights are being set between a specific dendritic tree and its corresponding soma
     * @param index index number for current neuron encoders are being solved for
     */
    private float[][] solveEncoders(int index)
    {
        int i = 0;
        int j = 0;
        float[][] weights = new float[this.size][this.somaDim];


        while (i<this.size)
        {
            while (j<this.somaDim)
            {
                if (i==index) //if i is the same as the index value (Soma neuron we are creating a termination for), then that neuron's specific
                { //encoders will be used in the weights matrix. All other values are set to 0, ensuring that the neuron only spikes when recieving
                    //inputs from it's specific dendritic tree
                    weights[i][j] = this.somaEncoders[i][j];
                }
                else
                {
                    weights[i][j] = 0;
                }
                j = j + 1;
            }

            i = i +1;
            j = 0;
        }

        return weights;


    }


    /**
     * Connects all nodes in the network
     * @throws StructuralException
     */
    private void connect() throws StructuralException
    {
        int i = 0;

        //connects dendritic trees to their respective soma terminations
        while(i<this.size)
        {

            this.addProjection(this.Dendrites[i].getSource(this.myConnectedOrigin), this.soma.getTarget("d" + i));
            this.addProjection(this.transfer.getSource("X"),this.Dendrites[i].getTarget("dinput" + i));
            i = i +1;
        }
    }

    /**exposes all terminations and origins
     *
     * @throws StructuralException
     */
    private void expose() throws StructuralException
    {
        if (this.oneDimTerminations == true)
        {
            int i = 0;
            while (i<this.dim)
            {
                this.exposeTermination(this.transfer.getTarget("i" + i), "i" + i);
                i = i + 1;
            }
        }
        else
        {
            this.exposeTermination(this.transfer.getTarget("input"), "input");
        }



        this.exposeOrigin(this.soma.getSource("X"),"X");
    }

    /**
     *
     * @return Soma ensemble
     */
    public NEFGroup getSoma()
    {
        return this.soma;
    }


    /**creates an ensemble of cell bodies
     *there are as many nodes in this ensemble as there are "nonlinear neurons" in the network
     */
    private void createSoma() throws StructuralException
    {
        NEFGroup e;
        NEFGroupFactoryImpl f = new NEFGroupFactoryImpl();
        NodeFactory g;

        //Originally a sigmoid function was given for the soma in the Poirazi et al. article as well.
        //This was not used due to the fact that it characterizes a rate response as opposed to spiking behaviour
        //Instead, a spiking LIFNeuron is used, until a spiking function can be found for pyramidal neurons
        //creates a standard node factory
        g = new LIFNeuronFactory();
        ((LIFNeuronFactory)g).setIntercept(new IndicatorPDF(-1,1));
        ((LIFNeuronFactory)g).setTauRC(0.02f);
        ((LIFNeuronFactory)g).setTauRef(0.002f);
        ((LIFNeuronFactory)g).setMaxRate(new IndicatorPDF(100,200));

        //if X is the origin the dendrites connect to the soma with, then this network is treated as a communication channel
        //thus the soma is given the same dimensions as the dendritic ensembles
        if (this.myConnectedOrigin.equals("X"))
        {
            this.somaDim = this.dim;
        }

        f.setNodeFactory(g);
        e = f.make("Soma", this.size, this.somaDim);
        this.soma = e;


        this.somaEncoders = this.soma.getEncoders();

    }


    /**Creates neurons which are meant to model the dendrites of pyramidal cells
     * Code is a modified version of NodeFactory written by Bryann Tripp
     */
    public static class PoiraziDendriteFactory implements NodeFactory
    {

        private static final long serialVersionUID = 1L;

        private final PoiraziDendriteSigmoidFactory pf;

        private static final float ourMaxTimeStep = .0005f;
        private static final Units ourCurrentUnits = Units.ACU;

        private final Random r;//random number generator for scale values

        private int range; //range of scales to pick from

        /**
         * Default constructor
         * Sets spikegenerator factory to a PoiraziDendriteSigmoidFactory
         */
        public PoiraziDendriteFactory()
        {
            pf = new PoiraziDendriteSigmoidFactory(); //spike generator for sigmoid dendrites
            r = new Random();
            range = r.nextInt(15); //arbitrary range number



        }

        /**Changes the range from which the random number generator r is allowed to choose from when scaling individual dendrites
         *
         * @param rb new range to choose from
         */
        public void changeRange(int rb)
        {
            this.range = rb;
        }


        /**
         * Returns type of node
         */
        public String getTypeDescription() {
            // TODO Auto-generated method stub
            return "Sigmoid Dendrite";
        }



        /**
         * Makes a "Dendrite" Node
         * @param name Name of the node in ensemble
         */
        public Node make(String name) throws StructuralException {

            SynapticIntegrator integrator = new LinearSynapticIntegrator(ourMaxTimeStep, ourCurrentUnits);
            SpikeGenerator generator = pf.make();
            float scale;//a new scale is created every time a dendrite is to be made

            scale = r.nextFloat() + r.nextInt(this.range);


            return new ExpandableSpikingNeuron(integrator, generator, scale, 0, name);








        }

    }






}
