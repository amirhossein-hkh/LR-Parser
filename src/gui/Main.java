package gui;

import static javafx.fxml.FXMLLoader.load;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = load(getClass().getResource("Input.fxml"));
		primaryStage.setTitle("LR Parser");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
