/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NengoGraphics.java". Description:
"@author User"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
 */

package ca.nengo.ui;

//import java.awt.Color;

import ca.nengo.ui.action.GeneratePythonScriptAction;
import ca.nengo.ui.action.RunSimulatorAction;
import ca.nengo.ui.action.SaveNodeAction;
import ca.nengo.ui.lib.action.ActionException;
import ca.nengo.ui.lib.action.StandardAction;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.handler.SelectionHandler;
import ca.nengo.ui.model.node.UINetwork;

/**
 * Top level instance of the NeoGraphics application
 * 
 * @author Shu Wu
 */
/**
 * @author User
 */
@Deprecated class NengoGraphics extends AbstractNengo {

    //	public static final String PLUGIN_DIRECTORY = "plugins";

    /**
     * Constructor; displays a splash screen
     */
    public NengoGraphics() {
        super();

        // Setup icon
//        try {
//            Image image = ImageIO.read(getClass().getClassLoader().getResource("ca/nengo/ui/nengologo256.png"));
//            setIconImage(image);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }




    //	/**
    //	 * Register plugins
    //	 */
    //	private void registerPlugins() {
    //		try {
    //			LinkedList<URL> pluginUrls = new LinkedList<URL>();
    //			LinkedList<JarFile> pluginJars = new LinkedList<JarFile>();
    //
    //			File pluginDir = new File(PLUGIN_DIRECTORY);
    //			pluginUrls.add(pluginDir.toURI().toURL());
    //
    //			File[] pluginJarFiles = pluginDir.listFiles(new FilenameFilter() {
    //				public boolean accept(File dir, String name) {
    //					if (name.endsWith("jar")) {
    //						return true;
    //					} else {
    //						return false;
    //					}
    //				}
    //			});
    //
    //			for (File jarFile : pluginJarFiles) {
    //				pluginUrls.add(jarFile.toURI().toURL());
    //			}
    //
    //			URL[] pluginUrlsArray = pluginUrls.toArray(new URL[] {});
    //
    //			URLClassLoader urlClassLoader = new URLClassLoader(pluginUrlsArray);
    //
    //			/*
    //			 * Loads all classes in each plugin jar
    //			 */
    //			for (File jarFile : pluginJarFiles) {
    //				try {
    //					JarFile jar = new JarFile(jarFile);
    //					pluginJars.add(jar);
    //					Enumeration<JarEntry> entries = jar.entries();
    //					while (entries.hasMoreElements()) {
    //						JarEntry entry = entries.nextElement();
    //						String fileName = entry.getName();
    //						if (fileName.endsWith(".class")) {
    //							String className = "";
    //							try {
    //								className = fileName.substring(0, fileName.lastIndexOf('.')).replace('/',
    //										'.');// .replace('$',
    //								// '.');
    //								Class<?> newClass = urlClassLoader.loadClass(className);
    //
    //								// Util.debugMsg("Registering class: " +
    //								// newClass.getName());
    //								ClassRegistry.getInstance().register(newClass);
    //
    //							} catch (ClassNotFoundException e) {
    //								e.printStackTrace();
    //							} catch (NoClassDefFoundError e) {
    //								// this only occurs for nested classes (i.e.
    //								// those with dollar signs in class name),
    //								// and perhaps only on the Mac
    //
    //								// System.out.println(className);
    //								// e.printStackTrace();
    //							}
    //						}
    //					}
    //
    //					pluginUrls.add(jarFile.toURI().toURL());
    //				} catch (IOException e) {
    //					e.printStackTrace();
    //				}
    //			}
    //
    //		} catch (MalformedURLException e) {
    //			e.printStackTrace();
    //		}
    //	}



}

/**
 * Runs the closest network to the currently selected obj
 * 
 * @author Shu Wu
 */
class RunNetworkAction extends StandardAction {

    private static final long serialVersionUID = 1L;

    public RunNetworkAction(String description) {
        super(description);

    }

    @Override
    protected void action() throws ActionException {
        WorldObject selectedNode = SelectionHandler.getActiveObject();

        UINetwork selectedNetwork = UINetwork.getClosestNetwork(selectedNode);
        if (selectedNetwork != null) {

            RunSimulatorAction runAction = new RunSimulatorAction("run", selectedNetwork);
            runAction.doAction();

        } else {
            throw new ActionException("No parent network to run, please select a node");
        }
    }

}

/**
 * Saves the closest network to the currently selected object
 * 
 * @author Shu Wu
 */
class SaveNetworkAction extends StandardAction {

    private static final long serialVersionUID = 1L;

    public SaveNetworkAction(String description) {
        super(description);
    }

    @Override
    protected void action() throws ActionException {
        WorldObject selectedNode = SelectionHandler.getActiveObject();

        UINetwork selectedNetwork = UINetwork.getClosestNetwork(selectedNode);
        if (selectedNetwork != null) {

            SaveNodeAction saveNodeAction = new SaveNodeAction(selectedNetwork);
            saveNodeAction.doAction();

        } else {
            throw new ActionException("No parent network to save, please select a node");
        }
    }
}

/**
 * Generates a script for the highest network including the selected object
 * 
 * @author Chris Eliasmith
 */
class GenerateScriptAction extends StandardAction {

    private static final long serialVersionUID = 1L;

    public GenerateScriptAction(String description) {
        super(description);
    }

    @Override
    protected void action() throws ActionException {
        WorldObject selectedNode = SelectionHandler.getActiveObject();

        UINetwork selectedNetwork = UINetwork.getClosestNetwork(selectedNode);
        if (selectedNetwork != null) {

            GeneratePythonScriptAction generatePythonScriptAction = new GeneratePythonScriptAction(selectedNetwork);
            generatePythonScriptAction.doAction();

        } else {
            throw new ActionException("No parent network to save, please select a node");
        }
    }



}
