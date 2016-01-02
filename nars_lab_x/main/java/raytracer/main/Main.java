/*
 * Main.java                              STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.main;

/**
 * Dieses Hauptprogramm startet den Raytracing-Vorgang zu einer vordefinierten
 * Szene und Kamera und zeigt dabei ein Vorschaubild an.
 * 
 * @author Mathias Kosch
 * @author Sassan Torabi-Goudarzi
 */
public class Main
{
    /**
     * Hauptprogramm.
     * 
     * @param argv Argumente.
     */
    public static void main(String... argv)
    {
        /*for (int i = 0; i < 100; i++)
            System.out.println(Noise.perlin_noise_1(Math.random()*20, 1, 10));
        System.exit(0);*/
        
        
        
        // Look&Feel anpassen:
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {}
        
        (new RaytracerFrame()).setVisible(true);
    }
}