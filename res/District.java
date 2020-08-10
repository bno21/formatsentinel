package sentinelFormatter.bot.res;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class District {

	private String name;
	private Position position = new Position();
	private boolean enabled;
	private Location[] location;

	public void loadStreet() throws URISyntaxException {
		if (name != null && !name.isEmpty()) {
			try {
				String streetJson = JsonResource.getResource("street.json");
				JSONArray streetArray = new JSONArray(streetJson);
				for (int i = 0; i < streetArray.length(); i++) {
					JSONObject districtObject = (JSONObject) streetArray.get(i);
					String districtName = districtObject.getString("name");
					if (districtName.equals(name)) {
						JSONArray districtStreetArray = districtObject.getJSONArray("street");
						Location[] streetLocation = new Location[districtStreetArray.length()];
						for (int j = 0; j < districtStreetArray.length(); j++) {
							streetLocation[j] = new Location();
							JSONObject districtStreetObject = (JSONObject) districtStreetArray.get(j);
							String districtStreetName = districtStreetObject.getString("streetName");
							streetLocation[j].setName(districtStreetName);
							int districtStreetFrequency = districtStreetObject.getInt("frequency");
							streetLocation[j].setFrequency(districtStreetFrequency);
							JSONArray buildingNameArray = (JSONArray) districtStreetObject.get("building");
							Location[] buildingLocation = new Location[buildingNameArray.length()];
							JSONArray relatedStreetArray = (JSONArray) districtStreetObject.get("relatedStreet");
							Location[] relatedStreetLocation = new Location[relatedStreetArray.length()];
							for (int k = 0; k < buildingNameArray.length(); k++) {
								buildingLocation[k] = new Location();
								JSONObject buildingObject = (JSONObject) buildingNameArray.get(k);
								String buildingName = buildingObject.getString("buildingName");
								int buildingFrequency = buildingObject.getInt("frequency");
								buildingLocation[k].setName(buildingName);
								buildingLocation[k].setFrequency(buildingFrequency);
							}
							streetLocation[j].setBuilding(buildingLocation);
							for (int k = 0; k < relatedStreetArray.length(); k++) {
								relatedStreetLocation[k] = new Location();
								JSONObject relatedStreetObject = (JSONObject) relatedStreetArray.get(k);
								String relatedStreetName = relatedStreetObject.getString("streetName");
								int relatedStreetFrequency = relatedStreetObject.getInt("frequency");
								relatedStreetLocation[k].setName(relatedStreetName);
								relatedStreetLocation[k].setFrequency(relatedStreetFrequency);
							}
							streetLocation[j].setRelatedStreet(relatedStreetLocation);
						}
						this.setLocation(streetLocation);
					}
				}
				
				//FIXME add sorting
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setPosition(int x, int y) {
		position.x = x;
		position.y = y;
	}

	public class Position {
		private int x;
		private int y;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}
	}

	public Location[] getLocation() {
		return location;
	}

	public void setLocation(Location[] location) {
		this.location = location;
	}
}
