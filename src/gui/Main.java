package gui;

import static javafx.fxml.FXMLLoader.load;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("LR Parser");
		primaryStage.setScene(new Scene(load(getClass().getResource("Input.fxml"))));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
