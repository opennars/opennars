package karpathy;


import nars.util.data.list.FasterList;


public class Net extends FasterList<Net.Layer> {

    /**
     // forward prop the network.
     // The trainer class passes is_training = true, but when this function is
     // called from outside (not from the trainer), it defaults to prediction mode
     */
    public DenseTensor forward(DenseTensor V) {
        int s = size();
        Layer[] l = array();
        for (int i = 0; i < s; i++) {
            V = l[i].forward(V);
        }
        return V;
    }
    public DenseTensor backward(DenseTensor V) {
        int s = size();
        Layer[] l = array();
        float[] lossResult = new float[1];
        for (int i = s-1; i >=0; i--) {
            V = l[i].backward(V, lossResult);
        }
        return V;
    }

    public interface Layer {

        DenseTensor forward(DenseTensor v);
        DenseTensor backward(DenseTensor v, float[] lossResult);
    }

    public static class Input implements Layer {

        public Input(int sx, int sy, int inputs) {

        }

        @Override public DenseTensor forward(DenseTensor v) {
            return v; //pass through
        }

        @Override public DenseTensor backward(DenseTensor v, float[] lossResult) {
            return v;  //pass through
        }
    }

    /**
    // implements an L2 regression cost layer,
    // so penalizes \sum_i(||x_i - y_i||^2), where x is its input
    // and y is the user-provided array of "correct" values.
    */
    public static class Regression implements Layer {

        private DenseTensor in_act;//, out_act;

        public Regression(int s) {
            /*
            this.num_inputs = opt.in_sx * opt.in_sy * opt.in_depth;
            this.out_depth = this.num_inputs;
            this.out_sx = 1;
            this.out_sy = 1;
             */
        }

        @Override
        public DenseTensor forward(DenseTensor V) {
            in_act = V;
            //this.out_act = V;
            return V;
        }

        @Override
        public DenseTensor backward(DenseTensor y, float[] lossResult) {

            // y is a list here of size num_inputs
            // or it can be a number if only one value is regressed
            // or it can be a struct {dim: i, val: x} where we only want to
            // regress on dimension i and asking it to have value x


              // compute and accumulate gradient wrt weights and bias of this layer
              DenseTensor x = in_act;

              float[] dw = x.diffable();

              //x.dw = global.zeros(x.w.length); // zero out the gradient of input Vol

              float loss = 0.0f;
              //if(y instanceof Array || y instanceof Float64Array) {
                for(int i=0;i<dw.length;i++) {
                  float dy = x.data[i] - y.data[i];
                  dw[i] = dy;
                  loss += 0.5*dy*dy;
                }
//              } else if(typeof y === 'number') {
//                // lets hope that only one number is being regressed
//                var dy = x.w[0] - y;
//                x.dw[0] = dy;
//                loss += 0.5*dy*dy;
//              } else {
//                // assume it is a struct with entries .dim and .val
//                // and we pass gradient only along dimension dim to be equal to val
//                var i = y.dim;
//                var yi = y.val;
//                float dy = x.w[i] - yi;
//                x.dw[i] = dy;
//                loss += 0.5f*dy*dy;
//              }


            lossResult[0] = loss;

            return x;
        }
    }

    public Net() {
        this(1);
    }

    public Net(int size) {
        super(size);
    }

    public float getCostLoss(DenseTensor x, DenseTensor y) {
        forward(x);
        float[] cl = new float[1];
        get(size()-1).backward(y, cl);
        return cl[0];
    }

}
