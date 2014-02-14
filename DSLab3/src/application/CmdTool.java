package application;
/* 18-842 Distributed Systems
 * Lab 0
 * Group 41 - ajaltade & dil1
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import message.*;

public class CmdTool {
	/*
	 * Command Type:
	 * quit: quit the whole process
	 * ps: print the information of current MessagePasser
	 * send command: dest <kind> <data>
	 * send log command : log <dest> <kind> <data>
	 * receive command: receive
	 * receive log command : receive log
	 * 
	 */
	private MultiCastMsgPasser msgPasser;
	public CmdTool(MultiCastMsgPasser msgPasser) {
		this.msgPasser = msgPasser;
	}
	public void executing() {
		String cmdInput = new String();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Message msg = null;
        
        while (!cmdInput.equals("quit")) {
        	
            System.out.print("CommandLine% ");
            
            try {
                cmdInput = in.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if(cmdInput.equals("quit")) {
       
            	System.exit(0);
            	
            } else if(cmdInput.equals("ps")) {
            	
            	System.out.println(this.msgPasser.toString());
            	
            } else if(cmdInput.equals("cleanup")) {
            	
            	System.out.println("we clean up the messagePasser and ClockService");
            	//this.msgPasser.cleanUp();
            	
            } else if (!cmdInput.equals(null) && !cmdInput.equals("\n")) {
            	
            	String[] array = cmdInput.split(" ");
            	if (array.length == 3) {
            		
            		//this.msgPasser.send(new TimeStampedMessage(array[0], array[1], array[2], null));
            		this.msgPasser.multCast(array[0], array[1], false, array[2]);
            		
            	} else if(cmdInput.equals("receive")) {
            		
            		msg = this.msgPasser.receive();		
            		if(msg == null) {
            			System.out.println("Nothing to pass to Aplication!");
            		} else {
            			System.out.println("We received: " + msg.getData());
            		}
            	}  else if(array.length == 2) {
            		
            		if(array[0].equals("receive") && array[1].equals("log")) {
            			msg = this.msgPasser.receive();
            			if(msg == null) {
            				System.out.println("Nothing to pass to Aplication!");
            			}
            			else {
            				System.out.println("We receive");
//((TimeStampedMessage)msg).dumpMsg();
               				//this.msgPasser.logEvent(((TimeStampedMessage)msg).getMsg(), this.msgPasser.getClockSer().getTs().makeCopy());
            			}
            				
            		} else if(array[0].equals("event")) {
            			
//System.out.println("Lamport time " + this.msgPasser.getClockSer().getTs().getLamportClock());
            			//this.msgPasser.getClockSer().addTS(this.msgPasser.getLocalName());
//System.out.println("Lamport time " + this.msgPasser.getClockSer().getTs().getLamportClock());
            			//this.msgPasser.logEvent(array[1], this.msgPasser.getClockSer().getTs().makeCopy());
            			
            		}
            		else {
            			System.out.println("Invalid Command!");
            		}
            	}
            	else if(array.length == 4) {
            		if(array[0].equals("log")) {
            			//TimeStampedMessage newMsg = new TimeStampedMessage(array[1], array[2], array[3], null);
            			//this.msgPasser.send(newMsg);
//System.out.println("send TS:" + this.msgPasser.getClockSer().getTs());
            			//this.msgPasser.logEvent(newMsg.getMsg(), this.msgPasser.getClockSer().getTs().makeCopy());
            		}
            		else {
            			System.out.println("Invalid Command!");
            		}
            	}
            	else {
            		System.out.println("Invalid Command!");
            	}
            	
            } else {
            	System.out.println("Invalid Command!");
            }
        }
	}
	

	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Arguments mismtach.\n" +
					"Required arguments - <Yaml config> <name of host>");
			System.exit(0);
		}
		MultiCastMsgPasser msgPasser = null;
		try {
			msgPasser = new MultiCastMsgPasser(args[0], args[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CmdTool tool = new CmdTool(msgPasser);
		tool.executing();
	}
}
