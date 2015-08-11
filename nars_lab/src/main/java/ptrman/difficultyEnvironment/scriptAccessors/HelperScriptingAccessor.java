package ptrman.difficultyEnvironment.scriptAccessors;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HelperScriptingAccessor {
    public ArrayRealVector create2dArrayRealVector(float x, float y) {
        return new ArrayRealVector(new double[]{x, y});
    }

    public List createList() {
        return new ArrayList<>();
    }
}
