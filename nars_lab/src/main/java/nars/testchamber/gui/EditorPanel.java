package nars.testchamber.gui;

import nars.testchamber.Cell.Logic;
import nars.testchamber.Cell.Machine;
import nars.testchamber.Cell.Material;
import nars.testchamber.*;
import nars.testchamber.object.Key;
import nars.testchamber.object.Pizza;
import org.apache.commons.io.FileUtils;
import processing.core.PVector;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
*
* @author me
*/
public class EditorPanel extends JPanel {

    @SuppressWarnings("HardcodedFileSeparator")
    final String levelPath = "./src/main/java/nars/grid2d/level/";

    public abstract static class EditorMode extends DefaultMutableTreeNode {

        @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
        public EditorMode(String label) {
            super(label);
        }

        public abstract void run();
    }

    @SuppressWarnings("HardcodedFileSeparator")
    public EditorPanel(Grid2DSpace s) {
        super(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        DefaultMutableTreeNode structMenu = new DefaultMutableTreeNode("Structural");
        root.add(structMenu);

        DefaultMutableTreeNode logicMenu = new DefaultMutableTreeNode("Logic");
        root.add(logicMenu);

        DefaultMutableTreeNode machineMenu = new DefaultMutableTreeNode("Machine");
        root.add(machineMenu);


        DefaultMutableTreeNode actionMenu = new DefaultMutableTreeNode("Force Action");
        root.add(actionMenu);

        DefaultMutableTreeNode goalMenu = new DefaultMutableTreeNode("Request Goal");
        root.add(goalMenu);

        DefaultMutableTreeNode knowMenu = new DefaultMutableTreeNode("Predefine knowledge");
        root.add(knowMenu);

        DefaultMutableTreeNode resourceMenu = new DefaultMutableTreeNode("Need of Resources");
        root.add(resourceMenu);

        DefaultMutableTreeNode mindSettings = new DefaultMutableTreeNode("Advanced Settings");
        root.add(mindSettings);

        /*mindSettings.add(new EditorMode("Delete all desires") {
            @Override
            public void run() {
                for(Concept c : s.nar.memory.concepts) {
                    if(c.desires!=null && !c.desires.isEmpty()) {
                        c.desires.clear();
                    }
                    ArrayList<TaskLink> toDelete=new ArrayList<TaskLink>();
                    for(TaskLink T : c.taskLinks) {
                        if(T.targetTask.sentence.punctuation==Symbols.GOAL_MARK) {
                            toDelete.add(T);
                        }    
                    }
                    for(TaskLink T : toDelete) {
                        c.taskLinks.take(T);
                    }
                }
            }
        });
        */

        //noinspection CloneableClassWithoutClone
        mindSettings.add(new EditorMode("Allow joy in action") {
            @Override
            public void run() {
                Hauto.allow_imitating=true;
            }
        });

        //noinspection CloneableClassWithoutClone
        mindSettings.add(new EditorMode("Don't allow joy") {
            @Override
            public void run() {
                Hauto.allow_imitating=false;
            }
        });

        //noinspection CloneableClassWithoutClone
        mindSettings.add(new EditorMode("Tell object categories") {
            @Override
            public void run() {
                TestChamber.staticInformation=true;
            }
        });

        //noinspection CloneableClassWithoutClone
        mindSettings.add(new EditorMode("Don't tell object categories") {
            @Override
            public void run() {
                TestChamber.staticInformation=false;
            }
        });

        //noinspection CloneableClassWithoutClone
        mindSettings.add(new EditorMode("Use complex feedback") {
            @Override
            public void run() {
                TestChamber.ComplexFeedback=true;
            }
        });

        //noinspection CloneableClassWithoutClone
        mindSettings.add(new EditorMode("Don't use complex feedback") {
            @Override
            public void run() {
                TestChamber.ComplexFeedback=false;
            }
        });
        
        //ComplexFeedback


        DefaultMutableTreeNode load = new DefaultMutableTreeNode("Load Scenario");
        root.add(load);
        DefaultMutableTreeNode save = new DefaultMutableTreeNode("Save Scenario");
        root.add(save);

        //String levelPath2="C:\\Users\\patrick.hammer\\IdeaProjects\\opennars\\nars_lab\\src\\main\\java\\nars\\grid2d\\level";
        File f = new File(levelPath); // current directory
        try {
            File[] files = f.listFiles();
            for (File file : files) {
                boolean is_file = false;
                if (!file.isDirectory()) {
                    if (file.getName().endsWith(".lvl")) {
                        try {
                            String path = file.getCanonicalPath();
                            String name = file.getName();

                            //noinspection CloneableClassWithoutClone
                            load.add(new EditorMode(name) {
                                @Override
                                public void run() {
                                    String allText = null;
                                    try {
                                        allText = FileUtils.readFileToString(new File(path));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //todo: fill level according to read text
                                    String[] values = allText.split("OBJECTS")[0].split(";");
                                    for (String cell : values) {
                                        String[] c = cell.split(",");
                                        if (c.length < 14) {
                                            continue;
                                        }

                                        if (c[11] != null && !c[11].isEmpty() && !c[11].contains("{")) {
                                            c[11] = '{' + c[11] + '}';
                                        }

                                        int i = Integer.valueOf(c[0]);
                                        int j = Integer.valueOf(c[1]);
                                        s.cells.readCells[i][j].charge = Float.valueOf(c[2]);
                                        s.cells.writeCells[i][j].charge = Float.valueOf(c[2]);

                                        s.cells.readCells[i][j].chargeFront = Boolean.valueOf(c[3]);
                                        s.cells.writeCells[i][j].chargeFront = Boolean.valueOf(c[3]);

                                        s.cells.readCells[i][j].conductivity = Float.valueOf(c[4]);
                                        s.cells.writeCells[i][j].conductivity = Float.valueOf(c[4]);

                                        s.cells.readCells[i][j].height = Float.valueOf(c[5]);
                                        s.cells.writeCells[i][j].height = Float.valueOf(c[5]);

                                        s.cells.readCells[i][j].is_solid = Boolean.valueOf(c[6]);
                                        s.cells.writeCells[i][j].is_solid = Boolean.valueOf(c[6]);

                                        s.cells.readCells[i][j].light = Float.valueOf(c[7]);
                                        s.cells.writeCells[i][j].light = Float.valueOf(c[7]);

                                        s.cells.readCells[i][j].logic = Logic.values()[Integer.valueOf(c[8])];
                                        s.cells.writeCells[i][j].logic = Logic.values()[Integer.valueOf(c[8])];
                                        if (s.cells.readCells[i][j].logic == Logic.SWITCH) {
                                            if (TestChamber.staticInformation)
                                                s.nar.input('<' + c[11] + " --> switch>.");
                                        }
                                        if (s.cells.readCells[i][j].logic == Logic.OFFSWITCH) {
                                            if (TestChamber.staticInformation)
                                                s.nar.input('<' + c[11] + " --> switch>.");
                                        }

                                        if (c[9] != null && !c[9].isEmpty()) {
                                            s.cells.readCells[i][j].machine = Machine.values()[Integer.valueOf(c[9])];
                                            s.cells.writeCells[i][j].machine = Machine.values()[Integer.valueOf(c[9])];
                                            if (s.cells.readCells[i][j].machine == Machine.Turret) {
                                                if (TestChamber.staticInformation)
                                                    s.nar.input('<' + c[11] + " --> firework>.");
                                            }
                                            if (s.cells.readCells[i][j].machine == Machine.Light) {
                                                if (TestChamber.staticInformation)
                                                    s.nar.input('<' + c[11] + " --> light>.");
                                            }
                                        } else {
                                            s.cells.readCells[i][j].machine = null;
                                            s.cells.writeCells[i][j].machine = null;
                                        }

                                        s.cells.readCells[i][j].material = Material.values()[Integer.valueOf(c[10])];
                                        s.cells.writeCells[i][j].material = Material.values()[Integer.valueOf(c[10])];

                                        if (s.cells.readCells[i][j].material == Material.Door) {
                                            if (TestChamber.staticInformation)
                                                s.nar.input('<' + c[11] + " --> door>.");
                                            //s.nar.input("<"+c[11]+" --> closed>. :|:");
                                        }

                                        s.cells.readCells[i][j].name = c[11];
                                        s.cells.writeCells[i][j].name = c[11];

                                        try {
                                            if (c[11] != null && !c[11].isEmpty()) {
                                                String value = c[11].replaceAll("[A-Za-z]", "").replaceAll("\\}", "").replaceAll("\\{", "");
                                                int res = Integer.parseInt(value);
                                                if (res > Hauto.entityID) {
                                                    Hauto.entityID = res + 1;
                                                }
                                            }
                                        } catch (Exception ex) {
                                        }


                                        s.cells.readCells[i][j].value = Float.valueOf(c[12]);
                                        s.cells.writeCells[i][j].value = Float.valueOf(c[12]);

                                        s.cells.readCells[i][j].value2 = Float.valueOf(c[13]);
                                        s.cells.writeCells[i][j].value2 = Float.valueOf(c[13]);
                                    }
                                    String[] objs = allText.split("OBJECTS")[1].split(";");
                                    ArrayList<GridObject> newobj = new ArrayList<>(); //new ArrayList we have to fill
                                    for (String obj : objs) {
                                        if ("\n".equals(obj))
                                            continue;
                                        String[] val = obj.split(",");
                                        if (val.length == 0) {
                                            continue;
                                        }

                                        if (val[1] != null && !val[1].isEmpty() && !val[1].contains("{")) {
                                            val[1] = '{' + val[1] + '}';
                                        }

                                        String name = val[1];

                                        try {
                                            if (name != null && !name.isEmpty()) {
                                                String value = name.replaceAll("[A-Za-z]", "");
                                                int res = Integer.parseInt(value);
                                                if (res > Hauto.entityID) {
                                                    Hauto.entityID = res + 1;
                                                }
                                            }
                                        } catch (Exception ex) {
                                        }

                                        float cx = Float.valueOf(val[2]);
                                        float cy = Float.valueOf(val[3]);
                                        int x = Integer.valueOf(val[5]);
                                        int y = Integer.valueOf(val[6]);
                                        if ("GridAgent".equals(val[0])) {
                                            s.objects.stream().filter(z -> z instanceof GridAgent).forEach(z -> {
                                                ((GridAgent) z).cx = cx;
                                                ((GridAgent) z).cy = cy;
                                                ((GridAgent) z).x = x;
                                                ((GridAgent) z).y = y;
                                                newobj.add(z);
                                                s.target = new PVector(x, y);
                                                s.current = new PVector(x, y);
                                            });
                                        }
                                        if ("Key".equals(val[0])) {
                                            Key addu = new Key(x, y, name);
                                            if (TestChamber.staticInformation)
                                                s.nar.input('<' + name + " --> Key>.");
                                            addu.space = s;
                                            newobj.add(addu);
                                        }
                                        if ("Pizza".equals(val[0])) {
                                            Pizza addu = new Pizza(x, y, name);
                                            if (TestChamber.staticInformation)
                                                s.nar.input('<' + name + " --> pizza>.");
                                            addu.space = s;
                                            newobj.add(addu);
                                        }
                                    }
                                    s.objects = newobj;
                                }
                            });

                        } catch (IOException ex) {
                            System.out.println("not able to get path of " + file.getName());
                        }
                    }
                }
            }
        } catch(Exception ex) {}

        //noinspection CloneableClassWithoutClone
        save.add(new EditorMode("Save") {
            @Override
            public void run() {
                //todo save to new file with file name dummy_i
                String filename= JOptionPane.showInputDialog("What is the name of the setLevel?: ")+".lvl";
                filename = levelPath + filename;
                StringBuilder wr=new StringBuilder();
                for(int i=0;i<s.cells.h;i++) { //its not python, we have to export it to file ourselves:
                    for(int j=0;j<s.cells.w;j++) {
                        wr.append(i).append(','); //also store coordinates, for case we may change size one day
                        wr.append(j).append(',');
                        wr.append(s.cells.readCells[i][j].charge).append(',');
                        wr.append(s.cells.readCells[i][j].chargeFront).append(',');
                        wr.append(s.cells.readCells[i][j].conductivity).append(',');
                        wr.append(s.cells.readCells[i][j].height).append(',');
                        wr.append(s.cells.readCells[i][j].is_solid).append(',');
                        wr.append(s.cells.readCells[i][j].light).append(',');
                        wr.append(s.cells.readCells[i][j].logic.ordinal()).append(',');
                        if(s.cells.readCells[i][j].machine!=null) { //wtf enum can be null? kk its java..
                            wr.append(s.cells.readCells[i][j].machine.ordinal()).append(',');
                        }
                        else
                            wr.append(',');
                        wr.append(s.cells.readCells[i][j].material.ordinal()).append(',');
                        wr.append(s.cells.readCells[i][j].name).append(',');
                        wr.append(s.cells.readCells[i][j].value).append(',');
                        wr.append(s.cells.readCells[i][j].value2).append(';');
                    }
                }
                wr.append("OBJECTS");
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }

                for(GridObject g : s.objects) {
                    if(g instanceof LocalGridObject) {
                        LocalGridObject toSave=(LocalGridObject) g;
                        boolean export=false;
                        if(g instanceof GridAgent) {
                            export=true;
                            wr.append("GridAgent"+ ',');
                        }
                        if(g instanceof Key) {
                            export=true;
                            wr.append("Key"+ ',');
                        }
                        if(g instanceof Pizza) {
                            export=true;
                            wr.append("Pizza"+ ',');
                        }
                        wr.append(toSave.doorname).append(',');
                        wr.append(toSave.cx).append(',');
                        wr.append(toSave.cy).append(',');
                        wr.append(toSave.cheading).append(',');
                        wr.append(toSave.x).append(',');
                        wr.append(toSave.y).append(';');
                    }
                }
                try {
                    PrintWriter outw = new PrintWriter(filename);
                    outw.write(wr.toString());
                    outw.flush();
                    outw.close();
                } catch (FileNotFoundException ex) {
                    System.out.println("impossible");
                }
            }
        });

        // DefaultMutableTreeNode extraMenu = new DefaultMutableTreeNode("Extra");
        // root.add(extraMenu);

        DefaultTreeModel model = new DefaultTreeModel(root);

        JTree toolTree = new JTree(model);
        toolTree.expandRow(0);
        add(new JScrollPane(toolTree), BorderLayout.CENTER);

        toolTree.addTreeSelectionListener(e -> {
            Object o = toolTree.getLastSelectedPathComponent();
            if (o instanceof EditorMode) {
                EditorMode m = (EditorMode) o;
                m.run();
            }
        });

        //noinspection CloneableClassWithoutClone
        structMenu.add(new EditorMode("Stone Wall") {
            @Override
            public void run() {
                s.cells.click("StoneWall", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        structMenu.add(new EditorMode("Dirt Floor") {
            @Override
            public void run() {
                s.cells.click("DirtFloor", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        structMenu.add(new EditorMode("Grass Floor") {
            @Override
            public void run() {
                s.cells.click("GrassFloor", "", "");
            }
        });

        //noinspection CloneableClassWithoutClone
        structMenu.add(new EditorMode("Water") {
            @Override
            public void run() {
                s.cells.click("Water", "", "");
            }
        });

        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("On Wire") {
            @Override
            public void run() {
                s.cells.click("OnWire", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("Off Wire") {
            @Override
            public void run() {
                s.cells.click("OffWire", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("And") {
            @Override
            public void run() {
                s.cells.click("AND", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("Or") {
            @Override
            public void run() {
                s.cells.click("OR", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("Xor") {
            @Override
            public void run() {
                s.cells.click("XOR", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("Not") {
            @Override
            public void run() {
                s.cells.click("NOT", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("Bridge") {
            @Override
            public void run() {
                s.cells.click("bridge", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("Uncertain50PercentBridge") {
            @Override
            public void run() {
                s.cells.click("uncertainbridge", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("Off Switch") {
            @Override
            public void run() {
                s.cells.click("offswitch", "", "");
            }
        });
        //noinspection CloneableClassWithoutClone
        logicMenu.add(new EditorMode("On Switch") {
            @Override
            public void run() {
                s.cells.click("onswitch", "", "");
            }
        });

        //noinspection CloneableClassWithoutClone
        machineMenu.add(new EditorMode("Light") {
            @Override
            public void run() {
                s.cells.click("Light", "", "");
            }
        });
        //since firework doesnt serve a special functionality yet
        //noinspection CloneableClassWithoutClone
        machineMenu.add(new EditorMode("Firework") {
            @Override public void run() { s.cells.click("Turret","",""); }
        });

        //noinspection CloneableClassWithoutClone
        machineMenu.add(new EditorMode("Door and Key") {
            @Override
            public void run() {
                s.cells.click("Door", "", "");
            }
        });

        //noinspection CloneableClassWithoutClone
        actionMenu.add(new EditorMode("Go-To named") {

            @Override
            public void run() {
                s.cells.click("", "goto", "");
                TestChamber.active=true;
            }
        });

        //noinspection CloneableClassWithoutClone
        actionMenu.add(new EditorMode("Pick named") {
            @Override
            public void run() {
                s.cells.click("", "pick", "");
                TestChamber.active=true;
            }
        });

        //noinspection CloneableClassWithoutClone
        actionMenu.add(new EditorMode("activate switch") {
            @Override
            public void run() {
                s.cells.click("", "activate", "");
                TestChamber.active=true;
            }
        });

        //noinspection CloneableClassWithoutClone
        actionMenu.add(new EditorMode("deactivate switch") {
            @Override
            public void run() {
                s.cells.click("", "deactivate", "");
                TestChamber.active=true;
            }
        });

        //noinspection CloneableClassWithoutClone
        actionMenu.add(new EditorMode("perceive/name") {
            @Override
            public void run() {
                s.cells.click("", "perceive", "");
            }
        });

        //noinspection OverlyComplexAnonymousInnerClass,CloneableClassWithoutClone
        EditorMode wu=new EditorMode("try things") {

            @SuppressWarnings("HardcodedFileSeparator")
            @Override
            public void run() {
                TestChamber.curiousity=true;
                TestChamber.active=true;
                int cnt=0;
                for (GridObject g : s.objects) {
                    if (g instanceof LocalGridObject) {
                        LocalGridObject obi = (LocalGridObject) g;
                        if (obi instanceof Key) {
                            //s.nar.input("<(^go-to," + obi.doorname + ") =/> <Self --> [curious]>>.");
                            //s.nar.input("<(^pick," + obi.doorname + ") =/> <Self --> [curious]>>.");
                            cnt+=2;
                        }
                        if (obi instanceof Pizza) {
                            //s.nar.input("<(^go-to," + obi.doorname + ") =/> <Self --> [curious]>>.");
                            cnt+=1;
                        }
                    }
                }
                for (int i = 0; i < s.cells.w; i++) {
                    for (int j = 0; j < s.cells.h; j++) {
                        if (s.cells.readCells[i][j].name.startsWith("switch") || s.cells.readCells[i][j].name.startsWith("place")) {
                            //s.nar.input("<(^go-to," + s.cells.readCells[i][j].name + ") =/> <Self --> [curious]>>.");
                            cnt+=1;
                        }
                        if (s.cells.readCells[i][j].logic == Logic.SWITCH || s.cells.readCells[i][j].logic == Logic.OFFSWITCH) {
                            s.nar.input("<(&/,"+"goto("+s.cells.readCells[i][j].name+"),activate(" + s.cells.readCells[i][j].name + ")) =/> <Self --> [curious]>>.");
                            s.nar.input("<(&/,"+"goto("+s.cells.readCells[i][j].name+"),deactivate(" + s.cells.readCells[i][j].name + ")) =/> <Self --> [curious]>>.");
                            cnt+=1;
                        }
                    }
                }
                
                s.nar.input("<<Self --> [curious]> =/> <Self --> [exploring]>>.");
                s.nar.input("<<Self --> [curious]> =/> <Self --> [exploring]>>.");
                s.nar.input("<Self --> [curious]>!");
                s.nar.input("<Self --> [curious]>!");
                s.nar.input("<Self --> [exploring]>!");
                s.nar.input("<Self --> [exploring]>!"); //testing with multiple goals
            }
        };
        goalMenu.add(wu);

        //noinspection CloneableClassWithoutClone
        goalMenu.add(new EditorMode("be somewhere") {
            @Override
            public void run() {
                TestChamber.active=true;
                s.cells.click("", "", "at");
            }
        });

        //noinspection CloneableClassWithoutClone
        goalMenu.add(new EditorMode("hold something") {
            @Override
            public void run() {
                TestChamber.active=true;
                s.cells.click("", "", "hold");
            }
        });

        //noinspection CloneableClassWithoutClone
        goalMenu.add(new EditorMode("make switched on") {
            @Override
            public void run() {
                TestChamber.active=true;
                s.cells.click("", "", "on");
            }
        });

        //noinspection CloneableClassWithoutClone
        goalMenu.add(new EditorMode("make switched off") {
            @Override
            public void run() {
                TestChamber.active=true;
                s.cells.click("", "", "off");
            }
        });

        //noinspection CloneableClassWithoutClone
        goalMenu.add(new EditorMode("make opened") {
            @Override
            public void run() {
                TestChamber.active=true;
                s.cells.click("", "", "opened");
            }
        });

        //noinspection CloneableClassWithoutClone
        goalMenu.add(new EditorMode("make closed") {
            @Override
            public void run() {
                TestChamber.active=true;
                s.cells.click("", "", "closed");
            }
        });

        //noinspection CloneableClassWithoutClone
        goalMenu.add(new EditorMode("be chatty") {
            @Override
            public void run() {
                TestChamber.active=true;
                s.nar.input("<<$1 --> [on]> <=> <(*,$1,SHOULD,BE,SWITCHED,ON) --> sentence>>.");
                s.nar.input("<(--,<$1 --> [on]>) <=> <(*,$1,SHOULD,BE,OFF) --> sentence>>.");
                s.nar.input("<<$1 --> [opened]> <=> <(*,$1,SHOULD,BE,OPENED) --> sentence>>.");
                s.nar.input("<(--,<$1 --> [opened]>) <=> <(*,$1,SHOULD,BE,CLOSED) --> sentence>>.");
                s.nar.input("<<$1 --> [hold]> <=> <(*,$1,SHOULD,BE,HOLD) --> sentence>>.");
                s.nar.input("<<$1 --> [at]> <=> <(*,SHOULD,BE,AT,$1) --> sentence>>.");
                s.nar.input("<pick($1) <=> <(*,$1,SHOULD,BE,PICKED) --> sentence>>.");
                s.nar.input("<activate($1) <=> <(*,$1,SHOULD,BE,ACTIVE) --> sentence>>.");
                s.nar.input("<deactivate($1) <=> <(*,$1,SHOULD,BE,NOT,ACTIVE) --> sentence>>.");
                s.nar.input("<goto($1) <=> <(*,SHOULD,GO,TO,$1) --> sentence>>.");
                s.nar.input("<(&&,<$1 --> sentence>,say($1)) =/> <I --> chatty>>.");
                s.nar.input("<I --> chatty>!");
                s.nar.input("<I --> chatty>!");
                s.nar.input("<I --> chatty>!");
                s.nar.input("<I --> chatty>!");
                s.nar.input("<I --> chatty>!");
            }
        });


        //noinspection CloneableClassWithoutClone
        knowMenu.add(new EditorMode("common sense") {
            @Override
            public void run() {
                s.nar.input("<(&/,<$1 --> [at]>,pick($1)) =/> <$1 --> [hold]>>.");
                s.nar.input("<goto($1) =/> <$1 --> [at]>>.");
                s.nar.input("<(&/,<$1 --> [at]>,activate($1)) =/> <$1 --> [on]>>.");
                s.nar.input("<(&/,<$1 --> [at]>,deactivate($1)) =/> <$1 --> [on]>>. %0.00;0.90%");
                //s.nar.input("(&&,<#1 --> on>,<<#1 --> on> =/> <#2 --> on>>).");
                //s.nar.input("(&&,<#1 --> on>,<<#1 --> on> =/> <#2 --> opened>>).");
            }
        });

        //noinspection CloneableClassWithoutClone
        knowMenu.add(new EditorMode("if you go to somewhere you will be there") {
            @Override
            public void run() {
                s.nar.input("<goto($1) =/> <$1 --> [at]>>.");
            }
        });

        //noinspection CloneableClassWithoutClone
        knowMenu.add(new EditorMode("if you are somewhere and you pick whats there, you will hold it") {
            @SuppressWarnings("HardcodedFileSeparator")
            @Override
            public void run() { /*s.nar.input("<(&/,<$1 --> at>,(^pick,$1)) =/> <$1 --> hold>>."); */

                s.objects.stream().filter(g -> g instanceof LocalGridObject).forEach(g -> {
                    LocalGridObject obi = (LocalGridObject) g;
                    if (obi instanceof Key) {
                        s.nar.input("<(&/,<" + obi.doorname + " --> [at]>,pick(" + obi.doorname + ")) =/> <" + obi.doorname + " --> [hold]>>.");
                    }
                });
                /*s.nar.input("<(&/,<key0 --> at>,(^pick,key0)) =/> <key0 --> hold>>.");
                 s.nar.input("<(&/,<key1 --> at>,(^pick,key1)) =/> <key1 --> hold>>.");
                 s.nar.input("<(&/,<key2 --> at>,(^pick,key2)) =/> <key2 --> hold>>.");
                 s.nar.input("<(&/,<key3 --> at>,(^pick,key3)) =/> <key3 --> hold>>.");*/
            }
        });  //s.nar.input("<(&/,<$1 --> at>,(^pick,$1)) =/> <$1 --> hold>>.");

        //noinspection CloneableClassWithoutClone
        resourceMenu.add(new EditorMode("need pizza") {
            @Override
            public void run() {
                wu.run();
                //s.nar.input("<(&&,<$1 --> pizza>,(^go-to,$1)) =/> <$1 --> eat>>."); //also works but better:
                s.nar.input("<goto($1) =/> <$1 --> [at]>>.");
                TestChamber.needpizza=true;
            }
        });
        //noinspection CloneableClassWithoutClone
        resourceMenu.add(new EditorMode("pizza") {
            @Override
            public void run() {
                s.cells.click("Pizza", "", "");
            }
        });
    }

}
