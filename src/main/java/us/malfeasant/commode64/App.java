package us.malfeasant.commode64;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import us.malfeasant.commode64.machine.Machine;
import us.malfeasant.commode64.timing.Impetus;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        //final Parameters params = getParameters();
        // TODO: more thorough arg parsing
        
    	var view = new ImageView();
    	
    	var imp = new Impetus();
    	var machine = new Machine();
    	imp.addCrystalListener(machine);
    	imp.addPowerListener(machine);
    	machine.bindImageProperty(view.imageProperty());
    	imp.start();
    	
    	stage.setOnCloseRequest(e -> {
    		// TODO determine if anything should be saved- disk image or similar?
    		imp.stop();
    	});
    	
        var pane = new BorderPane(view);
        stage.setScene(new Scene(pane));
        stage.show();
    }

    public static void main(String[] args) {
        // TODO: Basic arg parsing, abort on flagrant errors
        launch(args);
    }

}