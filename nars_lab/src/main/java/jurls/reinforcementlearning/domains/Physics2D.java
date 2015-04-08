/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thorsten
 */
public class Physics2D {

    public double gravity;
    public final List<Point> points = new ArrayList<>();
    public final List<Connection> connections = new ArrayList<>();
    public double floor;

    public Physics2D(double gravity,double floor) {
        this.gravity = gravity;
        this.floor = floor;
    }

    public void step(double delta) {
        final int numConnections = connections.size();
        for (int i = 0; i < numConnections; i++) {
            final Connection c = connections.get(i);
            double dx = c.p1.x - c.p2.x;
            double dy = c.p1.y - c.p2.y;
            double l = Math.sqrt(dx * dx + dy * dy);
            double dl = c.length - l;
            c.p1.vx += delta * 0.1 * dl * dx / l;
            c.p1.vy += delta * 0.1 * dl * dy / l;
            c.p2.vx -= delta * 0.1 * dl * dx / l;
            c.p2.vy -= delta * 0.1 * dl * dy / l;
        }
        
        final int numPoints = points.size();
        for (int i = 0; i < numPoints; i++) {
            final Point p = points.get(i);
            p.vy += delta * gravity;
            p.vx *= p.decayx;
            p.vy *= p.decayy;
            p.x += p.vx;
            p.y += p.vy;
        }
    }
}
