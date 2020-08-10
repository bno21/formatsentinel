package sentinelFormatter.bot;

import java.util.ArrayList;

public class PlateList extends ArrayList<VehiclePlate>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3473749542669283844L;
	private int currentPos;
	private boolean fastInput;
	
	public void clean() {
		for (int i = this.size()-1; i >= 0; i--) {
			 if ((this.get(i).getLicensePlate() == null || this.get(i).getLicensePlate().isEmpty()) &&
				 (this.get(i).getIdentificationPlate() == null || this.get(i).getIdentificationPlate().isEmpty()) &&
				 (this.get(i).getModel() == null || this.get(i).getModel().isEmpty()) &&
				 (this.get(i).getRemark() ==  null || this.get(i).getRemark().isEmpty())
				) {
				 this.remove(i);
			 }
		}
	}

	@Override
	public String toString() {
		String r = "";
		for (int i = 0; i < this.size(); i++) {
			r = r + "\r" + this.get(i).getLicensePlate() + " " + this.get(i).getIdentificationPlate() + " " + this.get(i).getModel() + " " + this.get(i).getRemark();
		}
		return r;
	}

	public int getCurrentPos() {
		return currentPos;
	}

	public void setCurrentPos(int currentPos) {
		this.currentPos = currentPos;
	}

	public boolean isFastInput() {
		return fastInput;
	}

	public void setFastInput(boolean fastInput) {
		this.fastInput = fastInput;
	}
}
