/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.vision;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import nars.gui.NARSwing;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import nars.NAR;
import nars.entity.Sentence;
import nars.io.Answered;
import nars.io.Narsese;



public class SymRecognizer extends javax.swing.JFrame {

    private Image fitimage(Image img , int w , int h)
    {
        BufferedImage resizedimage = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedimage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0,w,h,null);
        g2.dispose();
        return resizedimage;
    }
    
    int SZ = 20;
    private void canvasMousePressed(MouseEvent evt) {
        int X = evt.getX()/10;
        int Y = evt.getY()/10;
        
        int RAD = 2;
        for(int x=X-RAD;x<X+RAD;x++) {
            for(int y=Y-RAD;y<Y+RAD;y++) {
                if(x<0 || y<0 || x>=SZ || y>=SZ) {
                    continue;
                }
                float dx = Math.abs(X-x);
                float dy = Math.abs(Y-y);
                float distance = (float) Math.sqrt(dx*dx+dy*dy);
                //distance*=distance;
                float maxDistance = (float) Math.sqrt(Math.pow(Math.abs(X-RAD - X),2)+
                                            Math.pow(Math.abs(Y-RAD - Y),2));
                //maxDistance*=maxDistance;
                float R = 255.0f - 255.0f*(distance / maxDistance);
                Color col1 = new Color(canvasIMG.getRGB(x, y));
                R*=0.1;
                R+=col1.getRed();
                if(R > 255) {
                    R = 255;
                }
                if(R < 0) {
                    R = 0;
                }

                canvasIMG.setRGB(x,y, new Color((int)R,(int)R,(int)R).getRGB());
            }
        }
        //canvasIMG.setRGB(X, Y, new Color(255,0,0).getRGB());
        jLabel1.setIcon(new ImageIcon(fitimage(canvasIMG,jLabel1.getWidth(), jLabel1.getHeight())));
        jLabel1.repaint();
    }
    
    public BufferedImage canvasIMG;
    public BufferedImage exampleIMG;
    
    public SymRecognizer() {
        initComponents();
        //NAR nar = new NAR();
        //NARSwing gui = new NARSwing(nar);
        jButton3.setForeground(Color.RED);
        exampleIMG = new BufferedImage(SZ*10,SZ*10,BufferedImage.TYPE_INT_RGB);
        canvasIMG = new BufferedImage(SZ,SZ,BufferedImage.TYPE_INT_RGB);
        //JLabel picLabel = new JLabel(new ImageIcon(canvasIMG));
        jLabel1.setIcon(new ImageIcon(fitimage(canvasIMG,jLabel1.getWidth(), jLabel1.getHeight())));
        estimate.setIcon(new ImageIcon(exampleIMG));
        jLabel1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                canvasMousePressed(evt);
            }
            public void mouseReleased(MouseEvent evt) {
                canvasMousePressed(evt);
            }  
        });
        jLabel1.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                canvasMousePressed(e);
            }
            @Override public void mouseMoved(MouseEvent e) {}
        });
       // this.jPanel1.setBackground(Color.GRAY);
       this.getContentPane().setBackground(Color.DARK_GRAY);
       jButton2.setBackground(Color.BLACK);
        jButton3.setBackground(Color.BLACK);
         addPatternButton.setBackground(Color.BLACK);
       this.setTitle("Unsupervised recognition GUI");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        inputPanel = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        inputPanel2 = new javax.swing.JTextPane();
        estimate = new javax.swing.JLabel();
        addPatternButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        invar = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(102, 204, 0));

        jLabel1.setBackground(new java.awt.Color(255, 51, 51));
        jLabel1.setText("jLabel1");

        jButton2.setText("Clear");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setActionCommand("Determine most similar saved pattern");
        jButton3.setLabel("Determine most similar saved pattern");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(inputPanel);

        jLabel2.setText("Saved patterns");

        jScrollPane3.setViewportView(inputPanel2);

        estimate.setBackground(new java.awt.Color(255, 51, 51));
        estimate.setText("jLabel1");

        addPatternButton.setText("Save pattern");
        addPatternButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPatternButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Input");

        jLabel4.setText("Input");

        jLabel5.setText("Question");

        invar.setActionCommand("Assume translation invariance");
        invar.setLabel("Assume translation invariance");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane3)
                    .addComponent(jLabel5)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(estimate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addPatternButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(invar)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(estimate, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(addPatternButton)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(invar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        canvasIMG = new BufferedImage(SZ,SZ,BufferedImage.TYPE_INT_RGB);
        jLabel1.setIcon(new ImageIcon(fitimage(canvasIMG,jLabel1.getWidth(), jLabel1.getHeight())));
        inputPanel.setText("");
        inputPanel2.setText("");
        resetDetection();
    }//GEN-LAST:event_jButton2ActionPerformed

    public void clear() {
        for(int x=0;x<SZ*10;x+=1) {
            for(int y=0;y<SZ;y+=1) {
                exampleIMG.setRGB(x, y+SZ, Color.BLACK.getRGB());
            }
        }
        estimate.repaint();
    }
    
    public void resetDetection() {
        clear();
        nar.stop();
        for(Answered ans : q) {
            ans.off();
        }
    }
    
    NAR nar = new NAR();
    NARSwing gui = new NARSwing(nar);
    ArrayList<Answered> q = new ArrayList<Answered>();
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        resetDetection();
        StringBuilder build = new StringBuilder();
        StringBuilder build2 = new StringBuilder();
        build2.append("(&|,");
        for(int x=0;x<SZ;x+=1) {
            for(int y=0;y<SZ;y+=1) {
                int used_X = x;
                int used_Y = y;
                Color col1 = new Color(canvasIMG.getRGB(x, y));
                
                float col = ((float)col1.getRed()) / 255.0f;
                if(col > 0) { 
                    float freq = 0.5f+col/2.0f;
                    if(invar.isSelected()) {
                        build.append("<p["+String.valueOf(used_X)+","+String.valueOf(used_Y)+"] --> [on]>. :|: %"+String.valueOf(freq)+"%");
                        build.append("\n");
                        build2.append("<p["+String.valueOf(used_X)+","+String.valueOf(used_Y)+"] --> [on]>,");
                    } else {
                        build.append("<p_"+String.valueOf(used_X)+"_"+String.valueOf(used_Y)+" --> [on]>. :|: %"+String.valueOf(freq)+"%");
                        build.append("\n");
                        build2.append("<p_"+String.valueOf(used_X)+"_"+String.valueOf(used_Y)+" --> [on]>,");
                    }
                }
            }
        }
        inputPanel.setText(build.toString());
        if(evt == null) {
            String s2 = build2.toString();
            s2 = s2.substring(0, s2.length()-1);
            s2=s2+")?";
            inputPanel2.setText(s2);
        }
        else {
            nar.reset();
            int u = 0;
            Map<Answered,Integer> lookup = new HashMap<Answered,Integer>();
            //Map<Integer,Integer> bestAnswer = new HashMap<Integer,Integer>();
            Map<Integer,Float> truthExp = new HashMap<Integer,Float>();
            Map<Integer,Float> truthConf = new HashMap<Integer,Float>();
            for(int i=0;i<10;i++) {
                truthExp.put(i, 0.0f);
                truthConf.put(i, 0.0f);
            }
            for(String s : questions) {
                if(s!=null) {
                    Answered cur = new Answered() {
                        @Override
                        public void onSolution(Sentence belief) {
                            //System.out.println("solution: " + belief);
                            System.out.println(belief);
                            int index = lookup.get(this);
                            float howconf = belief.truth.getConfidence();
                            float confsofar = truthConf.get(index);
                            if(howconf > confsofar) {
                                truthExp.put(index, belief.truth.getExpectation());
                                truthConf.put(index, belief.truth.getConfidence());
                                //also mark image:
                                int maxu = 0;
                                float maxexp = 0.0f;
                                for(int u=0;u<10;u++) {
                                    if(truthExp.get(u) > maxexp) {
                                        maxu = u;
                                        maxexp = truthExp.get(u);
                                    }
                                }
                                clear();
                                for(int x=0;x<SZ;x+=1) {
                                    for(int y=0;y<SZ;y+=1) {
                                        Color col = new Color(canvasIMG.getRGB(x, y));
                                        exampleIMG.setRGB(x+maxu*SZ, y+SZ, 
                                                new Color(col.getRed(),0,0).getRGB());
                                    }
                                }
                                estimate.repaint();
                            }
                        }
                    };
                    q.add(cur);
                    lookup.put(cur, u);
                    try { 
                        nar.ask(s.substring(0,s.length()-1), cur);
                    } catch (Narsese.InvalidInputException ex) {
                        Logger.getLogger(SymRecognizer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                u++;
            }
            nar.param.duration.set(1000);
            nar.param.noiseLevel.set(0);
            nar.addInput(inputPanel.getText());
            nar.start(0);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    String[] questions = new String[10];
    int k =0;
    private void addPatternButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPatternButtonActionPerformed
        invar.setEnabled(false);
        for(int x=0;x<SZ;x+=1) {
            for(int y=0;y<SZ;y+=1) {
                exampleIMG.setRGB(x+k*SZ, y, canvasIMG.getRGB(x, y));
            }
        }
        estimate.repaint();
        jButton3ActionPerformed(null);
        questions[k]=inputPanel2.getText();
        k+=1;
    }//GEN-LAST:event_addPatternButtonActionPerformed


    public static void main(String args[]) {
        NARSwing.themeInvert();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SymRecognizer().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPatternButton;
    private javax.swing.JLabel estimate;
    private javax.swing.JTextPane inputPanel;
    private javax.swing.JTextPane inputPanel2;
    private javax.swing.JCheckBox invar;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables

    
}
