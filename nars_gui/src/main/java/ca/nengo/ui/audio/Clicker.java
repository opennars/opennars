package ca.nengo.ui.audio;
import javax.sound.sampled.*;

public class Clicker {
	final boolean started=false;
	SourceDataLine line;
	final byte[] value=new byte[bufferSize];
	int targetBytesAvailable=5000;

	private static final int bufferSize=1000;
	private static final int Hz=44100;
	
	public Clicker() {		
	}
	
	public boolean start() {
		if (started) return false;
		AudioFormat format=new AudioFormat(Hz,8,1,false,true);
		DataLine.Info info=new DataLine.Info(SourceDataLine.class, format,bufferSize);
		if (!AudioSystem.isLineSupported(info)) return false;
		
		try {
			line=(SourceDataLine)AudioSystem.getLine(info);
			line.open(format);
		} catch (LineUnavailableException e) {
			return false;
		}
		
		line.start();
		
		return true;
	}
	
	public void setTargetBytesAvailable(int value) {
		targetBytesAvailable=value;
	}
	
	public int getTargetBytesAvailable() {
		return targetBytesAvailable;
	}
	
	
	int steps;
	public void set(byte b) {
		int avail=line.available();
		
		if (avail<targetBytesAvailable) {
			steps-=1;
		} else {
			steps+=1;
		}
		if (steps<2) steps=2;
		if (steps>=bufferSize) steps=bufferSize-1;
		
		//steps=500;

		/*
		if (avail<=0) return;
		int steps;
		
		if (avail<100) {
			steps=1;
		} else if (avail>700) {
			steps=100;
		} else {
			steps=(avail-100)/7+1;
		}
		*/
		
		/*
		steps=avail;
		if (steps<0) steps=0;
		if (steps>bufferSize) steps=bufferSize;
		*/
		//System.err.println("steps: "+steps+"  avail: "+avail);
		
		
		//value[bufferSize-1]=b;
		value[0]=b;
		line.write(value,0, steps);
		
		//line.write(value,(int)(bufferSize-steps),(int)steps);
	}
	
	public void stop() {
		line.close();
	}
	
	
}
