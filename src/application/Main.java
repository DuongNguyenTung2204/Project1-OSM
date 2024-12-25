package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {

    // Static biến lưu trữ tham chiếu đến MapController
    private static MapController mapController;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));
            Parent root = loader.load();
            
            // Lấy controller từ FXML và lưu vào biến static
            mapController = loader.getController();
            
            primaryStage.setTitle("Map Viewer");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);        
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Phương thức static để lấy MapController
    public static MapController getMapController() {
        return mapController;
    }
}
