//
//package nars.rl.dl;
//
//import nars.rl.elsy.Perception;
//
//import java.util.Random;
//
///**
// *
// * @author me
// */
//abstract public class DAPerception extends Perception {
//
//    private final DenoisingAutoencoder da;
//
//    double pretrain_lr = 0.25;
//    double corruption_level = 0;
//    int pretraining_epochs = 100;
//    private final int n_ins;
//    private final double[] encoded;
//    private final double[] sensor;
//    double learning_rate = 0.1;
//
//    public DAPerception(double[] sensor, int reducedInputs) {
//        super();
//
//        this.sensor = sensor;
//
//        Random rng = new Random(123);
//
//
//        this.n_ins = sensor.length;
//
//        encoded = new double[ reducedInputs];
//
//
//        this.da = new DenoisingAutoencoder(n_ins, reducedInputs);
//    }
//
//    @Override
//    protected void updateInputValues() {
//        updateSOM();
//
//        for (int i = 0; i < encoded.length; i++)
//            setNextValue(encoded[i]);
//    }
//
//
//    public void updateSOM() {
//        if (output == null) return;
//
//        da.train(sensor, learning_rate, corruption_level);
//        da.encode(sensor, encoded, true, true);
//
//
//        //printArray(output);
//        //printArray(encoded);
//    }
//
//
//
//
//}
