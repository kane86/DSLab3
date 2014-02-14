package application;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
//import javax.swing.ImageIcon;
import javax.swing.*;

import message.*;

public class LogWindow {
	private JButton button = null;
	private JFrame frame = null;
	private JTextArea textArea = null;
	private JScrollPane scrollPane = null;
	
	private LogServerPasser msgPass = null;
	private String localName = "";
	
	public LogWindow(String configFileName, String localName) {
		try {
			msgPass = new LogServerPasser(configFileName, localName);
		} catch (IOException e) {
			System.out.println("Error! Can't start window!");
		}
		//Console console = new Console(configFileName, localName);
		//msgPass = new MessagePasser(configFileName, localName);
		this.localName = localName;
		
		frame = new JFrame("window");
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.out.println("Process shutted down!");
		        System.exit(0);
		    }
		}); 
		frame.setSize(600, 650);
		Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.width > displaySize.width)
			frameSize.width = displaySize.width;
		if (frameSize.height > displaySize.height)
			frameSize.height = displaySize.height;
		frame.setLocation((displaySize.width - frameSize.width) / 2,
				(displaySize.height - frameSize.height) / 2);
		
		add();
		frame.setVisible(true);
	}
	
	public void add() {
		frame.setTitle(localName);
		frame.setLayout(null);
		button = new JButton("print");
		button.setBounds(250, 20, 80, 40);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setHorizontalTextPosition(JButton.CENTER);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				msgPass.sortMessage();
			    textArea.setText(msgPass.outputMessages());
			}
		});
		

		frame.add(button);
		frame.getRootPane().setDefaultButton(button);
		
		textArea = new JTextArea(30, 35);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        scrollPane = new JScrollPane(textArea);
        Dimension textAreaSize = textArea.getPreferredSize();
        scrollPane.setBounds(100, 70, textAreaSize.width, textAreaSize.height);
        frame.add(scrollPane);
        
        //JLabel label = new JLabel("received messages: ");
        //label.setBounds(100, 330, 150, 20);
        //frame.add(label);
	}
	
	/*
	public void readMsg() {
		while (true) {
			Message msg = msgPass.receive();
			if (msg != null) {
				String res = "From " + msg.getSrc() +" seq num "+ msg.getSeqNum() + " : " + msg.getData().toString() + "\n";
				//System.out.println(msg.getData().toString());
				recvArea.append(res);
			}
		}
	}
	*/
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Please input the configuration file and local name by arguments!");
			return;
		}
		LogWindow mainWin = new LogWindow(args[0], args[1]);
		//mainWin.readMsg();
	}
}
