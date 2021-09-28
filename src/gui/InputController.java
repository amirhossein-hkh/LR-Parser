package gui;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.layout.AnchorPane.setBottomAnchor;
import static javafx.scene.layout.AnchorPane.setLeftAnchor;
import static javafx.scene.layout.AnchorPane.setRightAnchor;
import static javafx.scene.layout.AnchorPane.setTopAnchor;
import static javafx.scene.text.Font.font;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.stage.Modality.APPLICATION_MODAL;
import static javafx.stage.StageStyle.UTILITY;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lr0.LR0Parser;
import lr1.LR1Parser;
import util.Grammar;
import util.LRParser;

public class InputController implements Initializable {

	public static LRParser lrParser;
	
	private static String parserValue;
	private static String inputText; 

	@FXML private Label error;
	@FXML private TextArea input;
	@FXML private ComboBox parser;

	@FXML
	private void handleStart(ActionEvent event) throws IOException {
		if (parser.getValue() == null){
			error.setText("Choose a parser");
			error.setVisible(true);
			return;
		}
		
		boolean canBeParse = true;
		Grammar grammar = new Grammar(inputText = input.getText());
		parserValue = (String) parser.getValue();
		if (parserValue.equals("LR(0)") || parserValue.equals("SLR(1)")){
			LR0Parser lr0Parser; lrParser = lr0Parser = new LR0Parser(grammar);
			//LR0Parser lr0Parser = new LR0Parser(grammar); lrParser = lr0Parser;
			canBeParse = parserValue.equals("LR(0)") ? lr0Parser.parserLR0() : lr0Parser.parserSLR1();
		}
		else {
			LR1Parser lr1Parser; lrParser = lr1Parser = new LR1Parser(grammar);
			//LR1Parser lr1Parser = new LR1Parser(grammar); lrParser = lr1Parser;
			canBeParse = parserValue.equals("CLR(1)") ? lr1Parser.parseCLR1() : lr1Parser.parseLALR1();
		}
		if (!canBeParse) {
			error.setText("The grammar can not be parsed, click for details. choose a different parser or grammar.");
			error.setOnMouseClicked(e-> alert("\n" + lrParser.getLog() + "\n" + lrParser.getStatesList()));
			error.setVisible(true);
			return;
		}
		
		Button button = (Button) event.getSource();
		Stage stage = (Stage) button.getScene().getWindow();
		Parent root = FXMLLoader.load(getClass().getResource("Output.fxml"));
		Scene scene = new Scene(root);
		stage.setScene(scene);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		error.setVisible(false);
		parser.setItems(
			observableArrayList(
				"LR(0)",
				"SLR(1)",
				"CLR(1)",
				"LALR(1)"
			)
		);
		parser.setValue(parserValue);
		input.setText(inputText);
	}
	
	void alert(String text) {
		TextArea textArea = new TextArea(text);
		textArea.setFont(font("Monospaced", REGULAR , 13));
		setTopAnchor(textArea, 0.0);
		setRightAnchor(textArea, 0.0);
		setBottomAnchor(textArea, 0.0);
		setLeftAnchor(textArea, 0.0);
		Stage stage = new Stage(UTILITY);
		stage.setTitle("Grammar Conflicts");
		stage.initModality(APPLICATION_MODAL);
		stage.setScene(new Scene(new AnchorPane(textArea), 400, 550));
		stage.show();
	}
}
