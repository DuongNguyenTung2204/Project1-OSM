package application;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polyline;

public class MapController implements Initializable {
	@FXML
	private BorderPane mainBorder;
	
	@FXML
	private TextField searchField;

	@FXML
	private Button searchButton;
	
	@FXML
	private Button infoLocationButton;
	
	@FXML
	private Button directionButton;
	
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
	
	private CustomMapViewer mapViewer;
	private List<MapMarker> markers;
    private MapMarkerDot currentMarker = null; 
    
    // Biến lưu điểm bắt đầu và kết thúc
    private MapMarkerDot startMarker = null;
    private MapMarkerDot endMarker = null;

    // Biến đếm số lần nhấp chuột
    private int count = 0;  // Đếm số lần nhấp chuột
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Setup các thành phần mặc định cho bản đồ
		SwingNode swingNode = new SwingNode(); 
        mapViewer = new CustomMapViewer();
        Coordinate hanoi = new Coordinate(21.0285, 105.8542); 
        mapViewer.setDisplayPosition(hanoi, 12);
        swingNode.setContent(mapViewer);
        mapPane.getChildren().add(swingNode);
        
        markers = new ArrayList<>();
        
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
		clearAllMarkers();
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
            	markers.add(marker);
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
	
	// Tìm kiếm địa điểm sử dụng Overpass API
	// Amenity
	public void findAmenity(ActionEvent event) throws IOException, InterruptedException {
        clearAllMarkers();
		
		Button btn = (Button) event.getSource();
		String key = btn.getText().replace(" ", "_").toLowerCase();
	   
		double vd  = currentMarker.getLat();
		double kd  = currentMarker.getLon();
		
	    String apiUrl = "https://overpass-api.de/api/interpreter?data=";
	    String query = "[out:json];node[\"amenity\"=\"%s\"](around:5000,%f,%f);out;";
	    
	    String url = String.format(query, key, vd, kd);
	    String encodeUrl = URLEncoder.encode(url, "UTF-8");
	    
	    String uri = apiUrl + encodeUrl;
        
        HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
				.build(); 
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		JSONObject results = new JSONObject(response.body());
        JSONArray elements = results.getJSONArray("elements"); 
        
        if (elements.length() > 0) {
            for (int i = 0; i < elements.length(); i++) {
                JSONObject obj = elements.getJSONObject(i);
                
                if (obj.has("lat") && obj.has("lon")) {
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    
                    markers.add(new MapMarkerDot(lat, lon));
                }
            }
            
            Platform.runLater(() -> {
                for (MapMarker marker : markers) {
                    mapViewer.addMapMarker(marker);
                }
            });
        } else {
            System.out.println("Không tìm thấy");
        }	
	}
	
	// Shop
	public void findShop(ActionEvent event) throws IOException, InterruptedException {
        clearAllMarkers();
		
		Button btn = (Button) event.getSource();
		String key = btn.getText().replace(" ", "_").toLowerCase();
	   
		double vd  = currentMarker.getLat();
		double kd  = currentMarker.getLon();
		
	    String apiUrl = "https://overpass-api.de/api/interpreter?data=";
	    String query = "[out:json];node[\"shop\"=\"%s\"](around:5000,%f,%f);out;";
	    
	    String url = String.format(query, key, vd, kd);
	    String encodeUrl = URLEncoder.encode(url, "UTF-8");
	    
	    String uri = apiUrl + encodeUrl;
        
        HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
				.build(); 
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		JSONObject results = new JSONObject(response.body());
        JSONArray elements = results.getJSONArray("elements"); 
        
        if (elements.length() > 0) {
            for (int i = 0; i < elements.length(); i++) {
                JSONObject obj = elements.getJSONObject(i);
                
                if (obj.has("lat") && obj.has("lon")) {
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    
                    markers.add(new MapMarkerDot(lat, lon));
                }
            }
            
            Platform.runLater(() -> {
                for (MapMarker marker : markers) {
                    mapViewer.addMapMarker(marker);
                }
            });
        } else {
            System.out.println("Không tìm thấy");
        }	
	}
	
	// Leisure
	public void findLeisure(ActionEvent event) throws IOException, InterruptedException {
        clearAllMarkers();
		
		Button btn = (Button) event.getSource();
		String key = btn.getText().replace(" ", "_").toLowerCase();
	   
		double vd  = currentMarker.getLat();
		double kd  = currentMarker.getLon();
		
	    String apiUrl = "https://overpass-api.de/api/interpreter?data=";
	    String query = "[out:json];node[\"leisure\"=\"%s\"](around:5000,%f,%f);out;";
	    
	    String url = String.format(query, key, vd, kd);
	    String encodeUrl = URLEncoder.encode(url, "UTF-8");
	    
	    String uri = apiUrl + encodeUrl;
        
        HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
				.build(); 
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		JSONObject results = new JSONObject(response.body());
        JSONArray elements = results.getJSONArray("elements"); 
        
        if (elements.length() > 0) {
            for (int i = 0; i < elements.length(); i++) {
                JSONObject obj = elements.getJSONObject(i);
                
                if (obj.has("lat") && obj.has("lon")) {
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    
                    markers.add(new MapMarkerDot(lat, lon));
                }
            }
            
            Platform.runLater(() -> {
                for (MapMarker marker : markers) {
                    mapViewer.addMapMarker(marker);
                }
            });
        } else {
            System.out.println("Không tìm thấy");
        }	
	}
	
	// Tourism
	public void findTourism(ActionEvent event) throws IOException, InterruptedException {
        clearAllMarkers();
		
		Button btn = (Button) event.getSource();
		String key = btn.getText().replace(" ", "_").toLowerCase();
	   
		double vd  = currentMarker.getLat();
		double kd  = currentMarker.getLon();
		
	    String apiUrl = "https://overpass-api.de/api/interpreter?data=";
	    String query = "[out:json];node[\"tourism\"=\"%s\"](around:5000,%f,%f);out;";
	    
	    String url = String.format(query, key, vd, kd);
	    String encodeUrl = URLEncoder.encode(url, "UTF-8");
	    
	    String uri = apiUrl + encodeUrl;
        
        HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
				.build(); 
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		JSONObject results = new JSONObject(response.body());
        JSONArray elements = results.getJSONArray("elements"); 
        
        if (elements.length() > 0) {
            for (int i = 0; i < elements.length(); i++) {
                JSONObject obj = elements.getJSONObject(i);
                
                if (obj.has("lat") && obj.has("lon")) {
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    
                    markers.add(new MapMarkerDot(lat, lon));
                }
            }
            
            Platform.runLater(() -> {
                for (MapMarker marker : markers) {
                    mapViewer.addMapMarker(marker);
                }
            });
        } else {
            System.out.println("Không tìm thấy");
        }	
	}
	
	
	// Chức năng chỉ đường
	// Hàm để đánh dấu điểm bắt đầu và kết thúc
    public void markStartPoint(double lat, double lon) {
	    startMarker = new MapMarkerDot(lat, lon);
		mapViewer.addMapMarker(startMarker);
		System.out.println("Điểm bắt đầu đã được đánh dấu.");
    }

	public void markEndPoint(double lat, double lon) {
	    endMarker = new MapMarkerDot(lat, lon);
		mapViewer.addMapMarker(endMarker);
		System.out.println("Điểm kết thúc đã được đánh dấu.");
	}

	
	public void routing(ActionEvent event) {
	    System.out.println("Vui lòng chọn hai điểm trên bản đồ.");

	    count = 0; // Reset đếm số lần nhấp chuột
	    startMarker = null;
	    endMarker = null;

	    mapViewer.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            Coordinate position = (Coordinate) mapViewer.getPosition(e.getX(), e.getY());

	            if (position != null) {
	                // Lần nhấp chuột đầu tiên: Đánh dấu điểm bắt đầu
	                if (count == 0) {
	                    markStartPoint(position.getLat(), position.getLon());
	                    count++;
	                }
	                // Lần nhấp chuột thứ hai: Đánh dấu điểm kết thúc và tính toán đường đi
	                else if (count == 1) {
	                    markEndPoint(position.getLat(), position.getLon());
	                    count++;
	                    try {
							calculateRoute();
						} catch (IOException | InterruptedException e1) {
							e1.printStackTrace();
						}
	                    mapViewer.removeMouseListener(this); // Ngừng lắng nghe sự kiện nhấp chuột
	                }
	            }
	        }
	    });
	}
	
	
	public void calculateRoute() throws IOException, InterruptedException {
	    if (startMarker != null && endMarker != null) {
	        double startLat = startMarker.getLat();
	        double startLon = startMarker.getLon();
	        double endLat = endMarker.getLat();
	        double endLon = endMarker.getLon();

	        // Sử dụng OSRM API để lấy tuyến đường
	        String apiUrl = "https://router.project-osrm.org/route/v1/driving/" + startLon + "," + startLat + ";" + endLon + "," + endLat + "?overview=full&geometries=geojson";

	        HttpClient client = HttpClient.newHttpClient();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(apiUrl))
	                .header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
	                .build();

	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	        String responseBody = response.body();

	        // In ra phản hồi để kiểm tra cấu trúc
	        System.out.println(responseBody);

	        // Phân tích JSON
	        JSONObject jsonResponse = new JSONObject(responseBody);
	        JSONArray routes = jsonResponse.getJSONArray("routes");

	        JSONObject route = routes.getJSONObject(0);
	        JSONObject geometry = route.getJSONObject("geometry");
	        JSONArray coordinates = geometry.getJSONArray("coordinates");

	        List<Coordinate> routePoints = new ArrayList<>();
	        routePoints.add(new Coordinate(startLat, startLon));
	        for (int i = 0; i < coordinates.length(); i++) {
	            JSONArray coord = coordinates.getJSONArray(i);
	            routePoints.add(new Coordinate(coord.getDouble(1), coord.getDouble(0))); // Latitude, Longitude
	        }
	        routePoints.add(new Coordinate(endLat, endLon));

	        
	        drawRoute(routePoints);
	    }
	}

	public void drawRoute(List<Coordinate> routePoints) {
        // Gửi danh sách routePoints vào CustomJMapViewer
        mapViewer.setRoutePoints(routePoints);
    }
	// Xóa các điểm đánh dấu từ truy vấn trước đó khỏi bản đồ trước khi thực hiện truy vấn mới
	public void clearAllMarkers() {
		for (MapMarker marker : markers) {
            mapViewer.removeMapMarker(marker); 
        }
        markers.clear();
    }
	
	

	
}
