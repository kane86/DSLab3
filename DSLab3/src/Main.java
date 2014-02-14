import java.io.BufferedReader;
import java.io.InputStreamReader;

import application.Console;

public class Main {

	public static void main(String[] args) {
		Console console = new Console("./test.yaml", args[0]);
		while (true) {
			try {
				BufferedReader bufferRead = new BufferedReader(
						new InputStreamReader(System.in));
				console.Display();
				String s = bufferRead.readLine();
				if (s.equals("exit")) {
					break;
				}
				console.Action(s);
			} catch (Exception e) {
				System.out.println("Shut down console...");
			}
		}
	}

}
