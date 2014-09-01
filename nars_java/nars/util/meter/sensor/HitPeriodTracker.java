/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.meter.sensor;

import nars.util.meter.Sensor;
import nars.util.meter.session.StatsSession;

/**
 * Measures the period/interval duration between commit()'s, 
 * in milliseconds
 */
public class HitPeriodTracker extends AbstractSpanTracker {

    private long lastHitStamp = -1;

    public HitPeriodTracker(final StatsSession session) {
        super(session);
    }

    public HitPeriodTracker(final String id) {
        super(id);
    }

    @Override
    protected void startImpl(final long now) {
        super.startImpl(now);
    }

    @Override
    protected void stopImpl(final long now) {
        long nowNS = System.nanoTime();
        
        if (lastHitStamp > 0) {
            
            value = ((double)(nowNS - lastHitStamp))/1.0e6;

            session.update(this, now);
            
        }
        
        lastHitStamp = nowNS;
    }
    

    public void event() { 
        start();
        stop(); 
    }
    
    @Override
    public Sensor reset() {
        super.reset();
        lastHitStamp = -1;

        return this;
    }

}
