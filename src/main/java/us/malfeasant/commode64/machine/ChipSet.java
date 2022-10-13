package us.malfeasant.commode64.machine;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import us.malfeasant.commode64.machine.video.Video;

/**
 * Holds all chips as a set of properties - not sure I'm going to allow these to be changed after construction,
 * but if so, they can only be changed here.
 * @author Malfeasant
 */
public class ChipSet {
	final ReadOnlyObjectWrapper<Video> videoWrapper;
	public final ReadOnlyObjectProperty<Video> videoProp;
	// TODO properties for SID, CIAs, I/O expansions, anything else?
	ChipSet(Video v) {
		videoWrapper = new ReadOnlyObjectWrapper<Video>(v);
		videoProp = videoWrapper.getReadOnlyProperty();
	}
}
