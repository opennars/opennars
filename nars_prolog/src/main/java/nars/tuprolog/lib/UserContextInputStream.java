package nars.tuprolog.lib;

import nars.tuprolog.event.ReadEvent;
import nars.tuprolog.event.ReadListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class UserContextInputStream extends InputStream {
        
        private boolean avalaible;
        private boolean start;
        private int i;
        private InputStream result;
        /**
         * Changed from a single EventListener to multiple (ArrayList) ReadListeners
         *
         */
        private ArrayList<ReadListener> readListeners;
        /***/
        
        public UserContextInputStream()
        {
        		this.avalaible = false;
                this.start = true;
                this.readListeners = new ArrayList<>();
        }

        public synchronized InputStream getInput()
        {
                while (avalaible == false){
                        try {
                                wait();
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
                avalaible = false;
                notifyAll();
                return this.result;
        }
        
        public synchronized void putInput(InputStream input)
        {
                while (avalaible == true){
                        try {
                                wait();
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
                if(this.result != input)
                {
                        
                }
                this.result = input;
                avalaible = true;
                notifyAll();
        }
        
        public void setCounter(){
                start = true;
                result = null;
        }

        public int read() throws IOException
        {
        	if(start)
        	{
        		fireReadCalled();
        		getInput();
        		start = false;
        	}

        	do {
        		try {
        			i = result.read();

        			if(i == -1)
        			{
        				fireReadCalled();
        				getInput();
        				i = result.read();
        			}
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	} while (i < 0x20 && i >= -1);  

        	return i;                                       
        }
        
        /**
         * Changed these methods because there are more readListeners
         * from the previous version
         */
        private void fireReadCalled()
        {
                ReadEvent event = new ReadEvent(this);
               
                for(ReadListener r:readListeners){
                        r.readCalled(event);
                }
                
        }
        
        public void setReadListener(ReadListener r)
        {
                this.readListeners.add(r);
        }
        /***/
}