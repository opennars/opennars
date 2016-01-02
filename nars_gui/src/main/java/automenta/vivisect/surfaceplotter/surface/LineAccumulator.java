/*----------------------------------------------------------------------------------------*
  * LineAccumulator.java                                                                   *
 *                                                                                        *
 * Surface Plotter   version 1.30b1  17 May 1997                                          *
 *                   version 1.30b2  18 Oct 2001                                          *
 *                                                                                        *
 * Copyright (c) Yanto Suryono <yanto@fedu.uec.ac.jp>                                     *
 *                                                                                        *
 * This program is free software; you can redistribute it and/or modify it                *
 * under the terms of the GNU Lesser General Public License as published by the                  *
 * Free Software Foundation; either version 2 of the License, or (at your option)         *
 * any later version.                                                                     *
 *                                                                                        *
 * This program is distributed in the hope that it will be useful, but                    *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or          *
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for               *
 * more details.                                                                          *
 *                                                                                        *
 * You should have received a copy of the GNU Lesser General Public License along                *
 * with this program; if not, write to the Free Software Foundation, Inc.,                *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA                                  *
 *                                                                                        *
 *----------------------------------------------------------------------------------------*/

/* eric: moved from Vector to List, and LinkedList
 * 
 */
package automenta.vivisect.surfaceplotter.surface;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The class <code>LineAccumulator</code> accumulates line drawing information and
 * then draws all accumulated lines together. It is used as contour lines accumulator
 * in Surface Plotter. 
 *
 * @author  Yanto Suryono
 */
 
public class LineAccumulator {
  private final List<LineRecord> accumulator;
  
  /**
   * The constructor of <code>LineAccumulator</code>
   */
   
  LineAccumulator() {
    accumulator = new ArrayList<>();
  } 
  
  /**
   * Adds a line to the accumulator. 
   *
   * @param x1 the first point's x coordinate
   * @param y1 the first point's y coordinate
   * @param x2 the second point's x coordinate
   * @param y2 the second point's y coordinate
   */
   
  public void addLine(int x1, int y1, int x2, int y2) {
		if (x1<=0 || y1<=0 || x2<=0 || y2<=0 ) return; 
		//System.out.println("("+x1+","+y1+","+x2+","+y2+")");
    accumulator.add(new LineRecord(x1,y1,x2,y2));
  }
  
		
		
  /**
   * Clears accumulator.
   */
   
  public void clearAccumulator() {
    accumulator.clear();
  } 
  
  /**
   * Draws all accumulated lines.
   *
   * @param g the graphics context to render
   */
   
  public void drawAll(Graphics g) {
	    for(LineRecord line: accumulator)
	    	g.drawLine(line.x1,line.y1,line.x2,line.y2);
  }
}

/**
 * Represents a stright line.
 * Used by <code>LineAccumulator</code> class.
 *
 * @see LineAccumulator
 */
 
class LineRecord {
  /**
   * @param x1 the first point's x coordinate
   */
  public final int x1;

  /**
   * @param y1 the first point's y coordinate
   */
  public final int y1;

  /**
   * @param x2 the second point's x coordinate
   */
  public final int x2;

  /**
   * @param y2 the second point's y coordinate
   */
  public final int y2;
  
  /**
   * The constructor of <code>LineRecord</code>
   *
   * @param x1 the first point's x coordinate
   * @param y1 the first point's y coordinate
   * @param x2 the second point's x coordinate
   * @param y2 the second point's y coordinate
   */
   
  LineRecord(int x1, int y1, int x2, int y2) {
    this.x1 = x1; this.y1 = y1;
    this.x2 = x2; this.y2 = y2;
  }
}

