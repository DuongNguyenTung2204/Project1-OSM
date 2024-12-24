package application;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class BottomSceneController1 implements Initializable{
	
	@FXML
	private Label latLabel;
	
	@FXML
	private Label lonLabel;

	@FXML
	private Label displayNameLabel;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
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
