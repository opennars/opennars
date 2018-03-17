package nars.lab.microworld;

import nars.storage.Memory;
import nars.NAR;
//import nars.nal.nal8.Operation;
//import nars.nal.nal8.operator.SyncOperator;
//import nars.nar.Default;
//import nars.task.Task;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.language.Narsese.Narsese;
import nars.lab.testutils.ConceptMonitor;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

public class Pong extends Frame {

    public Pong() {
        String[] args = {"Pong"};
        MyPapplet mp = new MyPapplet ();
        mp.setSize(800,600);
        PApplet.runSketch(args, mp);
    }
    
    static MyPapplet.Obj agent;
        static MyPapplet.Obj ball;

    public class MyPapplet extends PApplet {

        float mouseScroll = 0;
        public void mouseWheel(MouseEvent event) {
            mouseScroll = -event.getCount();
            hamlib.mouseScrolled();
        }

        class Hai
        {
            Hai(){}
            public Obj obj;
            NAR nar;
            String LastInput ="";
            int nActions = 3;
            int direction = 0;
            Hai(int nactions,int nstates)
            {
                this.nActions = nactions; //for actions since we allow the same randomization phase as in QL
                nar = new NAR();
                nar.memory.addOperator(new Right("^Right"));
                nar.memory.addOperator(new Left("^Left"));
                (nar.param).noiseLevel.set(0);
                new NARSwing(nar); 
                //nar.start(0);
                Memory m = nar.memory;
               // m.conceptForgetDurations.setValue(1.0); //better for declarative reasoning tasks: 2
                //m.taskLinkForgetDurations.setValue(1.0); //better for declarative reasoning tasks: 4
                //m.termLinkForgetDurations.setValue(1.0); //better for declarative reasoning tasks: 10
                //NARide.show(nar.loop(), (i) -> {});
            }


            int lastAction=0;
            public class Right extends Operator {
                public Right(String name) {
                    super(name);
                }

                @Override
                public List<Task> execute(Operation operation, Term[] args, Memory memory) {
                    lastAction = 1;
                    memory.allowExecution = false;
                    //System.out.println("NAR decide left");
                    return null;
                }
            }
            public class Left extends Operator {
                public Left(String name) {
                    super(name);
                }

                @Override
                public List<Task> execute(Operation operation, Term[] args, Memory memory) {
                    lastAction = 2;
                    memory.allowExecution = false;
                    //System.out.println("NAR decide right");
                    return null;
                }
            }

            int k=0;
            float Alpha=0.08f;
            int UpdateSOM(float[] viewField,float reward) //input and reward
            {
                /*for(int i=0;i<viewField.length;i++) {
                    if(viewField[i]>0.1f) {
                        String s = "<{\""+String.valueOf(i)+"\"} --> [on]>. :|:";  // %"+String.valueOf(0.5f+0.5f*viewField[i])+"%";
                        if(this.LastInput != s) {
                            nar.addInput(s);
                            System.out.println(s);
                        }
                        this.LastInput = s;

                        //System.out.println("perceive "+s);
                    }
                }*/
                lastAction = 0;
                k++;
                if(k%4==0) {
                    nar.addInput("<{SELF} --> [good]>! :|:"); //future extension: change to self good
                    //System.out.println("food urge input");
                }
                //if(reward > 0) { //even more delayed reward (for the future)
                
                //show what the system perceived about the ballposition:
                /*Sentence left = ConceptMonitor.strongestProjectedInputEventBelief(nar, "<{left} --> [on]>");
                Sentence right = ConceptMonitor.strongestProjectedInputEventBelief(nar, "<{right} --> [on]>");
                Sentence middle = ConceptMonitor.strongestProjectedInputEventBelief(nar, "<{\"1\"} --> [on]>");
                if(left != null) {
                    System.out.print("left: ");
                    System.out.print(left.truth);
                    System.out.print(" ");
                }
                if(middle != null) {
                    System.out.print("middle: ");
                    System.out.print(middle.truth);
                    System.out.print(" ");
                }
                if(right != null) {
                    System.out.print("right: ");
                    System.out.print(right.truth);
                    System.out.print(" ");
                }
                if(left != null || middle != null || right != null) {
                    System.out.println();
                }*/
                                                   //                                         <(&/,<{right} --> [on]>,+12,(^Left,{SELF}),+13) =/> <{"1"} --> [on]>>
                                                   
                
                if(k%4 == 0) {
                Sentence hypo_left = ConceptMonitor.strongestPrecondition(nar, "<{SELF} --> [good]>",
                        "<(&/,<{left} --> [on]>,+1,(^Right,{SELF}),+1) =/> <{SELF} --> [good]>>");
                if(hypo_left != null) {
                    System.out.println("HypLeftWrong: " + hypo_left.truth);
                }
                
                Sentence hypo_right = ConceptMonitor.strongestPrecondition(nar, "<{SELF} --> [good]>",
                        "<(&/,<{right} --> [on]>,+12,(^Left,{SELF}),+13) =/> <{SELF} --> [good]>>");
                if(hypo_right != null) {
                    System.out.println("HypRightWrong: " + hypo_right.truth);
                }
                
                Sentence hypo_left_false = ConceptMonitor.strongestPrecondition(nar, "<{SELF} --> [good]>",
                        "<(&/,<{left} --> [on]>,+1,(^Left,{SELF}),+1) =/> <{SELF} --> [good]>>");
                if(hypo_left_false != null) {
                    System.out.println("HypLeft: " + hypo_left_false.truth);
                }
                
                Sentence hypo_right_false = ConceptMonitor.strongestPrecondition(nar,  "<{SELF} --> [good]>",
                        "<(&/,<{right} --> [on]>,+12,(^Right,{SELF}),+13) =/> <{SELF} --> [good]>>");
                if(hypo_right_false != null) {
                    System.out.println("HypRight: " + hypo_right_false.truth);
                }
                }
                
                if(k%20 == 0) {
                    this.LastInput = ""; //periodically receive new input even when not changed
                }
                
                float middle_distance = 20.0f; //approximately the drawing size of paddle plus ball radius
                if(Math.abs(agent.x - ball.x) < middle_distance) {
                    //touching the ball?
                    if(Math.abs(agent.x - ball.x) < middle_distance && ball.y < 120) { //same here
                        String s = "<{SELF} --> [good]>. :|:";
                        if(!s.equals(this.LastInput)) {
                            System.out.println("good mr_nars");
                            nar.addInput(s);
                            System.out.println(s);
                        }
                        this.LastInput = s;
                    } else {
                        String s = "<{middle} --> [on]>. :|:";
                        if(!s.equals(this.LastInput)) {
                            nar.addInput(s);
                        }
                        this.LastInput = s;
                    }
                } else {
                    if(agent.x < ball.x) {
                        String s = "<{right} --> [on]>. :|:";
                        if(!s.equals(this.LastInput)) {
                            nar.addInput(s);
                            System.out.println(s);
                        }
                        this.LastInput = s;
                    } else {
                        String s = "<{left} --> [on]>. :|:";
                        if(!s.equals(this.LastInput)) {
                            nar.addInput(s);
                            System.out.println(s);
                        }
                        this.LastInput = s;
                    }
                }
                /*/if(reward < 0) {
                    System.out.println("bad mr_nars");
                    nar.addInput("(--,<{SELF} --> [good]>). :|:");
                }*/
                
                nar.cycles(10);

                if(lastAction==0 && random(1.0f) < Alpha) { //if NAR hasn't decided chose a executable random action
                    lastAction = (int) random((float) nActions);
                    if(obj.x == 0) {
                        lastAction = 1;
                    }
                    if(obj.x == width) {
                        lastAction = 2;
                    }
                    if(lastAction == 1) {
                        //System.out.println("random left");
                        //nar.addInput("Left({SELF}). :|:");
                        
                        nar.addInput("Right({SELF}). :|:");
                    }
                    if(lastAction == 2) {
                        nar.addInput("Left({SELF}). :|:");
                        //System.out.println("random right");
                        //nar.addInput("Right({SELF}). :|:");
                    }
                }

                return lastAction;
            }
        }

        public void keyPressed()
        {
            if(key=='t')
            {
                agent.v=1.0f;
            }
            if(key=='g')
            {
                agent.v=-1.0f;
            }
            if(key=='f')
            {
                agent.a+=0.2f;
            }
            if(key=='h')
            {
                agent.a-=0.2;
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
            image(im[o.type],-o.s/2,-o.s/2,o.s,o.s);
        }
        int goods=1,bads=1;
        void hsim_Interact(Obj i,Obj j)
        {
            if(i.type==0 && j.type==1)
            {
                i.acc=1.0f;
                goods++;
            }
            if(i.type==0 && j.type==2)
            {
                i.acc=-1.0f;
                bads++;
            }
            if(j.type==1 || j.type==2)
            {
                //j.x=random(1)*width;
                //j.y=random(1)*height;
            }
        }
        int Hsim_eyesize=3; //9
        float[] viewField=new float[Hsim_eyesize*2];

        void hsim_ObjectTask(Obj oi)
        {
            oi.v=0;
            if(oi.type==2)
            {
                if(random(1)>0.5)
                {
                    // mem.ProcessingInteract(oi.x,oi.y,1.0,2.0);
                }
                oi.a+=0.05f;
            }
            if(oi.hai!=null)
            {
                for(int i=0;i<viewField.length;i++)
                {
                    viewField[i]=0;
                }
                int maxIndex=hamlib.MinMaxFrom(oi.visarea).MaxIndex;
                boolean Had=false;
                for(int i=0;i<oi.visarea.length;i++)
                {
                    if(i==maxIndex)
                    {
                        if(oi.visareatype[i]==2)
                        {
                            viewField[i]=oi.visarea[i];
                            Had=true;
                        }
                        if(oi.visareatype[i]==3)
                        {
                            viewField[i+Hsim_eyesize]=oi.visarea[i];
                            Had=true;
                        }
                    }
                }
                int action=oi.hai.UpdateSOM(viewField,oi.acc);
               /* if(!Had)
                {
                    action=0;
                }*/
                if(action==2)
                {
                    oi.hai.direction = 2;
                   // oi.x += 10;
                }
                else
                if(action==1)
                {
                    oi.hai.direction = 1;
                   // oi.x -= 10;
                }
                else
                if(action==0)
                {
                    oi.v=10.0f;
                    // mem.ProcessingInteract(oi.x,oi.y,1.0,10.0);
                }
                
                if(oi.hai.direction == 1) {
                    oi.x += 10.0f;
                } 
                else
                if(oi.hai.direction == 2) {
                    oi.x -= 10.0f;
                }
                oi.y = 0;
                
                
                if(oi.x>width)
                {
                    oi.x=width;
                }
                if(oi.x<0)
                {
                    oi.x=0;
                }
                if(oi.y>height)
                {
                    oi.y=height;
                }
                if(oi.y<0)
                {
                    oi.y=0;
                }
                oi.acc=0.0f;
                oi.v = 0;
                oi.a = (float) (Math.PI / 2.0);
            }
            else
            {
                if(oi.VX == 0 && oi.VY == 0) {
                    oi.VX = 3.0f;
                    oi.VY = 5.0f;
                }
                oi.v = 0.0f;
                if(oi.x>width)
                {
                    oi.x = width;
                    oi.VX = -oi.VX;
                }
                if(oi.x<0)
                {
                    oi.x = 0;
                    oi.VX = -oi.VX;
                }
                if(oi.y>height)
                {
                    oi.y = height;
                    oi.VY = -oi.VY;
                }
                if(oi.y<0)
                {
                    oi.y = 0;
                    oi.VY = -oi.VY;
                } 
                oi.x += oi.VX;
                oi.y += oi.VY;
            }
        }

        Obj lastclicked=null;
        void hsim_ElemClicked(Obj i)
        {
            if(lastclicked!=null)
            {
                lastclicked.DrawField=false;
            }
            lastclicked=i;
        }
        void hsim_ElemDragged(Obj i)
        {
            // mem.ProcessingInteract(i.x,i.y,1.0,3.0);
        }
        void hrend_DrawGUI()
        {
            fill(0);
            //text("viewfield and RF-Rewards:",20,20);
            //test.DrawViewFields(20,30,10);
            //test.hai.Draw(20,30,2);
        }
        void hrend_DrawBegin()
        {
            label1.text="opti index:"+((float)goods)/((float)bads)+ "FPS:"+frameRate;
            fill(138,138,128);
            pushMatrix();
            if(hamlib.Mode==hamlib.Hamlib3DMode)
            {
                translate(0,0,-2);
            }
            rect(0,0,width,height);
            popMatrix();
            //mem.DrawForProcessing(0.0f,0.0f,0.0f,0.01f,true);
        }

        void hrend_DrawEnd()
        {
            fill(0);
            //text("Hamlib simulation system demonstration",0,-5);
            stroke(255,255,255);
            line(0,0,width,0);
            line(width,height,width,0);
            line(width,height,0,height);
            line(0,0,0,height);
            noStroke();
            if(lastclicked!=null)
            {
                fill(255,0,0);
                rect(lastclicked.x,-20,5,20);
                rect(-20,lastclicked.y,20,5);
                rect(lastclicked.x,height+20,5,-20);
                rect(width+20,lastclicked.y,-20,5);
                lastclicked.DrawField=true;
                pushMatrix();
                if(hamlib.Mode==hamlib.Hamlib3DMode)
                {
                    translate(0,0,1);
                }
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
                hcam.zpos=100;
            }
            void mousePressed()
            {
                if(mouseButton==LEFT)
                {
                    checkSelect();
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
            void checkSelect()
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
                            return;
                        }
                    }
                }
            }
            float Cursor3DWidth=20;
            void DrawCursor(float x, float y)
            {
                fill(0);
                stroke(255);
                ellipse(x,y,Cursor3DWidth,Cursor3DWidth);
                noStroke();
            }
            float visarea=PI/2.01f;
            float viewdist=100.0f;

            void Simulate()
            {
                for(int i=0;i<obj.size();i++)
                {
                    Obj oi=((Obj)obj.get(i));
                    oi.a=hamlib.RadAngleRange(oi.a);
                    int Hsim_eyesize=oi.visarea.length;
                    for(int k=0;k<Hsim_eyesize;k++)
                    {
                        oi.visarea[k]=0;
                        oi.visareatype[k]=0;
                    }
                    for(int j=0;j<obj.size();j++)
                    {
                        Obj oj=((Obj)obj.get(j));
                        if(i!=j)
                        {
                            float dx=oi.x-oj.x;
                            float dy=oi.y-oj.y;
                            float d=sqrt(dx*dx+dy*dy);
                            if(oi.type==0)
                            {
                                float ati=atan2(dy,dx)+PI;
                                float diffi=hamlib.angleDiff(ati,oi.a);
                                float diffi2=hamlib.angleDiff(ati,oi.a-visarea);
                                float part=diffi/visarea;
                                float part2=diffi2/(visarea*2);
                                if(part<1.0 && d<viewdist)
                                {
                                    int index=min(Hsim_eyesize-1,max(0,(int)(part2*((float)Hsim_eyesize))));
                                    oi.visarea[index]=1.0f-d/viewdist;
                                    oi.visareatype[index]=oj.type+1;
                                }
                            }
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

                    if(oi.DrawField==true && oi.type==0)
                    {
                        stroke(255,0,0);
                        pushMatrix();
                        if(hamlib.Mode==hamlib.Hamlib3DMode)
                        {
                            translate(0,0, (float) 1.5);
                        }
                        line(oi.x,oi.y,oi.x+viewdist*cos(a+visarea),oi.y+viewdist*sin(a+visarea));
                        line(oi.x,oi.y,oi.x+viewdist*cos(a-visarea),oi.y+viewdist*sin(a-visarea));
                        popMatrix();
                        noStroke();
                    }
                    fill(255,0,0);
                    pushMatrix();
                    translate(oi.x,oi.y);
                    rotate(a+PI);
                    hsim_Draw(oi);
                    popMatrix();
                }
                if(hamlib.Mode)
                {
                    pushMatrix();
                    translate(0,0,-1);
                    DrawCursor(hnav.MouseToWorldCoordX(mouseX),hnav.MouseToWorldCoordY(mouseY));
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
        
//WaveMembran mem=new WaveMembran(100);



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
            boolean Mode; //2d or 3d
            boolean Hamlib3DMode=true;
            boolean Hamlib2DMode=false;
            void Init(boolean Mode3D)
            {
                noStroke();
                Mode=Mode3D;
                hnav.Init();
                if(Mode3D)
                {
                    hcam_Init();
                    // noCursor();
                }
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
                hcam_mouseDragged();
                hsim.mouseDragged();
            }
            void mouseReleased()
            {
                hnav.mouseReleased();
                hsim.mouseReleased();
            }
            void mouseMoved()
            {
                hcam_mouseMoved();
            }
            void keyPressed()
            {
                hnav.keyPressed();
                hcam_keyPressed();
                hgui.keyPressed();
            }
            void mouseScrolled()
            {
                hnav.mouseScrolled();
                hcam_mouseScrolled();
            }
            void Camera()
            {
                if(Mode==true)
                {
                    hcam_Transform();
                }
                else
                {
                    hnav.Transform();
                }
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



        class Hcam
        {
            Hcam(){}
            int mouse_x;
            int mouse_y;
            float xpos;
            float ypos;
            float zpos;
            float xrot;
            float yrot;
            float angle;
            float camzahigkeit;
            float speed=10.0f;
        }
        Hcam hcam=new Hcam();
        void hcam_mouseScrolled()
        {
            float mul=-1;
            if(mouseScroll>0)
            {
                mul=1;
            }
            float xrotrad, yrotrad;
            yrotrad=(float)(hcam.yrot/180*PI);
            xrotrad=(float)(hcam.xrot/180*PI);
            hcam.xpos+=mul*(float)(hcam.speed*2*sin(yrotrad));
            hcam.zpos-=mul*(float)(hcam.speed*2*cos(yrotrad));
            hcam.ypos-=mul*(float)(hcam.speed*2*sin(xrotrad));
        }
        void hcam_keyPressed()
        {
            if(key=='q')
            {
                hcam.xrot+=1;
                if(hcam.xrot>360)
                {
                    hcam.xrot-=360;
                }
            }
            if(key=='e')
            {
                hcam.xrot-=1;
                if(hcam.xrot<-360)
                {
                    hcam.xrot+=360;
                }
            }
            if(key=='s')
            {
                float xrotrad,yrotrad;
                yrotrad=(float)(hcam.yrot/180*PI);
                xrotrad=(float)(hcam.xrot/180*PI);
                hcam.xpos-=(float)(hcam.speed*sin(yrotrad));
                hcam.zpos+=(float)(hcam.speed*cos(yrotrad)) ;
                hcam.ypos+=(float)(hcam.speed*sin(xrotrad));
            }
            if(key=='w')
            {
                float xrotrad, yrotrad;
                yrotrad=(float)(hcam.yrot/180*PI);
                xrotrad=(float)(hcam.xrot/180*PI);
                hcam.xpos+=(float)(hcam.speed*sin(yrotrad));
                hcam.zpos-=(float)(hcam.speed*cos(yrotrad));
                hcam.ypos-=(float)(hcam.speed*sin(xrotrad));
            }
            if(key=='d')
            {
                hcam.yrot+=1;
                if(hcam.yrot>360)
                {
                    hcam.yrot-=360;
                }
            }
            if(key=='a')
            {
                hcam.yrot-=1;
                if(hcam.yrot<-360)
                {
                    hcam.yrot += 360;
                }
            }
        }
        int lastx=512,lasty=384;
        void hcam_mouseDragged()
        {
            if(mouseButton==CENTER)
            {
                float difx=(float)lastx-mouseX;
                float dify=(float)lasty-mouseY;

                hcam.xrot+=dify*hcam.camzahigkeit;
                if(hcam.xrot<-360)
                {
                    hcam.xrot+=360;
                }
                hcam.yrot-=difx*hcam.camzahigkeit;
                if(hcam.yrot<-360)
                {
                    hcam.yrot += 360;
                }
                lastx=mouseX;
                lasty=mouseY;
            }
        }
        void hcam_mouseMoved()
        {
            lastx=mouseX;
            lasty=mouseY;
        }

        float hcam_saved_xpos,hcam_saved_ypos,hcam_saved_zpos,hcam_saved_xrot,hcam_saved_yrot;
        void hcam_SaveCamPos()
        {
            hcam_saved_xpos=hcam.xpos;
            hcam_saved_ypos=hcam.ypos;
            hcam_saved_zpos=hcam.zpos;
            hcam_saved_xrot=hcam.xrot;
            hcam_saved_yrot=hcam.yrot;
        }
        void hcam_SetCamPos(float x,float y,float z,float xrot,float yrot)
        {
            hcam.xpos=x;
            hcam.ypos=y;
            hcam.zpos=z;
            hcam.xrot=xrot;
            hcam.yrot=yrot;
        }
        void hcam_LoadCamPos()
        {
            hcam.xpos=hcam_saved_xpos;
            hcam.ypos=hcam_saved_ypos;
            hcam.zpos=hcam_saved_zpos;
            hcam.xrot=hcam_saved_xrot;
            hcam.yrot=hcam_saved_yrot;
        }
        void hcam_Init()
        {
            hcam.xpos=width/2;
            hcam.ypos=height/2;
            hcam.zpos=0.0f;
            hcam.xrot=0.0f;
            hcam.yrot=0.0f;
            hcam.angle=0.0f;
            hcam.camzahigkeit=0.3f;
            hnav.difx=0;
            hnav.dify=0;
        }
        void hcam_Transform()
        {
            translate(width/2,height/2);
            translate(hnav.difx,hnav.dify);
            hnav.EnableZooming=false;
            rotateX(hcam.xrot/360*2*PI);
            rotateY(hcam.yrot/360*2*PI);
            translate(-hcam.xpos,-hcam.ypos,-hcam.zpos);
        }

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
                if(hamlib.Mode)
                {
                    return mouseX;
                }
                return 1/zoom*(x-difx-width/2);
            }
            float MouseToWorldCoordY(int y)
            {
                if(hamlib.Mode)
                {
                    return mouseY;
                }
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
            Hai hai=null;
            Hsim_Custom custom=null;
            Obj(Hsim_Custom customobj,Hai haiobj,int X,int Y,float A,float V,float Vx,float Vy,float S,int Type,int Hsim_eyesize)
            {
                hai=haiobj;
                custom=customobj;
                x=X;
                y=Y;
                a=A;
                v=V;
                s=S*4.0f;
                vx=Vx;
                vy=Vy;
                type=Type;
                if(Hsim_eyesize>0)
                {
                    visarea=new float[Hsim_eyesize];
                    visareatype=new float[Hsim_eyesize];
                }
            }
            void DrawViewFields(int x,int y,int RenderSize)
            {
                if(Hsim_eyesize>0)
                {
                    pushMatrix();
                    translate(x,y);
                    hamlib.Draw1DLine(visarea,10);
                    translate(0,10);
                    hamlib.Draw1DLine(visareatype,10);
                    popMatrix();
                }
            }
        }

        public void settings() {
            size(800,600);
        }

        public void setup()
        {
            this.size(800, 600);
            this.frameRate(50);
            //mem.simulate_consistency=0.05;
            //mem.simulate_damping=0.90;
            //size(worldSize-200,worldSize-200);
            hamlib.Init(false);
            im[0]=loadImage("."+File.separator+"nars_lab"+File.separator+"nars"+File.separator+"lab"+File.separator+"microworld"+File.separator+"bar.png");
            im[1]=loadImage("."+File.separator+"nars_lab"+File.separator+"nars"+File.separator+"lab"+File.separator+"microworld"+File.separator+"ball.png");
            im[2]=loadImage("."+File.separator+"nars_lab"+File.separator+"nars"+File.separator+"lab"+File.separator+"microworld"+File.separator+"fire.png");
            for(int i=0;i<1;i++)
            {
                int SomSize=10;
                Hai h=new Hai(nactions,SomSize);
                //h.som=new Hsom(SomSize,Hsim_eyesize*2);
                //h.som.Leaky=false;
                agent=new Obj(new Hsim_Custom(),h,(int)(random(1)*(double)width),(int)(random(1)*(double)height),random(1)*2*PI-PI,random(1),0,0,random(1)*5+20,0,Hsim_eyesize);
                h.obj = agent;
                hsim.obj.add(agent);
            }
            lastclicked=((Obj)hsim.obj.get(0));
            for(int i=0;i<1;i++)
            {
                ball = new Obj(null,null,width / 2,height / 2,random(1)*2*PI,0,0,0,25,1,10);
                hsim.obj.add(ball);
            }
            /*for(int i=0;i<5;i++)
            {
                hsim.obj.add(new Obj(null,null,(int)(random(1)*(double)width),(int)(random(1)*(double)height),random(1)*2*PI,0,0,0,random(1)*5+20,2,10));
            }*/
            hsim.viewdist=(float) Math.sqrt(width*width+height*height); //4
            label1=new Gui(0,height-25,width,25, "label1", "", false);
            hgui.gui.add(label1);
        }

        public void draw()
        {
            hamlib.Update(128,138,128);
            //mem.simulate(0);
        }



    }

    public static void main(String[] args) {
        NARSwing.themeInvert();
        new Pong();
    }
}
