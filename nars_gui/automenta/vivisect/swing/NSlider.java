/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package automenta.vivisect.swing;

import automenta.vivisect.Video;
import com.google.common.util.concurrent.AtomicDouble;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;



/**
 *
 * @author me
 */
public class NSlider extends JLabel implements MouseListener, MouseMotionListener {
    public final AtomicDouble value;
    protected float min;
    protected float max;
    protected Color barColor = null;
    protected Color backgroundColor = Color.BLACK;
    protected boolean dragging;
    protected NumberFormat nf = NumberFormat.getInstance();
    protected String prefix = "";
    
    public NSlider() {
        this(0,0,0);
    }
    
    public NSlider(float initialValue, float min, float max) {
        this(new AtomicDouble(initialValue), min, max);
    }

    public NSlider(AtomicDouble value, String prefix, float min, float max) {
        this(value, min, max);
        this.prefix = prefix;        
    }
    
    public NSlider(AtomicDouble value, float min, float max) {
        super();
        
        
        nf.setMaximumFractionDigits(3);
        
        this.value = value;        
        this.min = min;
        this.max = max;
        setBorder(new LineBorder(Color.WHITE));
        
        addMouseListener(this);
        addMouseMotionListener(this);
        
        setFont(Video.monofont.deriveFont(13f));
        
    }

    public float value() { return value.floatValue(); }
        
    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        g.setPaintMode();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, w, h);

        float p = (value.floatValue() - min) / (max-min);
        if (barColor == null) {
            //Green->Yellow->Red
            //g.setColor(Color.getHSBColor( (1f - (float)p) / 3.0f , 0.2f, 0.9f));
             g.setColor(Color.getHSBColor( (1f - (float)p) / 3.0f , 0.2f, 0.8f + 0.15f));
            
        }
        else {
            g.setColor(barColor);
        }
        
        int wp = (int)(((float)w) * p );
        g.setColor(barColor);
        g.fillRect(0, 0, wp, h);
        
        g.setXORMode(Color.BLACK);        
        super.paint(g);
    }
    
    public void onValueUpdated() {
        setText(getText());
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    @Override
    public String getText() {
        if (value!=null)
            return prefix + " " + nf.format(value.floatValue());
        return "";
    }
    

    

    protected void updatePosition(int x) {
        float p = ((float)x) / ((float)getWidth());
        float v = p * (max-min) + min;
        v = Math.max(v, min);
        v = Math.min(v, max);        
        setValue(v);
        repaint();
    }
    
    
    
    public void setValue(float v) {
        if (v != value.floatValue()) {
            value.set( v );     
            onChange(v);
        }
    }

    public void setMin(float min) {
        this.min = min;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }
    
    
    
    public void onChange(float v) {
        
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragging = true;

        updatePosition(e.getX());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        updatePosition(e.getX());
        dragging = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updatePosition(e.getX());        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (dragging) {
            updatePosition(e.getX());
        }
    }


}
