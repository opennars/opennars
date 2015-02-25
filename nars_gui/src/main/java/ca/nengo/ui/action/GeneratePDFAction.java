//package ca.nengo.ui.actions;
//
//import ca.nengo.ui.AbstractNengo;
//import ca.nengo.ui.lib.actions.StandardAction;
//import ca.nengo.ui.lib.world.piccolo.primitives.Text;
//import ca.nengo.ui.lib.world.piccolo.primitives.Universe;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfTemplate;
//import com.itextpdf.text.pdf.PdfWriter;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.geom.AffineTransform;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//
///**
// * TODO
// *
// * @author Chris Eliasmith
// */
//public class GeneratePDFAction extends StandardAction {
//    private static final long serialVersionUID = 1L;
//
//    /**
//     * @param description TODO
//     */
//    public GeneratePDFAction(String description) {
//        super(description);
//    }
//
//    protected void action() {
//    	String name = "Nengo";
//
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Save layout as PDF");
//        fileChooser.setSelectedFile(new File(name + ".pdf"));
//
//        Component ng = AbstractNengo.getInstance();
//
//        if (fileChooser.showSaveDialog(ng)==JFileChooser.APPROVE_OPTION) {
//            File file = fileChooser.getSelectedFile();
//
//            Universe universe = ((AbstractNengo) ng).getUniverse();
//            double w = universe.getSize().getWidth();
//            double h = universe.getSize().getHeight();
//
//            // Top of page method: prints to the top of the page
//            float pw = 550;
//            float ph = 800;
//
//            // create PDF document and writer
//           Document doc = new Document();
//           try{
//        	   PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
//        	   doc.open();
//
//            PdfContentByte cb = writer.getDirectContent();
//
//            // create a template
//            PdfTemplate tp = cb.createTemplate(pw,ph);
//            Graphics2D g2 = tp.createGraphicsShapes(pw,ph);
//
//            // scale the template to fit the page
//            AffineTransform at = new AffineTransform();
//            float s = (float) Math.min(pw/w,ph/h);
//            at.scale(s,s);
//            g2.setTransform(at);
//
//            // print the image to the template
//            // turning off setUseGreekThreshold allows small text to print
//            Text.setUseGreekThreshold(false);
//            universe.paint(g2);
//            Text.setUseGreekThreshold(true);
//            g2.dispose();
//
//            // add the template
//            cb.addTemplate(tp,20,0);
//
//            // clean up everything
//            doc.close();
//           } catch (DocumentException e) {
//        	   e.printStackTrace();
//           } catch (IOException e) {
//        	   e.printStackTrace();
//           }
//
//        }
//    }
//}
