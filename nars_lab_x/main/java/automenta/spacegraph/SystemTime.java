/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package automenta.spacegraph;


/** Implementation of {@link demos.util.Time} interface based
    on {@link System.currentTimeMillis}. Performs smoothing
    internally to avoid effects of poor granularity of
    currentTimeMillis on Windows platform in particular. */

public class SystemTime implements Time {
  private static final int DEFAULT_NUM_SMOOTHING_SAMPLES = 10;
  private long[] samples = new long[DEFAULT_NUM_SMOOTHING_SAMPLES];
  private int   numSmoothingSamples;
  private int   curSmoothingSample; // Index of current sample to be replaced
  private long baseTime = System.currentTimeMillis();
  private boolean hasCurTime;
  private double curTime;
  private double deltaT;

  /** Sets number of smoothing samples. Defaults to 10. Note that
      there may be a discontinuity in the reported time after a call
      to this method. */
  public void setNumSmoothingSamples(int num) {
    samples = new long[num];
    numSmoothingSamples = 0;
    curSmoothingSample = 0;
    hasCurTime = false;
  }

  /** Returns number of smoothing samples; default is 10. */
  public int getNumSmoothingSamples() {
    return samples.length;
  }

  /** Rebases this timer. After very long periods of time the
      resolution of this timer may decrease; the application can call
      this to restore higher resolution. Note that there may be a
      discontinuity in the reported time after a call to this
      method. */
  public void rebase() {
    baseTime = System.currentTimeMillis();
    setNumSmoothingSamples(samples.length);
  }

  public void update() {
    long tmpTime = System.currentTimeMillis();
    long diffSinceBase = tmpTime - baseTime;
    samples[curSmoothingSample] = diffSinceBase;
    curSmoothingSample = (curSmoothingSample + 1) % samples.length;
    numSmoothingSamples = Math.min(1 + numSmoothingSamples, samples.length);
    // Average of samples is current time
    double newCurTime = 0.0;
    for (int i = 0; i < numSmoothingSamples; i++) {
      newCurTime += samples[i];
    }
    newCurTime /= (1000.0f * numSmoothingSamples);
    double lastTime = curTime;
    if (!hasCurTime) {
      lastTime = newCurTime;
      hasCurTime = true;
    }
    deltaT = newCurTime - lastTime;
    curTime = newCurTime;
  }

  public double time() {
    return curTime;
  }

  public double deltaT() {
    return deltaT;
  }
}