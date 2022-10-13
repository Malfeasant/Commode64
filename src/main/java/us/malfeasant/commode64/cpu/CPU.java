package us.malfeasant.commode64.cpu;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CPU {
	public final BooleanProperty aecProp = new SimpleBooleanProperty(false);
	public final BooleanProperty rdyProp = new SimpleBooleanProperty(false);
	public final BooleanProperty resProp = new SimpleBooleanProperty(true);
	public final BooleanProperty irqProp = new SimpleBooleanProperty(false);
	public final BooleanProperty nmiProp = new SimpleBooleanProperty(false);
	
	State currentState = State.RESET;
	
	int accumulator;
	int x;
	int y;
	int pcl;	// pc is split in a real machine- for accuracy, it's split here too
	int pch;
	int sp;
	// status reg:
	boolean neg;
	boolean zero;
	boolean carry;
	boolean overflow;
	boolean decimal;
	boolean disableIrq;
	
	public void crystalTick() {
		
	}
}
