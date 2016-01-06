package nars.guifx.space;

import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * JavaFx with Google map simple sample. from:
 * https://raw.githubusercontent.com/
 * tomoTaka01/MapZoomSample/master/src/mapzoomsample/MapZoomSample.java
 * 
 * @author tomo
 */
public class WebMap extends BorderPane {
	private Slider slider;

	public WebMap() {

		setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		// // Zoom Label
		// HBox hbox1 = new HBox(10);
		// hbox1.setPadding(new Insets(15,12,0,12));
		// Label spinnerLabel = new Label("Zoom Label:");
		// spinnerLabel.setFont(new Font("Arial", 20));
		// TextField zoomText = new TextField(); // TODO spinner
		// Button button = new Button("set zoom");
		// hbox1.getChildren().addAll(spinnerLabel, zoomText, button);
		// map
		// HBox hbox2 = new HBox(10);
		// hbox2.setPadding(new Insets(15,12,15,12));
		WebView webView = new WebView();
		WebEngine engine = webView.getEngine();
		String html = getClass().getResource("osm.html").toExternalForm();
		engine.load(html);
		// hbox2.getChildren().addAll(webView);
		// // zoom slider
		// HBox hbox3 = new HBox(10);
		// hbox3.setPadding(new Insets(0,12,15,12));
		// Label sliderLabel = new Label("Zoom Slider:");
		// sliderLabel.setFont(new Font("Arial", 20));
		// initSlider();
		// hbox3.getChildren().addAll(sliderLabel,slider);
		// // set zoom action
		// button.setOnAction(e -> {
		// String zoom = zoomText.getText();
		// // invoke JavaScript function from JavaFX
		// engine.executeScript("changeZoom(" + zoom + ")");
		// });
		// JavaScript can call Java method using the name app
		JSObject win = (JSObject) engine.executeScript("window");
		win.setMember("app", new JavaApp());

		setCenter(webView);

	}

	private void initSlider() {
		slider = new Slider(0, 21, 21);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(3);
		slider.setBlockIncrement(3);
		slider.setValue(12);
	}

	public class JavaApp {
		// set the slider
		public void setZoom(int zoom) {
			slider.setValue(zoom);
		}
	}

}