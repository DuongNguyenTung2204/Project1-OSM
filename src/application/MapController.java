package application;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class MapController implements Initializable {
	
	@FXML
	private TextField searchField;

	@FXML
	private Button searchButton;
	
	@FXML
	private StackPane mapPane;

	@FXML
	private HBox BoxControl;

	private JMapViewer mapViewer;
    private MapMarkerDot currentMarker = null; 
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Setup các thành phần mặc định cho bản đồ
		SwingNode swingNode = new SwingNode();
        mapViewer = new JMapViewer();
        Coordinate hanoi = new Coordinate(21.0285, 105.8542);
        mapViewer.setDisplayPosition(hanoi, 12);
        swingNode.setContent(mapViewer);
        mapPane.getChildren().add(swingNode);
        
        // Tính năng đánh dấu
        mapViewer.addMouseListener(new MouseAdapter() {
        	
        	public void mouseClicked(MouseEvent e) {
        		Coordinate position = (Coordinate) mapViewer.getPosition(e.getX(), e.getY());
  
        		if (position != null) {
                    updateMarker(position.getLat(), position.getLon());
                }
        	}
		});
	}

	public void updateMarker(double lat, double lon) {
		if(currentMarker != null) {
			mapViewer.removeMapMarker(currentMarker);
		}
		
		currentMarker = new MapMarkerDot(lat, lon);
		mapViewer.addMapMarker(currentMarker);
	}
}
