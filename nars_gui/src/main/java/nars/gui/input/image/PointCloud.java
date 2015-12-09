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

public class PointCloud
{
  public static final int NUM_POINTS = 32; 
  private String _name = null;
  private ArrayList<PointCloudPoint> _points = null;
  
  // the following is NOT part of the originally published javascript implementation
  // and has been added to support addition of directional testing for point clouds
  // which represent unistroke gestures
  private boolean _isUnistroke = true;

  PointCloud(String name, double[] X, double[] Y, int[]ID)
  {
    _points = new ArrayList<>();
    for(int i = 0; i < X.length; i++)
    {
      _points.add(new PointCloudPoint(X[i], Y[i], ID[i]));
    }
    
    _name = name;

    int id = _points.get(0).getID();
    for(int i = 1; i < _points.size(); i++)
    {
      if(_points.get(i).getID() != id)
      {
        _isUnistroke = false;
        break;
      }
    }
  }
  
  public PointCloud(String name, ArrayList<PointCloudPoint> points)
  {
    if(name == null || name == "")
    {
      throw new IllegalArgumentException("Point cloud name must be supplied");
    }

    _name = name;

    if(null == points || points.size() < 2)
    {
      throw new IllegalArgumentException("Point cloud points do not define a gesture of minimum length");
    }

    _points = points;
    _points = PointCloudUtils.resample(_points, NUM_POINTS);
    _points = PointCloudUtils.scale(_points);
    _points = PointCloudUtils.translateTo(_points, PointCloudUtils.ORIGIN);
    
    // the following is NOT part of the originally published javascript implementation
    // and has been added to support addition of directional testing for point clouds
    // which represent unistroke gestures
    int id = _points.get(0).getID();
    for(int i = 1; i < _points.size(); i++)
    {
      if(_points.get(i).getID() != id)
      {
        _isUnistroke = false;
        break;
      }
    }
  }

  // the following is NOT part of the originally published javascript implementation
  // and has been added to support addition of directional testing for point clouds
  // which represent unistroke gestures
  public boolean isUnistroke()
  {
    return _isUnistroke;
  }
  
  public PointCloudPoint getFirstPoint()
  {
	  return _points.get(0);
  }

  public PointCloudPoint getLastPoint()
  {
	  return _points.get(_points.size() - 1);
  }

  public String getName()
  {
    return _name;
  }

  ArrayList<PointCloudPoint> getPoints()
  {
    return _points;
  }
  
  double greedyMatch(PointCloud reference)
  {
    double pointCount = _points.size();
    double e = 0.50;
    double step = Math.floor(Math.pow(pointCount, 1.0 - e));

    double min = Double.POSITIVE_INFINITY;

    for(double i = 0.0; i < pointCount; i += step)
    {
      double d1 = cloudDistance(reference, i);
      double d2 = reference.cloudDistance(this, i);
      min = Math.min(min, Math.min(d1, d2)); // min3
    }

    return min;
  }

  private double cloudDistance(PointCloud reference, double start)
  {
    ArrayList<PointCloudPoint> pts1 = _points;
    ArrayList<PointCloudPoint> pts2 = reference._points;
    
    if(pts1.size() != pts2.size())
    {
      throw new IllegalArgumentException("Both point clouds must contain the same number of points");
    }

    double pointCount = pts1.size();
    boolean[] matched = new boolean[(int) pointCount];

    for(int k = 0; k < pointCount; k++)
    {
      matched[k] = false;
    }

    double sum = 0;
    double i = start;

    do
    {
      int index = -1;
      double min = Double.POSITIVE_INFINITY;

      for(int j = 0; j < matched.length; j++)
      {
        if (!matched[j])
        {
          double d = PointCloudUtils.distance(pts1.get((int)i), pts2.get(j));
          if (d < min)
          {
            min = d;
            index = j;
          }
        }
      }

      matched[index] = true;
      double weight = 1.0 - ((i - start + pointCount) % pointCount) / pointCount;
      sum += weight * min;
      i = (i + 1.0) % pointCount;
    } while (i != start);

    return sum;
  }
}
