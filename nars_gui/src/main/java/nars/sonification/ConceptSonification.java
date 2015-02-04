package nars.sonification;

import automenta.vivisect.Audio;
import automenta.vivisect.audio.granular.Granulize;
import automenta.vivisect.audio.sample.SampleLoader;
import nars.core.Events;
import nars.core.NAR;
import nars.event.AbstractReaction;
import nars.logic.FireConcept;
import nars.logic.entity.Concept;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sonifies the activity of concepts being activated and forgotten
 */
public class ConceptSonification extends AbstractReaction {

    List<String> samples;

    private final Audio sound;
    Map<Concept, Granulize> playing = new HashMap();
    float audiblePriorityThreshold = 0.8f;


    public ConceptSonification(NAR nar, Audio sound) throws IOException {
        super(nar, true, Events.ConceptFired.class, Events.ConceptForget.class, Events.FrameEnd.class);

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
            Granulize g = playing.get(c);
            if (g == null) {
                if (!samples.isEmpty()) {
                    String sp = getSample(c);
                    //do {
                        try {
                            g = new Granulize(SampleLoader.load(sp), 0.1f, 0.1f);
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
            Granulize g = playing.remove(c);
            if (g!=null)
                g.stop();
        }
    }

    private void update(Concept c, Granulize g) {
        g.setStretchFactor(1f + 4f * (1f - c.getQuality()));
        g.setAmplitude( (c.getPriority() - audiblePriorityThreshold) / (1f - audiblePriorityThreshold) );
    }

    protected void updateConceptsPlaying() {
        for (Map.Entry<Concept, Granulize> e : playing.entrySet()) {
            update(e.getKey(), e.getValue());
        }
    }

    @Override
    public void event(Class event, Object[] args) {

        if (event == Events.FrameEnd.class) {
            updateConceptsPlaying();
        }

        else if (event == Events.ConceptFired.class) {
            FireConcept f = (FireConcept)args[0];
            update(f.getCurrentConcept());
        }

        else if (event == Events.ConceptForget.class) {
            Concept c = (Concept)args[0];
            update(c);
        }


    }
}
