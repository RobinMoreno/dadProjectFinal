package dad.us.dadVertx;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reading {

	private int idreading;
	private double temperature;
	private int smoke;
	private int iddevice;
	private Date date;
	private static final AtomicInteger COUNTER = new AtomicInteger(1);
	
public Reading() {
		
		this(0, 0f, 0, null);

	}
    
	public Reading (@JsonProperty("smoke") int smoke, @JsonProperty("temperature") Float temperature,
			@JsonProperty("iddevice") int iddevice, @JsonProperty("date") Date date) {
		
		super();
		this.idreading = COUNTER.getAndIncrement();
		this.temperature = temperature;
		this.smoke = smoke;
		this.iddevice = iddevice;
		this.date = date;
		
	}
	public double getTemperature() {
		return temperature;
	}
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	public int getSmoke() {
		return smoke;
	}
	public void setSmoke(int smoke) {
		this.smoke = smoke;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getIdreading() {
		return idreading;
	}
	public int getIddevice() {
		return iddevice;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + iddevice;
		result = prime * result + idreading;
		result = prime * result + smoke;
		long temp;
		temp = Double.doubleToLongBits(temperature);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Reading other = (Reading) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (iddevice != other.iddevice)
			return false;
		if (idreading != other.idreading)
			return false;
		if (smoke != other.smoke)
			return false;
		if (Double.doubleToLongBits(temperature) != Double.doubleToLongBits(other.temperature))
			return false;
		return true;
	}
	
	}
