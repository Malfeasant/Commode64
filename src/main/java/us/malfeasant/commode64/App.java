package us.malfeasant.commode64;

import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import us.malfeasant.commode64.machine.Machine;
import us.malfeasant.commode64.timing.Crystal;
import us.malfeasant.commode64.timing.Impetus;
import us.malfeasant.commode64.timing.Power;

/**
 * JavaFX App
 */
public class App extends Application {
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	
    @Override
    public void start(Stage stage) {
        //final Parameters params = getParameters();
        // TODO: more thorough arg parsing
    	
    	// TODO controls for crystal/power, for now just pull from prefs
    	Crystal crys = Crystal.valueOf(prefs.get(Crystal.class.getSimpleName(), Crystal.NTSC.name()));
    	Power power = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.NA.name()));
    	
    	var view = new ImageView();
    	var imp = new Impetus();
    	var machine = new Machine();
    	imp.addCrystalListener(machine);
    	imp.addPowerListener(machine);
    	view.imageProperty().bind(machine.imageProperty());
    	view.viewportProperty().bind(machine.viewportProperty());
    	imp.crystalProp().set(crys);
    	imp.powerProp().set(power);
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