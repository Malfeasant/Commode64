package us.malfeasant.commode64.cpu;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CPU {
	public final BooleanProperty aecProp = new SimpleBooleanProperty(false);
	public final BooleanProperty baProp = new SimpleBooleanProperty(false);
	public final BooleanProperty resProp = new SimpleBooleanProperty(true);
	public final BooleanProperty irqProp = new SimpleBooleanProperty(false);
	public final BooleanProperty nmiProp = new SimpleBooleanProperty(false);
	
	public void crystalTick() {
		
	}
}
