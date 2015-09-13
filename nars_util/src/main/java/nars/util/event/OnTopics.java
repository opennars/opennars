package nars.util.event;

import nars.util.data.list.FasterList;

import java.util.Collections;

/**
 * Created by me on 9/13/15.
 */
public class OnTopics<T extends Topic> extends FasterList<DefaultTopic.On> {

    OnTopics(int length) {
        super(length);
    }

    OnTopics() {
        this(1);
    }

    public OnTopics(DefaultTopic.On... r) {
        super(r.length);
        Collections.addAll(this, r);
    }

//        public void resume() {
//            for (Registration r : this)
//                r.resume();
//        }
//        public void pause() {
//            for (Registration r : this)
//                r.pause();
//        }
//        public void cancelAfterUse() {
//            for (Registration r : this)
//                r.cancelAfterUse();
//        }

    public synchronized void off() {
        for (int i = 0; i < this.size(); i++) {
            this.get(i).off();
        }
        clear();
    }

    public OnTopics add(DefaultTopic.On... elements) {
        Collections.addAll(this, elements);
        return this;
    }


}
