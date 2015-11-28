//package jhelp.engine.gui;
//
//import java.util.Stack;
//
//import jhelp.gui.twoD.JHelpComponent2D;
//import jhelp.gui.twoD.JHelpContainer2D;
//import jhelp.gui.twoD.JHelpFrame2D;
//import jhelp.gui.twoD.JHelpLayout;
//
///**
// * Frame 2D that able to show {@link JHelpComponent2DView3D}
// *
// * @author JHelp
// */
//public class JHelp2DFrame
//      extends JHelpFrame2D
//{
//   /**
//    * Create a new instance of JHelp2DFrame
//    *
//    * @param title
//    *           Frame title
//    * @param layout
//    *           Layout for place components
//    */
//   public JHelp2DFrame(final String title, final JHelpLayout layout)
//   {
//      super(title, layout);
//   }
//
//   /**
//    * Called when all dialog are hide, reactivate 3D screens
//    */
//   @Override
//   protected final void allDialogAreHiden()
//   {
//      final Stack<JHelpComponent2D> stack = new Stack<JHelpComponent2D>();
//      stack.push(this.getPanelRoot());
//      JHelpComponent2D component2d;
//
//      while(stack.isEmpty() == false)
//      {
//         component2d = stack.pop();
//
//         if(component2d instanceof JHelpComponent2DView3D)
//         {
//            ((JHelpComponent2DView3D) component2d).resume();
//         }
//         else if(component2d instanceof JHelpContainer2D)
//         {
//            for(final JHelpComponent2D child : ((JHelpContainer2D) component2d).children())
//            {
//               stack.push(child);
//            }
//         }
//      }
//   }
//
//   /**
//    * Called when first dialog is shown, pause 3D screens
//    */
//   @Override
//   protected final void atLeastOneDialogIsVisible()
//   {
//      final Stack<JHelpComponent2D> stack = new Stack<JHelpComponent2D>();
//      stack.push(this.getPanelRoot());
//      JHelpComponent2D component2d;
//
//      while(stack.isEmpty() == false)
//      {
//         component2d = stack.pop();
//
//         if(component2d instanceof JHelpComponent2DView3D)
//         {
//            ((JHelpComponent2DView3D) component2d).pause();
//         }
//         else if(component2d instanceof JHelpContainer2D)
//         {
//            for(final JHelpComponent2D child : ((JHelpContainer2D) component2d).children())
//            {
//               stack.push(child);
//            }
//         }
//      }
//   }
//
//   /**
//    * Create a linked {@link JHelpComponent2DView3D}.<br>
//    * The component will be link to this frame for ever.<br>
//    * That is to say, this component must be hierachically attach to this frame
//    *
//    * @param width
//    *           Start width (Must be > 0)
//    * @param height
//    *           Start height (must be > 0)
//    * @return Created linked {@link JHelpComponent2DView3D}
//    */
//   public JHelpComponent2DView3D createJHelpComponent2DView3D(final int width, final int height)
//   {
//      if((width <= 0) || (height <= 0))
//      {
//         throw new IllegalArgumentException("width and height MUST be > 0, but its specified " + width + "x" + height);
//      }
//
//      final ComponentView3D componentView3D = new ComponentView3D(width, height);
//      componentView3D.setFocusable(true);
//      componentView3D.requestFocus();
//      componentView3D.requestFocusInWindow();
//
//      return new JHelpComponent2DView3D(width, height, componentView3D, this);
//   }
//}