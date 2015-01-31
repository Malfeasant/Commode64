package us.malfeasant.commode64.cia;

/**
 * This only tests proper functionality- testing improper functionality (i.e. writing nonsense values) can wait.
 * @author Mischa
 *
 */
public class TestToD {

	public static void main(String[] args) {
		ToD tod = new ToD();
		tod.set(3, 0x12, true);
		for (int i = 0; i < 0x100; i++) {
			tod.tick();
			System.out.println("i = " + i + ":");
			switch (i) {
			case 0x8c:
			case 0x4c:
			case 0xc:
				System.out.println("Starting clock:");
				tod.set(0, 0, false);
				break;
			case 0x40:
				System.out.println("Setting hours to 11am:");
				tod.set(3, 0x11, false);
				break;
			case 0xa4:
			case 0x64:
			case 0x84:
			case 0x44:
				System.out.println("Setting minutes to 59:");
				tod.set(2, 0x59, false);
				break;
			case 0xa8:
			case 0x68:
			case 0x88:
			case 0x48:
				System.out.println("Setting seconds to 59:");
				tod.set(1, 0x59, false);
				break;
			case 0x80:
				System.out.println("Setting hours to 11pm:");
				tod.set(3, 0x91, false);
				break;
			}
			System.out.println(tod.debug());
		}
	}
}
