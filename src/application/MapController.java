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

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

	@FXML
	private Label latLabel;
	
	@FXML
	private Label lonLabel;
	
	@FXML
	private Label displayNameLabel;
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
        
        mapViewer.addMouseListener(new MouseAdapter() {
        	
        	public void mouseClicked(MouseEvent e) {
        		Coordinate position = (Coordinate) mapViewer.getPosition(e.getX(), e.getY());
  
        		if (position != null) {
                    updateMarker(position.getLat(), position.getLon());
                    try {
						getInfomationLocal(position.getLat(), position.getLon());
					} catch (IOException | InterruptedException e1) {
						e1.printStackTrace();
					}
                }
        	}
		});
	}

	// Tính năng đánh dấu
	public void updateMarker(double lat, double lon) {
		if(currentMarker != null) {
			mapViewer.removeMapMarker(currentMarker);
		}
		
		currentMarker = new MapMarkerDot(lat, lon);
		mapViewer.addMapMarker(currentMarker);
	}
	
	// Tính năng tìm kiếm
	public void searchLocation(ActionEvent event) throws IOException, InterruptedException {
		// Lấy tên địa điểm đã nhập
		String location = searchField.getText();
		// Tạo URL
		String url = "https://nominatim.openstreetmap.org/search?q=" + 
                location.replace(" ", "+") + "&format=json&addressdetails=1"; 
		
		// Tạo 1 client HTTP để gửi yêu cầu lấy dữ liệu
		HttpClient client = HttpClient.newHttpClient(); 
		// Tạo request HTTP đến URL trên
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
				.build(); 
		
		// Gửi yêu cầu lấy dữ liệu
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		
		JSONArray results = new JSONArray(response.body());
		
		if (results.length() > 0) {
            for(int i = 0; i < results.length(); i++) {
            	JSONObject obj = results.getJSONObject(i);
            	double lat = obj.getDouble("lat");
            	double lon = obj.getDouble("lon");
            	MapMarkerDot marker = new MapMarkerDot(lat, lon);
            	mapViewer.addMapMarker(marker);
            }
        } else {
            System.out.println("Không tìm thấy");
        }
	}
	
	// Hiển thị thông tin tại địa điểm đã đánh dấu
	public void getInfomationLocal(double lat, double lon) throws IOException, InterruptedException {
	    String url = String.format(
				"https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&format=json&addressdetails=1&extratags=1&namedetails=1", lat, lon);
		
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
				.build(); 
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		String result = response.body();
		JSONObject jsonResult = new JSONObject(result);
		String displayName = jsonResult.getString("display_name").replaceAll(", \\d{5,}", "");
		
		Platform.runLater(() -> {
	        latLabel.setText(String.valueOf(lat));
	        lonLabel.setText(String.valueOf(lon));
	        displayNameLabel.setText(displayName);
	    });
	}
}
