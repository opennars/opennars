/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.constraint.gui.panels;

import nars.core.Eventer;
import nars.core.Events.FrameEnd;
import nars.gui.output.chart.MeterVis;
import nars.util.meter.TemporalMetrics;
import nars.util.meter.event.ValueMeter;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;

/**
 *
 * @author me
 */
public class GraphPanel extends MeterVis {
    private final ChocoMetrics metrics;
    private final double start;


    public static class ChocoMetrics extends TemporalMetrics<Object> implements IMonitorOpenNode {

        public final Eventer event = Eventer.newSynchronous();
        private final Solver solver;
        
        public ChocoMetrics(Solver solver) {
            super(64);
            this.solver = solver;
            

            addMeter(new ValueMeter("nodes") {
                @Override protected Double getValue(Object key, int index) {
                    return (double)solver.getMeasures().getNodeCount();
                }
            });
            addMeter(new ValueMeter("vars.free") {
                @Override protected Double getValue(Object key, int index) {
                    double lds = 0.0;
                    for (int i = 0; i < solver.getNbVars(); i++) {
                        lds += solver.getVar(i).isInstantiated() ? 1 : 0;
                    }
                    return solver.getNbVars() - lds;
                }
            });
        }

        @Override
        public void beforeOpenNode() {
        }

        @Override
        public void afterOpenNode() {
            
        }
        
    }
    
    public GraphPanel(ChocoMetrics m) {
        super(m.event, m);
        this.metrics = m;
        this.start = t();
    }

    double t() {
        return System.currentTimeMillis()/1000.0;
    }
    
    int t = 0;
    public void update() {
        //metrics.update( t() - start );
        metrics.update(t++);
        metrics.event.notify(FrameEnd.class);
        //metrics.printCSV(System.out);
    }



  
    
    
}
