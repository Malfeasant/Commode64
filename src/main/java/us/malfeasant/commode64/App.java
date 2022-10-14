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
	public static final boolean DEBUG = true;	// Enables extra debugging stuff that might severely impact performance.
	// Supposedly since this is constant, if it's false the whole block should not even be compiled in...
	
    @Override
    public void start(Stage stage) {
        //final Parameters params = getParameters();
        // TODO: more thorough arg parsing
    	
    	// TODO controls for crystal/power, for now just pull from prefs
    	var config = Config.getDefault();
    	
    	var view = new ImageView();
    	var imp = new Impetus();
    	var machine = new Machine();
    	machine.variantProperty().bind(config.variantProperty());
    	imp.addCrystalListener(machine);
    	imp.addPowerListener(machine);
    	view.imageProperty().bind(machine.imageProperty());
    	view.viewportProperty().bind(machine.viewportProperty());
    	imp.crystalProp().bind(config.crystalProperty());
    	imp.powerProp().bind(config.powerProperty());
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