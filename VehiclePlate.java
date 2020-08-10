package sentinelFormatter.bot;

public class VehiclePlate {
	private String licensePlate;
	private String model;
	private String identificationPlate;
	private String remark;
	private String buffer;
	private boolean licensePlateNotAM;
	private int identificationPlatePos;
	private boolean modelComplete;
	private boolean createdByFastInput;
	private boolean licensePlateComplete;
	
	public String getLicensePlate() {
		return licensePlate;
	}
	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getIdentificationPlate() {
		return identificationPlate;
	}
	public void setIdentificationPlate(String identificationPlate) {
		this.identificationPlate = identificationPlate;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getBuffer() {
		return buffer;
	}
	public void setBuffer(String buffer) {
		this.buffer = buffer;
	}
	public boolean isLicensePlateNotAM() {
		return licensePlateNotAM;
	}
	public void setLicensePlateNotAM(boolean licensePlateNotAM) {
		this.licensePlateNotAM = licensePlateNotAM;
	}
	public int getIdentificationPlatePos() {
		return identificationPlatePos;
	}
	public void setIdentificationPlatePos(int identificationPlatePos) {
		this.identificationPlatePos = identificationPlatePos;
	}
	public boolean isModelComplete() {
		return modelComplete;
	}
	public void setModelComplete(boolean modelComplete) {
		this.modelComplete = modelComplete;
	}
	public boolean isCreatedByFastInput() {
		return createdByFastInput;
	}
	public void setCreatedByFastInput(boolean createdByFastInput) {
		this.createdByFastInput = createdByFastInput;
	}
	public boolean isLicensePlateComplete() {
		return licensePlateComplete;
	}
	public void setLicensePlateComplete(boolean licensePlateComplete) {
		this.licensePlateComplete = licensePlateComplete;
	}
	
}
