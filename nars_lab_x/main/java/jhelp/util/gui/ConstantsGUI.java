package jhelp.util.gui;

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 * Common constants for GUI
 * 
 * @author JHelp
 */
public interface ConstantsGUI
{
   /** Identity transform */
   public static final AffineTransform   AFFINE_TRANSFORM    = new AffineTransform();
   /** Flatness to use */
   public static final double            FLATNESS            = 0.01;
   /** Font render context */
   public static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(ConstantsGUI.AFFINE_TRANSFORM, true, false);
}