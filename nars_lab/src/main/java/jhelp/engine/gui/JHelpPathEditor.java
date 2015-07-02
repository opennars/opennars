//package jhelp.engine.gui;
//
//import jhelp.engine.twoD.Path;
//import jhelp.gui.twoD.JHelpActionListener;
//import jhelp.gui.twoD.JHelpBorderLayout;
//import jhelp.gui.twoD.JHelpBorderLayout.JHelpBorderLayoutConstraints;
//import jhelp.gui.twoD.JHelpButtonBehavior;
//import jhelp.gui.twoD.JHelpComponent2D;
//import jhelp.gui.twoD.JHelpLabelImage2D;
//import jhelp.gui.twoD.JHelpPanel2D;
//import jhelp.gui.twoD.JHelpSeparator2D;
//import jhelp.gui.twoD.JHelpSpinner2D;
//import jhelp.gui.twoD.JHelpSpinner2DListener;
//import jhelp.gui.twoD.JHelpVerticalLayout;
//import jhelp.gui.twoD.JHelpVerticalLayout.JHelpVerticalLayoutConstraints;
//import jhelp.util.gui.JHelpFont;
//import jhelp.util.gui.JHelpTextAlign;
//
///**
// * Path editor 2D component
// *
// * @author JHelp
// */
//public class JHelpPathEditor
//      extends JHelpPanel2D
//{
//   /**
//    * Manage user events
//    *
//    * @author JHelp
//    */
//   class EventManager
//         implements JHelpSpinner2DListener<Integer>, JHelpActionListener
//   {
//      /**
//       * Create a new instance of EventManager
//       */
//      EventManager()
//      {
//      }
//
//      /**
//       * Called when a button (Component with button behavior) is clicked <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param component2d
//       *           Component clicked
//       * @param identifier
//       *           Action identifier
//       * @see jhelp.gui.twoD.JHelpActionListener#actionAppend(jhelp.gui.twoD.JHelpComponent2D, int)
//       */
//      @Override
//      public void actionAppend(final JHelpComponent2D component2d, final int identifier)
//      {
//         switch(identifier)
//         {
//            case ID_ADD_LINE:
//               JHelpPathEditor.this.labelPath.addLine();
//            break;
//            case ID_ADD_CUBIC:
//               JHelpPathEditor.this.labelPath.addCubic();
//            break;
//            case ID_ADD_QUADRIC:
//               JHelpPathEditor.this.labelPath.addQuadric();
//            break;
//            case ID_REMOVE_SELECTED:
//               JHelpPathEditor.this.labelPath.removeSelectedElement();
//            break;
//         }
//      }
//
//      /**
//       * Called when a spinner change of value <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param spinner2d
//       *           Spinner that value changed
//       * @param newValue
//       *           New spinner value
//       * @see jhelp.gui.twoD.JHelpSpinner2DListener#spinnerValueChanged(jhelp.gui.twoD.JHelpSpinner2D, Object)
//       */
//      @Override
//      public void spinnerValueChanged(final JHelpSpinner2D<Integer> spinner2d, final Integer newValue)
//      {
//         JHelpPathEditor.this.labelPath.setPrecision(newValue);
//      }
//   }
//
//   /** Reptition delay for change automaticaly precision if user maitain mouse button down on spinner button in millisecond */
//   private static final int       DELAY_PRECISION    = 185;
//   /** Font to use */
//   private static final JHelpFont FONT               = new JHelpFont("Arial", 21);
//   /** Add cubic element action ID */
//   public static final int        ID_ADD_CUBIC       = 0xFFFFFFF9;
//   /** Add line element action ID */
//   public static final int        ID_ADD_LINE        = 0xFFFFFFF8;
//   /** Add quadric element action ID */
//   public static final int        ID_ADD_QUADRIC     = 0xFFFFFFF7;
//   /** Remove selected element action ID */
//   public static final int        ID_REMOVE_SELECTED = 0xFFFFFFF6;
//   /** Manage user events */
//   private final EventManager     eventManager;
//   /** Additional panel */
//   private final JHelpPanel2D     panel2dAdditonal;
//   /** Label that shows the path and react to mouse events */
//   JHelpLabelPath                 labelPath;
//   /** Spinner for choose the precision */
//   JHelpSpinner2D<Integer>        spinner2dPrecision;
//
//   /**
//    * Create a new instance of JHelpPathEditor
//    */
//   public JHelpPathEditor()
//   {
//      super(new JHelpBorderLayout());
//
//      this.eventManager = new EventManager();
//
//      this.labelPath = new JHelpLabelPath();
//      this.addComponent2D(this.labelPath, JHelpBorderLayoutConstraints.CENTER);
//
//      this.panel2dAdditonal = new JHelpPanel2D(new JHelpVerticalLayout());
//      this.panel2dAdditonal.addComponent2D(this.createButton("Add line", JHelpPathEditor.ID_ADD_LINE), JHelpVerticalLayoutConstraints.EXPANDED);
//      this.panel2dAdditonal.addComponent2D(this.createButton("Add quadric", JHelpPathEditor.ID_ADD_QUADRIC), JHelpVerticalLayoutConstraints.EXPANDED);
//      this.panel2dAdditonal.addComponent2D(this.createButton("Add cubic", JHelpPathEditor.ID_ADD_CUBIC), JHelpVerticalLayoutConstraints.EXPANDED);
//      this.panel2dAdditonal.addComponent2D(this.createButton("Delete", JHelpPathEditor.ID_REMOVE_SELECTED), JHelpVerticalLayoutConstraints.EXPANDED);
//      this.spinner2dPrecision = JHelpSpinner2D.createSpinerInteger(2, 128, this.labelPath.getPrecision());
//      this.spinner2dPrecision.setReapeatDelay(JHelpPathEditor.DELAY_PRECISION);
//      this.spinner2dPrecision.setSpinner2dListener(this.eventManager);
//      this.panel2dAdditonal.addComponent2D(this.spinner2dPrecision, JHelpVerticalLayoutConstraints.CENTER);
//      this.panel2dAdditonal.addComponent2D(new JHelpSeparator2D(), JHelpVerticalLayoutConstraints.EXPANDED);
//      this.addComponent2D(this.panel2dAdditonal, JHelpBorderLayoutConstraints.RIGHT);
//   }
//
//   /**
//    * Create a button
//    *
//    * @param text
//    *           Button text
//    * @param id
//    *           Action ID
//    * @return Created button
//    */
//   private JHelpComponent2D createButton(final String text, final int id)
//   {
//      final JHelpLabelImage2D labelImage2D = JHelpLabelImage2D.createTextLabel(text, JHelpPathEditor.FONT, 0xFF000000, 0xFFFFFF, JHelpTextAlign.CENTER);
//      JHelpButtonBehavior.giveButtonBehavior(id, labelImage2D, this.eventManager);
//      return labelImage2D;
//   }
//
//   /**
//    * Add an additional component (Use for additional actions).<br>
//    * Each additional components are add one bellow the other
//    *
//    * @param component2d
//    *           Component to add
//    * @param constraints
//    *           Constraints to use
//    */
//   public void addAdditonalComponent(final JHelpComponent2D component2d, final JHelpVerticalLayoutConstraints constraints)
//   {
//      this.panel2dAdditonal.addComponent2D(component2d, constraints);
//   }
//
//   /**
//    * Current edited path
//    *
//    * @return Current edited path
//    */
//   public Path getPath()
//   {
//      return this.labelPath.getPath();
//   }
//
//   /**
//    * Current precision
//    *
//    * @return Current precision
//    */
//   public int getPrecision()
//   {
//      return this.labelPath.getPrecision();
//   }
//
//   /**
//    * Change/define the path to edit
//    *
//    * @param path
//    *           New path to edit
//    */
//   public void setPath(final Path path)
//   {
//      this.labelPath.setPath(path);
//   }
//
//   /**
//    * Change/define the precision
//    *
//    * @param precision
//    *           New precision
//    */
//   public void setPrecision(final int precision)
//   {
//      this.labelPath.setPrecision(precision);
//      this.spinner2dPrecision.setValue(this.labelPath.getPrecision(), true);
//   }
//}