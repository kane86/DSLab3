import java.io.BufferedReader;
import java.io.InputStreamReader;

import application.*;

public class MainLog {

	public static void main(String[] args) {
		LogConsole console = new LogConsole("./test.yaml", args[0]);
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
