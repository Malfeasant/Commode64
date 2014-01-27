package us.malfeasant.commode64.vic;

public enum Color {
	BLACK(0x000000), WHITE(0xFFFFFF), RED(0x882000), CYAN(0x68D0A8),
	PURPLE(0xA838A0), GREEN(0x50B818), BLUE(0x181090), YELLOW(0xF0E858),
	ORANGE(0xA04800), BROWN(0x472B1B), LRED(0xC87870), DGRAY(0x484848),
	MGRAY(0x808080), LGREEN(0x98FF98), LBLUE(0x5090D0), LGRAY(0xB8B8B8);
	public final int rgb;
	Color(int def) {
		rgb = def;
	}
}
