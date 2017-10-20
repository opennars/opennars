package nars.lab.nalnet;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import nars.gui.NARSwing;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.atan2;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.max;
import static processing.core.PApplet.min;
import static processing.core.PApplet.sin;
import static processing.core.PApplet.sqrt;
import static processing.core.PConstants.BACKSPACE;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.DOWN;
import static processing.core.PConstants.ENTER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.PI;
import static processing.core.PConstants.RIGHT;
import static processing.core.PConstants.UP;

public class NALNetEditor extends Frame {

    //TODO: Pong and NALnet use common Hamlib functions
    //it is time to factor it out to not have too much duplicate code although in nars_lab it's less an issue. :)
    
    public NALNetEditor() {
        String[] args = {"NALNet editor"};
        MyPapplet mp = new MyPapplet ();
        mp.setSize(1024,768);
        PApplet.runSketch(args, mp);
    }

    public class MyPapplet extends PApplet {

        float mouseScroll = 0;
        public void mouseWheel(MouseEvent event) {
            mouseScroll = -event.getCount();
            hamlib.mouseScrolled();
        }
        public void keyPressed()
        {
            if(key=='t')
            {
                
            }
            hamlib.keyPressed();
        }
        public void mouseMoved()
        {
            hamlib.mouseMoved();
        }
        public void mouseReleased()
        {
            hamlib.mouseReleased();
        }
        public void mouseDragged()
        {
            hamlib.mouseDragged();
        }
        public void mousePressed()
        {
            hamlib.mousePressed();
        }
        void hsim_Draw(Obj o)
        {
            //image(im[o.type],-o.s/2,-o.s/2,o.s,o.s);
            ellipse(0,0,o.s,o.s);
        }
        void hsim_Interact(Obj i,Obj j)
        {
            
        }

        void hsim_ObjectTask(Obj oi)
        {
           
        }

        Obj lastclicked=null;
        void hsim_ElemClicked(Obj i)
        {
            if(lastclicked!=null)
            {

            }
            lastclicked=i;
        }
        int rastersize = 50;
        void hsim_ElemDragged(Obj i)
        {
            i.x = ((int)i.x + rastersize/2)/rastersize*rastersize;
            i.y = ((int) i.y + rastersize/2)/rastersize*rastersize;
            // mem.ProcessingInteract(i.x,i.y,1.0,3.0);
        }
        void hrend_DrawGUI()
        {
            fill(0);
            //text("viewfield and RF-Rewards:",20,20);
            //test.DrawViewFields(20,30,10);
            //test.hai.Draw(20,30,2);
        }
        
        int worksheet_width = 10000;
        int worksheet_height = 10000;
        void hrend_DrawBegin()
        {
            label1.text="context bar";
            fill(100,100,100);
            pushMatrix();
            rect(0,0,worksheet_width,worksheet_height);
            popMatrix();
            
            stroke(255,255,255);
            int width = worksheet_width;
            int height = worksheet_height;
            line(0,0,width,0);
            line(width,height,width,0);
            line(width,height,0,height);
            line(0,0,0,height);
            
            stroke(128,128,128);
            for(int i=0;i<width;i+=rastersize) {
                line(i,0,i,height); 
            }
            for(int i=0;i<height;i+=rastersize) {
                line(0,i,width,i); 
            }
        }

        void hrend_DrawEnd()
        {
            fill(0);
            //text("Hamlib simulation system demonstration",0,-5);
            int width = worksheet_width;
            int height = worksheet_height;
            
            noStroke();
            if(lastclicked!=null)
            {
                
                fill(255,0,0);
                rect(lastclicked.x,-20,5,20);
                rect(-20,lastclicked.y,20,5);
                rect(lastclicked.x,height+20,5,-20);
                rect(width+20,lastclicked.y,-20,5);
                //lastclicked.DrawField=true;
                pushMatrix();
                ellipse(lastclicked.x,lastclicked.y,10,10);
                popMatrix();
            }
        }
        void hgui_ElemEvent(Gui i)
        {
        }

        class Hsim
        {
            Hsim(){}
            ArrayList obj=new ArrayList();
            void Init()
            {
                smooth();
            }
            void mousePressed()
            {
                if(mouseButton==LEFT)
                {
                    if(!checkSelect())
                    {
                        int x = (int) hnav.MouseToWorldCoordX(mouseX);
                        int y = (int) hnav.MouseToWorldCoordY(mouseY);
                        AddNALNode(x, y, false);
                    }
                }
            }
            boolean dragged=false;
            void mouseDragged()
            {
                if(mouseButton==LEFT)
                {
                    dragged=true;
                    dragElems();
                }
            }
            void mouseReleased()
            {
                dragged=false;
                selected=null;
            }
            Obj selected=null;
            void dragElems()
            {
                if(dragged && selected!=null)
                {
                    selected.x=hnav.MouseToWorldCoordX(mouseX);
                    selected.y=hnav.MouseToWorldCoordY(mouseY);
                    hsim_ElemDragged(selected);
                }
            }
            boolean checkSelect()
            {
                if(selected==null)
                {
                    for(int i=0;i<obj.size();i++)
                    {
                        Obj oi=(Obj)obj.get(i);
                        float dx=oi.x-hnav.MouseToWorldCoordX(mouseX);
                        float dy=oi.y-hnav.MouseToWorldCoordY(mouseY);
                        float distance=sqrt(dx*dx+dy*dy);
                        if(distance<oi.s)
                        {
                            selected=oi;
                            hsim_ElemClicked(oi);
                            return true;
                        }
                    }
                }
                return false;
            }
            float Cursor3DWidth=20;
            void DrawCursor(float x, float y)
            {
                fill(0);
                stroke(255);
                ellipse(x,y,Cursor3DWidth,Cursor3DWidth);
                noStroke();
            }

            void Simulate()
            {
                for(int i=0;i<obj.size();i++)
                {
                    Obj oi=((Obj)obj.get(i));
                    oi.a=hamlib.RadAngleRange(oi.a);
                    for(int j=0;j<obj.size();j++)
                    {
                        Obj oj=((Obj)obj.get(j));
                        if(i!=j)
                        {
                            float dx=oi.x-oj.x;
                            float dy=oi.y-oj.y;
                            float d=sqrt(dx*dx+dy*dy);
                            if(d<(oi.s+oj.s)/2.0)
                            {
                                hsim_Interact(oi,oj);
                            }
                        }
                    }
                    hsim_ObjectTask(oi);
                    float a=oi.a;
                    float cosa=cos(a);
                    float sina=sin(a);
                    oi.x+=cosa*oi.v;
                    oi.y+=sina*oi.v;
                    oi.x+=oi.vx;
                    oi.y+=oi.vy;
                    fill(255,0,0);
                    pushMatrix();
                    translate(oi.x,oi.y);
                    rotate(a+PI);
                    hsim_Draw(oi);
                    popMatrix();
                }
            }
            void DrawViewField(Obj o,int x,int y)
            {
                if(o.type==0)
                {
                    for(int i=0;i<o.visarea.length;i++)
                    {
                        fill(o.visarea[i]*255);
                        rect(10*i+x,10+y,10,10);
                        fill(o.visareatype[i]/hamlib.MinMaxFrom(o.visarea).MaxValue*255.0f);
                        rect(10*i+x,20+y,10,10);
                    }
                }
            }
        }
        Hsim hsim=new Hsim();

        class Hsim_Custom
        {
            Hsim_Custom(){}
        }

        int nactions=3;
        int worldSize=800;
        PImage[] im=new PImage[10];
        Gui label1;

        class Hamlib
        {
            Hamlib(){}
            void FillDependendOnVal(float Val)
            {
                fill(128,0,128+Val*128);
            }
            void Draw2DPlane(float[][] input,int RenderSize)
            {
                for(int i=0;i<input.length;i++)
                {
                    for(int j=0;j<input[i].length;j++)
                    {
                        FillDependendOnVal(input[i][j]);
                        rect(i*RenderSize,j*RenderSize,RenderSize,RenderSize);
                    }
                }
            }
            void Draw1DLine(float[] input,int RenderSize)
            {
                for(int i=0;i<input.length;i++)
                {
                    FillDependendOnVal(input[i]);
                    rect(i*RenderSize,0,RenderSize,RenderSize);
                }
            }
            void farrcpy(float[] a,float[] b,int sz)
            {
                for(int i=0;i<sz;i++)
                {
                    a[i]=b[i];
                }
            }
            float angleDiff(float a,float b)
            {
                return min(abs(a-b),2*PI-abs((a-b)));
            }
            float deg(float radval)
            {
                return radval/(2*PI)*360;
            }
            float rad(float degval)
            {
                return degval/360*2*PI;
            }
            float RadAngleRange(float ret)
            { //spuckt zwischen 0 und 2*PI aus
                if(ret>2*PI)
                {
                    ret-=2*PI;
                }
                if(ret<0)
                {
                    ret+=2*PI;
                }
                return ret;
            }

            MinMaxClass MinMaxFrom(float[] arr)
            {
                MinMaxClass ret=new MinMaxClass();
                for(int i=0;i<arr.length;i++)
                {
                    if(arr[i]>ret.MaxValue)
                    {
                        ret.MaxValue=arr[i];
                        ret.MaxIndex=i;
                    }
                    if(arr[i]<ret.MinValue)
                    {
                        ret.MinValue=arr[i];
                        ret.MinIndex=i;
                    }
                }
                return ret;
            }
            float Integrate(float[] arr)
            {
                float ret=0;
                for(int i=0;i<arr.length;i++)
                {
                    ret+=arr[i];
                }
                return ret;
            }
            boolean Hamlib3DMode=true;
            boolean Hamlib2DMode=false;
            void Init(boolean Mode3D)
            {
                noStroke();
                hnav.Init();
                hsim.Init();
            }
            void mousePressed()
            {
                hnav.mousePressed();
                hsim.mousePressed();
                hgui.mousePressed();
            }
            void mouseDragged()
            {
                hnav.mouseDragged();
                hsim.mouseDragged();
            }
            void mouseReleased()
            {
                hnav.mouseReleased();
                hsim.mouseReleased();
            }
            void mouseMoved()
            {
 
            }
            void keyPressed()
            {
                hnav.keyPressed();
                hgui.keyPressed();
            }
            void mouseScrolled()
            {
                hnav.mouseScrolled();
            }
            void Camera()
            {
                hnav.Transform();
            }
            void Update(int r,int g,int b)
            {
                background(r,g,b);
                pushMatrix();
                Camera();
                hrend_DrawBegin();
                Simulate();
                hrend_DrawEnd();
                popMatrix();
                Gui();
                hrend_DrawGUI();
            }
            void Gui()
            {
                hgui.Draw();
            }
            void Simulate()
            {
                hsim.Simulate();
            }
        }
        Hamlib hamlib=new Hamlib();


        float max_distance;

        class Gui
        {
            float px;
            float py;
            float sx;
            float sy;
            boolean bTextBox;
            String text;
            String name;
            Gui(float Px,float Py,float Sx,float Sy, String Name, String Text, boolean TextBox)
            {
                px=Px;
                py=Py;
                sx=Sx;
                sy=Sy;
                name=Name;
                text=Text;
                bTextBox=TextBox;
            }
        }

        class Hgui
        {
            Hgui(){}

            ArrayList gui=new ArrayList();
            Gui selected=null;
            void keyPressed()
            {
                if(selected!=null && selected.bTextBox)
                {
                    if(keyCode==BACKSPACE)
                    {
                        int len=selected.text.length();
                        if(len-1>=0)
                        {
                            selected.text=selected.text.substring(0,len-1);
                        }
                    }
                    else
                    if(keyCode==ENTER)
                    {
                        hgui_ElemEvent(selected);
                    }
                    else
                    if(key>='a' && key<'z' || key>='A' && key<'Z')
                    {
                        selected.text+=key;
                    }
                }
            }
            void mousePressed()
            {
                
                for(int i=0;i<gui.size();i++)
                {
                    Gui g=((Gui)gui.get(i));
                    if(mouseX>g.px && mouseX<g.px+g.sx && mouseY>g.py && mouseY<g.py+g.sy)
                    {
                        if(!g.bTextBox)
                        {
                            hgui_ElemEvent(g);
                        }
                        selected=g;
                    }
                }
            }
            void Draw()
            {
                for(int i=0;i<gui.size();i++)
                {
                    Gui g=((Gui)gui.get(i));
                    fill(0,0,0);
                    rect(g.px,g.py,g.sx,g.sy);
                    fill(255,255,255);
                    text(g.text,g.px+g.sx/2,g.py+g.sy/2);
                }
            }
        }
        Hgui hgui=new Hgui();



        class Hnav
        {
            Hnav(){ }
            float savepx=0;
            float savepy=0;
            int selID=0;
            float zoom=1.0f;
            float difx=0;
            float dify=0;
            int lastscr=0;
            boolean EnableZooming=true;
            float scrollcamspeed=1.1f;

            float MouseToWorldCoordX(int x)
            {
                return 1/zoom*(x-difx-width/2);
            }
            float MouseToWorldCoordY(int y)
            {
                return 1/zoom*(y-dify-height/2);
            }
            boolean md=false;
            void mousePressed()
            {
                md=true;
                if(mouseButton==RIGHT)
                {
                    savepx=mouseX;
                    savepy=mouseY;
                }
            }
            void mouseReleased()
            {
                md=false;
            }
            void mouseDragged()
            {
                if(mouseButton==RIGHT)
                {
                    difx+=(mouseX-savepx);
                    dify+=(mouseY-savepy);
                    savepx=mouseX;
                    savepy=mouseY;
                }
            }
            float camspeed=20.0f;
            float scrollcammult=0.92f;
            boolean keyToo=true;
            void keyPressed()
            {
                if((keyToo && key=='w') || keyCode==UP)
                {
                    dify+=(camspeed);
                }
                if((keyToo && key=='s') || keyCode==DOWN)
                {
                    dify+=(-camspeed);
                }
                if((keyToo && key=='a') || keyCode==LEFT)
                {
                    difx+=(camspeed);
                }
                if((keyToo && key=='d') || keyCode==RIGHT)
                {
                    difx+=(-camspeed);
                }
                if(!EnableZooming)
                {
                    return;
                }
                if(key=='-' || key=='#')
                {
                    float zoomBefore=zoom;
                    zoom*=scrollcammult;
                    difx=(difx)*(zoom/zoomBefore);
                    dify=(dify)*(zoom/zoomBefore);
                }
                if(key=='+')
                {
                    float zoomBefore=zoom;
                    zoom/=scrollcammult;
                    difx=(difx)*(zoom/zoomBefore);
                    dify=(dify)*(zoom/zoomBefore);
                }
            }
            void Init()
            {
                difx=-width/2;
                dify=-height/2;
            }
            void mouseScrolled()
            {
                if(!EnableZooming)
                {
                    return;
                }
                float zoomBefore=zoom;
                if(mouseScroll>0)
                {
                    zoom*=scrollcamspeed;
                }
                else
                {
                    zoom/=scrollcamspeed;
                }
                difx=(difx)*(zoom/zoomBefore);
                dify=(dify)*(zoom/zoomBefore);
            }
            void Transform()
            {
                translate(difx+0.5f*width,dify+0.5f*height);
                scale(zoom,zoom);
            }
        }
        Hnav hnav=new Hnav();

        class MinMaxClass
        {
            float MaxValue;
            float MinValue;
            int MaxIndex;
            int MinIndex;
            MinMaxClass()
            {
                MaxValue=-999999;
                MinValue=999999;
                MaxIndex=0;
                MinIndex=0;
            }
        }


        class Obj
        {
            Obj(){}
            float[] visarea;
            float[] visareatype;
            int type;
            float x;
            float y;
            float a;
            float v;
            float vx;
            float vy;
            float VX;
            float VY;
            float s;
            float acc;
            boolean DrawField=false;
            Obj(int X,int Y,float S)
            {    
                x=X;
                y=Y;
                a=(float) Math.PI / 2.0f;
                v=0;
                s=S*4.0f;
                vx=0;
                vy=0;
                type=0;
            }
            void DrawViewFields(int x,int y,int RenderSize)
            {
                /*pushMatrix();
                translate(x,y);
                hamlib.Draw1DLine(visarea,10);
                translate(0,10);
                hamlib.Draw1DLine(visareatype,10);
                popMatrix();*/
            }
        }

        public void settings() {
            size(1024,768);
        }

        public void AddNALNode(int x, int y, boolean negated) {
            Obj obj=new Obj(x, y, 10);
            hsim.obj.add(obj);
            hsim_ElemDragged(obj);
        }
        
        public void setup()
        {
            this.size(1024, 768);
            this.frameRate(50);
            hamlib.Init(false);
            im[0]=loadImage("."+File.separator+"nars_lab"+File.separator+"nars"+File.separator+"lab"+File.separator+"microworld"+File.separator+"bar.png");
            im[1]=loadImage("."+File.separator+"nars_lab"+File.separator+"nars"+File.separator+"lab"+File.separator+"microworld"+File.separator+"ball.png");
            im[2]=loadImage("."+File.separator+"nars_lab"+File.separator+"nars"+File.separator+"lab"+File.separator+"microworld"+File.separator+"fire.png");
            
            
            lastclicked=null; //((Obj)hsim.obj.get(0));
            label1=new Gui(0,height-25,width,25, "label1", "", false);
            hgui.gui.add(label1);
        }

        public void draw()
        {
            hamlib.Update(0,0,0);
        }
    }

    public static void main(String[] args) {
        NARSwing.themeInvert();
        new NALNetEditor();
    }
}
