package us.malfeasant.commode64.plumbing;

public interface Port {
	interface R extends Port {
		int read();
	}
	interface W extends Port {
		void write(int w);
		void release();
	}
	interface RW extends R, W {
		int getDir();
		void setDir(int d);
	}
}
