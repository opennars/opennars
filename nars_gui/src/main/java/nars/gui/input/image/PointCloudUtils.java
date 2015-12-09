/**
 * The $P Point-Cloud Recognizer (Java version)
 *
 *  by David White
 *  Copyright (c) 2012, David White. All rights reserved.
 *
 *  based entirely on the $P Point-Cloud Recognizer (Javascript version)
 *  found at http://depts.washington.edu/aimgroup/proj/dollar/pdollar.html
 *  who's original header follows:
 *
 *************************************************************************
 * The $P Point-Cloud Recognizer (JavaScript version)
 *
 *  Radu-Daniel Vatavu, Ph.D.
 *  University Stefan cel Mare of Suceava
 *  Suceava 720229, Romania
 *  vatavu@eed.usv.ro
 *
 *  Lisa Anthony, Ph.D.
 *      UMBC
 *      Information Systems Department
 *      1000 Hilltop Circle
 *      Baltimore, MD 21250
 *      lanthony@umbc.edu
 *
 *  Jacob O. Wobbrock, Ph.D.
 *  The Information School
 *  University of Washington
 *  Seattle, WA 98195-2840
 *  wobbrock@uw.edu
 *
 * The academic publication for the $P recognizer, and what should be
 * used to cite it, is:
 *
 *  Vatavu, R.-D., Anthony, L. and Wobbrock, J.O. (2012).
 *    Gestures as point clouds: A $P recognizer for user interface
 *    prototypes. Proceedings of the ACM Int'l Conference on
 *    Multimodal Interfaces (ICMI '12). Santa Monica, California
 *    (October 22-26, 2012). New York: ACM Press, pp. 273-280.
 *
 * This software is distributed under the "New BSD License" agreement:
 *
 * Copyright (c) 2012, Radu-Daniel Vatavu, Lisa Anthony, and
 * Jacob O. Wobbrock. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of the University Stefan cel Mare of Suceava,
 *  University of Washington, nor UMBC, nor the names of its contributors
 *  may be used to endorse or promote products derived from this software
 *  without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Radu-Daniel Vatavu OR Lisa Anthony
 * OR Jacob O. Wobbrock BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
**/
package nars.gui.input.image;

import java.util.ArrayList;

public class PointCloudUtils
{
  public static final PointCloudPoint ORIGIN = new PointCloudPoint(0.0,0.0,0);

  private PointCloudUtils()
  {
	// prevent instantiation which is un-needed to use the static methods in this class
  }
  
  public static ArrayList<PointCloudPoint> translateTo(ArrayList<PointCloudPoint> points, PointCloudPoint pt) // translates points' centroid
  {
    PointCloudPoint c = centroid(points);
    ArrayList<PointCloudPoint> newpoints = new ArrayList<>();

    for(int i = 0; i < points.size(); i++)
    {
      PointCloudPoint p = points.get(i);
      double qx = p.getX() + pt.getX() - c.getX();
      double qy = p.getY() + pt.getY() - c.getY();
      newpoints.add(new PointCloudPoint(qx, qy, p.getID()));
    }

    return newpoints;
  }

  public static PointCloudPoint centroid(ArrayList<PointCloudPoint> points)
  {
    double x = 0.0;
    double y = 0.0;

    for(int i = 0; i < points.size(); i++)
    {
      PointCloudPoint p = points.get(i);
      x += p.getX();
      y += p.getY();
    }

    x /= points.size();
    y /= points.size();

    return new PointCloudPoint(x, y, 0);
  }

  // the following appears to scale all x,y values to between 0 and 1
  public static ArrayList<PointCloudPoint> scale(ArrayList<PointCloudPoint> points)
  {
    double minX = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;

    for(int i = 0; i < points.size(); i++)
    {
      PointCloudPoint p = points.get(i);
      
      minX = Math.min(minX, p.getX());
      minY = Math.min(minY, p.getY());
      maxX = Math.max(maxX, p.getX());
      maxY = Math.max(maxY, p.getY());
    }

    double size = Math.max(maxX - minX, maxY - minY);

    ArrayList<PointCloudPoint> newpoints = new ArrayList<>();

    for(int i = 0; i < points.size(); i++)
    {
      PointCloudPoint p = points.get(i);
      double qx = (p.getX() - minX) / size;
      double qy = (p.getY() - minY) / size;
      newpoints.add(new PointCloudPoint(qx, qy, p.getID()));
    }

    return newpoints;
  }

  public static double pathLength(ArrayList<PointCloudPoint> points) // length traversed by a point path
  {
    double d = 0.0;
    for(int i = 1; i < points.size(); i++)
    {
      PointCloudPoint p1 = points.get(i);
      PointCloudPoint p2 = points.get(i - 1);
      if(p1.getID() == p2.getID())
      {
        d += distance(p2, p1);
      }
    }

    return d;
  }

  public static ArrayList<PointCloudPoint> resample(ArrayList<PointCloudPoint> points, int numPoints)
  {
    double I = pathLength(points) / (numPoints - 1); // interval length
    double D = 0.0;
    ArrayList<PointCloudPoint> newpoints = new ArrayList<>();
    PointCloudPoint p = points.get(0);
    newpoints.add(new PointCloudPoint(p.getX(), p.getY(), p.getID()));

    for(int i = 1; i < points.size(); i++)
    {
      PointCloudPoint p1 = points.get(i);
      PointCloudPoint p2 = points.get(i - 1);
      if(p1.getID() == p2.getID())
      {
        double d = distance(p2, p1);

        if((D + d) >= I)
        {
          double qx = p2.getX() + ((I - D) / d) * (p1.getX() - p2.getX());
          double qy = p2.getY() + ((I - D) / d) * (p1.getY() - p2.getY());

          PointCloudPoint q = new PointCloudPoint(qx, qy, p1.getID());

          newpoints.add(q); // append new point 'q'
          points.add(i, q); // insert 'q' at position i in points s.t. 'q' will be the next i
          D = 0.0;
        }
        else
        {
          D += d;
        }
      }
    }

    if (newpoints.size() == numPoints - 1) // sometimes we fall a rounding-error short of adding the last point, so add it if so
    {
      p = points.get(points.size() - 1);
      newpoints.add(new PointCloudPoint(p.getX(), p.getY(), p.getID()));
    }

    return newpoints;
  }

  public static double pathDistance(ArrayList<PointCloudPoint> pts1, ArrayList<PointCloudPoint> pts2) // average distance between corresponding points in two paths
  {
    if(pts1.size() != pts2.size())
    {
      throw new IllegalArgumentException("Both arrays must be of same length");
    }

    double d = 0.0;
    for(int i = 0; i < pts1.size(); i++) // assumes pts1.length == pts2.length
    {
      d += distance(pts1.get(i), pts2.get(i));
    }

    return d / pts1.size();
  }

  public static double distance(PointCloudPoint p1, PointCloudPoint p2) // Euclidean distance between two points
  {
    double dx = p2.getX() - p1.getX();
    double dy = p2.getY() - p1.getY();
    return Math.sqrt(dx * dx + dy * dy);
  }
}
