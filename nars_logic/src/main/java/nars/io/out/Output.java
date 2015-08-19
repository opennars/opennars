package nars.io.out;

import nars.Events;
import nars.Events.Answer;
import nars.Memory;
import nars.NAR;
import nars.event.NARReaction;
import nars.nal.nal8.ImmediateOperator;
import nars.op.io.echo;
import nars.op.io.say;
import nars.util.event.EventEmitter;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Output Channel: Implements this and NAR.addOutput(..) to receive output signals on various channels
 */
public abstract class Output extends NARReaction {


    abstract public static class Channel {
        abstract public String getLinePrefix(Class c, Object[] args);
        abstract public CharSequence get(Class c, Object[] args);
        //abstract public void print(OutputStream o);
        boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class DefaultChannel extends Channel {

        final private String prefix;

        public DefaultChannel(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String getLinePrefix(Class c, Object[] args) {
            return prefix;
        }

        @Override
        public CharSequence get(final Class c, final Object[] args) {
            switch (args.length) {
                case 0: return "";
                case 1: return args[0].toString();
                default: return Arrays.toString(args);
            }
        }
    }

    public final Map<Class, Channel> channel = new IdentityHashMap();

    public static final Class[] DefaultOutputEvents = new Class[] {
            Events.IN.class,
            Events.EXE.class,
            Events.ERR.class,
            ImmediateOperator.class,
            echo.class,
            say.class,
            Answer.class,
            Events.OUT.class,
            //TaskProcess.class
            //Events.PluginsChange.class //this gets annoying
    };
            
    public Output(EventEmitter source, boolean active) {
        super(source, active, DefaultOutputEvents );

        channel.put(Events.IN.class, new DefaultChannel("IN"));
        channel.put(Events.EXE.class, new DefaultChannel("EXE"));
        channel.put(Events.ERR.class, new DefaultChannel("ERR"));
        channel.put(Events.OUT.class, new DefaultChannel("OUT"));
        channel.put(Events.TaskDerive.class, new DefaultChannel("OUT2"));
        channel.put(ImmediateOperator.class, new Channel() {

            @Override
            public String getLinePrefix(Class c, Object[] args) {
                return args[0].toString();
            }

            @Override
            public String get(Class c, Object[] args) {
                return args[0].toString();
            }
        });
        channel.put(echo.class, new DefaultChannel("ECHO"));
        channel.put(say.class, new DefaultChannel("SAY"));

    }
    
    public Output(Memory m, boolean active) {
        this(m.event, active);
    }

    public Output(NAR n, boolean active) {
        this(n.memory.event, active);
    }

    public Output(NAR n) {
        this(n, true);
    }

    @Override
    public void event(final Class event, final Object... args) {
        Channel c = channel.get(event);
        if (c!=null)
            if (c.isEnabled())
                output(c, event, args);
    }

    abstract protected boolean output(Channel channel, Class event, Object... args);

//    /** conversational (judgments, questions, etc...) output */
//    public static class OUT extends NARReaction {
//
//        AtomicInteger volume;
//        public final NAR n;
//
//        public OUT(NAR nar) {
//            super(nar);
//            this.n = nar;
//            this.volume = n.memory.param.outputVolume;
//        }
//
//
//        @Override
//        public void event(final Class event, final Object[] args) {
//
//            Task t = (Task)args[0];
//            if (t.isInput()) return; //input events will already have been output via IN channel
//
//            final float noiseLevel = 1.0f - (this.volume.get() / 100.0f);
//
//            if (t.summaryNotLessThan(noiseLevel)) {  // only report significant derived Tasks
//                n.emit(Events.OUT.class, t);
//            }
//        }
//
//        @Override
//        public Class[] getEvents() {
//            return new Class[] { TaskAdd.class };
//        }
//
//    }

}
