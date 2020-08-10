package sentinelFormatter.bot.res;

import java.util.ArrayList;

public class Location {
	String name;
	Integer frequency;
	Location[] building;
	Location[] relatedStreet;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public Location[] getBuilding() {
		return building;
	}

	public void setBuilding(Location[] building) {
		this.building = building;
	}

	public Location[] getRelatedStreet() {
		return relatedStreet;
	}

	public void setRelatedStreet(Location[] relatedStreet) {
		this.relatedStreet = relatedStreet;
	}
}
