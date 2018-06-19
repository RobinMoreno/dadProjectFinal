package dad.us.dadVertx;

import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Actuator {

	private int id;
	private String name;
	private boolean state;
	private ActuatorType type;
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
	
	public Actuator() {
		
		this("", false, null);

	}
	

	public Actuator (@JsonProperty("name") String name, @JsonProperty("state") Boolean state,
			@JsonProperty("type") ActuatorType type) {
		
		super();
		this.id = COUNTER.getAndIncrement();
		this.name = name;
		this.state = state;
		this.type = type;
		
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public boolean isState() {
		return state;
	}


	public void setState(boolean state) {
		this.state = state;
	}


	public ActuatorType getType() {
		return type;
	}


	public void setType(ActuatorType type) {
		this.type = type;
	}


	public int getId() {
		return id;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (state ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuator other = (Actuator) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (state != other.state)
			return false;
		if (type != other.type)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Actuator [id=" + id + ", name=" + name + ", state=" + state + ", type=" + type + "]";
	}
	
	
}
