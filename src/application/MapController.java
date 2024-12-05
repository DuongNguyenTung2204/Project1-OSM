package application;

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
	
	private JMapViewer mapViewer;
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
        mapViewer = new JMapViewer();
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
	public void routing(ActionEvent event) {
	    System.out.println("Vui lòng chọn hai điểm trên bản đồ.");

	    mapViewer.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            Coordinate position = (Coordinate) mapViewer.getPosition(e.getX(), e.getY());

	            if (position != null) {
	                // Lần nhấp chuột đầu tiên: Đánh dấu điểm bắt đầu
	                if (count == 0) {
	                    markStartPoint(position.getLat(), position.getLon());
	                    count++;  // Tăng count sau lần nhấp đầu tiên
	                }
	                // Lần nhấp chuột thứ hai: Đánh dấu điểm kết thúc
	                else if (count == 1) {
	                    markEndPoint(position.getLat(), position.getLon());
	                    count++;  // Tăng count sau lần nhấp thứ hai
	                    // Tiến hành tính toán đường đi
	                    calculateRoute();
	                }
	            }
	        }
	    });
	}

	// Tính toán và vẽ tuyến đường
	private void calculateRoute() {
	    if (startMarker == null || endMarker == null) {
	        System.out.println("Vui lòng chọn hai điểm trên bản đồ.");
	        return;
	    }

	    double startLat = startMarker.getLat();
	    double startLon = startMarker.getLon();
	    double endLat = endMarker.getLat();
	    double endLon = endMarker.getLon();

	    // API để tính toán tuyến đường
	    String apiUrl = "https://router.project-osrm.org/route/v1/driving/" + startLon + "," + startLat + ";" + endLon + "," + endLat + "?overview=full";

	    HttpClient client = HttpClient.newHttpClient();
	    HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(apiUrl))
	            .header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
	            .build();

	    client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(response -> {
	        JSONObject jsonResponse = new JSONObject(response);
	        JSONArray routes = jsonResponse.getJSONArray("routes");

	        if (routes.length() > 0) {
	            JSONObject route = routes.getJSONObject(0);
	            String geometry = route.getString("geometry");

	            // Decode polyline to list of coordinates
	            List<Coordinate> routeCoordinates = decodePolyline(geometry);

	            // Vẽ tất cả các điểm trước
	            Platform.runLater(() -> {
	                // Xóa các đường trước đó nếu cần
	                mapViewer.removeAllMapPolygons();

	                // Thêm tuyến đường vào bản đồ
	                mapViewer.addMapPolygon(new MapPolygonImpl(routeCoordinates));

	                // Xóa đoạn thừa nối giữa điểm đầu và điểm cuối
	                if (routeCoordinates.size() > 2) {
	                    // Xóa đoạn nối từ điểm đầu (startMarker) tới điểm cuối (endMarker)
	                    routeCoordinates.subList(0, 1).clear(); // Xóa điểm đầu
	                    routeCoordinates.subList(routeCoordinates.size() - 1, routeCoordinates.size()).clear(); // Xóa điểm cuối
	                }

	                // Cập nhật lại bản đồ sau khi xóa đoạn thừa
	                mapViewer.removeAllMapPolygons();  // Xóa lại bản đồ để vẽ mới
	                mapViewer.addMapPolygon(new MapPolygonImpl(routeCoordinates));  // Thêm lại tuyến đường đã loại bỏ đoạn thừa
	            });
	        } else {
	            System.out.println("Không tìm thấy tuyến đường.");
	        }
	    }).exceptionally(ex -> {
	        ex.printStackTrace();
	        return null;
	    });
	}



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

	// Hàm giải mã polyline thành danh sách các tọa độ
	private List<Coordinate> decodePolyline(String encoded) {
	    List<Coordinate> coordinates = new ArrayList<>();
	    int index = 0;
	    int len = encoded.length();
	    int lat = 0;
	    int lng = 0;

	    while (index < len) {
	        int b;
	        int shift = 0;
	        int result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        coordinates.add(new Coordinate(lat / 1E5, lng / 1E5));
	    }

	    return coordinates;
	}

	
	// Xóa các điểm đánh dấu từ truy vấn trước đó khỏi bản đồ trước khi thực hiện truy vấn mới
	public void clearAllMarkers() {
		for (MapMarker marker : markers) {
            mapViewer.removeMapMarker(marker); 
        }
        markers.clear();
    }
	
	

	
}
