package ca.nengo.ui.util;

import ca.nengo.sim.SimulatorEvent;
import ca.nengo.sim.SimulatorListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;


public class ProgressIndicator extends JPanel implements ActionListener, SimulatorListener {
	public static final long serialVersionUID=1;
	
	final JProgressBar bar;
	final JButton stop;
	
	String text;

	Thread javaThread=null;
	
	Timer timer=null;
	long timerStart;

	boolean isRunning=false;
	boolean interruptFlag;
	
	int percentage=-1;
	

	public ProgressIndicator() {
		setLayout(new BorderLayout());
		
		bar=new JProgressBar(0,100);
		bar.setStringPainted(true);
		bar.setString("");
		bar.setIndeterminate(true);				
		add(bar);
		
		stop=new JButton();
		stop.setMargin(new Insets(0,0,0,0));
		stop.setIcon(new ImageIcon("python/images/stop.png"));
		add(stop,BorderLayout.EAST);
		stop.addActionListener(this);

		setVisible(false);
		
		timer = new Timer();
	}
	
	void updateBarString() {
		String bar=this.text;
		if (isRunning) {
			long delta=(System.currentTimeMillis()-timerStart)/1000;
			bar+=" ";
			if (delta>60*60) {
				
				bar+=String.format("%02d:", delta/(60*60));
			}
			bar+=String.format("%02d:%02d", (delta%(60*60))/60,delta%60);
		}		
		if (percentage>=0) {
			bar+=String.format(" (%02d)%%", percentage);
		}
		if (interruptFlag) {
			bar="[Attempting to stop] "+bar;
		}
		this.bar.setString(bar);
	}
	
	public void start(String text) {
		timerStart=System.currentTimeMillis();
		this.text=text;
		this.isRunning=true;
		timer.schedule(
		        new TimerTask() {
					@Override
		            public void run() {
						if (!isRunning) {
							cancel();
							return;
						}
						if (!isVisible())
							setVisible(true);
						
						updateBarString();
		            }
		        }, 
		        1000,1000);		
	}
	
	public void stop() {
		if (!isRunning) return;
		
		this.setVisible(false);
		isRunning=false;
		javaThread=null;
		bar.setIndeterminate(true);		
		percentage=-1;
		
//		// TODO: figure out why this needs a 100ms delay before scrolling to the bottom
//		//        (without this delay, the console ends up a few lines above the bottom)
//		timer.schedule(
//		        new TimerTask() {
//					@Override
//		            public void run() {
//                        //NengoClassic.getInstance().getScriptConsole().scrollToBottom();
//		            }
//		        },
//		        100
//		);
		
	}
	
	
	
	public void setText(String text) {
		this.text=text;
	}
	
	
	public void setThread() {
		javaThread=Thread.currentThread();
	}

	public void interrupt() {
		
		
		interruptFlag=true;
		updateBarString();
		timer.schedule(
		        new TimerTask() {
		            @SuppressWarnings("deprecation")
					@Override
		            public void run() {
		            	if (isRunning)
		            		javaThread.stop();
		            }
		        }, 
		        3000 
		);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		interrupt();
	}

	public void processEvent(SimulatorEvent event) {
		if (!isRunning) return;
		
		percentage=(int)(100*event.getProgress());
		
		if (percentage!=bar.getValue()) {
			bar.setIndeterminate(false);
			bar.setMaximum(100);
			bar.setValue(percentage);
		}
		
		if (interruptFlag) event.setInterrupt(true);
	}
	

}



