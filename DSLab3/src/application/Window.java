package application;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
//import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import message.*;

public class Window {
	private JButton button = null;
	private JButton recvButton = null;
	private JCheckBox logCheckBox = null;
	private JFrame frame = null;
	private JTextArea textArea = null;
	private JTextArea recvArea = null;
	private JScrollPane scrollPane = null;
	private JScrollPane recvScrollPane = null;
	
	private MultiCastMsgPasser msgPass = null;
	private String localName = "";
	
	public Window(String configFileName, String localName) {
		try {
			msgPass = new MultiCastMsgPasser(configFileName, localName);
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
		button = new JButton("send");
		button.setBounds(250, 220, 80, 40);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setHorizontalTextPosition(JButton.CENTER);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String line = textArea.getText();
			    String[] words = line.trim().split("\\s+");
			    if (words.length < 3) {
			    	System.out.println("The destination and message kind and message body can not be empty!");
			    	return;
			    }
			    StringBuffer data = new StringBuffer();
			    for (int i = 2; i < words.length; i++) {
			    	data.append( words[i] );
			    	data.append(" ");
			    }
			    String mynewstring = data.toString();
			    
			    boolean isUniCast = msgPass.msgPass.configFile.isNodeExist(words[0]);
			    boolean isMultiCast = msgPass.msgPass.configFile.isGrpExist(words[0]);
			    if (isUniCast == false && isMultiCast == false) {
			    	textArea.setText("Can not find this destnation!");
			    } else if (isUniCast == true) {
			    	Message m = new TimeStampedMessage(words[0], words[1], logCheckBox.isSelected(), mynewstring, msgPass.msgPass.GetClock().GetTimeStamp());
			    	msgPass.msgPass.GetClock().Update();
			    	msgPass.msgPass.send(m);
			    	textArea.setText("");
			    } else {
			    	msgPass.multCast(words[0], words[1], logCheckBox.isSelected(), mynewstring);
			    	textArea.setText("");
			    }
				
			    //Message msg = new Message(words[0], words[1], mynewstring);
			    //msgPass.send(msg);
			    
				//System.out.println("ActionEvent!");
			}
		});
		
		/*
		button.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				System.out.println("ChangeEvent!");
			}
		});
		*/
		frame.add(button);
		frame.getRootPane().setDefaultButton(button);
		
		logCheckBox = new JCheckBox("record log");
		logCheckBox.setBounds(100, 220, 120, 40);
		logCheckBox.setSelected(false);
		frame.add(logCheckBox);
		
		textArea = new JTextArea(10, 35);
        textArea.setEditable(true);
        textArea.setLineWrap(true);
        scrollPane = new JScrollPane(textArea);
        Dimension textAreaSize = textArea.getPreferredSize();
        scrollPane.setBounds(100, 50, textAreaSize.width, textAreaSize.height);
        frame.add(scrollPane);
        
        //JLabel label = new JLabel("received messages: ");
        //label.setBounds(100, 330, 150, 20);
        //frame.add(label);
        
        recvArea = new JTextArea(10, 35);
        recvArea.setEditable(false);
        recvArea.setLineWrap(true);
        recvScrollPane = new JScrollPane(recvArea);
        Dimension recvTextAreaSize = recvArea.getPreferredSize();
        recvScrollPane.setBounds(100, 350, recvTextAreaSize.width, recvTextAreaSize.height);
        frame.add(recvScrollPane);
        
        recvButton = new JButton("receive");
        recvButton.setBounds(250, 520, 80, 40);
        recvButton.setMargin(new Insets(0, 0, 0, 0));
        recvButton.setHorizontalTextPosition(JButton.CENTER);
        recvButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Message m = msgPass.receive();
				if (m != null) {
					//String res = m.getSeqNum() + " " + m.getSource() + ": " + m.getData() + " " + MessagePasser.printBuffer(m.GetTimeStamp()) + "\n";
					//recvArea.append(Arrays.toString(msgPass.msgPass.GetClock().GetTimeStamp()));
					recvArea.append((m.toString()+"\n\n"));
					
				}
			}
		});
        frame.add(recvButton);
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
		Window mainWin = new Window(args[0], args[1]);
		//mainWin.readMsg();
	}
}
