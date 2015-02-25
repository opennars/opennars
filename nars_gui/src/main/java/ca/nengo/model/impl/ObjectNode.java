package ca.nengo.model.impl;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Source;
import ca.nengo.model.Target;
import ca.nengo.util.ScriptGenException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Wraps a POJO/Bean
 */
public class ObjectNode<O> extends AbstractNode {

    private O obj;


    public ObjectNode(String name, O object) {
        super(name);

        setObject(object);
    }

    protected void setObject(O object) {

        this.obj = object;

        List<Target> inputs = new ArrayList();
        List<Source> outputs = new ArrayList();


        for (Method m : obj.getClass().getMethods()) {
            //if (!m.isAccessible()) continue;
            System.out.println(m);
            buildMethod(m, inputs, outputs, 2);
        }
        System.out.println(inputs);
        System.out.println(outputs);

        setInputs(inputs);
        setOutputs(outputs);
    }

    public O getObject() {
        return obj;
    }

    @Override
    public void run(float startTime, float endTime) throws SimulationException {

    }

    @Override
    public Node[] getChildren() {
        return new Node[0];
    }

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return null;
    }

    @Override
    public void reset(boolean randomize) {

    }

    public static class ActionPushButton<O> extends ObjectTarget<Boolean> {

        private final Method method;
        private final O obj;

        public ActionPushButton(Node parent, Method m, O instance) {
            super(parent, parent.getName() + "_" + m.toGenericString(), Boolean.class);
            this.method = m;
            this.obj = instance;
        }

    }

    public static class ProducerPushButton<O> extends ObjectSource {

        private final Method method;
        private final O obj;

        public ProducerPushButton(Node parent, Method m, O instance) {
            super(parent, parent.getName() + "_" + m.toGenericString());
            this.method = m;
            this.obj = instance;
        }

    }

    private void buildMethod(Method m, List<Target> inputs, List<Source> outputs, int remainingDepth) {
        if (remainingDepth < 1) return;

        if (m.getParameterCount() == 0) {
            if (m.getReturnType() == Void.class) {
                //pushbutton
                ActionPushButton pb = new ActionPushButton(this, m, obj);
                //TODO add a "sub-target source" to collect the output of the method
                inputs.add(pb);
            }
            else {
                ProducerPushButton pb = new ProducerPushButton(this, m, obj);
                outputs.add(pb);
            }
        }
        else if (m.getParameterCount() == 1) {
            ObjectTarget o = new ObjectTarget(this, getName() + "_" + m.toGenericString(), m.getReturnType());
            inputs.add(o);
        }
        else {
            //create a sub-node for this multi-arg method
        }


    }

}
