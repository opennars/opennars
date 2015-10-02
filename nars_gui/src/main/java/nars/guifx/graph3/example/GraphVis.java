package nars.guifx.graph3.example;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import java.util.ArrayList;
import java.util.List;

/** graph visualization model */
public class GraphVis {

    List<String> parameters = new ArrayList<>();

    String wtf = "dsfsdfsdf";

    int num = 14159;

    Interval ii = new Interval(0, 1);

    public Interval getIi() {
        return ii;
    }

    public String getWtf() {
        return wtf;
    }

    public int getNum() {
        return num;
    }

    /**
     * Add an object as a tag to the set of parameters.
     *
     * @param tag   tag name
     * @param param the object
     */
    public void add(String tag) {
        parameters.add(tag);
    }




}
