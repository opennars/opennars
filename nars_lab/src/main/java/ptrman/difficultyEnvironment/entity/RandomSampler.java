package ptrman.difficultyEnvironment.entity;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.math.RandomUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RandomSampler {
    public static List<ArrayRealVector> sample(ArrayRealVector scale, int count) {
        List<ArrayRealVector> result = new ArrayList<>();

        for( int i = 0; i < count; i++ ) {
            double x = (RandomUtil.radicalInverse(i, 2) - 0.5f) * 2.0f * scale.getDataRef()[0];
            double y = (RandomUtil.radicalInverse(i, 3) - 0.5f) * 2.0f * scale.getDataRef()[1];

            result.add(new ArrayRealVector(new double[]{x, y}));
        }

        return result;
    }
}
