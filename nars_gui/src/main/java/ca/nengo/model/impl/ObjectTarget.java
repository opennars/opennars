package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.util.MU;

/**
 * Termination that receives input unaltered.
 */
public class ObjectTarget<V> implements Target<V> {

    private static final long serialVersionUID = 1L;

    private Node myNode;
    private String myName;
    private int myDimension;
    private float[][] myTransform;
    private V myValues;

    /**
     * @param node Parent node
     * @param name Termination name
     * @param dimension Dimensionality of input
     */
    public ObjectTarget(Node node, String name) {
        this(node, name, 1);
    }

    public ObjectTarget(Node node, String name, int dimension) {
        myNode = node;
        myName = name;
        myDimension = 1;
    }


    /**
     * @param node Parent node
     * @param name Termination name
     * @param dimension Dimensionality of input
     * @param transform Transformation matrix
     */
    public ObjectTarget(Node node, String name, int dimension, float[][] transform) {
        assert MU.isMatrix(transform);
        assert dimension == transform.length;

        myNode = node;
        myName = name;
        myDimension = transform[0].length;
        myTransform = transform;
    }

    /**
     * @param node Parent node
     * @param name Termination name
     * @param transform Transformation matrix
     */
    public ObjectTarget(Node node, String name, float[][] transform) {
        assert MU.isMatrix(transform);

        myNode = node;
        myName = name;
        myDimension = transform[0].length;
        myTransform = transform;
    }


    public int getDimensions() {
        return myDimension;
    }

    public String getName() {
        return myName;
    }

    @Override public boolean applies(V value) {
        if (value instanceof InstantaneousOutput)
            return (((InstantaneousOutput)value).getDimension() == myDimension);
        return true;
    }

    public void apply(V values) throws SimulationException {
        if (!applies(values)) {
            throw new SimulationException("Input is wrong type"); //(expected " + myDimension + " got " + v.getDimension() + ')');
        }

        if ((myTransform != null) && (values instanceof InstantaneousOutput)) {
            InstantaneousOutput v = (InstantaneousOutput) values;
            if (v instanceof RealOutput) {
                float[] transformed = MU.prod(myTransform, ((RealOutput) values).getValues());
                values = (V) new RealOutputImpl(transformed, v.getUnits(), v.getTime());
            } else {
                throw new SimulationException("Transforms can only be performed on RealOutput in a PassthroughNode");
            }
        }

        myValues = values;
    }



    public Node getNode() {
        return myNode;
    }

    /**
     * @return Transformation matrix
     */
    public float[][] getTransform() {
        if (myTransform != null)
            return myTransform.clone();
        else
            // If transform is null, then PassthroughTermination is just doing a identity transform.
            // So, return identity matrix.
            return MU.I(myDimension);
    }

    public boolean getModulatory() {
        return false;
    }

    public float getTau() {
        return 0;
    }

    public void setModulatory(boolean modulatory) {
        throw new RuntimeException("A termination on a passthrough node is never modulatory");
    }

    public void setTau(float tau) throws StructuralException {
        throw new StructuralException("A termination on a passthrough node has no dynamics");
    }

    public V get(){
        return myValues;
    }

    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    public void reset(boolean randomize) {
        myValues = null;
    }

    @Override
    public ObjectTarget clone() throws CloneNotSupportedException {
        return this.clone(myNode);
    }

    public ObjectTarget clone(Node node) throws CloneNotSupportedException {
        ObjectTarget result = (ObjectTarget) super.clone();
        result.myNode = node;
        if (myValues != null)
            result.myValues = ((InstantaneousOutput)myValues).clone();
        if (myTransform != null)
            result.myTransform = MU.clone(myTransform);
        return result;
    }

}
