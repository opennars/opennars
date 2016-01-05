package jurls.reinforcementlearning.domains.arcade.agents;

import java.io.File;
import java.io.IOException;

/**
 * Created by me on 5/20/15.
 */
public enum RunAgent {
    ;


    @SuppressWarnings("HardcodedFileSeparator")
    public static void main(String[] args) throws IOException {


        String rom = "space_invaders";
        String romPath = "/home/me/roms";
        String alePath = "/home/me/neuro/ale_0.4.4/ale_0_4";

        String aleCommand = alePath + "/ale -game_controller fifo_named " + romPath + '/' + rom + ".bin";
        System.out.println(aleCommand);

        Process proc = Runtime.getRuntime().exec(aleCommand, new String[] {} , new File(alePath));



        // Parameters; default values
        boolean useGUI = true;
        String namedPipesName = alePath + "/ale_fifo_";

        //HumanAgent agent = new HumanAgent(useGUI, namedPipesName, false);
        RLAgent agent = new RLAgent(useGUI, namedPipesName);

        agent.run();

        //proc.waitFor();
        System.in.read();
    }

}
