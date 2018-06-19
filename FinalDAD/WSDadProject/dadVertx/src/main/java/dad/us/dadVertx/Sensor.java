package dad.us.dadVertx;

import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sensor {

	private int id;
	private String name;
	private boolean state;
	private SensorType type;
	private boolean reading;
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
	
	public Sensor() {
		
		this("", false, null, false);

	}
	

	public Sensor (@JsonProperty("name") String name, @JsonProperty("state") Boolean state,
			@JsonProperty("type") SensorType type, @JsonProperty("reading") Boolean reading) {
		
		super();
		this.id = COUNTER.getAndIncrement();
		this.name = name;
		this.state = state;
		this.type = type;
		this.reading = reading;
		
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

	public SensorType getType() {
		return type;
	}

	public void setType(SensorType type) {
		this.type = type;
	}

	public boolean isReading() {
		return reading;
	}

	public void setReading(boolean reading) {
		this.reading = reading;
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
		result = prime * result + (reading ? 1231 : 1237);
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
		Sensor other = (Sensor) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (reading != other.reading)
			return false;
		if (state != other.state)
			return false;
		if (type != other.type)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Sensor [id=" + id + ", name=" + name + ", state=" + state + ", type=" + type + ", reading=" + reading
				+ "]";
	}

}
