package nars.sonification;

import nars.Audio;
import nars.Global;
import nars.NAR;
import nars.audio.SoundProducer;
import nars.audio.granular.Granulize;
import nars.audio.sample.SampleLoader;
import nars.audio.sample.SonarSample;
import nars.concept.Concept;
import nars.event.FrameReaction;
import nars.nar.Default;
import org.infinispan.commons.util.concurrent.ConcurrentWeakKeyHashMap;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Sonifies the activity of concepts being activated and forgotten
 */
public class ConceptSonification extends FrameReaction {

    List<SonarSample> samples = Global.newArrayList();

    private final Audio sound;
    Map<Concept, SoundProducer> playing = new ConcurrentWeakKeyHashMap<>();
    //Global.newHashMap();

    float audiblePriorityThreshold = 0.2f;


    public ConceptSonification(NAR nar, Audio sound) throws IOException {
        super(nar);

        this.sound = sound;

        //Events.ConceptProcessed.class,
            /*Premise f = (Premise)args[0];
            update(f.getConcept());*/

        updateSamples();

        nar.memory.eventConceptProcessed.on(c -> {
            update(c.getConcept());
        });
        //TODO update all existing concepts on start?
    }

    @Override
    public void onFrame() {
        updateConceptsPlaying();
    }

    public static void main(String[] args) throws IOException, LineUnavailableException {
        Default d = new Default(1000, 1, 3);
        new ConceptSonification(d, new Audio(64));
        d.input("<a-->b>.", "<b-->c>.", "<c-->d>.", "<d-->e>.");
        d.loop(20);
    }

    protected void updateSamples() throws IOException {

//        {

        //Files.list(Paths.get("/tmp")).forEach(System.out::println);

        Files.list(Paths.get("/tmp")). // Paths.get("/home/me/share/wav")).
                map(p -> p.toAbsolutePath().toString()).filter(x -> x.endsWith(".wav"))
                .map(s -> SampleLoader.load(s))
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
    final SonarSample getSample(final Concept c) {
        final List<SonarSample> samples = this.samples;
        return samples.get(Math.abs(c.hashCode()) % samples.size());
    }

    public void update(Concept c) {
        //if (c.getPriority() > audiblePriorityThreshold) {
        final boolean audible = audible(c);
        if (!audible) return;

        Granulize g;// = playing.get(c);

        //if ((g == null) && audible) {

        SonarSample sp = getSample(c);


        //do {
        //try {
        g = new Granulize(sp, 0.25f, 0.1f);
        g.pitchFactor.set(0.1);

        //g = new SineWave(Video.hashFloat(c.hashCode()));
        SoundProducer removed = playing.put(c, g);
        sound.play(g, 1f, 1);
        if (removed != null)
            removed.stop();

        if (g != null)
            update(c, g);

            /*} catch (Exception e) {
                e.printStackTrace();
                samples.remove(sp);
                return;
            }*/
        //} while ((g == null) && (!samples.isEmpty()));


//        }
//        else {
//
//        }


    }

    protected final boolean audible(Concept c) {
        return c.getPriority() > audiblePriorityThreshold;
    }

    private void update(Concept c, SoundProducer g) {


//        if (g instanceof Granulize) {
//            ((Granulize) g).setStretchFactor(0.25f);// + 4f * (1f - c.getQuality()));
//        }
        if (audible(c)) {
            //TODO autmatic gain control
            float vol = 0.1f + 0.9f * c.getPriority();
            System.out.println(c + " at " + vol);
            ((SoundProducer.Amplifiable) g).setAmplitude(vol);
        }
        else {
            SoundProducer x = playing.remove(c);
            if (x != null)
                x.stop();
        }
    }

    protected void updateConceptsPlaying() {
        for (Map.Entry<Concept, SoundProducer> e : playing.entrySet()) {
            update(e.getKey(), e.getValue());
        }
    }


}
