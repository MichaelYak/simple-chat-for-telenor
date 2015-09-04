package chat;

import java.util.Scanner;

public class DisplayService implements Runnable {
	private Scanner socketIn;

	public DisplayService(Scanner socketIn) {
		this.socketIn = socketIn;
	}

	public void run() {

		while (true) {
			if (socketIn.hasNext()) {
				String line = socketIn.nextLine();
				System.out.println(line);
			} else {
				return;
			}
		}
	}
}