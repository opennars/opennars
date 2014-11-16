package nars.gui.output;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.NAR;
import nars.core.NAR.PluginState;
import nars.core.Plugin;
import nars.util.PackageUtility;

/**
 * Manages the activated set of plugins in a NAR, and a menu for adding additional ones
 * and presets of them.
 */
public class PluginPanel extends NPanel {
    private final NAR nar;
    private final JPanel plugins;
    private final JMenuBar menu;
    private final JPanel pluginsWrap;

    public PluginPanel(NAR nar) {
        super(new BorderLayout());
        this.nar = nar;
        
        
        menu = new JMenuBar();
        initMenu();
        
        plugins = new JPanel(new GridBagLayout());
        pluginsWrap = new JPanel(new BorderLayout());
        pluginsWrap.add(plugins, BorderLayout.NORTH);
        
        add(menu, BorderLayout.NORTH);
        add(new JScrollPane(pluginsWrap), BorderLayout.CENTER);
        
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
            add(j, BorderLayout.CENTER);
            
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
        }    
        
    }
    
    protected void update() {
        plugins.removeAll();
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.gridy = 0;
        
        List<PluginState> ppp = nar.getPlugins();
        if (!ppp.isEmpty()) {
            for (PluginState p : ppp) {
                PluginPane pp = new PluginPane(p);
                pp.setBorder(new BevelBorder(BevelBorder.RAISED));            
                plugins.add(pp, gc);
                gc.gridy++;
            }
        }
        else {
            plugins.add(new JLabel("(empty)"), gc);
        }
    
        
        pluginsWrap.doLayout();
        pluginsWrap.validate();
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
