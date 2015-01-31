package automenta.vivisect.audio.granular;

import java.io.InputStream;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;


public class JavaSoundAudioPlayer implements AudioPlayer, Runnable {

	private TimeStretcherCompact timeStretcher = null;
	private boolean stopped;
	private Thread audioThread;

	public JavaSoundAudioPlayer() {
		audioThread = new Thread(this);
		audioThread.start();
	}

    float[] buffer;
    byte[] byteBuffer;

    @Override
	public void run() {
		try {
			int bufferSize = 8192;
			DataLine.Info wanted = new DataLine.Info(SourceDataLine.class, new AudioFormat(44100F, 16, 2, true, true), bufferSize);
			Info[] infos = AudioSystem.getMixerInfo();

            if ((buffer == null) || (buffer.length!=bufferSize)) {
                buffer = new float[bufferSize];

            }

			for (Info info : infos) {
				Mixer mixer = AudioSystem.getMixer(info);
				if (!mixer.isLineSupported(wanted)) {
					continue;
				}
				SourceDataLine outputLine = (SourceDataLine) mixer.getLine(wanted);
                int byteBufferSize = bufferSize * outputLine.getFormat().getFrameSize();

				if (byteBuffer == null || byteBuffer.length < byteBufferSize) {
                    byteBuffer = new byte[ byteBufferSize ];
                }



				outputLine.open();
				outputLine.start();
				try {
					while(!stopped) {
						Arrays.fill(buffer, 0.0F);
						if (timeStretcher != null) {
							timeStretcher.process(buffer);
						}
						convertFloatBufferToBytes(buffer, byteBuffer);
						outputLine.write(byteBuffer, 0, byteBufferSize);
					}
				} finally {
					outputLine.stop();
					outputLine.close();
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void convertFloatBufferToBytes(final float[] input, final byte[] output) {
		int byteIndex = 0;
        final int l = input.length;
		for(int i = 0; i < l; i++) {
			final int iSample = quantize16(input[i]*twoPower15);
            final byte b1 = (byte) (iSample >> 8);
            final byte b2 = (byte) (iSample & 0xFF);
			output[byteIndex++]=b1;
			output[byteIndex++]=b2;
			output[byteIndex++]=b1;
			output[byteIndex++]=b2;
		}
	}

	protected int quantize16(float sample) {
		if (sample>=32767.0f) {
			return 32767;
		} else if (sample<=-32768.0f) {
			return -32768;
		} else {
			return (int) (sample<0?(sample-0.5f):(sample+0.5f));
		}
	}

	private static final float twoPower15 = 32768.0f;
	private static final float invTwoPower15 = 1 / twoPower15;

	public void loadFile(InputStream fileStream) {
		AudioInputStream sample;
		try {
			sample = AudioSystem.getAudioInputStream(fileStream);
			AudioFormat format = sample.getFormat();
			int frameSize = format.getFrameSize();
			format.getEncoding();
			byte[] buffer = new byte[1024 * frameSize];
			float[] floatSample = new float[(int) sample.getFrameLength()];
			int bytesRead;
			int frame = 0;
			while ((bytesRead = sample.read(buffer)) > 0) {
				for (int offset = 0; offset < bytesRead;) {
					floatSample[frame++] = ((float) ((buffer[offset + 1] << 8) | (buffer[offset] & 0xFF))) * invTwoPower15;
					offset += frameSize;
				}
			}
			loadFile(floatSample);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void loadFile(float[] samples) {
		timeStretcher = new TimeStretcherCompact(samples, 44100, 0.08F);
	}

	public void play() {
		if (timeStretcher != null) {
			 timeStretcher.play();
		}
	}

	public void setStretchFactor(double stretchFactor) {
		if (timeStretcher != null) {
			timeStretcher.setStretchFactor((float) stretchFactor);
		}
	}

	public void stop() {
		if (timeStretcher != null) {
			timeStretcher.stop();
		}
	}

	public void destroy() {
		stopped = true;
		try {
			audioThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
