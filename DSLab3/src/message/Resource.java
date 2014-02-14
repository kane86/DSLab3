package message;

public class Resource {
	String name;
	String curOwner;
	ResourceState state = ResourceState.FREE;
	
	public Resource(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
		return;
	}
	
	public String getCurOwner() {
		return this.curOwner;
	}
	
	public void setCurOwner(String owner) {
		this.curOwner = owner;
		return;
	}
	
	public ResourceState getState() {
		return this.state;
	}
	
	public void setState(ResourceState state) {
		this.state = state;
		return;
	}
}