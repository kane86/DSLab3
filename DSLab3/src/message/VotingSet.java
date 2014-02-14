package message;

import clockService.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VotingSet {
   
    private ArrayList<String> members = null;
  
	public VotingSet(List<String> membersInGrp) {
	
		members = new ArrayList<String>();
		for (String member : membersInGrp) {
			members.add(member);
		}
	}
	
	public VotingSet() {
		members = new ArrayList<String>();
	}
	
	public void addMember(String member) {
		members.add(member);
	}
	
	public ArrayList<String> getMembers() {
		return members;
	}
}