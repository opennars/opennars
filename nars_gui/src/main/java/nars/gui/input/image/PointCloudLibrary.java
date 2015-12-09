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

import java.util.*;
import java.util.stream.Collectors;

import static nars.gui.input.image.PointCloudPoint.pcp;

public class PointCloudLibrary
{
  private static double CLOSE_ENOUGH = 0.3;
  private static PointCloudLibrary demoLibrary = null;
  private ArrayList<PointCloud> _pointClouds =  new ArrayList<>();

  public PointCloudLibrary()
  {
  }

  public PointCloudLibrary(ArrayList<PointCloud> pointClouds)
  {
    if(null == pointClouds)
    {
      throw new IllegalArgumentException("Point clouds cannot be null");
    }
    
    _pointClouds = pointClouds;
  }

  // the following is NOT part of the originally published javascript implementation
  // and has been added to support addition of directional testing for point clouds
  // which represent unistroke gestures
  public boolean containsOnlyUnistrokes()
  {
    for (PointCloud _pointCloud : _pointClouds) {
      if (!_pointCloud.isUnistroke()) {
        return false;
      }
    }
    
    return true;
  }

  Set<PointCloud> getPointCloud(String name)
  {
    HashSet<PointCloud> result = _pointClouds.stream().filter(_pointCloud -> _pointCloud.getName().equals(name)).collect(Collectors.toCollection(HashSet::new));

    return result;
  }
  
  public Set<String> getNames()
  {
    HashSet<String> result = _pointClouds.stream().map(PointCloud::getName).collect(Collectors.toCollection(HashSet::new));

    return result;
  }
  
  public void addPointCloud(PointCloud cloud)
  {
    _pointClouds.add(cloud);
  }
  
  // removes one or more point clouds carrying the specified name (which
  // is case sensitive) from the library. if no matches are found, null is
  // returned, else those removed are returned
  public ArrayList<PointCloud> removePointCloud(String name)
  {
    ArrayList<PointCloud> result = null;
    
    if(null == name || "" == name)
    {
      throw new IllegalArgumentException("Name must be provided");
    }
    
    for(int i = 0; i < _pointClouds.size(); i++)
    {
      PointCloud p = _pointClouds.get(i);
      if(name != p.getName())
      {
        continue;
      }
      
      if(result == null)
      {
        result = new ArrayList<>();
        result.add(p);
        _pointClouds.remove(i);
      }
    }
    
    return result;
  }
  
  public void clear()
  {
    if(this == demoLibrary)
    {
      _pointClouds = new ArrayList<>();
      populateDemoLibrary(this);
    }
    else
    {
      _pointClouds = new ArrayList<>();
    }
  }
  
  public int getSize()
  {
    return _pointClouds.size();
  }

  // most closely matches published javascript implementation of $P,
  // returns only the single, best match. note that the score member of
  // the result contains the aggregate distances between the two gestures.
  // for the original implementation see originalRecognize() below.
  public PointCloudMatchResult recognize(PointCloud inputGesture)
  {
    return recognize(inputGesture, false);
  }

  // as with published javascript implementation of $P, this returns only
  // the single, best match. however it permits use of directional testing
  // and, as such, should be used only with unistrokes. note that the score
  // member of the result contains the aggregate distances between the two
  // gestures. for the original implementation see originalRecognize() below.
  public PointCloudMatchResult recognize(PointCloud inputGesture, boolean testDirectionality)
  {
    return recognizeAll(inputGesture, testDirectionality)[0];
  }

  // unlike the published javascript implementation of $P, this returns an array
  // of results - one for each point cloud in the library sorted in order of
  // increasing aggregate distance between the point clouds. it also permits use of
  // some simple directional testing which should be used only with unistrokes. note
  // that the score member of the result contains the aggregate distance between the
  // two gestures. for the original implementation see originalRecognize() below.
  public PointCloudMatchResult[] recognizeAll(PointCloud inputGesture, boolean testDirectionality)
  {
    // the following is NOT part of the originally published javascript implementation
    // and has been added to support addition of directional testing for point clouds
    // which represent unistroke gestures
    if(testDirectionality)
    {
      if(!inputGesture.isUnistroke())
      {
        throw new IllegalArgumentException("If testDirectionality is true, input gesture must contain a unistroke");
      }

      if(! containsOnlyUnistrokes())
      {
        throw new IllegalArgumentException("If testDirectionality is true, the point cloud library must contain only unistroke point clouds");
      }
    }

    double b = Double.POSITIVE_INFINITY;
    int u = -1;
    PointCloudMatchResult[] results = new PointCloudMatchResult[_pointClouds.size()];

    for(int i = 0; i < _pointClouds.size(); i++) // for each point-cloud template
    {
      PointCloud pointCloud = _pointClouds.get(i);

      // the following is NOT part of the originally published javascript implementation
      // and has been added to support addition of directional testing for point clouds
      // which represent unistroke gestures
      if(testDirectionality)
      {
        // test to see if the gestures match roughly in directionality
        // if not, keep looking
        PointCloudPoint refStart = pointCloud.getFirstPoint();
        PointCloudPoint inStart = inputGesture.getFirstPoint();
        PointCloudPoint refEnd = pointCloud.getLastPoint();
        PointCloudPoint inEnd = inputGesture.getLastPoint();
        
        if((PointCloudUtils.distance(refStart, inStart) > CLOSE_ENOUGH) ||
           (PointCloudUtils.distance(refEnd, inEnd) > CLOSE_ENOUGH))
        {
          results[i] = new PointCloudMatchResult(pointCloud.getName(), Double.POSITIVE_INFINITY);
          continue;
        }
      }

      double d = pointCloud.greedyMatch(inputGesture);
      results[i] = new PointCloudMatchResult(pointCloud.getName(), d);
    }

    Arrays.sort(results, (obj1, obj2) -> {
      if(obj1.getScore() < obj2.getScore())
      {
        return -1;
      }

      if(obj1.getScore() > obj2.getScore())
      {
        return 1;
      }

      return 0;
    });

    return results;
  }
  
  /* this method implements the recognize routine as originally published in
   * javascript. in this author's experience:
  
   * (a) the score being normalized between 0 and 1 offered little meaning or
   * relationship to the quality of the match as implied by the word "score".
   * "correct" matches were found to result with scores at both extremes of the
   * range.
   * 
   * (b) on occasion it was helpful to have the results of matches for each of
   * the point clouds in the library. the original implementation provides only
   * the "best" match.
   * 
   * (c) $P as published implements directional invariance and does so by design.
   * the main rationale for this decision stems from the value of directional invariance
   * when the recognizer is used with multi-stroke gestures. however, this attribute
   * of the recognizer renders it incapable of discerning similar but semantically
   * different unistroke gestures such as a single top->bottom or left->right
   * from a single bottom->top or right->left. 
   */
  public PointCloudMatchResult originalRecognize(PointCloud inputGesture)
  {
    double b = Double.POSITIVE_INFINITY;
    int u = -1;

    for(int i = 0; i < _pointClouds.size(); i++) // for each point-cloud template
    {
      PointCloud pointCloud = _pointClouds.get(i);
      double d = inputGesture.greedyMatch(pointCloud);
      
      if (d < b)
      {
        b = d; // best (least) distance
        u = i; // point-cloud
      }
    }

    if(u == -1)
    {
      return new PointCloudMatchResult("No match", 0.0);
    }
    else
    {
      double r = Math.max((b - 2.0) / -2.0, 0.0);
      return new PointCloudMatchResult(_pointClouds.get(u).getName(), r);
    }
  }
  
  public static PointCloudLibrary getDemoLibrary()
  {
    if(null != demoLibrary)
    {
      return demoLibrary;
    }

    demoLibrary = new PointCloudLibrary();
    populateDemoLibrary(demoLibrary);
    return demoLibrary;
  }
  
  private static void populateDemoLibrary(PointCloudLibrary library)
  {
    ArrayList<PointCloudPoint> p = new ArrayList<>();

    p.add(pcp(30,7,1));
    p.add(pcp(103,7,1));
    p.add(pcp(66,7,2));
    p.add(pcp(66,87,2));
    library.addPointCloud(new PointCloud("T", p));

    p = new ArrayList<>();
    p.add(pcp(177,92,1));
    p.add(pcp(177,2,1));
    p.add(pcp(182,1,2));
    p.add(pcp(246,95,2));
    p.add(pcp(247,87,3));
    p.add(pcp(247,1,3));
    library.addPointCloud(new PointCloud("N", p));

    p = new ArrayList<>();
    p.add(pcp(345,9,1));
    p.add(pcp(345,87,1));
    p.add(pcp(351,8,2));
    p.add(pcp(363,8,2));
    p.add(pcp(372,9,2));
    p.add(pcp(380,11,2));
    p.add(pcp(386,14,2));
    p.add(pcp(391,17,2));
    p.add(pcp(394,22,2));
    p.add(pcp(397,28,2));
    p.add(pcp(399,34,2));
    p.add(pcp(400,42,2));
    p.add(pcp(400,50,2));
    p.add(pcp(400,56,2));
    p.add(pcp(399,61,2));
    p.add(pcp(397,66,2));
    p.add(pcp(394,70,2));
    p.add(pcp(391,74,2));
    p.add(pcp(386,78,2));
    p.add(pcp(382,81,2));
    p.add(pcp(377,83,2));
    p.add(pcp(372,85,2));
    p.add(pcp(367,87,2));
    p.add(pcp(360,87,2));
    p.add(pcp(355,88,2));
    p.add(pcp(349,87,2));
    library.addPointCloud(new PointCloud("D", p));

    p = new ArrayList<>();
    p.add(pcp(507,8,1));
    p.add(pcp(507,87,1));
    p.add(pcp(513,7,2));
    p.add(pcp(528,7,2));
    p.add(pcp(537,8,2));
    p.add(pcp(544,10,2));
    p.add(pcp(550,12,2));
    p.add(pcp(555,15,2));
    p.add(pcp(558,18,2));
    p.add(pcp(560,22,2));
    p.add(pcp(561,27,2));
    p.add(pcp(562,33,2));
    p.add(pcp(561,37,2));
    p.add(pcp(559,42,2));
    p.add(pcp(556,45,2));
    p.add(pcp(550,48,2));
    p.add(pcp(544,51,2));
    p.add(pcp(538,53,2));
    p.add(pcp(532,54,2));
    p.add(pcp(525,55,2));
    p.add(pcp(519,55,2));
    p.add(pcp(513,55,2));
    p.add(pcp(510,55,2));
    library.addPointCloud(new PointCloud("P", p));

    p = new ArrayList<>();
    p.add(pcp(30,146,1));
    p.add(pcp(106,222,1));
    p.add(pcp(30,225,2));
    p.add(pcp(106,146,2));
    library.addPointCloud(new PointCloud("X", p));

    p = new ArrayList<>();
    p.add(pcp(188,137,1));
    p.add(pcp(188,225,1));
    p.add(pcp(188,180,2));
    p.add(pcp(241,180,2));
    p.add(pcp(241,137,3));
    p.add(pcp(241,225,3));
    library.addPointCloud(new PointCloud("H", p));

    p = new ArrayList<>();
    p.add(pcp(371,149,1));
    p.add(pcp(371,221,1));
    p.add(pcp(341,149,2));
    p.add(pcp(401,149,2));
    p.add(pcp(341,221,3));
    p.add(pcp(401,221,3));
    library.addPointCloud(new PointCloud("I", p));

    p = new ArrayList<>();
    p.add(pcp(526,142,1));
    p.add(pcp(526,204,1));
    p.add(pcp(526,221,2));
    library.addPointCloud(new PointCloud("exclamation", p));

    p = new ArrayList<>();
    p.add(pcp(12,347,1));
    p.add(pcp(119,347,1));
    library.addPointCloud(new PointCloud("line", p));

    p = new ArrayList<>();
    p.add(pcp(177,396,1));
    p.add(pcp(223,299,1));
    p.add(pcp(262,396,1));
    p.add(pcp(168,332,1));
    p.add(pcp(278,332,1));
    p.add(pcp(184,397,1));
    library.addPointCloud(new PointCloud("five-point star", p));

    p = new ArrayList<>();
    p.add(pcp(382,310,1));
    p.add(pcp(377,308,1));
    p.add(pcp(373,307,1));
    p.add(pcp(366,307,1));
    p.add(pcp(360,310,1));
    p.add(pcp(356,313,1));
    p.add(pcp(353,316,1));
    p.add(pcp(349,321,1));
    p.add(pcp(347,326,1));
    p.add(pcp(344,331,1));
    p.add(pcp(342,337,1));
    p.add(pcp(341,343,1));
    p.add(pcp(341,350,1));
    p.add(pcp(341,358,1));
    p.add(pcp(342,362,1));
    p.add(pcp(344,366,1));
    p.add(pcp(347,370,1));
    p.add(pcp(351,374,1));
    p.add(pcp(356,379,1));
    p.add(pcp(361,382,1));
    p.add(pcp(368,385,1));
    p.add(pcp(374,387,1));
    p.add(pcp(381,387,1));
    p.add(pcp(390,387,1));
    p.add(pcp(397,385,1));
    p.add(pcp(404,382,1));
    p.add(pcp(408,378,1));
    p.add(pcp(412,373,1));
    p.add(pcp(416,367,1));
    p.add(pcp(418,361,1));
    p.add(pcp(419,353,1));
    p.add(pcp(418,346,1));
    p.add(pcp(417,341,1));
    p.add(pcp(416,336,1));
    p.add(pcp(413,331,1));
    p.add(pcp(410,326,1));
    p.add(pcp(404,320,1));
    p.add(pcp(400,317,1));
    p.add(pcp(393,313,1));
    p.add(pcp(392,312,1));
    p.add(pcp(418,309,2));
    p.add(pcp(337,390,2));
    library.addPointCloud(new PointCloud("null", p));

    p = new ArrayList<>();
    p.add(pcp(506,349,1));
    p.add(pcp(574,349,1));
    p.add(pcp(525,306,2));
    p.add(pcp(584,349,2));
    p.add(pcp(525,388,2));
    library.addPointCloud(new PointCloud("arrowhead", p));

    p = new ArrayList<>();
    p.add(pcp(38,470,1));
    p.add(pcp(36,476,1));
    p.add(pcp(36,482,1));
    p.add(pcp(37,489,1));
    p.add(pcp(39,496,1));
    p.add(pcp(42,500,1));
    p.add(pcp(46,503,1));
    p.add(pcp(50,507,1));
    p.add(pcp(56,509,1));
    p.add(pcp(63,509,1));
    p.add(pcp(70,508,1));
    p.add(pcp(75,506,1));
    p.add(pcp(79,503,1));
    p.add(pcp(82,499,1));
    p.add(pcp(85,493,1));
    p.add(pcp(87,487,1));
    p.add(pcp(88,480,1));
    p.add(pcp(88,474,1));
    p.add(pcp(87,468,1));
    p.add(pcp(62,464,2));
    p.add(pcp(62,571,2));
    library.addPointCloud(new PointCloud("pitchfork", p));

    p = new ArrayList<>();
    p.add(pcp(177,554,1));
    p.add(pcp(223,476,1));
    p.add(pcp(268,554,1));
    p.add(pcp(183,554,1));
    p.add(pcp(177,490,2));
    p.add(pcp(223,568,2));
    p.add(pcp(268,490,2));
    p.add(pcp(183,490,2));
    library.addPointCloud(new PointCloud("six-point star", p));

    p = new ArrayList<>();
    p.add(pcp(325,499,1));
    p.add(pcp(417,557,1));
    p.add(pcp(417,499,2));
    p.add(pcp(325,557,2));
    p.add(pcp(371,486,3));
    p.add(pcp(371,571,3));
    library.addPointCloud(new PointCloud("asterisk", p));

    p = new ArrayList<>();
    p.add(pcp(546,465,1));
    p.add(pcp(546,531,1));
    p.add(pcp(540,530,2));
    p.add(pcp(536,529,2));
    p.add(pcp(533,528,2));
    p.add(pcp(529,529,2));
    p.add(pcp(524,530,2));
    p.add(pcp(520,532,2));
    p.add(pcp(515,535,2));
    p.add(pcp(511,539,2));
    p.add(pcp(508,545,2));
    p.add(pcp(506,548,2));
    p.add(pcp(506,554,2));
    p.add(pcp(509,558,2));
    p.add(pcp(512,561,2));
    p.add(pcp(517,564,2));
    p.add(pcp(521,564,2));
    p.add(pcp(527,563,2));
    p.add(pcp(531,560,2));
    p.add(pcp(535,557,2));
    p.add(pcp(538,553,2));
    p.add(pcp(542,548,2));
    p.add(pcp(544,544,2));
    p.add(pcp(546,540,2));
    p.add(pcp(546,536,2));
    library.addPointCloud(new PointCloud("half-note", p));
  }
}
