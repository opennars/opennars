package nars.sonification;

import automenta.vivisect.Audio;
import automenta.vivisect.Video;
import automenta.vivisect.audio.SoundProducer;
import automenta.vivisect.audio.granular.Granulize;
import automenta.vivisect.audio.synth.SineWave;
import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.premise.Premise;
import nars.concept.Concept;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sonifies the activity of concepts being activated and forgotten
 */
public class ConceptSonification extends NARReaction {

    List<String> samples;

    private final Audio sound;
    Map<Concept, SoundProducer> playing = new HashMap();
    float audiblePriorityThreshold = 0.8f;


    public ConceptSonification(NAR nar, Audio sound) throws IOException {
        super(nar, true, Events.ConceptProcessed.class, Events.ConceptForget.class, Events.FrameEnd.class);

        this.sound = sound;

        updateSamples();

        //TODO update all existing concepts on start?
    }

    protected void updateSamples() throws IOException {

        samples = Files.list(Paths.get("/home/me/share/wav")).
                map(p -> p.toAbsolutePath().toString() ).filter( x -> x.endsWith(".wav") ).collect(Collectors.toList());

        Collections.shuffle(samples);
    }

    /** returns file path to load sample */
    String getSample(Concept c) {
        return samples.get(Math.abs(c.hashCode()) % samples.size());
    }

    public void update(Concept c) {
        if (c.getPriority() > audiblePriorityThreshold) {
            SoundProducer g = playing.get(c);
            if (g == null) {
                if (!samples.isEmpty()) {
                    String sp = getSample(c);
                    //do {
                        try {
                            //g = new Granulize(SampleLoader.load(sp), 0.1f, 0.1f);
                            g = new SineWave(Video.hashFloat(c.hashCode()));
                        } catch (Exception e) {
                            samples.remove(sp);
                            g = null;
                            return;
                        }
                    //} while ((g == null) && (!samples.isEmpty()));

                    playing.put(c, g);
                    sound.play(g, 1f, 1);
                }
            }

            if (g!=null)
                update(c, g);
        }
        else {
            SoundProducer g = playing.remove(c);
            if (g!=null)
                g.stop();
        }
    }

    private void update(Concept c, SoundProducer g) {
        if (g instanceof Granulize) {
            ((Granulize)g).setStretchFactor(1f + 4f * (1f - c.getQuality()));
        }
        if (g instanceof SoundProducer.Amplifiable) {
            ((SoundProducer.Amplifiable)g).setAmplitude((c.getPriority() - audiblePriorityThreshold) / (1f - audiblePriorityThreshold));
        }
    }

    protected void updateConceptsPlaying() {
        for (Map.Entry<Concept, SoundProducer> e : playing.entrySet()) {
            update(e.getKey(), e.getValue());
        }
    }

    @Override
    public void event(Class event, Object[] args) {

        if (event == Events.FrameEnd.class) {
            updateConceptsPlaying();
        }

        else if (event == Events.ConceptProcessed.class) {
            Premise f = (Premise)args[0];
            update(f.getConcept());
        }

        else if (event == Events.ConceptForget.class) {
            Concept c = (Concept)args[0];
            update(c);
        }


    }
}
