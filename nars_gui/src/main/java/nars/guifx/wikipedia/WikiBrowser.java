package nars.guifx.wikipedia;

import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO wikibrowser does not need to modify any DOM links because they will be intercepted.
 * this will make processing the page faster.
 *
 * TODO
*      --cache the page manipulation JS code in a JSObject instead of creating and inputting a new script string each page load
*      --manipulate links in batches, so it doesnt freeze slower devices?
 *        --parameters for link decoration
 *     --use p2p/infinispan cache as page cache
*      --language switch
 *
 */
public abstract class WikiBrowser extends BorderPane {

    public final int TIMEOUT_MS = 15 * 1000;
    private final WebView webview;
    private final WebEngine webEngine;
    private final WikiOntology wikiOntology;

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public WikiBrowser(String startWikiPage) {

        wikiOntology = new WikiOntology();

        webview = new WebView();
        webEngine = webview.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        processPage();
                        webview.setVisible(true);
                    }
                });
            }
        });




        /*
         webEngine.locationProperty().addListener(new ChangeListener<String>() {
         @Override public void changed(ObservableValue<? extends String> ov, final String oldLoc, final String loc) {
         System.out.println(loc);
         }
         });
         */

        setCenter(webview);

        setTop(newControls());


        if (startWikiPage != null)
            loadWikiPage(startWikiPage);

    }

    static String readFile(URI uri, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(uri));
        return new String(encoded, encoding);
    }

//    static String jquery = "";
//
//    static {
//        try {
//            jquery = readFile(WikiBrowser.class.getResource("jquery.js").toURI(), Charset.defaultCharset());
//        } catch (Exception ex) {
//            Logger.getLogger(WikiBrowser.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    @SuppressWarnings("HardcodedFileSeparator")
    protected synchronized void processPage() {
        boolean wikiFilter = false;

        String location = webEngine.getLocation();
        if (location.contains("://en.m.wikipedia.org") && !location.contains("api.php")) {
            wikiFilter = true;
        }

        JSObject win =  (JSObject) webEngine.executeScript("window");
        win.setMember("app", this);


        webEngine.setJavaScriptEnabled(true);


        //TODO store compiled
        //webEngine.executeScript(jquery);

        //webEngine.executeScript("var MINI = require('minified'); var _=MINI._, $=MINI.$, $$=MINI.$$, EE=MINI.EE, HTML=MINI.HTML;");


        if (wikiFilter) {

            JSObject jsobj = (JSObject) webEngine.executeScript("window");
            jsobj.setMember("category", wikiOntology);


            //String script = "$(document).ready(function() {";
            String script = "window.setTimeout(function() {";

            script += "$('body').after('<style>.crb { border: 1px solid #aaa; margin: 2px; padding: 1px; }</style>');";

            script += "$('head, .header, #page-actions, #jump-to-nav, .top-bar, .navigation-drawer, .edit-page').remove();";

            //Add tag button to each tlink
            //String plusLink = "$('<a class=\"crb\" href=\"tag:/' + h + '\">+</a>').click(function() { window.app.tagAdd(h); } )";

            String plusLink = "$('<a class=\"crb\" href=\"tag:/' + h + '\">+</a>').click(function() { window.app.tagAdd(h); } ) ";

            script += "var linkTransform = function() { var t = $(this); var h = t.attr('href'); if (h && h.indexOf('#')!==-1) return; t.after(" + plusLink + " ); }; ";
            script += "$('a').each(linkTransform);";
            script += "$('#section_0').each(linkTransform);";


            script += "if (window.mw) { category.add(window.mw.config.get('wgCategories')); }";
            script += "}, 0);";

            webEngine.executeScript(script);
        }

        //webEngine.setJavaScriptEnabled(false);
        //if ((target.indexOf('Portal:') != 0) && (target.indexOf('Special:') != 0)) {
        //  t.after(newPopupButton(target));
//        EventListener listener = new EventListener() {
//            @Override
//            public void handleEvent(Event ev) {
//                String domEventType = ev.getType();
//
//                System.err.println("EventType: " + domEventType + " " + ev);
//
//                if (domEventType.equals("click")) {
//                    String href = ((Element) ev.getTarget()).getAttribute("href");
//
//                    if (href != null)
//                        if (href.startsWith("tag://")) {
//                            tagAdd(href);
//                        }
//                }
//            }
//        };

//        String currentTag = getCurrentPageTag();
//        if (currentTag != null) {
//
//            HashMultiset<String> links = HashMultiset.create();
//
//            Document doc = webEngine.getDocument();
//            NodeList nodeList = doc.getElementsByTagName("a");
//            for (int i = 0; i < nodeList.getLength(); i++) {
//                org.w3c.dom.Node item = nodeList.item(i);
//                ((EventTarget)item).addEventListener("click", listener, false);
//
//                if (item instanceof Element) {
//                    Element e = (Element)item;
//                    String href = e.getAttribute("href");
//                    if ((href!=null) && (href.startsWith("tag://wiki/"))) {
//                        String wikipage = href.substring("tag://wiki/".length());
//
//                        if (wikipage.equals(currentTag))
//                            continue;
//
//                        if (core.getTag(wikipage)!=null)
//                            links.add(wikipage);
//                    }
//                }
//            }
//
//        }


    }

    //
//        
//	protected class MyWebViewClient extends WebViewClient {
//
//
//		@Override
//		public boolean shouldOverrideUrlLoading(WebView view, String url) {
//			/*
//			 * if (Uri.parse(url).getHost().equals("www.example.com")) { // This
//			 * is my web site, so do not override; let my WebView load the page
//			 * return false; } // Otherwise, the tlink is not for a page on my
//			 * site, so launch another Activity that handles URLs Intent intent
//			 * = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//			 * startActivity(intent); return true;
//			 */
//
//			final String originalURL = url;
//			String urlPrefix = "http://en.m.wikipedia.org/";
//			if (url.startsWith(urlPrefix)) {
//				url = url.substring(urlPrefix.length());
//				final String turl = urlPrefix + "wiki/" + url;
//
//				AlertDialog.Builder builder = new AlertDialog.Builder(TagActivity.this);
//			    builder.setTitle(url);
//			    CharSequence[] items = new CharSequence[8];
//			    items[0] = "Go...";
//			    items[1] = "Learner";
//			    items[2] = "Learner Collaborator";
//			    items[3] = "Collaborator Learner";
//			    items[4] = "Collaborator Teacher";
//			    items[5] = "Teacher Collaborator";
//			    items[6] = "Teacher";
//			    items[7] = "Cancel";
//
//
//			    builder.setItems(items, new DialogInterface.OnClickListener() {
//			           public void onClick(DialogInterface dialog, int which) {
//			        	   if (which == 0) {
//			        		   runOnUiThread(new Runnable() {
//			        			  @Override public void run() {
//					        		  loadWikiPage(turl);			        				
//			        			  } 
//			        		   });
//			        	   }
//			        	   else if (which < 7) {
//			        		   Toast.makeText(TagActivity.this, "Tagged.", Toast.LENGTH_SHORT).show();
//			        	   }
//
//		        		   dialog.dismiss();
//			           }
//			    });
//
//				AlertDialog dialog = builder.create();
//				dialog.show();
//
//				return true;
//			}
//
//			return false;
//
//		}
//	}
//
    public void loadWikiSearchPage(String query) {
        webview.setVisible(false);
        webEngine.load("http://en.m.wikipedia.org/w/index.php?search=" + URLEncoder.encode(query));
    }

    @SuppressWarnings("HardcodedFileSeparator")
    public void loadWikiPage(String urlOrTag) {
        webview.setVisible(false);
        if (!urlOrTag.contains("/"))
            urlOrTag = "http://en.m.wikipedia.org/wiki/" + urlOrTag;
        webEngine.load(urlOrTag);
    }

    @SuppressWarnings("HardcodedFileSeparator")
    public String getWikiTag(String url) {
        if (url.contains(".wikipedia.org/wiki/")) {
            int p = url.lastIndexOf('/');
            String tag = url.substring(p + 1);

            int hashLocation = tag.indexOf('#');
            if (hashLocation != -1) {
                tag = tag.substring(0, hashLocation);
            }
            return tag;
        }
        return null;
    }

    public String getCurrentPageTag() {
        return getWikiTag(webEngine.getLocation());
    }

    @SuppressWarnings("HardcodedFileSeparator")
    public void tagAdd(String url) {

        String wikiPrefix = "/wiki/";
        String tag;
        if (url.startsWith(wikiPrefix)) {
            //ordinary wikilink
            tag = url.substring(wikiPrefix.length());
        } else if ("undefined".equals(url)) {
            //section header link
            tag = getCurrentPageTag();
        } else {
            System.err.println("unknown tagAdd url: " + url);
            return;
        }

        onTagClicked(tag);
    }

    public abstract void onTagClicked(String id);

    protected Node newControls() {
        Button nearButton = new Button("Near");
        Button backButton = new Button("Back");
        Button searchButton = new Button("Search");
        TextField searchField = new TextField("");

        BorderPane p = new BorderPane();
        p.setCenter(searchField);
        p.setRight(searchButton);
        p.setLeft(new HBox(nearButton, backButton));

        nearButton.setOnAction(event -> {
//                SpacePoint location = SpacePoint.get(core.getMyself());
//                if (location == null)
//                    return;
//
//                double lat = location.lat;
//                double lon = location.lon;
//
//                double rad = 10000; //meters
//                int num = 10;
//
//                String geoURL = "http://en.m.wikipedia.org/w/api.php?action=query&list=geosearch&format=json&gscoord=" + lat + "%7C" + lon + "&gsradius=" + rad + "&gslimit=" + num;
//                loadWikiPage(geoURL);
//
//                //Alternate method but requires html5 browser geolocation api:
//                //"https://en.m.wikipedia.org/wiki/Special:Nearby"
        });

        EventHandler<ActionEvent> search = event -> {
            loadWikiSearchPage(searchField.getText());
            searchField.setText("");
        };

        searchButton.setOnAction(search);
        searchField.setOnAction(search);

        return p;
    }


    public class WikiOntology {

        public WikiOntology() {

        }


        public void add(Object o) {
            JSObject j = (JSObject) o;
            List<String> categories = new ArrayList();

            int length = (int) j.getMember("length");
            for (int i = 0; i < length; i++) {
                Object x = j.getSlot(i);
                if (x instanceof String) {
                    String cat = (String) x;
                    if (cat.startsWith("All articles "))
                        continue;
                    if (cat.startsWith("Articles with "))
                        continue;
                    if (cat.startsWith("Articles containing "))
                        continue;
                    if (cat.startsWith("Articles needing "))
                        continue;
                    if (cat.startsWith("CS1 errors:"))
                        continue;
                    if (cat.startsWith("Use dmy dates "))
                        continue;
                    if (cat.startsWith("Pages using citations with "))
                        continue;
                    if (cat.startsWith("Pages containing cite ")) continue;
                    if (cat.startsWith("Wikipedia articles ")) continue;
                    if (cat.startsWith("All accuracy ")) continue;
                    if (cat.startsWith("All disambiguation ")) continue;
                    if (cat.startsWith("Wikipedia semi-protected ")) continue;
                    if (cat.startsWith("Commons category with ")) continue;
                    if (cat.startsWith("Articles to be expanded ")) continue;
                    if (cat.startsWith("Use British English ")) continue;
                    if (cat.startsWith("All Wikipedia articles ")) continue;
                    if (cat.startsWith("Wikipedia indefinitely ")) continue;
                    if (cat.startsWith("Articles prone to ")) continue;
                    if (cat.startsWith("All article ")) continue;
                    if (cat.startsWith("Vague or ambiguous ")) continue;


                    String c = categoryToPage(cat);
                    if (!c.equals(getCurrentPageTag()))
                        categories.add(c);
                }
            }

            if (!categories.isEmpty()) {
//                NTag n = new NTag(getCurrentPageTag(), getCurrentPageTag(), categories);
//                core.publish(n);
            }
        }
    }

    public static String categoryToPage(String category) {
        return category.replaceAll(" ", "_");
    }

}


//String url = urlOrTag;
//String[] sects = url.split("/");
//String tag = sects[sects.length-1];
// process page loading
//		try {
//			Document d = Jsoup.parse(new URL(url), TIMEOUT_MS);
//
//			d.select("head").after(
//					"<style>.crb { border: 1px solid gray; }</style>");
//
//			d.select(".header").remove();
//			d.select("#page-actions").remove();
//			//d.select("#contentSub").remove();
//			d.select("#jump-to-nav").remove();
//			//d.select(".IPA").remove();
//			//d.select("script").remove();
//
//			Elements links = d.select("a");
//			for (Element e : links) {
//				String href = e.attr("href");
//				if (href.startsWith("/wiki")) {
//					String target = href.substring(5);
//					e.attributes().put("href", target);
//					e.attributes().put("class", "crb");
//				}
//			}
//			Elements headings = d.select("#section_0");
//			for (Element e : headings) {
//				e.html("<a href='/" + tag + "' class='crb'>" + e.text() + "</a>");
//			}
//
//			webEngine.loadContent(d.html());
//                        
//		} catch (Exception e) {
//			webEngine.loadContent(e.toString());
//		}
//    }

//        public void loadWikiPageOLD(String urlOrTag) {
//		webEngine.loadContent("Loading...");
//
//                
//		String url = urlOrTag;
//
//		String[] sects = url.split("/");
//		String tag = sects[sects.length-1];
//
//		try {
//			Document d = Jsoup.parse(new URL(url), TIMEOUT_MS);
//
//			d.select("head").after(
//					"<style>.crb { border: 1px solid gray; }</style>");
//
//			d.select(".header").remove();
//			d.select("#page-actions").remove();
//			//d.select("#contentSub").remove();
//			d.select("#jump-to-nav").remove();
//			//d.select(".IPA").remove();
//			//d.select("script").remove();
//
//			Elements links = d.select("a");
//			for (Element e : links) {
//				String href = e.attr("href");
//				if (href.startsWith("/wiki")) {
//					String target = href.substring(5);
//					e.attributes().put("href", target);
//					e.attributes().put("class", "crb");
//				}
//			}
//			Elements headings = d.select("#section_0");
//			for (Element e : headings) {
//				e.html("<a href='/" + tag + "' class='crb'>" + e.text() + "</a>");
//			}
//
//			webEngine.loadContent(d.html());
//                        
//		} catch (Exception e) {
//			webEngine.loadContent(e.toString());
//		}
//
//	}
//
