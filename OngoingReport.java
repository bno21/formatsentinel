package sentinelFormatter.bot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import sentinelFormatter.bot.res.Action;
import sentinelFormatter.bot.res.Deployment;
import sentinelFormatter.bot.res.District;
import sentinelFormatter.bot.res.IdentificationPlate;
import sentinelFormatter.bot.res.JsonResource;
import sentinelFormatter.bot.res.Region;

public class OngoingReport {

	List<User> user = new ArrayList<User>();
	Region[] region;
	static Deployment[] deployment;
	Action[] action;
	static IdentificationPlate[] identificationPlate;
	static String[] numPad;

	public OngoingReport() throws URISyntaxException {
		this.region = loadRegion();
		this.deployment = loadDeployment();
		this.action = loadAction();
		this.identificationPlate = loadIdentificationPlate();
		this.numPad = loadNumPad();
	}
	public SentinelMessage getSentinelMessageByChatId(String chatId) {
		for (User cUser : user) {
			if (cUser.getChatId().equals(chatId)) {
				if (new Date().getTime() - cUser.getLastUpdate().getTime() > Constant.TIME_OUT) {
					user.remove(cUser); // Remove TimeOut case
				} else {
					SentinelMessage sentinelMessage = cUser.getSentinelMessage();
					return sentinelMessage;
				}
			}
		}

		User newUser = new User(chatId);
		user.add(newUser);
		return newUser.getSentinelMessage();
	}

	public SentinelMessage getSentinelMessageByChatId(long chatId) {
		return getSentinelMessageByChatId(String.valueOf(chatId));
	}

	public void updateSentinelMessageByChatId(String chatId, SentinelMessage sentinelMessage) {
		for (int i = 0; i < user.size(); i++) {
			User cUser = user.get(i);
			if (cUser.getChatId().equals(chatId)) {
				user.remove(cUser);
				cUser.setSentinelMessage(sentinelMessage);
				cUser.setLastUpdate(new Date());
				user.add(cUser);
			}
		}
	}

	public void updateSentinelMessageByChatId(long chatId, SentinelMessage sentinelMessage) {
		updateSentinelMessageByChatId(String.valueOf(chatId), sentinelMessage);
	}

	public void removeSentinelMessageByChatId(String chatId) {
		for (User cUser : user) {
			if (cUser.getChatId().equals(chatId)) {
				user.remove(cUser); // Remove TimeOut case
				return;
			}
		}
		return;
	}

	public void removeSentinelMessageByChatId(long chatId) {
		removeSentinelMessageByChatId(String.valueOf(chatId));
	}

	public Map<String, Integer> getUserReportStatusByChatId(String chatId) {
		SentinelMessage sentinelMessage = getSentinelMessageByChatId(chatId);
		Map<String, Integer> userReport = new LinkedHashMap<String, Integer>();
		String time = sentinelMessage.getTime();
		userReport.put("time", validTimeFormat(time));
		String district = sentinelMessage.getDistrict();
		userReport.put("district", validDistrictFormat(district));
		String location = sentinelMessage.getLocation();
		userReport.put("location", validLocationFormat(location));
		String deployment = sentinelMessage.getCurrentDeployment();
		boolean deploymentComplete = sentinelMessage.isDeploymentComplete();
		userReport.put("deployment", validDeploymentFormat(deployment, deploymentComplete));
		String action = sentinelMessage.getCurrentAction();
		boolean actionComplete = sentinelMessage.isActionComplete();
		userReport.put("action", validActionFormat(action, actionComplete));
		boolean plateComplete = sentinelMessage.isPlateComplete();
		PlateList plate = sentinelMessage.getCurrentPlate();
		userReport.put("plate", validPlateFormat(plate, plateComplete));
		return userReport;
	}

	public Map<String, Integer> getUserReportStatusByChatId(long chatId) {
		return getUserReportStatusByChatId(String.valueOf(chatId));
	}
	
	public void resetLastUpdatedSentinelMessageByChatId(String chatId) {
		Map<String, Integer> status = getUserReportStatusByChatId(chatId);
		SentinelMessage sentinelMessage = getSentinelMessageByChatId(chatId);
		if (status.get("action") != Constant.ACTION_MISSING) {
			sentinelMessage.setCurrentAction(null);
		} else if (status.get("deployment") != Constant.DEPLOYMENT_MISSING) {
			sentinelMessage.setCurrentDeployment(null);
		} else if (status.get("location") != Constant.LOCATION_VALUE_MISSING) {
			sentinelMessage.setLocation(null);
		} else if (status.get("district") != Constant.DISTRICT_VALUE_MISSING) {
			sentinelMessage.setDistrict(null);
		} else {
			sentinelMessage.setTime(null);
		}
		updateSentinelMessageByChatId(chatId, sentinelMessage);
	}
	
	private Region[] loadRegion() throws URISyntaxException {
		try {
			String regionJson = JsonResource.getResource("district.json");
			JSONObject districtObject = new JSONObject(regionJson);
			JSONArray cityArray = (JSONArray) districtObject.get("city");
			Region[] returnRegion = new Region[cityArray.length()];
			for (int i = 0; i < cityArray.length(); i++) {
				JSONObject city = (JSONObject) cityArray.get(i);
				Region region = new Region();
				region.setName(city.getString("name"));
				JSONArray districtArray = (JSONArray) city.get("district");
				ArrayList<District> district = new ArrayList<District>();
				for (int j = 0; j < districtArray.length(); j++) {
					JSONObject cDistrictObj = (JSONObject) districtArray.get(j);
					District cDistrict = new District();
					cDistrict.setName(cDistrictObj.getString("name"));
					cDistrict.setPosition(((JSONObject) cDistrictObj.get("pos")).getInt("x"),
							((JSONObject) cDistrictObj.get("pos")).getInt("y"));
					cDistrict.setEnabled(cDistrictObj.getBoolean("enabled"));
					cDistrict.loadStreet();
					district.add(cDistrict);
				}
				region.setDistrict(district.toArray(new District[district.size()]));
				returnRegion[i] = region;
			}
			return returnRegion;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	protected Region[] getRegion() {
		return region;
	}

	protected Region getRegion(String name) {
		for (int i = 0; i < region.length; i++) {
			if (region[i].getName().equals(name)) {
				return region[i];
			}
		}
		return null;
	}
	
	private static Deployment[] loadDeployment() throws URISyntaxException {
		try {
			String deploymentJson;
			deploymentJson = JsonResource.getResource("deployment.json");
			JSONObject deploymentObject = new JSONObject(deploymentJson);
			JSONArray deploymentArray = deploymentObject.getJSONArray("deployment");
			Deployment[] deployment = new Deployment[deploymentArray.length()];
			for (int i = 0; i < deploymentArray.length(); i++) {
				deployment[i] = new Deployment();
				JSONObject cDeployment = deploymentArray.getJSONObject(i);
				deployment[i].setName(cDeployment.getString("name"));
				deployment[i].setType(cDeployment.getString("type"));
			}
			return deployment;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Action[] loadAction() throws URISyntaxException {
		try {
			String actionJson = JsonResource.getResource("action.json");
			JSONObject actionObject = new JSONObject(actionJson);
			JSONArray actionArray = actionObject.getJSONArray("action");
			Action[] action = new Action[actionArray.length()];
			for (int i = 0; i < actionArray.length(); i++) {
				action[i] = new Action();
				JSONObject cAction = actionArray.getJSONObject(i);
				action[i].setName(cAction.getString("name"));
			}
			return action;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static IdentificationPlate[] loadIdentificationPlate() throws URISyntaxException {
		try {
			String identificationPlateJson = JsonResource.getResource("identificationPlate.json");
			JSONObject identificationPlateObject = new JSONObject(identificationPlateJson);
			JSONArray identificationPlateArray = identificationPlateObject.getJSONArray("identificationPlate");
			IdentificationPlate[] identificationPlate = new IdentificationPlate[identificationPlateArray.length()];
			for (int i = 0; i < identificationPlateArray.length(); i++) {
				identificationPlate[i] = new IdentificationPlate();
				JSONObject cIdentificationPlate = identificationPlateArray.getJSONObject(i);
				identificationPlate[i].setName(cIdentificationPlate.getString("name"));
				identificationPlate[i].setCount(cIdentificationPlate.getInt("count"));
			}
			return identificationPlate;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String[] loadNumPad() {
		String[] numPad = {"7", "8", "9", "4", "5", "6", "1", "2", "3", null, "0", null};
		return numPad;
	}
	
	protected Deployment[] getDeployment() {
		return deployment;
	}
	
	protected Deployment getDeployment(String name) {
		for (int i = 0; i < deployment.length; i++) {
			if (deployment[i].getName().equals(name)) {
				return deployment[i];
			}
		}
		return null;
	}

	private int validTimeFormat(String time) {
		if (time == null) {
			return Constant.TIME_VALUE_MISSING;
		} else if (time.equals("")) {
			return Constant.TIME_VALUE_EMPTY;
		} else if (time.length() < 4) {
			return Constant.TIME_VALUE_INCOMPLETE;
		} else if (!(time.charAt(0) == '0' || time.charAt(0) == '1' || time.charAt(0) == '2' || time.length() > 4)) {
			return Constant.TIME_FIRST_VALUE_INVALID;
		} else if (time.charAt(2) == '6' || time.charAt(2) == '7' || time.charAt(2) == '8' || time.charAt(2) == '9') {
			return Constant.TIME_THIRD_VALUE_INVALID;
		} else {
			return Constant.VALID;
		}
	}

	private boolean isValidRegion(String regionName) {
		for (int i = 0; i < region.length; i++) {
			if (region[i].getName().equals(regionName)) {
				return true;
			}
		}
		return false;
	}

	private boolean isValidDistrict(String districtName) {
		for (int i = 0; i < region.length; i++) {
			for (int j = 0; j < region[i].getDistrict().length; j++) {
				if (region[i].getDistrict()[j].getName().equals(districtName)) {
					return true;
				}
			}
		}
		return false;
	}

	private int validDistrictFormat(String district) {
		if (district == null) {
			return Constant.DISTRICT_VALUE_MISSING;
		} else if (district.equals("")) {
			return Constant.DISTRICT_VALUE_EMPTY;
		} else if (isValidRegion(district)) {
			if (district.equals(Constant.DISTRICT_REGION_NT_MSG)) {
				return Constant.DISTRICT_REGION_VALID_NT;
			}
			return Constant.DISTRICT_REGION_VALID;
		} else if (isValidDistrict(district)) {
			return Constant.VALID;
		} else {
			return Constant.DISTRICT_UNKNOWN;
		}
	}

	private int[] streetCounter(String streetName) {
		int count = 0;
		int equalCount = 0;
		for (int i = 0; i < region.length; i++) {
			if (region[i].getDistrict() != null) {
				for (int j = 0; j < region[i].getDistrict().length; j++) {
					if (region[i].getDistrict()[j].getLocation() != null) {
						for (int k = 0; k < region[i].getDistrict()[j].getLocation().length; k++) {
							if (streetName.contains(region[i].getDistrict()[j].getLocation()[k].getName())) {
								count++;
								if (streetName.equals(region[i].getDistrict()[j].getLocation()[k].getName())) {
									equalCount++;
								}
							}
						}
					}
				}
			}
		}
		int[] countResult = { count, equalCount };
		return countResult;
	}

	private int validLocationFormat(String location) {
		if (location == null) {
			return Constant.LOCATION_VALUE_MISSING;
		} else if (location.equals("")) {
			return Constant.LOCATION_VALUE_EMPTY;
		} else {
			int[] streetCounter = streetCounter(location);
			int streetCount = streetCounter[0];
			int equalCount = streetCounter[1];
			if (streetCount == 0) {
				return Constant.LOCATION_UNKNOWN;
			} else if (equalCount >= 1) {
				return Constant.LOCATION_ONE_STREETNAME_ONLY;
			} else {
				return Constant.VALID;
			}
		}
	}
	
	private int validDeploymentFormat(String deployment, boolean complete) {
		if (deployment == null) {
			return Constant.DEPLOYMENT_MISSING;
		} else if (deployment.equals("")) {
			return Constant.DEPLOYMENT_EMPTY;
		} else {
			String lastChar = deployment.substring(deployment.length()-1);
			if (lastChar.matches("^[0-9]*$")) {
				return Constant.DEPLOYMENT_INCOMPLETE;
			} else if (complete == true){
				return Constant.VALID;
			} else {
				return Constant.DEPLOYMENT_READY;
			}
		}
	}
	
	private int validActionFormat(String action, boolean complete) {
		if (action == null) {
			return Constant.ACTION_MISSING;
		} else if (action.equals("")) {
			return Constant.ACTION_EMPTY;
		} else {
			if (action.contains(Constant.ACTION_LOCATION_LABEL)) {
				return Constant.ACTION_INCOMPLETE_REQUIRE_LOCATION_INPUT;
			} else if (action.contains(Constant.ACTION_STREET_LABEL) ) {
				return Constant.ACTION_INCOMPLETE_REQUIRE_STREET_INPUT;
			}
			else if (complete == true) {
				return Constant.VALID;
			} else {
				return Constant.ACTION_READY;
			}
		}
	}
	
	private int validPlateFormat(PlateList plate, boolean complete) {
		if (complete == true) {
			return Constant.VALID;
		}
		if (plate == null || plate.size() == 0) {
			return Constant.PLATE_MISSING;
		}
		VehiclePlate cPlate = plate.get(plate.size()-1);
		if (cPlate.isCreatedByFastInput() == false) {
			if (cPlate.getLicensePlate() == null && cPlate.isLicensePlateComplete() == false) {
				return Constant.PLATE_INCOMPLETE_LICENSE_PLATE;
			}
			if (cPlate.getIdentificationPlate() == null && (cPlate.getLicensePlate() != null && cPlate.getLicensePlate().contains("AM"))) {
				return Constant.PLATE_INCOMPLETE_IDENTIFICATION_PLATE;
			}
			if (cPlate.getModel() == null && cPlate.isModelComplete() == false) {
				return Constant.PLATE_INCOMPLETE_MODEL;
			}
		}
		if (cPlate.getRemark() == null) {
			return Constant.PLATE_INCOMPLETE_REMARK;
		}
		return Constant.PLATE_READY;
	}
}
