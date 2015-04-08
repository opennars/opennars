/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.reinforcementlearning.domains.martialarts;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author thorsten
 */
public class World {

    public final Player rlPlayer = new Player();
    public final Player opponentPlayer = new Player();
    public final int width = 800;
    public final int height = 600;
    public BufferedImage background = null;
    public final List<Hit> hits = Collections.synchronizedList(new ArrayList<>());
}
