package nars;

import org.apache.maven.cli.MavenCli;

/**
 * OpenNARS Build and Continuous Integration Agent
 */
public class Build {

    public static void main(String[] args) {

        MavenCli cli = new MavenCli();
        System.setProperty("maven.multiModuleProjectDirectory","/home/me/.m2");

        int result = cli.doMain(
                new String[]{
                    "test"
                },

                "/home/me/opennars",
                //"/home/me/opennars/nars_logic",

                System.out, System.out
        );
        System.out.println("result: " + result);

    }
}
