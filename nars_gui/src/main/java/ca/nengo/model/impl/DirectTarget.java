package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.util.MU;

/**
 * Termination that receives input unaltered.
 */
public class DirectTarget implements Target {

    private static final long serialVersionUID = 1L;

    private Node myNode;
    private String myName;
    private int myDimension;
    private float[][] myTransform;
    private InstantaneousOutput myValues;

    /**
     * @param node Parent node
     * @param name Termination name
     * @param dimension Dimensionality of input
     */
    public DirectTarget(Node node, String name, int dimension) {
        myNode = node;
        myName = name;
        myDimension = dimension;
    }

    /**
     * @param node Parent node
     * @param name Termination name
     * @param dimension Dimensionality of input
     * @param transform Transformation matrix
     */
    public DirectTarget(Node node, String name, int dimension, float[][] transform) {
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
    public DirectTarget(Node node, String name, float[][] transform) {
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

    public void setValues(InstantaneousOutput values) throws SimulationException {
        if (values.getDimension() != myDimension) {
            throw new SimulationException("Input is wrong dimension (expected " + myDimension + " got " + values.getDimension() + ')');
        }

        if (myTransform != null) {
            if (values instanceof RealOutput) {
                float[] transformed = MU.prod(myTransform, ((RealOutput) values).getValues());
                values = new RealOutputImpl(transformed, values.getUnits(), values.getTime());
            } else {
                throw new SimulationException("Transforms can only be performed on RealOutput in a PassthroughNode");
            }
        }

        myValues = values;
    }

    /**
     * @return Values currently stored in termination
     */
    public InstantaneousOutput getValues() {
        return myValues;
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

    public InstantaneousOutput get(){
        return myValues;
    }

    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    public void reset(boolean randomize) {
        myValues = null;
    }

    @Override
    public DirectTarget clone() throws CloneNotSupportedException {
        return this.clone(myNode);
    }

    public DirectTarget clone(Node node) throws CloneNotSupportedException {
        DirectTarget result = (DirectTarget) super.clone();
        result.myNode = node;
        if (myValues != null)
            result.myValues = myValues.clone();
        if (myTransform != null)
            result.myTransform = MU.clone(myTransform);
        return result;
    }

}
