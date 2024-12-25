package application;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

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
	private VBox bottomVbox;
	
	@FXML
	private StackPane mapPane;

	@FXML
	private HBox BoxControl;
	
	private BottomSceneController1 bottomSceneController1;
	//private BottomSceneController2 bottomSceneController2;
	
	private CustomMapViewer mapViewer;
	private List<MapMarker> markers;
    private MapMarkerDot currentMarker = null; 
    private MapMarkerDot myLocationMarker = null;
    
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
        Coordinate myLocation = null;
        
		try {
			myLocation = getMyLocation();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		
        mapViewer.setDisplayPosition(myLocation, 12);
        swingNode.setContent(mapViewer);
        mapPane.getChildren().add(swingNode);
        infoLocationButton.fire();
        markers = new ArrayList<>();
        
        mapViewer.addMouseListener(new MouseAdapter() {
        	
        	public void mouseClicked(MouseEvent e) {
        		Coordinate position = (Coordinate) mapViewer.getPosition(e.getX(), e.getY());
  
        		if (position != null) {
                    updateMarker(position.getLat(), position.getLon());
                    
                    try {
						 bottomSceneController1.getInfomationLocal(position.getLat(), position.getLon());
					} catch (IOException | InterruptedException e1) {
						e1.printStackTrace();
					}
				
                }
        	}
		});
	}
	
    public void loadBottomScene1(ActionEvent event) throws IOException {
    	infoLocationButton.getStyleClass().add("selected");
        directionButton.getStyleClass().remove("selected");
        // Thay đổi nội dung bottomVbox cho "Thông tin địa điểm"
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("BottomScene1.fxml"));
        VBox newContent = loader.load();
        bottomSceneController1 = loader.getController();
        bottomVbox.getChildren().setAll(newContent.getChildren());
    }

    public void loadBottomScene2(ActionEvent event) throws IOException {
    	directionButton.getStyleClass().add("selected");
        infoLocationButton.getStyleClass().remove("selected");
        // Thay đổi nội dung bottomVbox cho "Chỉ đường"
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("BottomScene2.fxml"));
        VBox newContent = loader.load();
        //bottomSceneController2 = loader.getController();
        bottomVbox.getChildren().setAll(newContent.getChildren());
    }

	// Tính năng đánh dấu
	public void updateMarker(double lat, double lon) {
		if(currentMarker != null) {
			mapViewer.removeMapMarker(currentMarker);
		}
		
		currentMarker = new CurrentMapMarker(lat, lon, Color.RED, 15);
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
            	MapMarkerDot marker = new RequestMapMarker(lat, lon, Color.RED, 15);
            	markers.add(marker);
            	mapViewer.addMapMarker(marker);
            }
        } else {
            System.out.println("Không tìm thấy");
        }
	}
	
	// Tìm kiếm địa điểm sử dụng Overpass API
	// Amenity
	public void findAmenity(ActionEvent event) throws IOException, InterruptedException {
        clearAllMarkers();
		
        MapMarkerDot markerSource = myLocationMarker;
		Button btn = (Button) event.getSource();
		String key = btn.getText().replace(" ", "_").toLowerCase();
	   
		if(currentMarker != null) {
			markerSource = currentMarker;
		}
		
		double vd  = markerSource.getLat();
		double kd  = markerSource.getLon();
		
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
                    
                    markers.add(new RequestMapMarker(lat, lon, Color.RED, 15));                 
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
        
        MapMarkerDot markerSource = myLocationMarker;
		Button btn = (Button) event.getSource();
		String key = btn.getText().replace(" ", "_").toLowerCase();
	   
		if(currentMarker != null) {
			markerSource = currentMarker;
		}
		
		double vd  = markerSource.getLat();
		double kd  = markerSource.getLon();
		
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
                    
                    markers.add(new RequestMapMarker(lat, lon, Color.RED, 15));
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
		
        MapMarkerDot markerSource = myLocationMarker;
		Button btn = (Button) event.getSource();
		String key = btn.getText().replace(" ", "_").toLowerCase();
	   
		if(currentMarker != null) {
			markerSource = currentMarker;
		}
		
		double vd  = markerSource.getLat();
		double kd  = markerSource.getLon();
		
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
                    
                    markers.add(new RequestMapMarker(lat, lon, Color.RED, 15));
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
		
        MapMarkerDot markerSource = myLocationMarker;
		Button btn = (Button) event.getSource();
		String key = btn.getText().replace(" ", "_").toLowerCase();
	   
		if(currentMarker != null) {
			markerSource = currentMarker;
		}
		
		double vd  = markerSource.getLat();
		double kd  = markerSource.getLon();
		
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
                    
                    markers.add(new RequestMapMarker(lat, lon, Color.RED, 15));
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
	public void markRouting1(Consumer<JSONObject> callback) throws IOException, InterruptedException {
	    count = 0;
		if (count == 0) {
            mapViewer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Khi người dùng click, lấy vị trí và đánh dấu điểm
                    Coordinate position = (Coordinate) mapViewer.getPosition(e.getX(), e.getY());
                    startMarker = new CurrentMapMarker(position.getLat(), position.getLon(), Color.RED, 15);
            		mapViewer.addMapMarker(startMarker);
            		System.out.println("Điểm bắt đầu đã được đánh dấu.");
            		
            		try {
                        JSONObject info = getInfomationLocal(startMarker.getLat(), startMarker.getLon());
                        callback.accept(info);  // Trả kết quả qua callback
                    } catch (IOException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
            		
                    count = 1;  // Đánh dấu rằng đã nhận click
                    // Xoá MouseListener sau khi click lần đầu tiên
                    mapViewer.removeMouseListener(this);  // Loại bỏ MouseListener
                }
            });
        }
	}
	
	public void markRouting2(Consumer<JSONObject> callback) throws IOException, InterruptedException {
	    count = 0;
		if (count == 0) {
            mapViewer.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Khi người dùng click, lấy vị trí và đánh dấu điểm
                    Coordinate position = (Coordinate) mapViewer.getPosition(e.getX(), e.getY());
                    endMarker = new CurrentMapMarker(position.getLat(), position.getLon(), Color.RED, 15);
            		mapViewer.addMapMarker(endMarker);
            		System.out.println("Điểm kết thúc đã được đánh dấu.");
            		
            		try {
                        JSONObject info = getInfomationLocal(endMarker.getLat(), endMarker.getLon());
                        callback.accept(info);  // Trả kết quả qua callback
                    } catch (IOException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
            		
                    count = 1;  // Đánh dấu rằng đã nhận click
                    // Xoá MouseListener sau khi click lần đầu tiên
                    mapViewer.removeMouseListener(this);  // Loại bỏ MouseListener
                }
            });
        }
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
	
	// Lấy tọa độ của máy
	public Coordinate getMyLocation() throws IOException, InterruptedException {
	    String url = "https://freegeoip.app/json/";

	    HttpClient client = HttpClient.newBuilder()
	            .followRedirects(HttpClient.Redirect.ALWAYS) // Xử lý chuyển hướng
	            .build();

	    HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(url))
	            .header("User-Agent", "TestMap/1.0 (duongnguyentung2229@gmail.com)")
	            .build();

	    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
	    String responseBody = response.body();

	    if (responseBody.trim().startsWith("{")) {
	        JSONObject jsonResponse = new JSONObject(responseBody);

	        double latitude = jsonResponse.getDouble("latitude");
	        double longitude = jsonResponse.getDouble("longitude");

	        if (mapViewer != null) {
	        	myLocationMarker = new CustomMapMarker(latitude, longitude, Color.BLUE, 15, 2);
	            mapViewer.addMapMarker(myLocationMarker);
	        }

	        return new Coordinate(latitude, longitude);
	    } else {
	        throw new RuntimeException("Invalid response: " + responseBody);
	    }
	}
	
	// Lấy thông tin địa điểm đánh dấu 
	public JSONObject getInfomationLocal(double lat, double lon) throws IOException, InterruptedException {
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
		
		return jsonResult;
	}

	public MapMarkerDot getStartMarker() {
		return startMarker;
	}

	public MapMarkerDot getEndMarker() {
		return endMarker;
	}
}
