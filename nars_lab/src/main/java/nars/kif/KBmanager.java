package nars.kif;

/**
 * This code is copyright Articulate Software (c) 2003. Some portions copyright
 * Teknowledge (c) 2003 and reused under the terms of the GNU license. This
 * software is released under the GNU Public License
 * <http://www.gnu.org/copyleft/gpl.html>. Users of this code also consent, by
 * use of this code, to credit Articulate Software and Teknowledge in any
 * writings, briefings, publications, presentations, or other representations of
 * any software which incorporates, builds on, or uses this code. Please cite
 * the following article in any publication with references:
 *
 * Pease, A., (2003). The Sigma Ontology Development Environment, in Working
 * Notes of the IJCAI-2003 Workshop on Ontology and Distributed Systems, August
 * 9, Acapulco, Mexico.
 */
import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * This is a class that manages a group of knowledge bases. It should only have
 * one instance, contained in its own static member variable.
 */
public class KBmanager {

    /**
     * ***************************************************************
     * A numeric (bitwise) constant used to signal whether type prefixes
     * (sortals) should be added during formula preprocessing.
     */
    public static final int USE_TYPE_PREFIX = 1;

    /**
     * ***************************************************************
     * A numeric (bitwise) constant used to signal whether holds prefixes should
     * be added during formula preprocessing.
     */
    public static final int USE_HOLDS_PREFIX = 2;

    /**
     * ***************************************************************
     * A numeric (bitwise) constant used to signal whether the closure of
     * instance and subclass relastions should be "cached out" for use by the
     * logic engine.
     */
    public static final int USE_CACHE = 4;

    /**
     * ***************************************************************
     * A numeric (bitwise) constant used to signal whether formulas should be
     * translated to TPTP format during the processing of KB constituent files.
     */
    public static final int USE_TPTP = 8;

    private static KBmanager manager = new KBmanager();
    private static final String CONFIG_FILE = "config.xml";

    private final HashMap preferences = new HashMap();
    protected HashMap kbs = new HashMap();
    private final boolean initialized = false;
    private int oldInferenceBitValue = -1;
    private String error = "";

    /**
     * ***************************************************************
     * Set an error string for file loading.
     */
    public void setError(String er) {
        error = er;
    }

    /**
     * ***************************************************************
     * Get the error string for file loading.
     */
    public String getError() {
        return error;
    }

    /**
     * ***************************************************************
     * Set default attribute values if not in the configuration file.
     */
    private void setDefaultAttributes() {

        try {
            String sep = File.separator;
            String base = System.getenv("SIGMA_HOME");
            String tptpHome = System.getenv("TPTP_HOME");
            if (base == null || base.isEmpty()) {
                base = System.getProperty("user.dir");
            }
            if (tptpHome == null || tptpHome.isEmpty()) {
                tptpHome = System.getProperty("user.dir");
            }
            System.out.println("INFO in KBmanager.setDefaultAttributes(): base == " + base);
            String tomcatRoot = System.getenv("CATALINA_HOME");
            System.out.println("INFO in KBmanager.setDefaultAttributes(): CATALINA_HOME == " + tomcatRoot);
            if ((tomcatRoot == null) || tomcatRoot.isEmpty()) {
                tomcatRoot = System.getProperty("user.dir");
            }
            File tomcatRootDir = new File(tomcatRoot);
            File baseDir = new File(base);
            File tptpHomeDir = new File(tptpHome);
            File kbDir = new File(baseDir, "KBs");
            File inferenceTestDir = new File(kbDir, "tests");
            // The links for the test results files will be broken if
            // they are not put under [Tomcat]/webapps/sigma.
            // Unfortunately, we don't know where [Tomcat] is.
            File testOutputDir = new File(tomcatRootDir, ("webapps" + sep + "sigma" + sep + "tests"));
            preferences.put("baseDir", baseDir.getCanonicalPath());
            preferences.put("tptpHomeDir", tptpHomeDir.getCanonicalPath());
            preferences.put("kbDir", kbDir.getCanonicalPath());
            preferences.put("inferenceTestDir", inferenceTestDir.getCanonicalPath());
            preferences.put("testOutputDir", testOutputDir.getCanonicalPath());
            // No way to determine the full inferenceEngine path without
            // asking the user.
            preferences.put("inferenceEngine", "kif");
            preferences.put("loadCELT", "no");
            preferences.put("showcached", "yes");
            preferences.put("typePrefix", "yes");
            preferences.put("holdsPrefix", "no");  // if no then instantiate variables in predicate position
            preferences.put("cache", "no");
            preferences.put("TPTP", "yes");
            preferences.put("port", "8080");
            preferences.put("hostname", "localhost");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * ***************************************************************
     * Read an XML-formatted configuration file. The method initializeOnce()
     * sets the preferences based on the contents of the configuration file.
     * This routine has the side effect of setting the variable called
     * "configuration". It also creates the KBs directory and an empty
     * configuration file if none exists.
     */
    /*private SimpleElement readConfiguration() throws IOException {

     SimpleElement configuration = null;
     System.out.println("INFO in KBmanager.readConfiguration()"); 
     StringBuilder xml = new StringBuilder();
     String kbDirStr = (String) preferences.get("kbDir");
     File kbDir = null;
     if ((kbDirStr == null) || kbDirStr.isEmpty()) {
     kbDirStr = System.getProperty("user.dir");
     }
     kbDir = new File(kbDirStr);
     if (!kbDir.exists()) {
     kbDir.mkdir();
     preferences.put("kbDir", kbDir.getCanonicalPath());
     }
     File configFile = new File(kbDir, CONFIG_FILE);
     if (!configFile.exists()) {
     writeConfiguration();
     }

     BufferedReader br = null;
     try {
     br = new BufferedReader(new FileReader(configFile));
     SimpleDOMParser sdp = new SimpleDOMParser();
     configuration = sdp.parse(br);
     }
     catch (java.io.IOException e) {
     System.out.println("Error in KBmanager.readConfiguration(): IO exception parsing file " + 
     configFile.getCanonicalPath() + "\n" + e.getMessage());
     }
     finally {
     if (br != null) {
     try {
     br.close();
     }
     catch (Exception ex) {
     }
     }
     }
     return configuration;
     }
     */
    /**
     * ***************************************************************
     * Double the backslash in a filename so that it can be saved to a text file
     * and read back properly.
     */
    @SuppressWarnings("HardcodedFileSeparator")
    public static String escapeFilename(String fname) {

        StringBuilder newstring = new StringBuilder("");

        for (int i = 0; i < fname.length(); i++) {
            if (fname.charAt(i) == 92 && fname.charAt(i + 1) != 92) {
                newstring = newstring.append("\\\\");
            }
            if (fname.charAt(i) == 92 && fname.charAt(i + 1) == 92) {
                newstring = newstring.append("\\\\");
                i++;
            }
            if (fname.charAt(i) != 92) {
                newstring = newstring.append(fname.charAt(i));
            }
        }
        return newstring.toString();
    }

    /**
     * ***************************************************************
     * Create a new empty KB with a name.
     *
     * @param name - the name of the KB
     */
    public void addKB(String name) {

        KB kb = new KB(name, (String) preferences.get("kbDir"));
        kbs.put(name.intern(), kb);
        System.out.println("INFO in KBmanager.addKB: Adding KB: " + name);
    }

    /**
     * ***************************************************************
     * Remove a knowledge base.
     *
     * @param name - the name of the KB
     */
    public void removeKB(String name) {

        KB kb = (KB) kbs.get(name);
        if (kb == null) {
            error = "KB " + name + " does not exist and cannot be removed.";
            return;
        }
        //try {
        if (kb.inferenceEngine != null) {
            kb.inferenceEngine.terminate();
        }
        /*}
         catch (IOException ioe) {
         System.out.println("Error in KBmanager.removeKB(): Error terminating logic engine: " + ioe.getMessage());
         }*/
        kbs.remove(name);
        //try {
        writeConfiguration();
        /*}
         catch (IOException ioe) {
         System.out.println("Error in KBmanager.removeKB(): Error writing configuration file. " + ioe.getMessage());
         }*/

        System.out.println("INFO in KBmanager.removeKB: Removing KB: " + name);
    }

    /**
     * ***************************************************************
     * Write the current configuration of the system. Call writeConfiguration()
     * on each KB object to write its manifest.
     */
    public void writeConfiguration() {
//
//        FileWriter fw = null;
//        PrintWriter pw = null;
//        Iterator it; 
//        String dir = (String) preferences.get("kbDir");
//        File fDir = new File(dir);
//        File file = new File(fDir, CONFIG_FILE);
//        String key;
//        String value;
//        KB kb = null;
//
//        SimpleElement configXML = new SimpleElement("configuration");
//
//        it = preferences.keySet().iterator();
//        while (it.hasNext()) {
//            key = (String) it.next();
//            value = (String) preferences.get(key);
//            //System.out.println("INFO in KBmanager.writeConfiguration(): key, value: " + key + " " + value);
//            if (key.compareTo("kbDir") == 0 || key.compareTo("celtdir") == 0 || 
//                key.compareTo("inferenceEngine") == 0 || key.compareTo("inferenceTestDir") == 0)
//                value = escapeFilename(value);
//            if (key.compareTo("userName") != 0) {
//                SimpleElement preference = new SimpleElement("preference");
//                preference.setAttribute("name",key);
//                preference.setAttribute("value",value);
//                configXML.addChildElement(preference);
//            }
//        }
//        it = kbs.keySet().iterator();
//        while (it.hasNext()) {
//            key = (String) it.next();
//            kb = (KB) kbs.get(key);
//            SimpleElement kbXML = kb.writeConfiguration();            
//            configXML.addChildElement(kbXML);
//        }
//
//        try {
//            fw = new FileWriter( file );
//            pw = new PrintWriter(fw);
//            pw.println(configXML.toFileString());
//        }
//        catch (java.io.IOException e) {                                                  
//            throw new IOException("Error writing file " + file.getCanonicalPath() + ".\n " + e.getMessage());
//        }
//        finally {
//            if (pw != null) {
//                pw.close();
//            }
//            if (fw != null) {
//                fw.close();
//            }
//        }
    }

    /**
     * ***************************************************************
     * Get the KB that has the given name.
     */
    public KB getKB(String name) {

        if (!kbs.containsKey(name)) {
            System.out.println("Error in KBmanager.getKB(): KB " + name + " not found.");
        }
        return (KB) kbs.get(name.intern());
    }

    /**
     * ***************************************************************
     * Returns true if a KB with the given name exists.
     */
    public boolean existsKB(String name) {

        return kbs.containsKey(name);
    }

    /**
     * ***************************************************************
     * Remove the KB that has the given name.
     */
    public void remove(String name) {
        kbs.remove(name);
    }

    /**
     * ***************************************************************
     * Get the one instance of KBmanager from its class variable.
     */
    public static KBmanager getMgr() {

        if (manager == null) {
            manager = new KBmanager();
        }
        return manager;
    }

    /**
     * ***************************************************************
     * Get the Set of KB names in this manager.
     */
    public Set getKBnames() {
        return kbs.keySet();
    }

    /**
     * ***************************************************************
     * Get the preference corresponding to the given kef.
     */
    public String getPref(String key) {
        String ans = (String) preferences.get(key);
        if (ans == null) {
            ans = "";
        }
        return ans;
    }

    /**
     * ***************************************************************
     * Set the preference to the given value.
     */
    public void setPref(String key, String value) {
        preferences.put(key, value);
    }

    /**
     * ***************************************************************
     * Returns an int value, the bitwise interpretation of which indicates the
     * current configuration of logic parameter (preference) settings. The
     * int value is computed from the KBmanager preferences at the time this
     * method is evaluated.
     *
     * @return An int value indicating the current configuration of logic
     * parameters, according to KBmanager preference settings.
     */
    public int getInferenceBitValue() {
        int bv = 0;
        String[] keys = {"typePrefix", "holdsPrefix", "cache", "TPTP"};
        int[] vals = {USE_TYPE_PREFIX, USE_HOLDS_PREFIX, USE_CACHE, USE_TPTP};
        String pref;
        for (int i = 0; i < keys.length; i++) {
            pref = getPref(keys[i]);
            if (Formula.isNonEmptyString(pref) && "yes".equalsIgnoreCase(pref)) {
                bv += vals[i];
            }
        }
        return bv;
    }

    /**
     * ***************************************************************
     * Returns the last cached logic bit value setting.
     *
     * @return An int value indicating the logic parameter configuration at
     * the time the value was set.
     */
    public int getOldInferenceBitValue() {
        return oldInferenceBitValue;
    }

    /**
     * ***************************************************************
     * Sets the value of the private variable oldInferenceBitValue.
     *
     * @return void
     */
    public void setOldInferenceBitValue(int bv) {
        oldInferenceBitValue = bv;
    }

}
