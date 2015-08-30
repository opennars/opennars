package nars.guifx;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import nars.NAR;
import nars.guifx.space.LazyTabX;
import nars.guifx.space.WebMap;


/**
 * Created by me on 8/11/15.
 */
public class InputPane extends TabPane {

    public InputPane(NAR n) {
        super();

        setSide(Side.BOTTOM);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        getTabs().add(new LazyTabX("Wiki", () -> {

            //"Navigate wikipedia to collect tags to use as input terms"
            return new WikiBrowser("Happiness") {

                @Override
                public void onTagClicked(String id) {

                }
            };
        } ));
        getTabs().add(new LazyTabX("Space", () -> new WebMap()) /*"Space", "Navigate a 2D map to input (map region-as-shape analysis, and lists of features and their locations)")*/);

        getTabs().add(new Tab("Narsese", new NarseseInput(n)));
        getTabs().add(new ComingSoonTab("Library", "Apps, APIs, Interfaces, and Examples (ex: from .NAL files) that can be input"));

        getTabs().add(new ComingSoonTab("Natural", "Natural language input in any of the major languages, using optional strategies (ex: CoreNLP)"));
        {
            /*getTabs().add(new Tab("En"));
            getTabs().add(new Tab("Es"));
            getTabs().add(new Tab("Fr"));
            getTabs().add(new Tab("De"));*/
        }
        getTabs().add(new ComingSoonTab("Sensors", "List of live signals and data sources which can be enabled, disabled, and reprioritized"));
        getTabs().add(new ComingSoonTab("Data", "Spreadsheet view for entering tabular data"));
        getTabs().add(new ComingSoonTab("Draw", "Drawing/composing an image that can be input"));
        getTabs().add(new ComingSoonTab("Webcam", "Webcam/video stream record; audio optional"));
        getTabs().add(new ComingSoonTab("Audio", "Microphone/audio stream record, w/ freq and noise analyzers, and optional speech recognition via multiple strategies"));


        getTabs().add(new ComingSoonTab("Time", "Navigate a timeline to view and edit significants in any time region"));
        getTabs().add(new ComingSoonTab("Patterns", "Frequently-used inputs and templates selectable via speed-dial button grid"));
        getTabs().add(new ComingSoonTab("URL", "Bookmarks that will create a new browser tab for web pages. Also includes URL navigation textfield"));

    }
}
