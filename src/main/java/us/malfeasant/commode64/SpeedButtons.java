/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package us.malfeasant.commode64;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 *
 * @author mischa
 */
class SpeedButtons {
    private final HBox speedButtonBox;
    private final ToggleGroup speedButtonGroup;
    SpeedButtons() {
        speedButtonGroup = new ToggleGroup();
        var stepButton = new ToggleButton(">|");
        stepButton.setTooltip(new Tooltip("Pause/Single Step"));
        stepButton.setToggleGroup(speedButtonGroup);
        var playButton = new ToggleButton(">");
        playButton.setTooltip(new Tooltip("Run realtime"));
        playButton.setToggleGroup(speedButtonGroup);
        var fastButton = new ToggleButton(">>");
        fastButton.setTooltip(new Tooltip("Run unrestricted"));
        fastButton.setToggleGroup(speedButtonGroup);
        speedButtonBox = new HBox(stepButton, playButton, fastButton);
    }
    
    HBox getButtonBox() {
        return speedButtonBox;
    }
    
    /**
     * Listen to this property to be alerted when button selection changes.
     * @return ToggleGroup's selectedToggleProperty
     */
    ReadOnlyObjectProperty<Toggle> selectedToggleProperty() {
        return speedButtonGroup.selectedToggleProperty();
    }
}
