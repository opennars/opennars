///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.rl;
//
//import automenta.vivisect.dimensionalize.HyperassociativeMap;
//import automenta.vivisect.swing.NWindow;
//import nars.build.Default;
//import nars.core.Events.ConceptForget;
//import nars.core.Events.ConceptNew;
//import nars.core.NAR;
//import nars.event.AbstractReaction;
//import nars.io.Texts;
//import nars.io.narsese.InvalidInputException;
//import nars.logic.NALOperator;
//import nars.logic.entity.CompoundTerm;
//import nars.logic.entity.Concept;
//import nars.logic.entity.Term;
//import nars.logic.entity.Variable;
//import nars.logic.nal4.Image;
//import nars.util.graph.NARGraph;
//
//import javax.swing.*;
//import javax.swing.table.AbstractTableModel;
//import javax.swing.table.DefaultTableCellRenderer;
//import javax.swing.table.TableModel;
//import javax.swing.table.TableRowSorter;
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import static java.awt.BorderLayout.CENTER;
//import static nars.logic.nal7.Tense.Eternal;
//
///**
// * TODO add existing concepts before it is added
// */
//public class TermVectors extends AbstractReaction {
//
//    NARGraph graph;
//    HyperassociativeMap map;
//    private final NAR nar;
//
//    /**
//     * edge with a value indicating relative position of a subterm (0=start,
//     * 1=end)
//     */
//    public static class ContentPosition {
//
//        public final double position;
//
//        public ContentPosition(final double p) {
//            this.position = p;
//        }
//
//        @Override
//        public String toString() {
//            return "content[" + Texts.n2(position) + "]";
//        }
//
//    }
//
//    public TermVectors(NAR n, int dimensions) {
//        super(n, ConceptNew.class, ConceptForget.class);
//        this.nar = n;
//        this.graph = new NARGraph();
//
//
//        setDimensions(dimensions);
//    }
//
//    public void setDimensions(int d) {
//        this.map = new HyperassociativeMap(graph, d) {
//
//            @Override
//            public double getEdgeWeight(Object e) {
//                if (e instanceof ContentPosition) {
//                    return 1.0 + ((ContentPosition) e).position;
//                }
//                return 1.0;
//            }
//
//        };
//    }
//
//    public NALOperator getNativeOperator(NALOperator t) {
//        if (!graph.containsVertex(t))
//            graph.addVertex(t);
//        return t;
//    }
//
//    public Term getTerm(Term t) {
//        //TODO map variables to common vertex
//        if (t instanceof Variable) {
//            t = Term.get(((Variable) t).getType() + "0");
//        }
//
//        if (!graph.containsVertex(t)) {
//            graph.addVertex(t);
//
//
//            if (t.operate()!=null)
//                graph.addEdge(getNativeOperator(t.operate()), t);
//
//            //add subcomponents
//            if (t instanceof CompoundTerm) {
//                CompoundTerm ct = (CompoundTerm) t;
//                float index = 0;
//
//                float numSubTerms = ct.term.length;
//
//                //handle Image with index as virtual term
//                if (ct instanceof Image) {
//                    numSubTerms++;
//                }
//
//                for (Term s : ct.term) {
//                    if ((ct instanceof Image) && (((Image) ct).relationIndex == index)) {
//                        index++;
//                    }
//
//                    float p = numSubTerms > 1 ? index / (numSubTerms - 1) : 0.5f;
//                    graph.addEdge(getTerm(s), ct, new ContentPosition(p));
//
//                    index++;
//                }
//            }
//        }
//
//        return t;
//    }
//
//    public void removeTerm(Term t) {
//        graph.removeVertex(t);
//    }
//
//    @Override
//    public void event(Class event, Object[] args) {
//        if (event == ConceptNew.class) {
//            getTerm(((Concept) args[0]).term);
//        } else if (event == ConceptForget.class) {
//            removeTerm(((Concept) args[0]).term);
//        }
//    }
//
//    public static void main(String[] args) throws InvalidInputException {
//        int dimensions = 1;
//
//        NAR n = new NAR(new Default());
//        TermVectors t = new TermVectors(n, dimensions);
//
//        n.believe("<a --> b>", Eternal, 1f, 0.9f);
//        n.believe("<b --> a>", Eternal, 1f, 0.9f);
//        n.believe("<(*,a,b,c) --> d>", Eternal, 1f, 0.9f);
//        n.believe("<(&&,d,b,c) --> a>", Eternal, 1f, 0.9f);
//        n.run(50);
//
//        t.map.run(300);
//
//        System.out.println(t.graph);
//        System.out.println(t.map);
//
//        Collection items = t.map.keys();
//
//        List<Object> rows = new ArrayList(items.size());
//        double[][] m = new double[items.size()][dimensions];
//        int j = 0;
//        for (Object i : items) {
//            rows.add(i);
//            m[j++] = t.map.getPosition(i).getDataRef();
//        }
//
//        //new NWindow("3", new ThreeDView(t.map)).show(600,400,true);
//
//        new NWindow("dimensionalized terms",
//                new MatrixPanel(rows, m)).show(600, 400, true);
//
//    }
//
//    public static class ColorCellRenderer extends DefaultTableCellRenderer {
//
//        boolean showLabel = false;
//
//        public ColorCellRenderer() {
//
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
//            //Cells are by default rendered as a JLabel.
//            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
//
//            if (value instanceof Number) {
//                double s = ((Number) value).doubleValue();
//
//                Color c = Color.getHSBColor((float) s * 0.5f + 0.3f, 0.9f, 0.9f);
//                if (!showLabel) {
//                    l.setText("");
//                }
//                l.setBackground(c);
//            } else {
//                l.setBackground(Color.WHITE);
//            }
//
//            //Return the JLabel which renders the cell.
//            return l;
//        }
//    }
//
//    public static class MatrixTableModel extends AbstractTableModel {
//
//        private double[][] matrix;
//        private final List<Object> rows;
//        private final double min;
//        private final double max;
//
//        public MatrixTableModel(List<Object> rows, double[][] mat, double min, double max) {
//            this.matrix = mat;
//            this.rows = rows;
//            this.min = min;
//            this.max = max;
//        }
//
//        @Override
//        public int getRowCount() {
//            return matrix.length;
//        }
//
//        @Override
//        public int getColumnCount() {
//            return matrix[0].length + 1;
//        }
//
//        @Override
//        public Object getValueAt(int rowIndex, int columnIndex) {
//            if (columnIndex == 0) {
//                return rows.get(rowIndex).toString();
//            }
//
//            float v = (float) matrix[rowIndex][columnIndex - 1];
//            if (min == max) {
//                return v;
//            }
//            float s = (float) ((v - min) / (max - min));
//            return s;
//        }
//
//        @Override
//        public String getColumnName(int columnIndex) {
//            return Integer.toString(columnIndex);
//
//        }
//
//    }
//
//    public static class MatrixPanel extends JPanel {
//
//        public MatrixPanel(List<Object> rows, double[][] matrix) {
//            super(new BorderLayout());
//
//            double min = matrix[0][0], max = matrix[0][0];
//            for (double[] x : matrix) {
//                for (double d : x) {
//                    if (d < min) {
//                        min = d;
//                    }
//                    if (d > max) {
//                        max = d;
//                    }
//                }
//            }
//
//            MatrixTableModel model = new MatrixTableModel(rows, matrix, min, max);
//            JTable table = new JTable(model);
//            table.setDefaultRenderer(Object.class, new ColorCellRenderer());
//            RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
//
//            table.setRowSorter(sorter);
//            table.setShowGrid(false);
//            JScrollPane scroll = new JScrollPane(table);
//            add(scroll, CENTER);
//        }
//
//    }
//
//}
