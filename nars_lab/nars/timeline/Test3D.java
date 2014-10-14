package nars.timeline;

import javax.swing.JFrame;
import nars.gui.NWindow;
import org.jbox2d.common.MathUtils;
import processing.core.PApplet;

public class Test3D extends PApplet {

    float frameRate = 20f;
    private final int initialWidth;
    private final int initialHeight;

    public static void main(String[] args) {
        NWindow n = new NWindow("3D Test", new Test3D(1000, 800));
        n.pack();
        n.setVisible(true);        
        n.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public Test3D(int w, int h) {
        super();
        this.initialWidth = w;
        this.initialHeight = h;
        init();
  
    }

    public void setup() {
        size(initialWidth, initialHeight, P3D);
        noStroke();
        frameRate(frameRate);                       
    }

    protected void updateCamera() {
        float cameraY = height / 2.0f;
        float fov = /*mouseX / (float)(width) **/ PI / 2;
        float cameraZ = cameraY / tan(fov / 2.0f);
        float aspect = (float)(width)/height;

        perspective(fov, aspect, cameraZ / 10.0f, cameraZ * 10.0f);
        
    }
    
    public void draw() {
        lights();
        background(0);
        
        updateCamera();

        translate(width / 2f + 30f, height / 2f, 0);
        rotateX(-PI / 6f);
        rotateY(PI / 3f + mouseY / (float)(height) * MathUtils.PI);
        
        box(45);
        
        translate(0, 0, -50);
        box(30);
        
    }
    
    
    /*
    
    
import picking.*;

Picker picker;
float a = 0.0;

void setup() {
  size(200, 150, P3D);
  picker = new Picker(this);
}

void draw() {
  a += 0.01;

  background(255);

  picker.start(0);
  drawBox(80, 75, 50, #ff8800);

  picker.start(1);
  drawBox(140, 75, 20, #eeee00);

  picker.stop();

  color c = 0;
  int id = picker.get(mouseX, mouseY);
  switch (id) {
    case 0:
      c = #ff8800;
      break;
    case 1:
      c = #eeee00;
      break;
  }
  drawBorder(10, c);
}

void drawBox(int x, int y, int w, color c) {
  stroke(0);
  fill(c);
  pushMatrix();
    translate(x, y);
    rotateX(a); rotateY(a);
    box(w);
  popMatrix();
}

void drawBorder(int w, color c) {
  noStroke();
  fill(c);
  rect(0,   0, width, w);
  rect(0, height - w, width, w);
  rect(0,   0, w, height);
  rect(width - w, 0, w, height);
}
    */

}
