package application;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Popup;

public class BottomSceneController2 implements Initializable{

	@FXML
	private Button startingPointButton;
	
	@FXML
	private Button destinationButton;
	
	@FXML
	private Label startingPointLabel;
	
	@FXML
	private Label destinationLabel;
	
	@FXML
	private Button getDirectionsButton;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML
	public void markStartingPoint(ActionEvent event) throws IOException, InterruptedException {
		Main.getMapController().markRouting1(info -> {
	        Platform.runLater(() -> {
	            startingPointLabel.setText(info.getString("display_name").replaceAll(", \\d{5,}", ""));
	        });
	    });
	}
	
	@FXML
	public void markDestination(ActionEvent event) throws IOException, InterruptedException {
		Main.getMapController().markRouting2(info -> {
	        Platform.runLater(() -> {
	            destinationLabel.setText(info.getString("display_name").replaceAll(", \\d{5,}", ""));
	        });
	    });
	}
	
	@FXML
	public void routing(ActionEvent event) throws IOException, InterruptedException {
		Main.getMapController().calculateRoute();
	}
}
