package vnc;

import automenta.vivisect.swing.NWindow;
import vnc.viewer.cli.VNCProperties;
import vnc.viewer.swing.ParametersHandler;


abstract public class VNCControl extends VNCClient {

    public VNCControl(VNCProperties param) {
        super(param);

    }

    public static void main(String[] args) {


        VNCProperties param = new VNCProperties("localhost",5091);

        ParametersHandler.completeParserOptions(param);

        param.parse(args);
        if (param.isSet(ParametersHandler.ARG_HELP)) {
            printUsage(param.optionsUsage());
            System.exit(0);
        }

        NWindow w = new NWindow("VNC", new VNCControl(param) {
            @Override public String getParameter(String p) {
                return null;
            }
        }).show(800,600,true);

    }
}
