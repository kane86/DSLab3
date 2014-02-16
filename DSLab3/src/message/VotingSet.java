package message;

import clockService.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VotingSet {
   
    private ArrayList<String> members = null;
    String name;
  
	public VotingSet(List<String> membersInGrp) {

		members = new ArrayList<String>();
		for (String member : membersInGrp) {
			members.add(member);
		}
	}

	public VotingSet(String name) {
		members = new ArrayList<String>();
		this.name = name;
	}

	public VotingSet(List<String> membersInGrp, String name) {

		members = new ArrayList<String>();
		for (String member : membersInGrp) {
			members.add(member);
		}

		this.name = name;
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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
		return;
	}
}