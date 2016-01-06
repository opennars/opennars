//package nars.nlp.gf;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.google.common.collect.Sets;
//import nars.util.language.JSON;
//import org.apache.commons.io.IOUtils;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.client.utils.URLEncodedUtils;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.function.Consumer;
//
///**
// * Grammatical Framework HTTP server interface
// * https://code.google.com/p/grammatical-framework/wiki/GFWebServiceAPI
// * https://github.com/Kaljurand/GF-Java/blob/master/src/main/java/ch/uzh/ifi/attempto/gfservice/GfTree.java
// */
//public class GrammaticalFrameworkClient {
//
//    final public String host;
//    final int port;
//    final ExecutorService exe = Executors.newCachedThreadPool();
//    CloseableHttpClient http = HttpClients.createDefault();
//
//    /** starts and stops a 'gf' executable at a given path by running: gf -server */
//    public static class GrammaticalFrameworkServer {
//        //TODO
//    }
//
//    public GrammaticalFrameworkClient(String host, int port) {
//        this.port = port;
//        this.host = host;
//    }
//
//    public GrammaticalFrameworkClient(String host) {
//        this(host, 41296);
//    }
//
//    public GrammaticalFrameworkClient() {
//        this("localhost");
//    }
//
//    public JsonNode get(String url) {
//        return get(new HttpGet(url));
//    }
//
//    public JsonNode get(HttpUriRequest req) {
//
//        try {
//            CloseableHttpResponse resp = http.execute(req);
//            String s =IOUtils.toString(resp.getEntity().getContent());
//            resp.close();
//            return JSON.toJSON(s);
//        } catch (Exception e) {
//            //e.printStackTrace();
//            System.err.println(e);
//        }
//
//        return null;
//    }
//
//    protected void exe(String url, Consumer<JsonNode> recv) {
//        exe.submit( () -> {
//            recv.accept(get(url));
//        } );
//    }
//
//    public void treeRandomAsync(String grammar, Consumer<JsonNode> recv) {
//        //http://localhost:41296/grammars/Foods.pgf?command=random
//        exe(getBaseURL() + "/grammars/" + grammar + ".pgf?command=random", recv);
//    }
//    public JsonNode treeRandom(String grammar) {
//        //http://localhost:41296/grammars/Foods.pgf?command=random
//        return get(getBaseURL() + "/grammars/" + grammar + ".pgf?command=random");
//    }
//
//    public JsonNode linearize(String grammar, String lang, String tree)  {
//        // http://localhost:41296/grammars/Foods.pgf?command=linearize&tree=Pred+(That+Pizza)+(Very+Boring)&to=FoodsEng
//
////        try {
//            String uu = getBaseURL() +
//                    "/grammars/" + grammar + ".pgf?";
//            //HttpPost httpPost = new HttpPost(uu);
//
//            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//            nvps.add(new BasicNameValuePair("command", "linearize"));
//            nvps.add(new BasicNameValuePair("tree", tree));
//            nvps.add(new BasicNameValuePair("to", grammar + lang));
//
//            //httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//            uu += URLEncodedUtils.format(nvps, (String)null);
//
//        return get(uu);
//        //        }
////        catch (UnsupportedEncodingException e) {
////            return null;
////        }
//
//    }
//
//
//    /** to request next words, make sure the at linearized text ends with a ' ' char */
//    public JsonNode complete(String grammar, String lang, String linearized, String cat, int limit) {
//        //http://localhost:41296/grammars/Foods.pgf?command=complete&input=that+pizza+is+very+&from=FoodsEng
//        String uu = getBaseURL() +
//                "/grammars/" + grammar + ".pgf?";
//        //HttpPost httpPost = new HttpPost(uu);
//
//        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//        nvps.add(new BasicNameValuePair("command", "complete"));
//        nvps.add(new BasicNameValuePair("input", linearized));
//        nvps.add(new BasicNameValuePair("limit", Integer.toString(limit)));
//
//        if (cat!=null)
//            nvps.add(new BasicNameValuePair("cat", cat));
//
//        if (lang == null) lang = "";
//
//        nvps.add(new BasicNameValuePair("from",
//                grammar + lang
//        ));
//
//        //httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//        uu += URLEncodedUtils.format(nvps, (String)null);
//
//        return get(uu);
//    }
//
//    public JsonNode tree(String grammar, String lang, String linearized) {
//        return tree(grammar, lang, linearized, null);
//    }
//
//    public JsonNode tree(String grammar, String lang, String linearized, String startCat) {
//        // http://localhost:41296/grammars/Foods.pgf
//        //          ?command=parse&input=that+pizza+is+very+boring&from=FoodsEng
//        String uu = getBaseURL() +
//                "/grammars/" + grammar + ".pgf?";
//        //HttpPost httpPost = new HttpPost(uu);
//
//        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//        nvps.add(new BasicNameValuePair("command", "parse"));
//        nvps.add(new BasicNameValuePair("input", linearized));
//
//        if (lang == null) lang = "";
//
//        nvps.add(new BasicNameValuePair("from",
//                grammar + lang
//        ));
//
//        if (startCat!=null)
//            nvps.add(new BasicNameValuePair("cat", startCat));
//
//        //httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//        uu += URLEncodedUtils.format(nvps, (String)null);
//
//        return get(uu);
//    }
//
////    public String linearize(String tree) {
////
////    }
//
//    public String getBaseURL() {
//        return "http://" + host + ":" + port;
//    }
//
//
//    public static void main(String[] args) {
//        GrammaticalFrameworkClient h = new GrammaticalFrameworkClient();
//        System.out.println( JSON.pretty( h.treeRandom("Phrasebook") ) );
//        System.out.println( JSON.pretty( h.linearize("Foods", "Eng", "Pred (That Pizza) (Very Boring)") ) );
//        System.out.println( JSON.pretty( h.tree("Foods", "Eng", "that pizza is very boring")  ) );
//
//    }
//
//
//    public static final char SPACE = ' ';
//    public static final char PLEFT = '(';
//    public static final char PRIGHT = ')';
//    public static final Set<Character> LAYOUT_CHARS = Sets.newHashSet(SPACE, '\t', '\n', '\f', '\r');
//
//    // TODO: make immutable if exposed to the client
//    public static class GfFun {
//        private final String mName;
//        private List<GfFun> mArgs;
//        public GfFun(String name) {
//            mName = name;
//        }
//
//        public void addArg(GfFun fun) {
//            if (mArgs == null) {
//                mArgs = new ArrayList<GfFun>();
//            }
//            mArgs.add(fun);
//        }
//
//        public String getName() {
//            return mName;
//        }
//
//        public List<GfFun> getArgs() {
//            if (mArgs == null) {
//                return Collections.emptyList();
//            }
//            return mArgs;
//        }
//
//        public boolean hasArgs() {
//            return mArgs != null;
//        }
//    }
//
//    /**
//     * Structured representation of GF trees. Current features:
//     *   - constructs GfTree from a strings, e.g. "a (b (c d)) e", some syntax errors are tolerated
//     *   - getter for all function names and leaf names in the tree
//     */
//    public static class GfTree {
//
//
//        // [ \t\n\x0B\f\r]
//
//
//        public final GfFun mRoot;
//        public final int mSize;
//        public final Set<String> mFunctionNames;
//        public final Set<String> mLeafNames;
//        public final String mString;
//
//
//
//
//
//        public GfTree(String str) throws RuntimeException {
//            StringBuilder name = new StringBuilder();
//            int end = consumeName(str, 0, name);
//            mRoot = new GfFun(name.toString());
//            end = consumeArgs(str, mRoot, end);
//            if (end != str.length()) {
//                throw new RuntimeException("" + end);
//            }
//
//            StringBuilder sb = new StringBuilder();
//            Set<String> funs = Sets.newHashSet();
//            Set<String> leaves = Sets.newHashSet();
//            mSize = initData(mRoot, sb, funs, leaves);
//            mFunctionNames = funs;
//            mLeafNames = leaves;
//            mString = sb.toString();
//        }
//
//
//        public int size() {
//            return mSize;
//        }
//
//        public Set<String> getFunctionNames() {
//            return mFunctionNames;
//        }
//
//        /**
//         * Leaves are functions that have no arguments
//         */
//        public Set<String> getLeafNames() {
//            return mLeafNames;
//        }
//
//        public String toString() {
//            return mString;
//        }
//
//        public boolean hasFunctionNames(String... names) {
//            return hasNames(mFunctionNames, names);
//        }
//
//
//        public boolean hasLeafNames(String... names) {
//            return hasNames(mLeafNames, names);
//        }
//
//
//        public boolean equals(GfTree tree) {
//            return toString().equals(tree.toString());
//        }
//
//    }
//
//    private static boolean hasNames(Set<String> set, String... names) {
//        for (String name : names) {
//            if (! set.contains(name)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private static int initData(GfFun fun, StringBuilder sb, Set<String> funs, Set<String> leaves) {
//        int funCount = 1;
//        String name = fun.getName();
//        sb.append(name);
//        funs.add(name);
//        if (! fun.hasArgs()) {
//            leaves.add(name);
//        }
//        for (GfFun arg : fun.getArgs()) {
//            sb.append(SPACE);
//            if (arg.hasArgs()) {
//                sb.append(PLEFT);
//                funCount += initData(arg, sb, funs, leaves);
//                sb.append(PRIGHT);
//            } else {
//                funCount += initData(arg, sb, funs, leaves);
//            }
//        }
//        return funCount;
//    }
//
//
//    private static int consumeArgs(String str, GfFun fun, int begin) throws RuntimeException {
//        int i = begin;
//        while (i < str.length()) {
//            char ch = str.charAt(i);
//            if (ch == PLEFT) {
//                i++;
//                StringBuilder sb = new StringBuilder();
//                int end = consumeName(str, i, sb);
//                GfFun funWithArgs = new GfFun(sb.toString());
//                fun.addArg(funWithArgs);
//                i = consumeArgs(str, funWithArgs, end);
//            } else if (LAYOUT_CHARS.contains(ch)) {
//                // Skip whitespace
//                i++;
//            } else if (ch == PRIGHT) {
//                i++;
//                break;
//            } else {
//                StringBuilder sb = new StringBuilder();
//                i = consumeName(str, i, sb);
//                fun.addArg(new GfFun(sb.toString()));
//            }
//        }
//        return i;
//    }
//
//
//    private static int consumeName(String str, int begin, StringBuilder sb) throws RuntimeException {
//        int i = begin;
//        int start = -1;
//        while (i < str.length()) {
//            char ch = str.charAt(i);
//            if (LAYOUT_CHARS.contains(ch)) {
//                if (start != -1) {
//                    break;
//                }
//                // ignore preceding whitespace
//            } else if (ch == PLEFT || ch == PRIGHT) {
//                break;
//            } else if (start == -1) {
//                start = i;
//            }
//            i++;
//        }
//        if (start == -1) {
//            throw new RuntimeException(String.valueOf(i));
//        }
//        sb.append(str.substring(start, i));
//        return i;
//    }
//
// }
