///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.demo.swing;
//
//import automenta.netention.PropertyValue;
//import automenta.netention.impl.MemoryDetail;
//import automenta.netention.impl.MemorySelf;
//import automenta.netention.demo.Demo;
//import automenta.netention.demo.Demo;
//import automenta.netention.swing.SeedSelfBuilder;
//import automenta.netention.swing.SelfBrowserPanel;
//import automenta.spacegraph.swing.SwingWindow;
//import automenta.netention.swing.widget.DetailEditPanel;
//import flexjson.JSONDeserializer;
//import flexjson.JSONSerializer;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.GridLayout;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.swing.JComponent;
//import javax.swing.JPanel;
//import javax.swing.JTabbedPane;
//import javax.swing.JTextArea;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//import javax.swing.border.LineBorder;
//
///**
// *
// * @author seh
// */
//public class RunDetailEdit implements Demo {
//
//    @Override
//    public String getDescription() {
//        return "";
//    }
//
//    @Override
//    public String getName() {
//        return "Detail Editing";
//    }
//
//
//
//    public JPanel newPanel() {
//        final Logger logger = Logger.getLogger(SelfBrowserPanel.class.getName());
//
//        MemorySelf self = new MemorySelf("me", "Me");
//        new SeedSelfBuilder().build(self);
//        logger.log(Level.INFO, "Loaded Seed Self");
//
//        final MemorySelf mSelf = self;
//
//        final MemoryDetail d = new MemoryDetail("X");
//        mSelf.addDetail(d);
//
//        final EncodingPanel ep = new EncodingPanel(d);
//
//        final DetailEditPanel views = new DetailEditPanel(mSelf, new MemoryDetail(), false, false) {
//
//            @Override
//            protected void deleteThis() {
//            }
//
//            @Override
//            protected void patternChanged() {
//            }
//        };
//        views.setBackground(Color.WHITE);
//        views.sentences.setOpaque(false);
//        views.bottomBar.setOpaque(false);
//
//        DetailEditPanel edits = new DetailEditPanel(mSelf, d, true, false) {
//
//            @Override protected void deleteThis() {
//            }
//
//            @Override
//            protected void patternChanged() {
//            }
//
//            @Override
//            protected synchronized void updateDetail() {
//                super.updateDetail();
//                ep.refresh();
//                views.setDetail(getDetail(ep.getJSON()));
//            }
//
//            @Override
//            protected JComponent getLinePanel(PropertyValue pv) {
//                JComponent c = super.getLinePanel(pv);
//                JPanel p = new JPanel(new BorderLayout());
//                p.setOpaque(true);
//                p.setBackground(getColor(pv, 0.2f, 1.0f));
//                p.setBorder(new LineBorder(getColor(pv, 0.2f, 0.8f), 2));
//                p.add(c, BorderLayout.CENTER);
//                return p;
//            }
//        };
//
//        JPanel p = new JPanel(new GridLayout(1, 2));
//        JTabbedPane s = new JTabbedPane();
//        {
//            s.addTab("Read-Only", views);
//            s.addTab("JSON", ep);
//        }
//
//        s.setBorder(new LineBorder(Color.BLACK, 13));
//        edits.setBorder(new LineBorder(Color.BLACK, 13));
//        p.add(edits);
//        p.add(s);
//        return p;
//    }
//
//    public static class EncodingPanel extends JPanel {
//
//        private final JTextArea textArea;
//        private final MemoryDetail detail;
//
//        public EncodingPanel(MemoryDetail d) {
//            super(new BorderLayout());
//
//            this.detail = d;
//
//            textArea = new JTextArea();
//            textArea.setLineWrap(true);
//            textArea.setWrapStyleWord(true);
//            add(textArea, BorderLayout.CENTER);
//
//            refresh();
//        }
//
//        public String getJSON() {
//            return textArea.getText();
//        }
//
//        public void refresh() {
//            JSONSerializer serializer = new JSONSerializer();
//            //String output = serializer.include("patterns", "properties", "whenCreated", "whenModified").prettyPrint(detail);
//            String output = serializer.include("patterns", "properties", "whenCreated", "whenModified").serialize(detail);
//            textArea.setText(output);
//        }
//    }
//
//    public static MemoryDetail getDetail(String json) {
//        MemoryDetail md = new JSONDeserializer<MemoryDetail>().deserialize(json);
//        System.out.println(md.getName());
//        System.out.println(md.getPatterns());
//        System.out.println(md.getProperties());
//        return md;
//    }
//
//    public static Color getColor(PropertyValue pv, float s, float b) {
//        float h = ((float) (pv.getProperty().hashCode() % 512)) / 512.0f;
//        return Color.getHSBColor(h, s, b);
//    }
//
//    public static void main(String[] args) {
//
//
//        SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                JPanel p = new RunDetailEdit().newPanel();
//
//                SwingWindow window = new SwingWindow(p, 900, 800, true);
//            }
//        });
//
//    }
//}
