package sentinelFormatter.bot;

import java.util.ArrayList;

public class SentinelMessage {

	private String time;
	private String district;
	private int districtPosX;
	private int districtPosY;
	private String location;
	private int locationPos;
	private String lastLocation;
	private ArrayList<String> deployment;
	private boolean deploymentComplete;
	private ArrayList<String> action;
	private boolean actionComplete;
	private ArrayList<String> remark;
	private int deploymentActionCycleCount = 0;
	private boolean plateComplete;
	private ArrayList<PlateList> plate;
	private boolean inPlate;
	
	public SentinelMessage() {
		deployment = new ArrayList<String>();
		deployment.add(null);
		action = new ArrayList<String>();
		action.add(null);
		remark = new ArrayList<String>();
		remark.add(null);
		plate = new ArrayList<PlateList>();
		plate.add(null);
	}
	
	public void addNewDeploymentActionCycle() {
		deploymentActionCycleCount++;
		deployment.add(null);
		action.add(null);
		remark.add(null);
		deploymentComplete = false;
		actionComplete = false;
		plate.add(new PlateList());
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCurrentAction() {
		return action.get(deploymentActionCycleCount);
	}

	public void setCurrentAction(String action) {
		this.action.set(deploymentActionCycleCount, action);
	}

	public String displaySentinelMessage() {
		String returnString = time + " #" + district + " " + location + " ";
		for (int i = 0; i <= deploymentActionCycleCount; i++) {
			returnString += deployment.get(i) + " " + action.get(i) + " ";
			if (plate.get(i) != null) {
				returnString += "\n";
				for (int j = 0; j < plate.get(i).size(); j++) {
					VehiclePlate vp = plate.get(i).get(j);
					returnString += (vp.getLicensePlate() == null ||vp.getLicensePlate().isEmpty()) ? "" : vp.getLicensePlate() + " ";
					returnString += (vp.getIdentificationPlate() == null || vp.getIdentificationPlate().isEmpty()) ? "" : vp.getIdentificationPlate() + " ";
					returnString += (vp.getModel() == null || vp.getModel().isEmpty()) ? "\n" : vp.getModel() + "\n";
				}
			}
		}
		return returnString;
	}

	public int getDistrictPosX() {
		return districtPosX;
	}

	public void setDistrictPosX(int districtPosX) {
		this.districtPosX = districtPosX;
	} 

	public int getDistrictPosY() {
		return districtPosY;
	}

	public void setDistrictPosY(int districtPosY) {
		this.districtPosY = districtPosY;
	}

	public int getLocationPos() {
		return locationPos;
	}

	public void setLocationPos(int locationPos) {
		this.locationPos = locationPos;
	}

	public String getCurrentDeployment() {
		return deployment.get(deploymentActionCycleCount);
	}

	public void setCurrentDeployment(String deployment) {
		this.deployment.set(deploymentActionCycleCount, deployment);
	}

	public boolean isDeploymentComplete() {
		return deploymentComplete;
	}

	public void setDeploymentComplete(boolean deploymentComplete) {
		this.deploymentComplete = deploymentComplete;
	}

	public boolean isActionComplete() {
		return actionComplete;
	}

	public void setActionComplete(boolean actionComplete) {
		this.actionComplete = actionComplete;
	}

	public String getCurrentRemark() {
		return remark.get(deploymentActionCycleCount);
	}

	public void setCurrentRemark(String remark) {
		this.remark.set(deploymentActionCycleCount, remark);
	}

	public int getDeploymentActionCycleCount() {
		return deploymentActionCycleCount;
	}

	public void setDeploymentActionCycleCount(int deploymentActionCycleCount) {
		this.deploymentActionCycleCount = deploymentActionCycleCount;
	}

	public PlateList getCurrentPlate() {
		return plate.get(deploymentActionCycleCount);
	}

	public void setCurrentPlate(PlateList plate) {
		this.plate.set(deploymentActionCycleCount, plate);
	}

	public String getLastLocation() {
		return lastLocation;
	}

	public void setLastLocation(String lastLocation) {
		this.lastLocation = lastLocation;
	}

	public boolean isPlateComplete() {
		return plateComplete;
	}

	public void setPlateComplete(boolean plateComplete) {
		this.plateComplete = plateComplete;
	}

	public boolean isInPlate() {
		return inPlate;
	}

	public void setInPlate(boolean inPlate) {
		this.inPlate = inPlate;
	}
}
