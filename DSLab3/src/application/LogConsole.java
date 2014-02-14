package application;

import java.io.IOException;

import message.*;

public class LogConsole {

	private LogServerPasser passer;

	public LogConsole(String configuration_filename, String local_name) {
		try {
			passer = new LogServerPasser(configuration_filename, local_name);
		} catch (IOException e) {
			System.out.println("Error! Can't start console");
		}
	}
	
	public void Display() {
		System.out.print(passer.GetLocalName() + ">> ");
	}

	public void Action(String s) {
		String[] ss = s.split(" ");
		if (ss.length <= 0) {
			System.out.println();
		} 
		if (ss[0].equals("print") == false) {
			System.out.println("The log server only take only one command \"print\"!");
		} else {
		    passer.sortMessage();
		    System.out.print(passer.outputMessages());
		}
	}

}
