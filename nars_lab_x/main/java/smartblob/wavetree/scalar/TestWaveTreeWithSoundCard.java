/** Ben F Rayfield offers Wavetree opensource GNU LGPL 2+ */
package smartblob.wavetree.scalar;

/** Generates audio (a simple equation of sines), buffers it with a Wave tree,
and plays it by sending bytes to the sound-card which are played on the computer's speakers.
<br><br>
If the sound skips, try increasing Java's memory. Each audio sample takes about 30 bytes.
*/
public class TestWaveTreeWithSoundCard{

	public static void main(String s[]) throws LineUnavailableException{
		System.out.println("Source-code should be in this Jar file. Unzip it.");
		WaveFactory.test(System.out);
		//valid times are now plus minus about 40 years,
		//but in this test, only plus minus a few seconds will be used
		Wave wave = WaveFactory.ampSizle(0., System.currentTimeMillis()*2);
		int bufferSize = 16 * 1024; //If sound skips, increase this or get a better sound-card
		double now = System.currentTimeMillis();
		double timeStarted = now;
		SourceDataLine sourceLine = getSourceDataLine(bufferSize);
		AudioFormat format = sourceLine.getFormat();
		System.out.println("DataLine = "+sourceLine);
		System.out.println("DataLine.getFormat() = "+format);
		double audioSampleMillis = 1000. / format.getSampleRate();
		double sinecount = 0, sinecount2 = 0;
		double duration = 10000; //would be 10 seconds if playbackSpeed was always 1
		int bal = 0;
		for(double millis=0; millis<duration; millis+=audioSampleMillis){
			double amplitude = Math.sin(sinecount += .1*Math.sin(sinecount2 += .0001))*.4;
			//amplitude must range at most -1 to 1
			Wave audioSample = WaveFactory.ampSizle(amplitude, audioSampleMillis);
			//Its inefficient to create a Wave for each audio sample.
			//Its more efficient to create a Wave of many audio samples and then merge it with the main Wave.
			//But this is just a sample code.
			wave = wave.overwrite(audioSample, now+millis);
			if(bal++ % 10 == 0) wave = wave.balanceTree();
			//must balance before tree exceeds depth 127,
			//and for efficiency, should do it more often than that
		}
		wave = wave.balanceTree();
		double playbackSpeed = .9;
		sourceLine.open(format, bufferSize);
		sourceLine.start();
		System.out.println("Opened and started DataLine");
		double playingTime = now; //not syncronized with System.currentTimeMillis() because that doesnt have enough accuracy
		double doubleArray[] = new double[1024]; //audio samples
		byte audioData[] = new byte[doubleArray.length*2];
		boolean bigEndian = format.isBigEndian();
		try{
			while(true){
				int bytesNotUsedInSoundCardBuffer = sourceLine.available();
				if(playbackSpeed != 1.5 && playingTime > timeStarted+duration/2){
					System.out.println("WaveTree test is half over. Increasing playback speed.");
					playbackSpeed = 1.5;
				}
				if(playingTime > timeStarted+duration){
					System.out.println("WaveTree test ended. You should have heard a smooth sound that changed frequencies.");
					return;
				}
				if(bytesNotUsedInSoundCardBuffer > audioData.length){
					double dStart = playingTime;
					double dSize = doubleArray.length*audioSampleMillis*playbackSpeed;
					double dIncrement = dSize/doubleArray.length;
					for(int i=0; i<doubleArray.length; i++){
						//doubleArray[i] = wave.amp(dStart+i*dIncrement);
						//doubleArray[i] = wave.sub(dStart+i*dIncrement, dStart+(i+1)*dIncrement).aveAmp();
						double x = dStart+i*dIncrement;
						doubleArray[i] = wave.sub(x, x+dIncrement).aveAmp();
					}
					for(int i=0; i<doubleArray.length; i++){
						double d = doubleArray[i];
						d *= Short.MAX_VALUE;
						short sh = (short) d;
						audioData[i+i] = (byte)(sh/256);
						audioData[i+i+1] = (byte)(sh-256*audioData[i+i]);
						if(!bigEndian){
							byte temp = audioData[i+i];
							audioData[i+i] = audioData[i+i+1];
							audioData[i+i+1] = temp;
						}
						//audioData[i+i+1] = (byte) 0;
						//FIXME: use correct big endian mapping. dont divide.
					}
					sourceLine.write(audioData, 0, audioData.length);
					playingTime += dSize;
				}else{
					try{
						Thread.sleep(50);
					}catch(InterruptedException e){}
				}
			}
		}finally{
			sourceLine.close();
		}
	}

	static SourceDataLine getSourceDataLine(int bufferSize) throws LineUnavailableException{
		float sampleRate = 22050;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,format,bufferSize);
		try{
			return (SourceDataLine) AudioSystem.getLine(info);
		}catch(LineUnavailableException e){
			e.printStackTrace();
			Line.Info[] lineInfos = AudioSystem.getSourceLineInfo(info);
			if(lineInfos.length == 0) throw new LineUnavailableException("Cant get any SourceDataLine");
			return (SourceDataLine) AudioSystem.getLine(lineInfos[0]);
		}
	}

}
