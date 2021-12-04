package gui;

import static gui.InputController.lrParser;
import static javafx.fxml.FXMLLoader.load;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class OutputController implements Initializable {

	@FXML private TextField input;
	@FXML private TextArea output;
	@FXML private Label result;

	@FXML
	private void handleGrammar(ActionEvent event) {
		output.setText("\n" + lrParser.getGrammar());
	}

	@FXML
	private void handleFirst(ActionEvent event) {
		output.setText("\n" + lrParser.getGrammar().getFirstSets());
	}

	@FXML
	private void handleFollow(ActionEvent event) {
		output.setText("\n" + lrParser.getGrammar().getFallowSets());
	}

	@FXML
	private void handleState(ActionEvent event) {
		output.setText("\n" + lrParser.getStatesList());
	}

	@FXML
	private void handleGoTo(ActionEvent event) {
		output.setText("\n" + lrParser.getActionGoToTable().toString(1));
	}

	@FXML
	private void handleAction(ActionEvent event) {
		output.setText("\n" + lrParser.getActionGoToTable().toString(2));
	}

	@FXML
	private void handleActionGoTo(ActionEvent event) {
		output.setText("\n" + lrParser.getActionGoToTable());
	}

	@FXML
	private void handleLog(ActionEvent event) {
		output.setText("\n" + lrParser.getLog());
	}

	@FXML
	private void handleBack(ActionEvent event) throws IOException {
		Button button = (Button) event.getSource();
		Stage stage = (Stage) button.getScene().getWindow();
		stage.setScene(new Scene(load(getClass().getResource("Input.fxml"))));
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		result.setVisible(false);
		input.textProperty().addListener(
			(observable, oldValue, newValue)-> {
				try {
					if (lrParser.accept(input.getText())) {
						result.setText("Accepted");
						result.setTextFill(GREEN);
						result.setVisible(true);
					}
					else {
						result.setText("Rejected");
						result.setTextFill(RED);
						result.setVisible(true);
					}
					output.setText("\n" + lrParser.getLog());
				}
				catch (Throwable t) {
					//output.setText("\n" + lrParser.getLog() + "\n" + t);
					var sw = new StringWriter();
					t.printStackTrace(new PrintWriter(sw));
					output.setText("\n" + lrParser.getLog() + "\n" + sw.toString());
					throw t;
				}
			}
		);
		output.setText("\n" + lrParser.getGrammar());
	}
}
