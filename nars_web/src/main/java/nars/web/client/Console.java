package nars.web.client;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

/*import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.Selector;
import com.google.gwt.query.client.Selectors;
import static com.google.gwt.query.client.GQuery.*;
import static com.google.gwt.query.client.css.CSS.*;
*/

/**
 * Created by me on 7/22/15.
 */

public class Console implements EntryPoint {
    private VerticalPanel mainPanel = new VerticalPanel();
    private FlexTable stocksFlexTable = new FlexTable();
    private HorizontalPanel addPanel = new HorizontalPanel();
    private TextBox newSymbolTextBox = new TextBox();
    private Button addStockButton = new Button("Add");
    private Label lastUpdatedLabel = new Label();

    /**
     * Entry point method.
     */
    public void onModuleLoad() {
        GWT.log("Hello World!", null);

        Button b = new Button("Click me", new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.alert("Hello, AJAX");
            }
        });

        RootPanel.get().add(b);

        // TODO Create table for stock data.
        // TODO Assemble Add Stock panel.
        // TODO Assemble Main panel.
        // TODO Associate the Main panel with the HTML host page.
        // TODO Move cursor focus to the input box.
    }
}