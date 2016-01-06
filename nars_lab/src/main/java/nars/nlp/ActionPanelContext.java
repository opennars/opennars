//package nars.io.nlp;
//
//import javax.swing.*;
//import javax.swing.plaf.basic.BasicComboBoxRenderer;
//import javax.swing.table.AbstractTableModel;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//
//public class ActionPanelContext {
//    public static class Item {
//        public EnumTypeType type;
//        public String[] parameters;
//        public boolean isNegated;
//    }
//    
//    private static class MyTableModel extends AbstractTableModel {
//        private ActionPanelContext context;
//        
//        public MyTableModel(ActionPanelContext context) {
//            this.context = context;
//        }
//        
//        @Override
//        public Object getValueAt(int row, int col) {
//            Item item = context.items.get(row);
//            
//            if (col == 1) {
//                return item.isNegated;
//            }
//            else if(col == 2) {
//                return new Boolean(false);
//            }
//            // else
//            
//            return getTextOfItem(item);
//        }
//
//        @Override
//        public int getRowCount() {
//            return context.items.size();
//        }
//
//        @Override
//        public int getColumnCount() {
//            return 3;
//        }
//        
//        private String getTextOfItem(Item item) {
//            TypeInfo typeInfoForItem = TYPES.get(item.type);
//            
//            String result = typeInfoForItem.typeAsText;
//            result += " ";
//            
//            int lastParameterIndex = item.parameters.length-1;
//            
//            for (int i = 0; i < item.parameters.length; i++) {
//                result += item.parameters[i];
//                
//                if (i != lastParameterIndex) {
//                    result += " ";
//                }
//            }
//            
//            return result;
//        }
//        
//        public Class getColumnClass(int c) {
//            if (c==1 || c==2) {
//                return Boolean.class;
//            }
//            else {
//                return String.class;
//            }
//        }
//        
//        public boolean isCellEditable(int row, int col) {
//            return col == 2;
//        }
//        
//        public void setValueAt(Object value, int row, int col) {
//            if (col == 2) {
//                context.removeRow(row);
//            }
//        }
//    }
//    
//    private static class ComboboxItemRenderer extends BasicComboBoxRenderer
//    {
//        public Component getListCellRendererComponent(
//            JList list, Object value, int index,
//            boolean isSelected, boolean cellHasFocus)
//        {
//            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//            
//            EnumTypeType type = (EnumTypeType)value;
//            
//            if (value != null) {
//                setText( TYPES.get(type).typeAsText );
//            }
//            
//            return this;
//        }
//    }
//    
//    private enum EnumTypeType {
//        METAPROXIMITY,
//        METAINSIDE,
//        METAMOTION,
//        METAONSTAGE,
//        CUSTOM
//    }
//    
//    /**
//     * info class for the action types
//     * 
//     */
//    public static class TypeInfo {
//        public int numberOfParameters = -1; // -1 is not specified/dynamic
//        public String typeAsText;
//        
//        public TypeInfo(String typeAsText, int numberOfParameters) {
//            this.numberOfParameters = numberOfParameters;
//            this.typeAsText = typeAsText;
//        }
//    }
//    
//    static Map<EnumTypeType, TypeInfo> TYPES;
//    static {
//        Map<EnumTypeType, TypeInfo> tmp = new LinkedHashMap<EnumTypeType, TypeInfo>();
//        tmp.put(EnumTypeType.METAPROXIMITY, new TypeInfo("meta-proximity", 2));
//        tmp.put(EnumTypeType.METAINSIDE, new TypeInfo("meta-inside", 2));
//        tmp.put(EnumTypeType.METAMOTION, new TypeInfo("meta-motion", 1));
//        tmp.put(EnumTypeType.METAONSTAGE, new TypeInfo("meta-onstage", 1));
//        tmp.put(EnumTypeType.CUSTOM, new TypeInfo("custom", -1));
//        TYPES = Collections.unmodifiableMap(tmp);
//    }
//    
//    private static class TransferButtonListener implements ActionListener {
//        public TransferButtonListener(ActionPanelContext context) {
//            this.context = context;
//        }
//        
//        private ActionPanelContext context;
//        
//        public void actionPerformed (ActionEvent ae){
//            context.transferPressed();
//        }
//    }
//    
//    private static class ComboBoxUpdateListener implements ActionListener {
//        public void actionPerformed(ActionEvent e) {
//            JComboBox cb = (JComboBox)e.getSource();
//            EnumTypeType type = (EnumTypeType)cb.getSelectedItem();
//            cb.setSelectedItem(TYPES.get(type).typeAsText);
//        }
//    }
//    
//    public JPanel panel;
//    public ArrayList<Item> items = new ArrayList<>();
//    
//    private JTextField textfieldParameters[];
//    private JComboBox typeDropdown;
//    private JTable actionTable;
//    private JCheckBox negatedAction = new JCheckBox("Negated");
//    
//    public void transferPressed() {
//        EnumTypeType selectedType = (EnumTypeType)typeDropdown.getSelectedItem();
//        TypeInfo typeInfoForSelectedType = TYPES.get(selectedType);
//        
//        Item createdItem = new Item();
//        createdItem.type = selectedType;
//        createdItem.isNegated = negatedAction.isSelected();
//        
//        // if there can be zero or many parameters
//        if (typeInfoForSelectedType.numberOfParameters == -1) {
//            int numberOfParameters = 0;
//            
//            // count number of parameters
//            for (int i = 0; i < textfieldParameters.length; i++) {
//                if (textfieldParameters[i].getText().isEmpty()) {
//                    break;
//                }
//                
//                numberOfParameters++;
//            }
//            
//            createdItem.parameters = new String[numberOfParameters];
//            
//            // transfer
//            for (int i = 0; i < createdItem.parameters.length; i++) {
//                createdItem.parameters[i] = textfieldParameters[i].getText();
//            }
//        }
//        else
//        {
//            createdItem.parameters = new String[typeInfoForSelectedType.numberOfParameters];
//            
//            // check if parameters are not empty
//            for (int i = 0; i < createdItem.parameters.length; i++) {
//                if (textfieldParameters[i].getText().isEmpty()) {
//                    // we just return because it is invalid to add this item
//                    return;
//                }
//            }
//            
//            for (int i = 0; i < createdItem.parameters.length; i++) {
//                createdItem.parameters[i] = textfieldParameters[i].getText();
//            }
//        }
//        
//        items.add(createdItem);
//        
//        resetTextfieldAndDropdown();
//        updateList();
//    }
//    
//    public static ActionPanelContext createPanelContext(NlpStoryGui storyGui) {
//        ActionPanelContext resultPanelContext = new ActionPanelContext();
//        
//        resultPanelContext.panel = new JPanel();
//        
//        
//        // dropdown and parameter panel
//        
//        EnumTypeType[] possibleActionTypes = {EnumTypeType.METAPROXIMITY, EnumTypeType.METAINSIDE, EnumTypeType.METAMOTION, EnumTypeType.METAONSTAGE, EnumTypeType.CUSTOM};
//        resultPanelContext.typeDropdown = new JComboBox(possibleActionTypes);
//        resultPanelContext.typeDropdown.addActionListener(new ComboBoxUpdateListener());
//        
//        resultPanelContext.typeDropdown.setRenderer(new ComboboxItemRenderer());
//        resultPanelContext.typeDropdown.setPreferredSize(new Dimension(120, 20));
//        
//        resultPanelContext.textfieldParameters = new JTextField[2];
//        resultPanelContext.textfieldParameters[0] = new JTextField();
//        resultPanelContext.textfieldParameters[1] = new JTextField();
//        
//        
//        JPanel dropdownAndParameterPanel = new JPanel();
//        dropdownAndParameterPanel.setLayout(new GridLayout(4, 1, 0, 8));
//        dropdownAndParameterPanel.add(resultPanelContext.typeDropdown);
//        dropdownAndParameterPanel.add(resultPanelContext.textfieldParameters[0]);
//        dropdownAndParameterPanel.add(resultPanelContext.textfieldParameters[1]);
//        dropdownAndParameterPanel.add(resultPanelContext.negatedAction);
//        
//        ////////
//        JPanel leftPanel = new JPanel();
//        leftPanel.setLayout(new BorderLayout());
//        
//        //JButton applyButton = new JButton("apply");
//        JButton transferButton = new JButton(">>");
//        transferButton.addActionListener(new TransferButtonListener(resultPanelContext));
//        
//        //leftPanel.add(applyButton, BorderLayout.SOUTH);
//        leftPanel.add(transferButton, BorderLayout.EAST);
//        leftPanel.add(dropdownAndParameterPanel, BorderLayout.CENTER);
//        
//        BorderLayout mainLayout = new BorderLayout();
//        
//        resultPanelContext.actionTable = createActionTable(resultPanelContext.items, resultPanelContext);
//        
//        resultPanelContext.panel.setLayout(mainLayout);
//        resultPanelContext.panel.add(leftPanel, BorderLayout.WEST);
//        resultPanelContext.panel.add(resultPanelContext.actionTable, BorderLayout.CENTER);
//        
//        return resultPanelContext;
//    }
//    
//    // called from table handler
//    public void removeRow(int index) {
//        items.remove(index);
//        updateList();
//    }
//    
//    public void updateList() {
//        actionTable.updateUI();
//    }
//    
//    private static JTable createActionTable(ArrayList<Item> items, ActionPanelContext context) {
//        MyTableModel tableModel = new MyTableModel(context);
//
//        JTable table = new JTable(tableModel);
//
//        table.updateUI();
//        
//        return table;
//    }
//    
//    private void resetTextfieldAndDropdown() {
//        typeDropdown.setSelectedIndex(0);
//        
//        for (int i = 0; i < textfieldParameters.length; i++) {
//            textfieldParameters[i].setText("");
//        }
//    }
//    
//    
// }
