package nars.sonification;

import nars.Audio;
import nars.Global;
import nars.NAR;
import nars.audio.SoundProducer;
import nars.audio.granular.Granulize;
import nars.audio.sample.SampleLoader;
import nars.audio.sample.SonarSample;
import nars.concept.Concept;
import nars.nar.Default;
import nars.util.event.FrameReaction;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Sonifies the activity of concepts being activated and forgotten
 */
public class ConceptSonification extends FrameReaction {

    List<SonarSample> samples = Global.newArrayList();


    public final Audio sound;
    //TODO use bag
    public Map<Concept, SoundProducer> playing;
    private final int polyphony;

    static class PlayingMap extends LinkedHashMap<Concept, SoundProducer> {
        private final int maxSize;

        public PlayingMap(int maxSize) {
            super(maxSize);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Concept, SoundProducer> eldest) {
            return size() > maxSize;
        }
    }
    //Global.newHashMap();

    float audiblePriorityThreshold = 0.0f;

    public ConceptSonification(NAR nar, Audio sound) throws IOException {
        super(nar);

        polyphony = sound.maxChannels;

        playing = Collections.synchronizedMap(
                new PlayingMap(polyphony)
        );

        this.sound = sound;


        //Events.ConceptProcessed.class,
            /*Premise f = (Premise)args[0];
            update(f.getConcept());*/

        updateSamples();

        nar.memory.eventConceptProcess.on(c -> update(c.getConcept()));
        //TODO update all existing concepts on start?
    }

    @Override
    public void onFrame() {
        updateConceptsPlaying();
    }

    @SuppressWarnings("HardcodedFileSeparator")
    public static void main(String[] args) throws IOException, LineUnavailableException {
        Default d = new Default();
        Audio a = new Audio(32);

        new ConceptSonification(d, a);

        new Thread( () -> {
            try {
                a.record("/tmp/x.WAV");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } ).start();

        //d.stdout();

        d.input("$0.6$ <a-->b>.", "$0.5$ <b-->c>.",
                "$0.6$ <c-->d>.", "$0.6$ <d-->e>."
        );
        d.loop(500);


    }

    @SuppressWarnings("HardcodedFileSeparator")
    protected void updateSamples() throws IOException {

//        {

        //Files.list(Paths.get("/tmp")).forEach(System.out::println);

        Files.list(Paths.get("/tmp")). // Paths.get("/home/me/share/wav")).
                map(p -> p.toAbsolutePath().toString()).filter(x -> x.endsWith(".wav"))
                .map(SampleLoader::load)
                .forEach(s -> samples.add(s));

//        }

        //{
        //samples = Files.list(Paths.get("/home/me/share/wav")).
        //       map(p -> p.toAbsolutePath().toString() ).filter( x -> x.endsWith(".wav") ).collect(Collectors.toList());

//            samples.add(
//                    SampleLoader.digitize(t ->
//                                    (float) (Math.sin(t * 1000.0f) + 0.25 * Math.sin(Math.exp(t * 200.0f))),
//                            44100 /* sample rate */, 0.5f /* clip duration */)
//                    );

//            for (int i = 0; i < 3; i++) {
//                for (int j = 0; j < 12; j++) {
//                    final float f = 16 * (1 << i) + (j / 12f);
//                    samples.add(
//                            SampleLoader.digitize(t ->
//
//                                            (float) (Math.sin(f * t) ) /* + 0.1 * Math.tan(Math.cos(f * t / 2f)))*/,
//                                    44100 /* sample rate */, 1f /* clip duration */)
//                    );
//                }
//            }
//
//        }

        //Collections.shuffle(samples);
    }

    /**
     * returns file path to load sample
     */
    final SonarSample getSample(Concept c) {
        List<SonarSample> samples = this.samples;
        int s = samples.size();
        if (s == 1)
            return samples.get(0);
        else if (s == 2)
            throw new RuntimeException("no samples to granulize");
        else
            return samples.get(Math.abs(c.get().hashCode() % s));
    }

    public void update(Concept c) {
        boolean audible = audible(c);
        if (!audible) return;

        // = playing.get(c);

        //if ((g == null) && audible) {

        SoundProducer g = playing.computeIfAbsent(c, cc -> {
            SoundProducer sp = sound(cc);
            sound.play(sp, 1.0f, 1.0f);
            playing.put(cc, sp);
            return sp;
        });

        if (g != null)
            update(c, g);


    }

    private SoundProducer sound(Concept c) {
        //do {
        //try {
        SonarSample sp = getSample(c);

        Granulize g = new Granulize(sp,
                /* grain size */
                0.3f * (1 + c.get().volume()/ 2.0f),
                1.0f
            ).at(
                //terms get positoined according to their hash
                c.get().hashCode()
            );
        return g;

        //g = new SineWave(Video.hashFloat(c.hashCode()));


    }

    protected final boolean audible(Concept c) {
        return 1f /*c.getPriority()*/ > audiblePriorityThreshold;
    }

    static final double twoTo12 = Math.pow((2),1/ 12.0);

    /** return if it should continue */
    private boolean update(Concept c, SoundProducer g) {

        if (audible(c)) {
            //TODO autmatic gain control
            float vol = 1f; //0.9f * c.getBudget().getPriority();
            //System.out.println(c + " at " + vol);
            ((SoundProducer.Amplifiable) g).setAmplitude(vol /
                    ((polyphony)/8.0f)
            );

            if (g instanceof Granulize) {
                Granulize gg = ((Granulize) g);
                gg.setStretchFactor(
                        0.85f /*+ c.getDurability()/ 3.0f*/);// + 4f * (1f - c.getQuality()));


                float pitch = 1.0f;
                //        0.5f + 0.5f * c.getQuality();

                /*
                        //higher term volume = higher pitch
                        //F = {[(2)^1/12]^n} * 220 Hz
                        0.35f * (float)Math.pow(twoTo12,
                            //Math.floor(
                                //Math.sqrt(
                                    3 * c.getTerm().volume()
                            //    )
                        ));
                    */

                gg.pitchFactor.set( pitch );
            }
            return true;
        }
        else {
            return false;
        }
    }

    protected void updateConceptsPlaying() {
        Iterator<Map.Entry<Concept, SoundProducer>> ie = playing.entrySet().iterator();
        while (ie.hasNext()) {
            Map.Entry<Concept, SoundProducer> e = ie.next();

            Concept c = e.getKey();
            boolean cont = update(c, e.getValue());
            if (!cont) {
                ie.remove();
                SoundProducer x = playing.remove(c);
                /*if (x != null)
                    x.stop();*/
            }
        }
    }

}
