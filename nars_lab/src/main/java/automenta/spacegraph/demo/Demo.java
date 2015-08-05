package automenta.spacegraph.demo;

import javax.swing.*;

public interface Demo {

    public String getName();

    public String getDescription();

    public abstract JPanel newPanel();
}
