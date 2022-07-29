package us.malfeasant.commode64;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 *
 * @author Malfeasant
 */
public class SpeedButtons {
    private final HBox speedButtonBox;
    private final ToggleGroup speedButtonGroup = new ToggleGroup();
    private final ReadOnlyObjectProperty<SpeedSet> speedProperty;
    
    SpeedButtons() {
        var stepButton = makeButton(SpeedSet.STEP);
        stepButton.setOnAction(e -> {	// immediately unselect this once clicked
        	stepButton.setSelected(false);
        });
        var playButton = makeButton(SpeedSet.REAL);
        var fastButton = makeButton(SpeedSet.FAST);
        speedButtonBox = new HBox(stepButton, playButton, fastButton);
        
        var prop = new ReadOnlyObjectWrapper<SpeedSet>();
        prop.bind(Bindings.createObjectBinding(() -> {
        	var selected = speedButtonGroup.selectedToggleProperty().getValue();
        	return selected == null ? null : (SpeedSet) (selected.getUserData());
    	}, speedButtonGroup.selectedToggleProperty()));
        speedProperty = prop.getReadOnlyProperty();
    }
    
    private RadioButton makeButton(SpeedSet s) {
    	var button = new RadioButton(s.label);
        button.getStyleClass().remove("radio-button");	// pain in arse.  I want the behavior of a radio button
        button.getStyleClass().add("toggle-button");	// but the look of a toggle button.
        button.setTooltip(new Tooltip(s.tooltip));
        button.setToggleGroup(speedButtonGroup);
        button.setUserData(s);
        return button;
    }
    
    public enum SpeedSet {
    	STEP(">|", "Pause/Single Step"), REAL(">", "Run realtime"), FAST(">>", "Run unrestricted");
    	final String label;
    	final String tooltip;
    	SpeedSet(String label, String tooltip) {
    		this.label = label;
    		this.tooltip = tooltip;
    	}
    }
    HBox getButtonBox() {
        return speedButtonBox;
    }
    
    /**
     * Listen to this property to be alerted when button selection changes.
     * @return read only property which holds a speed enum- can be null, in fact will be null if machine is paused.
     */
    ReadOnlyObjectProperty<SpeedSet> selectedSpeedProperty() {
    	return speedProperty;
    }
}
