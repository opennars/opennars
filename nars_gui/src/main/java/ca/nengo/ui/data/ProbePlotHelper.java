package ca.nengo.ui.data;

import ca.nengo.model.SimulationMode;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.ui.action.PlotTimeSeries;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.util.NengoConfigManager;
import ca.nengo.ui.util.NengoConfigManager.UserProperties;
import ca.nengo.util.Probe;
import ca.nengo.util.TimeSeries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helps plot probes
 * 
 * @author Shu Wu
 */
public class ProbePlotHelper {

    private static ProbePlotHelper singleton;

    /**
     * @return TODO
     */
    public static ProbePlotHelper getInstance() {
        if (singleton == null) {
            singleton = new ProbePlotHelper();
        }
        return singleton;
    }

    /**
     * TODO
     */
    public final float DEFAULT_PLOTTER_TAU_FILTER = 0.01f;

    /**
     * TODO
     */
    public final int DEFAULT_SUB_SAMPLING = 0;

    private ProbePlotHelper() {
    }

    /**
     * @param probe
     * @param plotName
     * @return The default plotting action
     */
    public StandardAction getDefaultAction(Probe probe, String plotName) {

        return getDefaultAction(probe.getData(), plotName, isApplyTauFilterByDefault(probe));

    }

    /**
     * @param applyFilterByDefault
     * @param data
     * @param plotName
     * @return The default plotting action
     */
    public StandardAction getDefaultAction(TimeSeries data,
            String plotName,
            boolean applyFilterByDefault) {

        if (applyFilterByDefault) {
            return new PlotTimeSeries("Plot w/ filter", data, plotName, false,
                    getDefaultTauFilter(), getDefaultSubSampling());
        } else {
            return new PlotTimeSeries("Plot raw data", data, plotName, false, 0, 0);
        }

    }

    /**
     * @return TODO
     */
    public int getDefaultSubSampling() {
        String savedValue = NengoConfigManager.getUserProperty(UserProperties.PlotterDefaultSubSampling);
        return savedValue != null ? Integer.parseInt(savedValue) : DEFAULT_SUB_SAMPLING;

    }

    /**
     * @return TODO
     */
    public float getDefaultTauFilter() {
        String savedTau = NengoConfigManager.getUserProperty(UserProperties.PlotterDefaultTauFilter);
        return savedTau != null ? Float.parseFloat(savedTau) : DEFAULT_PLOTTER_TAU_FILTER;

    }

    /**
     * @param data TODO
     * @param plotName TODO
     * @return TODO
     */
    public Collection<StandardAction> getPlotActions(TimeSeries data, String plotName) {
        List<StandardAction> actions = new ArrayList<StandardAction>(2);

        actions.add(new PlotTimeSeries("Plot raw data", data, plotName, false, 0, 0));
        actions.add(new PlotTimeSeries("Plot w/ options", data, plotName, true,
                getDefaultTauFilter(), getDefaultSubSampling()));
        return actions;
    }

    /**
     * @param probe
     *            Probe
     * @return Whether to apply tau filters in timeseries plots for that probe
     */
    public boolean isApplyTauFilterByDefault(Probe probe) {

        if (probe.getTarget() instanceof NEFGroup) {
            NEFGroup e = (NEFGroup) probe.getTarget();
            if ((e.getMode() == SimulationMode.DEFAULT )
            		|| (e.getMode() == SimulationMode.PRECISE) 
            		|| (e.getMode() == SimulationMode.APPROXIMATE)
            		|| (e.getMode() == SimulationMode.EXPRESS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param value TODO
     */
    public void setDefaultSubSampling(int value) {
        NengoConfigManager.setUserProperty(UserProperties.PlotterDefaultSubSampling,
                Integer.toString(value));
    }

    /**
     * @param value TODO
     */
    public void setDefaultTauFilter(float value) {
        NengoConfigManager.setUserProperty(UserProperties.PlotterDefaultTauFilter,
                Float.toString(value));
    }

}
