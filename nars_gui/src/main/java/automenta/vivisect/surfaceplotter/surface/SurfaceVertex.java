/*----------------------------------------------------------------------------------------*
 * SurfaceVertex.java                                                                     *
 *                                                                                        *
 * Surface Plotter   version 1.10    14 Oct 1996                                          *
 *                   version 1.20     8 Nov 1996                                          *
 *                   version 1.30b1  17 May 1997                                          *
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
package automenta.vivisect.surfaceplotter.surface;

import java.awt.*;

/**
 * The class <code>SurfaceVertex</code> represents a surfaceVertex in 3D space.
 * 
 * @author Yanto Suryono
 */

public final class SurfaceVertex {
	private Point projection;
	private int project_index;

	private static int master_project_index = 0; // over 4 billion times to
													// reset

	/**
	 * The x coordinate
	 */
	public float x;

	/**
	 * The y coordinate
	 */
	public float y;

	/**
	 * The z coordinate
	 */
	public float z;

	/**
	 * The constructor of <code>SurfaceVertex</code>. The x and y coordinated
	 * must be in normalized form, i.e: in the range -10 .. +10.
	 * 
	 * @param ix
	 *            the x coordinate
	 * @param iy
	 *            the y coordinate
	 * @param iz
	 *            the z coordinate
	 */

	public SurfaceVertex() {
		set(0, 0, 0);
	}

	public SurfaceVertex(float ix, float iy, float iz) {
		set(ix, iy, iz);
	}
	public void set(float ix, float iy, float iz) {
		x = ix;
		y = iy;
		z = iz;
		project_index = master_project_index - 1;
	}

	/**
	 * Determines whether this surfaceVertex is invalid, i.e has invalid
	 * coordinates value.
	 * 
	 * @return <code>true</code> if this surfaceVertex is invalid
	 */

	public boolean isInvalid() {
		return Float.isNaN(z);
	}

	/**
	 * Gets the 2D projection of the surfaceVertex.
	 * 
	 * @return the 2D projection
	 */

	public Point projection(Projector projector) {
		/* if (project_index != master_project_index) */
		projection = projector.project(x, y, (z - projector.zmin)
				* projector.zfactor - 10);
		project_index = master_project_index;
		return projection;
	}

	/**
	 * Transforms coordinate values to fit the scaling factor of the projector.
	 * This routine is only used for transforming center of projection in
	 * Surface Plotter.
	 */

	public void transform(Projector projector) {
		x = x / projector.getXScaling();
		y = y / projector.getYScaling();
		z = (projector.zmax - projector.zmin)
				* (z / projector.getZScaling() + 10) / 20 + projector.zmin;
	}

	/**
	 * Invalidates all vertices. This will force the projector to recalculate
	 * surfaceVertex projection.
	 */

	public static void invalidate() {
		master_project_index++;
	}

	/**
	 * Sets the projector to project this surfaceVertex.
	 * 
	 * @param projector
	 *            the projector
	 */
	/*
	 * public void setProjector(Projector projector) { this.projector =
	 * projector; } /*
	 */
	/**
	 * Sets the minimum and maximum value of z range. This values is used to
	 * compute a factor to normalized z values into the range -10 .. +10.
	 * 
	 * @param zmin
	 *            the minimum z
	 * @param zmax
	 *            the maximum z
	 */

}
