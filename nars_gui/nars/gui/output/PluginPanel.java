package nars.gui.output;

import automenta.vivisect.Video;
import automenta.vivisect.swing.ReflectPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import nars.util.EventEmitter.EventObserver;
import nars.util.Events;
import nars.NAR;
import nars.NAR.PluginState;
import nars.util.Plugin;
import nars.gui.util.PackageUtility;

/**
 * Manages the activated set of plugins in a NAR, and a menu for adding additional ones
 * and presets of them.
 */
public class PluginPanel extends VerticalPanel {
    private final NAR nar;
    private final JMenuBar menu;
    

    public PluginPanel(NAR nar) {
        super();
        
        this.nar = nar;
        
        
        menu = new JMenuBar();
        initMenu();
        
        
        add(menu, BorderLayout.NORTH);
        
        update();
        
    }
    
    protected void initMenu() {
        menu.add(new JLabel(" + "));
        
        TreeMap<String, JMenu> menus = new TreeMap();
        try {
            TreeSet<Class> plugins = new TreeSet<>(new Comparator<Class>() {
                @Override public int compare(Class o1, Class o2) {
                    return o1.getSimpleName().compareTo(o2.getSimpleName());
                }                
            });
            plugins.addAll(PackageUtility.getClasses("nars.plugin", false));
            for (Class c : plugins) {
                if (!Plugin.class.isAssignableFrom(c))
                    continue;
                
                String[] p = c.getPackage().getName().split("\\.");
                String category = p[2];
                JMenu j = menus.get(category);
                if (j == null) {
                    j = new JMenu(category);
                    menus.put(category, j);
                }
                JMenuItem x = newAddPluginItem(c);
                j.add(x);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PluginPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (JMenu j : menus.values()) {
            menu.add(j);
        }
        
    }

    
    public class PluginPane extends JPanel {
        private final PluginState plugin;

        public PluginPane(PluginState p) {
            super(new BorderLayout());
            
            this.plugin = p;
            final JLabel j = new JLabel(p.plugin.name().toString());
            j.setFont(Video.monofont);            
            add(j, BorderLayout.NORTH);
            
            JPanel buttons = new JPanel(new FlowLayout());
            add(buttons, BorderLayout.EAST);
            
            JCheckBox e = new JCheckBox();
            e.setSelected(p.isEnabled());
            e.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent ae) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            boolean s = e.isSelected();
                            p.setEnabled(s);
                        }                        
                    });
                }
            });
            buttons.add(e);
            
            JButton removeButton = new JButton("X");
            removeButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent ae) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            removePlugin(plugin);
                        }                        
                    });
                }
            });
            
            buttons.add(removeButton);            
            
            
            add(new ReflectPanel(p.plugin), BorderLayout.CENTER);
        }    
        
    }
    

    
    protected void update() {
        content.removeAll();
        
        int i = 0;
        List<PluginState> ppp = nar.getPlugins();
        if (!ppp.isEmpty()) {
            for (PluginState p : ppp) {
                PluginPane pp = new PluginPane(p);
                pp.setBorder(new BevelBorder(BevelBorder.RAISED));            
                addPanel(i++, pp);
            }
        }
        else {
            addPanel(i++, new JLabel("(No plugins active.)"));
        }
    
        
        contentWrap.doLayout();
        contentWrap.validate();
    }
    

    @Override
    public void onShowing(boolean b) {
        nar.memory.event.set(new EventObserver() {
            @Override public void event(Class event, Object[] arguments) {
                if (event == Events.PluginsChange.class)
                    update();
            }            
        }, b, Events.PluginsChange.class);
    }

    private JMenuItem newAddPluginItem(Class c) {
        String name = c.getSimpleName();
        JMenuItem j = new JMenuItem(name);
        j.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                addPlugin(c);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        update();
                    }                    
                });
            }            
        });
        return j;
    }
    
    protected void addPlugin(Class c) {
        try {
            Plugin p = (Plugin)c.newInstance();
            nar.addPlugin(p);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.toString());
        }
    }
    protected void removePlugin(PluginState ps) {
        nar.removePlugin(ps);
    }
    
    
    
    
}
