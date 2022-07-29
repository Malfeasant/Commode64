package us.malfeasant.commode64;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        //final Parameters params = getParameters();
        // TODO: more thorough arg parsing
        
    	var imp = new Impetus();
    	
    	stage.setOnCloseRequest(e -> {
    		// TODO determine if anything should be saved- disk image or similar?
    		imp.shutdown();
    	});
    	
        // Setup the buttons which control simulation speed, stepping
        
        var pane = new BorderPane();
        var speedButtons = new SpeedButtons();
        imp.bindSpeedProp(speedButtons.selectedSpeedProperty());
        pane.setBottom(speedButtons.getButtonBox());
        stage.setScene(new Scene(pane));
        stage.show();
    }

    public static void main(String[] args) {
        // TODO: Basic arg parsing, abort on flagrant errors
        launch(args);
    }

}