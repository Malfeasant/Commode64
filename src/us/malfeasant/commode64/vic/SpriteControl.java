package us.malfeasant.commode64.vic;

class SpriteControl {
	int x;
	int y;
	boolean enable;
	boolean expandX;
	boolean expandY;
	boolean behind;
	boolean multicolor;
	boolean collideS;
	boolean collideFG;
	Color color;
	int data;	// this is stored in RAM at the end of text matrix, but is looked up once per line and held
}
