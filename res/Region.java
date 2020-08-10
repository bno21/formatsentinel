package sentinelFormatter.bot.res;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Region {

	private String name;
	private District[] district;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public District[] getDistrict() {
		return district;
	}
	
	public District getDistrict(String districtName) {
		for (int i = 0; i < district.length; i++) {
			if (district[i].getName().equals(districtName)) {
				return district[i];
			}
		}
		return null;
	}

	public District getDistrictByPos(int posX, int posY) {
		for (int i = 0; i < district.length; i++) {
			if (district[i].getPosition().getX() == posX && district[i].getPosition().getY() == posY) {
				return district[i];
			}
		}
		return null;
	}

	public void setDistrict(District[] district) {
		this.district = district;
	}
}
