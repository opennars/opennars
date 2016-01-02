package nars.video;

import nars.util.signal.OneDHaar;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Signal sampled from system sound devices (via Java Media)
 */
public class AudioSource implements WaveSource {
    private final float frameRate;
    private TargetDataLine line;
    private final Mixer mixer;
    private final DataLine.Info dataLineInfo;
    private final AudioFormat audioFormat;

    private byte[] audioBytes;
    private short[] samples;


    public AudioSource(int device, float frameRate) {
        this.frameRate = frameRate;

        // Pick a format...
        // NOTE: It is better to enumerate the formats that the system supports,
        // because getLine() can error out with any particular format...
        audioFormat = new AudioFormat(44100.0F, 16, 1, true, false);

        // Get TargetDataLine with that format
        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
        System.out.println(Arrays.toString(minfoSet));

        mixer = AudioSystem.getMixer(minfoSet[device]);
        System.out.println(mixer);
        System.out.println(mixer.getMixerInfo());
        System.out.println(Arrays.toString(mixer.getControls()));

        dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        System.out.println(dataLineInfo);
    }


    @Override
    public int start() {
        // Open and start capturing audio
        // It's possible to have more control over the chosen audio device with this line:
        try {
            line = (TargetDataLine) mixer.getLine(dataLineInfo);
            //line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            line.open(audioFormat);
            line.start();

            System.out.println(line);

            int sampleRate = (int) audioFormat.getSampleRate();
            int numChannels = audioFormat.getChannels();

            float period = 1.0f / frameRate;

            // Let's initialize our audio buffer...
            int audioBufferSize = (int) (sampleRate * numChannels * period);
            audioBufferSize = OneDHaar.largestPowerOf2NoGreaterThan(audioBufferSize);

            audioBytes = new byte[audioBufferSize * 2];
            samples = new short[audioBufferSize];

            return audioBufferSize;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return 0;

        }


    }

    @Override
    public void stop() {

    }

    @Override
    public int next(float[] buffer) {

        int bufferSamples = buffer.length;
        short[] samples = this.samples;
        if (this.samples == null) return 0;


        // Read from the line... non-blocking
        int avail = line.available();
        int nBytesRead = line.read(audioBytes, 0,
                //bufferSamples*2
                Math.min(bufferSamples * 2, avail)
        );

        // Since we specified 16 bits in the AudioFormat,
        // we need to convert our read byte[] to short[]
        // (see source from FFmpegFrameRecorder.recordSamples for AV_SAMPLE_FMT_S16)
        // Let's initialize our short[] array
        int nSamplesRead = nBytesRead / 2;


        // Let's wrap our short[] into a ShortBuffer and
        // pass it to recordSamples
        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);

        int start = Math.max(0, nSamplesRead - bufferSamples);
        int end = nSamplesRead;
        int j = 0;
        float fmax = Short.MAX_VALUE;
        for (int i = start; i < end; i++)
            buffer[j++] = samples[i] / fmax;

        return nSamplesRead;
    }

}
