package dad.us.dadVertx;

import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceCustom {
	
	private int id;
	private String name;
	private boolean state;
	private float temperature;
	private boolean smoke;
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
	
	public DeviceCustom() {
		
		this("", false, 0f, false);

	}

	public DeviceCustom (@JsonProperty("name") String name, @JsonProperty("state") Boolean state,
			@JsonProperty("temperature") Float temperature, @JsonProperty("smoke") Boolean smoke) {
		
		super();
		this.id = COUNTER.getAndIncrement();
		this.name = name;
		this.state = state;
		this.temperature = temperature;
		this.smoke = smoke;
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	
	public boolean isSmoke() {
		return smoke;
	}
	
	public void setSmoke(boolean smoke) {
		this.smoke = smoke;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (state ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(temperature);
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
		DeviceCustom other = (DeviceCustom) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (state != other.state)
			return false;
		if (Float.floatToIntBits(temperature) != Float.floatToIntBits(other.temperature))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Device [ID=" + id + ", Nombre=" + name + ", Estado=" + state + ", Temperatura=" + temperature + "]";
	}

}
