package kz.atu.inventory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/kz/atu/inventory/main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(
                getClass().getResource("/kz/atu/inventory/style.css").toExternalForm());

        stage.setTitle("Учёт склада");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.setOnCloseRequest(e -> controller.saveQuietly());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}