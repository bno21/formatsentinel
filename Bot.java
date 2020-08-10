package sentinelFormatter.bot;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.mysql.cj.util.StringUtils;

import sentinelFormatter.bot.res.Action;
import sentinelFormatter.bot.res.Deployment;
import sentinelFormatter.bot.res.District;
import sentinelFormatter.bot.res.IdentificationPlate;
import sentinelFormatter.bot.res.Location;
import sentinelFormatter.bot.res.Region;

public class Bot extends TelegramLongPollingBot {

	OngoingReport ongoingReport;

	public Bot() throws URISyntaxException {
		ongoingReport = new OngoingReport();
	}

	@Override
	public void onUpdateReceived(Update update) {
		// We check if the update has a message and the message has text
		if (update.hasMessage() && update.getMessage().hasText()) {
			Message message = update.getMessage();
			String chatId = String.valueOf(message.getChatId());
			String text = message.getText();
			byte[] charset;
			String fn = "" + message.getFrom().getFirstName();
			String ln = "" + message.getFrom().getLastName();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			System.out.println("Message: " + text + " [" + fn + " " + ln + "]" + sdf.format(calendar.getTime()));
			try {
				charset = text.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (text.equals("/start") || text.equals("/reset")) {
				ongoingReport.removeSentinelMessageByChatId(chatId);
			}
			if (text.equals("/new")) {
				String lastDistrict = ongoingReport.getSentinelMessageByChatId(chatId).getDistrict();
				ongoingReport.removeSentinelMessageByChatId(chatId);
				SentinelMessage se = ongoingReport.getSentinelMessageByChatId(chatId);
				se.setLastLocation(lastDistrict);
				ongoingReport.updateSentinelMessageByChatId(chatId, se);
			}
			if (text.equals(Constant.CANCEL)) {
				ongoingReport.resetLastUpdatedSentinelMessageByChatId(chatId);
			}
			buildSentinelStructure(message, chatId, text);
		}
	}

	private void buildSentinelStructure(Message message, long chatId, String text) {
		buildSentinelStructure(message, String.valueOf(message.getChatId()), text);
	}

	private void buildSentinelStructure(Message message, String chatId, String text) {
		Map<String, Integer> reportStatus = ongoingReport.getUserReportStatusByChatId(chatId);
		if (text.equals(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE)
				|| ongoingReport.getSentinelMessageByChatId(message.getChatId()).isInPlate()) {
			plate(message, text);
			if (ongoingReport.getSentinelMessageByChatId(message.getChatId()).isPlateComplete()) {
				// Reset entry
				SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
				if (sentinelMessage.getDistrict() != null && sentinelMessage.getDistrict().equals("")) {
					sentinelMessage.setDistrict(null);
				}
				if (sentinelMessage.getCurrentRemark() != null && sentinelMessage.getCurrentRemark().equals("")) {
					sentinelMessage.setCurrentRemark(null);
				}
				sentinelMessage.setInPlate(false);
				sentinelMessage.setPlateComplete(false);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				buildSentinelStructure(message, message.getChatId(), "");
			}
		} else {
			if (reportStatus.get("time") == null || reportStatus.get("time") != Constant.VALID) {
				time(message, text);
			}
			reportStatus = ongoingReport.getUserReportStatusByChatId(chatId);
			if (reportStatus.get("time") == Constant.VALID) {
				if (reportStatus.get("district") == null || !(reportStatus.get("district") == Constant.VALID
						|| reportStatus.get("district") == Constant.DISTRICT_UNKNOWN)) {
					district(message, text);
				}
				reportStatus = ongoingReport.getUserReportStatusByChatId(chatId);
				if (reportStatus.get("district") == Constant.VALID
						|| reportStatus.get("district") == Constant.DISTRICT_UNKNOWN) {
					if (reportStatus.get("location") == null || !(reportStatus.get("location") == Constant.VALID
							|| reportStatus.get("location") == Constant.LOCATION_UNKNOWN)) {
						location(message, text);
					}
					reportStatus = ongoingReport.getUserReportStatusByChatId(chatId);
					if (reportStatus.get("location") == Constant.VALID
							|| reportStatus.get("location") == Constant.LOCATION_UNKNOWN) {
						if (reportStatus.get("deployment") == null
								|| !(reportStatus.get("deployment") == Constant.VALID)) {
							deployment(message, text);
						}
						reportStatus = ongoingReport.getUserReportStatusByChatId(chatId);
						if (reportStatus.get("deployment") == Constant.VALID) {
							if (reportStatus.get("action") == null || !(reportStatus.get("action") == Constant.VALID)) {
								action(message, text);
							}
							reportStatus = ongoingReport.getUserReportStatusByChatId(chatId);
							if (reportStatus.get("action") == Constant.VALID) {
								remark(message, text);
							}
						}
					}
				}
			}
		}
	}

	private void remark(Message message, String text) {
		/*
		 * SentinelMessage sentinelMessage =
		 * ongoingReport.getSentinelMessageByChatId(message.getChatId()); String remark
		 * = sentinelMessage.getRemark(); String newRemark = message.getText();
		 * sentinelMessage.setRemark(remark + "\n" + newRemark);
		 * ongoingReport.updateSentinelMessageByChatId(message.getChatId(),
		 * sentinelMessage);
		 */
		SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
		String remark = sentinelMessage.getCurrentRemark();
		String newRemark = text;
		try {
			if (newRemark.equals(Constant.MESSAGE_REMARK_NEW_INFO)) {
				sentinelMessage.addNewDeploymentActionCycle();
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
				buildSentinelStructure(message, String.valueOf(message.getChatId()), text);
			} else if (newRemark.equals(Constant.MESSAGE_REMARK_FORWARD)) {
				ForwardMessage forwardMessage = new ForwardMessage();
				forwardMessage.setFromChatId(message.getChatId());
				forwardMessage.setChatId(Constant.MESSAGE_FORWARD_BOT_NAME);
				forwardMessage.setMessageId(message.getMessageId());
				execute(forwardMessage);
			}
			else if (sentinelMessage.getCurrentRemark() == null) {
				sentinelMessage.setCurrentRemark("");
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(Constant.MESSAGE_REMARK, message, getRemarkKeyPad());
			} else {
				sentinelMessage.setCurrentRemark(remark + newRemark);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
			}

		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void fastInputPlate(Message message, String text) throws TelegramApiException {
		SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
		sentinelMessage.setInPlate(true);
		PlateList plate = sentinelMessage.getCurrentPlate();
		VehiclePlate cPlate = new VehiclePlate();
		int pos = 0;
		if (plate == null) {
			plate = new PlateList();
		}
		if (!plate.isEmpty()) {
			if (text.equals(Constant.MESSAGE_ENTER_VEHICLE_FAST_INPUT_MODE)) {
				pos = plate.getCurrentPos();
				cPlate = plate.get(pos);
				if (cPlate.getIdentificationPlate() != null) {
					if (cPlate.getIdentificationPlate().isEmpty() == false || cPlate.isLicensePlateComplete()) {
						plate.setCurrentPos(plate.getCurrentPos() + 1);
						cPlate = new VehiclePlate();
						plate.add(cPlate);
					} else if (cPlate.getIdentificationPlate().isEmpty() == true
							&& cPlate.getLicensePlate().isEmpty() == false) {
						try {
							Integer.parseInt(cPlate.getLicensePlate());
							cPlate.setLicensePlate("AM" + cPlate.getLicensePlate());
						} catch (Exception e) {

						}
						cPlate.setLicensePlateComplete(true);
					}
				} else if (cPlate.isCreatedByFastInput() == false
						&& (cPlate.getLicensePlate() == null || cPlate.getLicensePlate().isEmpty() == false)
						&& cPlate.getBuffer() != null && cPlate.getBuffer().isEmpty() == false) {
					// Convert original input to fast input format
					int charLength = text.length() > 4 ? 4 : text.length();
					String firstCharLengthString = text.substring(0, charLength);
					if (firstCharLengthString.contains("/")) {
						cPlate.setLicensePlateComplete(true);
						cPlate.setIdentificationPlate(cPlate.getBuffer());
					} else {
						cPlate.setLicensePlate(cPlate.getBuffer());
						cPlate.setLicensePlateComplete(true);
						try {
							Integer.parseInt(cPlate.getLicensePlate());
							cPlate.setLicensePlate("AM" + cPlate.getLicensePlate());
						} catch (Exception e) {

						}
					}
					cPlate.setCreatedByFastInput(true);
				}
			} else if (text.equals(Constant.MESSAGE_PLATE_FAST_PLATE_INPUT_COMPLETE)) {
				plate.setFastInput(false);
				plate.clean();
				sentinelMessage.setCurrentPlate(plate);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
				sendMessage(Constant.MESSAGE_PLATE_RETURN_OPTIONS, message,
						getVehicleLicensePlateReturnOptionsKeyPad());
				return;
			} else {
				if (text.equals(Constant.PLATE_PLATOON_LABEL)) {
					text = (" " + Constant.PLATE_PLATOON_LABEL);
				}
				pos = plate.getCurrentPos();
				cPlate = plate.get(pos);
				if (cPlate.getIdentificationPlate() != null && cPlate.getIdentificationPlate().length() >= 7
						&& cPlate.getIdentificationPlate()
								.substring(cPlate.getIdentificationPlate().length() - 7,
										cPlate.getIdentificationPlate().length())
								.equals(Constant.PLATE_PLATOON_LABEL)) {
					text = (" " + text);
				}
				if (cPlate.isLicensePlateComplete() == false) {
					if (cPlate.getLicensePlate() == null) {
						cPlate.setLicensePlate(text);
					} else {
						cPlate.setLicensePlate(cPlate.getLicensePlate() + text);
					}
				} else {
					if (cPlate.getIdentificationPlate() == null) {
						cPlate.setIdentificationPlate(text);
					} else {
						cPlate.setIdentificationPlate(cPlate.getIdentificationPlate() + text);
					}
				}
			}
			if (plate.size() == plate.getCurrentPos()) {
				cPlate = new VehiclePlate();
				plate.add(cPlate);
			} else {
				plate.set(plate.getCurrentPos(), cPlate);
			}
			sentinelMessage.setCurrentPlate(plate);
			ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
			sendMessage(sentinelMessage.displaySentinelMessage(), message, getFastInputPlateKeyPad());
		}
	}

	private void plate(Message message, String text) {
		int status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("plate");
		try {
			SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
			sentinelMessage.setInPlate(true);
			PlateList plate = sentinelMessage.getCurrentPlate();
			VehiclePlate cPlate = new VehiclePlate();
			int pos = 0;
			if (plate == null) {
				plate = new PlateList();
			}
			if (!plate.isEmpty()) {
				if (message.getText().equals(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE)) {
					plate.setCurrentPos(plate.getCurrentPos() + 1);
					plate.add(new VehiclePlate());
				}
				pos = plate.getCurrentPos();
				cPlate = plate.get(pos);
			}
			if (!(message.getText().equals(Constant.COMPLETE)
					|| message.getText().equals(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE)
					|| message.getText().equals(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE_NOT_AM)
					|| message.getText()
							.equals(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_IN_ALPHABETICAL_ORDER)
					|| message.getText()
							.equals(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_IN_POPULARITY_ORDER)
					|| message.getText().equals(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_NO_PLATE)
					|| message.getText().equals(Constant.MESSAGE_PLATE_NEW_PLATE)
					|| message.getText().equals(Constant.MESSAGE_PLATE_EXIT_PLATE)
					|| message.getText().equals(Constant.MESSAGE_ENTER_VEHICLE_FAST_INPUT_MODE))) {
				if (cPlate.getBuffer() == null) {
					cPlate.setBuffer("");
				}
				cPlate.setBuffer(cPlate.getBuffer() + message.getText());
			}
			if (message.getText().equals(Constant.MESSAGE_ENTER_VEHICLE_FAST_INPUT_MODE) || plate.isFastInput()) {
				plate.setFastInput(true);
				fastInputPlate(message, text);
			} else {
				if (status == Constant.PLATE_INCOMPLETE_LICENSE_PLATE) {
					if (cPlate.isLicensePlateNotAM()) {
						cPlate.setLicensePlate(message.getText());
						cPlate.setBuffer(null);
					}
					if (message.getText().equals(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE_NOT_AM)) {
						cPlate.setLicensePlateNotAM(true);
					}
					if (message.getText().equals(Constant.COMPLETE)) {
						String prepend = "";
						try {
							Integer.parseInt(cPlate.getBuffer());
							prepend += "AM";
						} catch (Exception e) {
						}
						cPlate.setLicensePlate(prepend + cPlate.getBuffer());
						cPlate.setBuffer(null);
					}
				}
				if (status == Constant.PLATE_INCOMPLETE_IDENTIFICATION_PLATE) {
					if (text.equals(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_NO_PLATE)) {
						cPlate.setIdentificationPlate("");
						cPlate.setBuffer(null);
					}
					if (text.equals(Constant.COMPLETE)) {
						if (cPlate.getBuffer().contains(Constant.PLATE_PLATOON_LABEL)) {
							cPlate.setBuffer(cPlate.getBuffer().replace(Constant.PLATE_PLATOON_LABEL,
									" " + Constant.PLATE_PLATOON_LABEL + " "));
						}
						cPlate.setIdentificationPlate(cPlate.getBuffer());
						cPlate.setBuffer(null);
					}
					for (int i = 0; i < OngoingReport.identificationPlate.length; i++) {
						if (message.getText().contains(OngoingReport.identificationPlate[i].getName())) {
							sendMessage(Constant.MESSAGE_ENTER_VEHICLE_IDENTIFICATION_PLATE + "\n" + cPlate.getBuffer(),
									message, getVehicleIdentificationPlateNumKeyPad());
							break;
						}
					}
				}
				if (status == Constant.PLATE_INCOMPLETE_MODEL) {
					/*
					 * for (int i = 0; i < OngoingReport.deployment.length; i++) { if
					 * (message.getText().equals(OngoingReport.deployment[i].getName())) {
					 * cPlate.setModel(cPlate.getBuffer()); cPlate.setBuffer(null); break; } }
					 */
					cPlate.setModel(cPlate.getBuffer());
					cPlate.setBuffer(null);
					cPlate.setModelComplete(true);
					if (cPlate.getModel() == null) {
						cPlate.setModel("");
					}
				}
				if (status == Constant.PLATE_INCOMPLETE_REMARK) {
					if (message.getText().equals(Constant.MESSAGE_PLATE_NEW_PLATE)) {
						cPlate.setRemark(cPlate.getBuffer());
						cPlate.setBuffer(null);
						plate.setCurrentPos(plate.getCurrentPos() + 1);
					}
					if (message.getText().equals(Constant.MESSAGE_PLATE_EXIT_PLATE)) {
						cPlate.setRemark(cPlate.getBuffer());
						cPlate.setBuffer(null);
						sentinelMessage.setInPlate(false);
						sentinelMessage.setPlateComplete(true);
					}
				}

				if (plate.size() == plate.getCurrentPos()) {
					cPlate = new VehiclePlate();
					plate.add(cPlate);
				} else {
					plate.set(plate.getCurrentPos(), cPlate);
				}
				sentinelMessage.setCurrentPlate(plate);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				int newStatus = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("plate");
				if (newStatus == Constant.PLATE_INCOMPLETE_LICENSE_PLATE) {
					if (cPlate.isLicensePlateNotAM()) {
						sendMessage(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE_NOT_AM, message, resetKeyPad());
					} else if (cPlate.getBuffer() == null || cPlate.getBuffer().isEmpty()) {
						sendMessage(Constant.MESSAGE_ENTER_VEHICLE_LICENSE_PLATE, message,
								getVehicleLicensePlateKeyPad());
					}
				}
				if (newStatus == Constant.PLATE_INCOMPLETE_IDENTIFICATION_PLATE) {
					if (text.equals(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_IN_ALPHABETICAL_ORDER)) {
						sendMessage(Constant.MESSAGE_ENTER_VEHICLE_IDENTIFICATION_PLATE, message,
								getVehicleIdentificationPlateKeyPad(IdentificationPlateMode.ALPHABETICAL));
					} else if (text.equals(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_IN_POPULARITY_ORDER)) {
						sendMessage(Constant.MESSAGE_ENTER_VEHICLE_IDENTIFICATION_PLATE, message,
								getVehicleIdentificationPlateKeyPad(IdentificationPlateMode.POPULAR));
					} else if (cPlate.getBuffer() == null
							|| cPlate.getBuffer().isEmpty() && cPlate.getLicensePlate().contains("AM")) {
						sendMessage(Constant.MESSAGE_ENTER_VEHICLE_IDENTIFICATION_PLATE, message,
								getVehicleIdentificationPlateKeyPad(IdentificationPlateMode.POPULAR));
					}
				}
				if (newStatus == Constant.PLATE_INCOMPLETE_MODEL) {
					if (cPlate.getBuffer() == null || cPlate.getBuffer().isEmpty()) {
						sendMessage(Constant.MESSAGE_SELECT_VEHICLE_MODEL, message, getVehicleDeployment());
					}
				}
				if (newStatus == Constant.PLATE_INCOMPLETE_REMARK) {
					if (cPlate.getBuffer() == null || cPlate.getBuffer().isEmpty()) {
						sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
						sendMessage(Constant.MESSAGE_PLATE_RETURN_OPTIONS, message,
								getVehicleLicensePlateReturnOptionsKeyPad());
					}
				}

			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void action(Message message, String text) {
		int status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("action");
		try {
			SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
			String action = sentinelMessage.getCurrentAction();
			String newAction = text;
			if (newAction.equals(Constant.COMPLETE) && status == Constant.ACTION_READY) {
				sentinelMessage.setActionComplete(true);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
			} else {
				if (status == Constant.ACTION_INCOMPLETE_REQUIRE_LOCATION_INPUT) {
					action = action.replace(Constant.ACTION_LOCATION_LABEL, newAction);
					sentinelMessage.setCurrentAction(action);
					sendMessage(Constant.MESSAGE_ENTER_ACTION_TYPE, message, getActionKeyPad());
				} else if (status == Constant.ACTION_INCOMPLETE_REQUIRE_STREET_INPUT) {
					action = action.replace(Constant.ACTION_STREET_LABEL, newAction);
					sentinelMessage.setCurrentAction(action);
					sendMessage(Constant.MESSAGE_ENTER_ACTION_TYPE, message, getActionKeyPad());
				} else {
					if (!newAction.equals(Constant.COMPLETE)) {
						sentinelMessage.setCurrentAction(action + newAction);
					}
					ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
					status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("action");
					if (status == Constant.ACTION_MISSING || action == null) {
						action = "";
						sentinelMessage.setCurrentAction(action);
					}
					if (status == Constant.ACTION_INCOMPLETE_REQUIRE_LOCATION_INPUT) {
						sendMessage(Constant.MESSAGE_ENTER_ACTION_MOVING, message,
								getDistrictKeyPadByDistrict(sentinelMessage.getDistrict()));
					} else if (status == Constant.ACTION_INCOMPLETE_REQUIRE_STREET_INPUT) {
						sendMessage(Constant.MESSAGE_ENTER_ACTION_MOVING, message,
								getLocationKeyPadByDistrict(sentinelMessage.getDistrict(), 0));
					} else {
						sendMessage(Constant.MESSAGE_ENTER_ACTION_TYPE, message, getActionKeyPad());
					}
				}
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void deployment(Message message, String text) {
		int status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("deployment");
		try {
			SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
			String deployment = sentinelMessage.getCurrentDeployment();
			String newDeployment = text;
			if (newDeployment.equals(Constant.COMPLETE) && status == Constant.DEPLOYMENT_READY) {
				sentinelMessage.setDeploymentComplete(true);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
			} else {
				if (newDeployment.equals(Constant.COMPLETE) && status == Constant.DEPLOYMENT_INCOMPLETE) {
					ReplyKeyboardMarkup k = getDeploymentNameKeyPad();
					sendMessage(Constant.MESSAGE_ENTER_DEPLOYMENT_TYPE, message, getDeploymentNameKeyPad());
				} else {
					if (!newDeployment.equals(Constant.COMPLETE)) {
						if (deployment != null && deployment.isEmpty() == false && newDeployment != null && newDeployment.isEmpty() == false && StringUtils.isStrictlyNumeric(deployment) == false && StringUtils.isStrictlyNumeric(newDeployment) == false) {
							sentinelMessage.setCurrentDeployment(deployment + " " + newDeployment);
						} else {
							sentinelMessage.setCurrentDeployment(deployment + newDeployment);
						}
					}
					ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
					status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("deployment");
					if (status == Constant.DEPLOYMENT_MISSING || deployment == null) {
						deployment = "";
						sentinelMessage.setCurrentDeployment(deployment);
					}
					ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
					sendMessage(Constant.MESSAGE_ENTER_DEPLOYMENT_QUANTITY, message, getDeploymentNumberKeyPad());
				}
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void location(Message message, String text) {
		int status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("location");
		int districtStatus = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("district");
		try {
			if (districtStatus == Constant.DISTRICT_UNKNOWN) {
				SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
				String location = sentinelMessage.getLocation();
				String newLocation = text;
				if (location == null) {
					location = "";
					sentinelMessage.setLocation(location);
					ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
					sendMessage(Constant.MESSAGE_ENTER_LOCATION_DISTRICT_UNKNOWN, message,
							getLocationKeyPadByDistrict(sentinelMessage.getDistrict(), 0));
				} else if (location != null && !newLocation.equals("")) {
					sentinelMessage.setLocation(location + newLocation);
					ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
					sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
				}
			} else {
				SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
				String location = sentinelMessage.getLocation();
				String newLocation = text;
				if (newLocation.equals(Constant.LOCATION_SELECTION_MOVE_LEFT_SYMBOL)) {
					if (status == Constant.LOCATION_ONE_STREETNAME_ONLY) {
						int locationPos = sentinelMessage.getLocationPos() - Constant.LOCATION_RESULT_STEP < 0 ? 0
								: sentinelMessage.getLocationPos() - Constant.LOCATION_RESULT_STEP;
						sentinelMessage.setLocationPos(locationPos);
						ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
						sendMessage(Constant.MESSAGE_ENTER_LOCATION_STREET_BUILDING, message,
								getRelatedStreetKeyPadByDistrictLocation(sentinelMessage.getDistrict(), location,
										locationPos));
					} else {
						int locationPos = sentinelMessage.getLocationPos() - Constant.LOCATION_RESULT_STEP < 0 ? 0
								: sentinelMessage.getLocationPos() - Constant.LOCATION_RESULT_STEP;
						sentinelMessage.setLocationPos(locationPos);
						ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
						sendMessage(Constant.MESSAGE_ENTER_LOCATION_STREET, message,
								getLocationKeyPadByDistrict(sentinelMessage.getDistrict(), locationPos));
					}
				} else if (newLocation.equals(Constant.LOCATION_SELECTION_MOVE_RIGHT_SYMBOL)) {
					if (status == Constant.LOCATION_ONE_STREETNAME_ONLY) {
						int locationPos = sentinelMessage.getLocationPos() + Constant.LOCATION_RESULT_STEP;
						sentinelMessage.setLocationPos(locationPos);
						ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
						sendMessage(Constant.MESSAGE_ENTER_LOCATION_STREET_BUILDING, message,
								getRelatedStreetKeyPadByDistrictLocation(sentinelMessage.getDistrict(), location,
										locationPos));
					} else {
						int locationPos = sentinelMessage.getLocationPos() + Constant.LOCATION_RESULT_STEP;
						sentinelMessage.setLocationPos(locationPos);
						ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
						sendMessage(Constant.MESSAGE_ENTER_LOCATION_STREET, message,
								getLocationKeyPadByDistrict(sentinelMessage.getDistrict(), locationPos));
					}
				} else {
					sentinelMessage.setLocation(location + newLocation);
					ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
					status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("location");
					if (status == Constant.LOCATION_VALUE_MISSING || location == null) {
						location = "";
						sentinelMessage.setLocation(location);
						ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
						sendMessage(Constant.MESSAGE_ENTER_LOCATION_STREET, message,
								getLocationKeyPadByDistrict(sentinelMessage.getDistrict(), 0));
					} else if (status == Constant.LOCATION_ONE_STREETNAME_ONLY) {
						int locationPos = 0;
						sentinelMessage.setLocationPos(locationPos);
						ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
						sendMessage(Constant.MESSAGE_ENTER_LOCATION_STREET_BUILDING, message,
								getRelatedStreetKeyPadByDistrictLocation(sentinelMessage.getDistrict(), newLocation,
										locationPos));
					} else if (status == Constant.VALID || status == Constant.LOCATION_UNKNOWN) {
						if (status == Constant.VALID) {
							sentinelMessage.setLocation(sentinelMessage.getLocation());
						}
						ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
						sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
					}
				}
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void district(Message message, String text) {
		int status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("district");
		try {
			SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
			String district = sentinelMessage.getDistrict();
			String newDistrict = text;
			int districtPosX = sentinelMessage.getDistrictPosX();
			int districtPosY = sentinelMessage.getDistrictPosY();
			if (newDistrict.equals(Constant.DISTRICT_SELECTION_MOVE_LEFT_SYMBOL)) {
				districtPosX = districtPosX - 3;
				sentinelMessage.setDistrictPosX(districtPosX);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(Constant.MESSAGE_ENTER_DISTRICT, message,
						getDistrictKeyPadByRegion(district, districtPosX, districtPosY));
			} else if (newDistrict.equals(Constant.DISTRICT_SELECTION_MOVE_RIGHT_SYMBOL)) {
				districtPosX = districtPosX + 3;
				sentinelMessage.setDistrictPosX(districtPosX);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(Constant.MESSAGE_ENTER_DISTRICT, message,
						getDistrictKeyPadByRegion(district, districtPosX, districtPosY));
			} else if (newDistrict.equals(Constant.DISTRICT_SELECTION_MOVE_UP_SYMBOL)) {
				districtPosY = districtPosY + 2;
				sentinelMessage.setDistrictPosY(districtPosY);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(Constant.MESSAGE_ENTER_DISTRICT, message,
						getDistrictKeyPadByRegion(district, districtPosX, districtPosY));
			} else if ((newDistrict.equals(Constant.DISTRICT_SELECTION_MOVE_DOWN_SYMBOL))) {
				districtPosY = districtPosY - 2;
				sentinelMessage.setDistrictPosY(districtPosY);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(Constant.MESSAGE_ENTER_DISTRICT, message,
						getDistrictKeyPadByRegion(district, districtPosX, districtPosY));
			} else {
				sentinelMessage.setDistrict(newDistrict);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("district");
			}
			if (status == Constant.DISTRICT_VALUE_MISSING || district == null) {
				district = "";
				sentinelMessage.setDistrict(district);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(Constant.MESSAGE_ENTER_REGION, message,
						getDistrictRegionKeyPad(sentinelMessage.getLastLocation()));
			} else if (status == Constant.DISTRICT_REGION_VALID) {
				sendMessage(Constant.MESSAGE_ENTER_DISTRICT, message,
						getDistrictKeyPadByRegion(newDistrict, districtPosX, districtPosY));
			} else if (status == Constant.DISTRICT_REGION_VALID_NT) {
				sendMessage(Constant.MESSAGE_ENTER_DISTRICT, message,
						getDistrictKeyPadByRegion(newDistrict, districtPosX, districtPosY, true));
			} else if (status == Constant.VALID) {
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void time(Message message, String text) {
		int status = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("time");
		try {
			SentinelMessage sentinelMessage = ongoingReport.getSentinelMessageByChatId(message.getChatId());
			String time = sentinelMessage.getTime();
			if (time == null) {
				time = "";
				sentinelMessage.setTime(time);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				sendMessage(Constant.MESSAGE_ENTER_TIME, message, getTimeKeyPad());
			} else {
				sentinelMessage.setTime(sentinelMessage.getTime() + text);
				ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
				int newTimeStatus = ongoingReport.getUserReportStatusByChatId(message.getChatId()).get("time");
				if (!(newTimeStatus == Constant.TIME_VALUE_INCOMPLETE || newTimeStatus == Constant.VALID)) {
					sentinelMessage.setTime("");
					ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
					sendMessage(Constant.MESSAGE_INVALID_TIME, message, getTimeKeyPad());
				} else if (newTimeStatus == Constant.VALID) {
					ongoingReport.updateSentinelMessageByChatId(message.getChatId(), sentinelMessage);
					sendMessage(sentinelMessage.displaySentinelMessage(), message, resetKeyPad());
				}
			}
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private ReplyKeyboardMarkup getFastInputPlateKeyPad() {
		IdentificationPlate[] identificationPlate = OngoingReport.identificationPlate;
		List<String> numPad = Arrays.asList(OngoingReport.numPad);
		ArrayList<String> extraOptions = new ArrayList<String>();
		Arrays.sort(identificationPlate, new Comparator<IdentificationPlate>() {
			@Override
			public int compare(IdentificationPlate o1, IdentificationPlate o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		ArrayList<String> identificationPlateList = new ArrayList<String>();
		for (int i = 0; i < identificationPlate.length; i++) {
			identificationPlateList.add(identificationPlate[i].getName());
		}
		extraOptions.add("Platoon");
		extraOptions.add("/");
		extraOptions.add(Constant.MESSAGE_ENTER_VEHICLE_FAST_INPUT_MODE);
		extraOptions.add(Constant.MESSAGE_PLATE_FAST_PLATE_INPUT_COMPLETE);
		ReplyKeyboardMarkup numPadKeyboardMarkup = buildDefaultLayout(numPad, extraOptions, -1, 4);
		ReplyKeyboardMarkup identificationPlateKeyboardMarkup = buildDefaultLayout(identificationPlateList, null, -1,
				6);
		return combineKeyboardLayout(numPadKeyboardMarkup, identificationPlateKeyboardMarkup);
	}

	private ReplyKeyboardMarkup getVehicleLicensePlateReturnOptionsKeyPad() {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow row1 = new KeyboardRow();
		KeyboardButton button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		button11.setText(Constant.MESSAGE_PLATE_NEW_PLATE);
		button12.setText(Constant.MESSAGE_PLATE_EXIT_PLATE);
		row1.add(button11);
		row1.add(button12);
		keyboard.add(row1);
		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(false);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;
	}

	private ReplyKeyboardMarkup getVehicleDeployment() {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		int rowCount = 3;
		int extraCount = 0;
		String[] extraOptions = { Constant.COMPLETE };
		List<String> vehicleList = new ArrayList<String>();
		for (int i = 0; i < OngoingReport.deployment.length; i++) {
			if (OngoingReport.deployment[i].getType().equals("vehicle")) {
				vehicleList.add(OngoingReport.deployment[i].getName());
			}
		}
		int column = vehicleList.size() / rowCount + 1;
		for (int i = 0; i < rowCount; i++) {
			KeyboardRow row = new KeyboardRow();
			for (int j = 0; j < column; j++) {
				KeyboardButton button = new KeyboardButton(Constant.KEYBOARD_EMPTY);
				if (vehicleList.size() > i * column + j - extraCount) {
					if (j == column - 1 && extraOptions.length > 0) {
						if (extraCount < extraOptions.length) {
							button.setText(extraOptions[extraCount]);
							extraCount++;
						}
					} else {
						button.setText(vehicleList.get(i * column + j - extraCount));
					}
				}
				row.add(button);
			}
			keyboard.add(row);
		}
		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(true);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;
	}

	private ReplyKeyboardMarkup getVehicleIdentificationPlateNumKeyPad() {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow row1 = new KeyboardRow();
		KeyboardRow row2 = new KeyboardRow();
		KeyboardRow row3 = new KeyboardRow();
		KeyboardRow row4 = new KeyboardRow();

		KeyboardButton button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button13 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button14 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button21 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button22 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button23 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button24 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button31 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button32 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button33 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button34 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button41 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button42 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button43 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button44 = new KeyboardButton(Constant.KEYBOARD_EMPTY);

		button11.setText("7");
		button12.setText("8");
		button13.setText("9");
		button14.setText("/");
		button21.setText("4");
		button22.setText("5");
		button23.setText("6");
		button24.setText("Platoon");
		button31.setText("1");
		button32.setText("2");
		button33.setText("3");
		button41.setText(Constant.CANCEL);
		button42.setText("0");
		button43.setText(Constant.COMPLETE);

		row1.add(button11);
		row1.add(button12);
		row1.add(button13);
		row1.add(button14);
		row2.add(button21);
		row2.add(button22);
		row2.add(button23);
		row2.add(button24);
		row3.add(button31);
		row3.add(button32);
		row3.add(button33);
		row3.add(button34);
		row4.add(button41);
		row4.add(button42);
		row4.add(button43);
		row4.add(button44);
		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		keyboard.add(row4);
		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(false);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;
	}

	private ReplyKeyboardMarkup combineKeyboardLayout(ReplyKeyboardMarkup main,
			ReplyKeyboardMarkup... keyboardMarkups) {
		List<KeyboardRow> mainKeyboard = main.getKeyboard();
		for (int i = 0; i < keyboardMarkups.length; i++) {
			List<KeyboardRow> secondaryKeyboard = keyboardMarkups[i].getKeyboard();
			for (int j = 0; j < secondaryKeyboard.size(); j++) {
				mainKeyboard.add(mainKeyboard.size(), secondaryKeyboard.get(j));
			}
		}
		main.setKeyboard(mainKeyboard);
		return main;
	}

	private ReplyKeyboardMarkup buildDefaultLayout(List<String> valueList, List<String> extraOptions, int rowCountIn,
			int columnCountIn) {
		if (valueList.isEmpty()) {
			return null;
		}
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		int rowCount = 4;
		int columnCount = 4;
		int columnOffset = 0;
		if (extraOptions == null) {
			extraOptions = new ArrayList<String>();
		}
		if (rowCountIn < 0 && columnCountIn > 0) {
			if (extraOptions.size() > 0) {
				columnOffset--;
			}
			rowCount = (int) Math.ceil(valueList.size() / (double) (columnCountIn + columnOffset));
		} else if (columnCountIn < 0 && rowCountIn > 0) {
			columnCount = (int) Math.ceil(valueList.size() / (double) rowCountIn);
		}
		if (rowCountIn > 0) {
			rowCount = rowCountIn;
		}
		if (columnCountIn > 0) {
			columnCount = columnCountIn;
		}
		int extraCount = 0;
		for (int i = 0; i < rowCount; i++) {
			KeyboardRow row = new KeyboardRow();
			for (int j = 0; j < columnCount; j++) {
				KeyboardButton button = new KeyboardButton(Constant.KEYBOARD_EMPTY);
				if (j == columnCount - 1 && extraOptions.size() > 0) {
					if (extraCount < extraOptions.size()) {
						if (extraOptions.get(extraCount) != null && extraOptions.get(extraCount).isEmpty() == false) {
							button.setText(extraOptions.get(extraCount));
						}
					}
					extraCount++;
				} else if (valueList.size() > i * columnCount + j - extraCount) {
					// String value = valueList.get(i * columnCount + j - extraCount); //debug only
					if (valueList.get(i * columnCount + j - extraCount) != null
							&& valueList.get(i * columnCount + j - extraCount).isEmpty() == false) {
						button.setText(valueList.get(i * columnCount + j - extraCount));
					}
				}
				row.add(button);
			}
			keyboard.add(row);
		}
		replyKeyboardMarkup.setKeyboard(keyboard);
		if (extraOptions.size() == 0) {
			replyKeyboardMarkup.setOneTimeKeyboard(true);
		} else {
			replyKeyboardMarkup.setOneTimeKeyboard(false);
		}
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;

	}

	private ReplyKeyboardMarkup getVehicleIdentificationPlateKeyPad(IdentificationPlateMode mode) {
		return getVehicleIdentificationPlateKeyPad(mode, 0);
	}

	private ReplyKeyboardMarkup getVehicleIdentificationPlateKeyPad(IdentificationPlateMode mode,
			int firstResultOrder) {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();

		if (mode.equals(IdentificationPlateMode.POPULAR)) {
			IdentificationPlate[] identificationPlate = OngoingReport.identificationPlate;
			Arrays.sort(identificationPlate, new Comparator<IdentificationPlate>() {
				@Override
				public int compare(IdentificationPlate o1, IdentificationPlate o2) {
					return o2.getCount() - o1.getCount();
				}
			});
			ArrayList<String> identificationPlateList = new ArrayList<String>();
			ArrayList<String> extraOptions = new ArrayList<String>();
			extraOptions.add(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_IN_ALPHABETICAL_ORDER);
			extraOptions.add(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_NO_PLATE);
			for (int i = 0; i < identificationPlate.length; i++) {
				identificationPlateList.add(identificationPlate[i].getName());
			}
			replyKeyboardMarkup = buildDefaultLayout(identificationPlateList, extraOptions, 6, 6);
		}
		if (mode.equals(IdentificationPlateMode.ALPHABETICAL)) {
			IdentificationPlate[] identificationPlate = OngoingReport.identificationPlate;
			Arrays.sort(identificationPlate, new Comparator<IdentificationPlate>() {

				@Override
				public int compare(IdentificationPlate o1, IdentificationPlate o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			ArrayList<String> identificationPlateList = new ArrayList<String>();
			ArrayList<String> extraOptions = new ArrayList<String>();
			extraOptions.add(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_IN_POPULARITY_ORDER);
			extraOptions.add(Constant.MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_NO_PLATE);
			for (int i = 0; i < identificationPlate.length; i++) {
				identificationPlateList.add(identificationPlate[i].getName());
			}
			replyKeyboardMarkup = buildDefaultLayout(identificationPlateList, extraOptions, -1, 6);
		}
		return replyKeyboardMarkup;

	}

	private ReplyKeyboardMarkup getVehicleLicensePlateKeyPad() {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow row1 = new KeyboardRow();
		KeyboardRow row2 = new KeyboardRow();
		KeyboardRow row3 = new KeyboardRow();
		KeyboardRow row4 = new KeyboardRow();

		KeyboardButton button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button13 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button14 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button21 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button22 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button23 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button24 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button31 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button32 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button33 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button34 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button41 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button42 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button43 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button44 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		button11.setText("7");
		button12.setText("8");
		button13.setText("9");
		button14.setText("/");
		button21.setText("4");
		button22.setText("5");
		button23.setText("6");
		button24.setText(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE_NOT_AM);
		button31.setText("1");
		button32.setText("2");
		button33.setText("3");
		button34.setText(Constant.MESSAGE_ENTER_VEHICLE_FAST_INPUT_MODE);
		button41.setText(Constant.CANCEL);
		button42.setText("0");
		button43.setText(Constant.COMPLETE);

		row1.add(button11);
		row1.add(button12);
		row1.add(button13);
		row1.add(button14);
		row2.add(button21);
		row2.add(button22);
		row2.add(button23);
		row2.add(button24);
		row3.add(button31);
		row3.add(button32);
		row3.add(button33);
		row3.add(button34);
		row4.add(button41);
		row4.add(button42);
		row4.add(button43);
		row4.add(button44);
		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		keyboard.add(row4);

		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(false);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;

	}

	private ReplyKeyboardMarkup getRemarkKeyPad() {
		//Button13 is used for forwarding message and it is disabled
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardButton button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		//KeyboardButton button13 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		button11.setText(Constant.MESSAGE_REMARK_NEW_INFO);
		button12.setText(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE);
		//button13.setText(Constant.MESSAGE_REMARK_FORWARD);
		KeyboardRow row1 = new KeyboardRow();
		row1.add(button11);
		row1.add(button12);
		//row1.add(button13);
		keyboard.add(row1);
		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(true);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;
	}

	private ReplyKeyboardMarkup getDeploymentNameKeyPad() {
		Deployment[] deployment = ongoingReport.getDeployment();
		ArrayList<String> deploymentList = new ArrayList<String>();
		ArrayList<String> extraOptions = new ArrayList<String>();
		for (int i = 0; i < deployment.length; i++) {
			deploymentList.add(deployment[i].getName());
		}
		return buildDefaultLayout(deploymentList, null, -1, 6);
	}

	private ReplyKeyboardMarkup getActionKeyPad() {
		Action[] action = ongoingReport.action;
		ArrayList<String> actionList = new ArrayList<String>();
		ArrayList<String> extraOptions = new ArrayList<String>();
		extraOptions.add(Constant.COMPLETE);
		extraOptions.add(Constant.CANCEL);
		for (int i = 0; i < action.length; i++) {
			actionList.add(action[i].getName());
		}
		return buildDefaultLayout(actionList, extraOptions, -1, 4);
	}

	private ReplyKeyboardMarkup getDeploymentNumberKeyPad() {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow row1 = new KeyboardRow();
		KeyboardRow row2 = new KeyboardRow();
		KeyboardRow row3 = new KeyboardRow();
		KeyboardRow row4 = new KeyboardRow();

		KeyboardButton button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button13 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button21 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button22 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button23 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button31 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button32 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button33 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button41 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button42 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button43 = new KeyboardButton(Constant.KEYBOARD_EMPTY);

		button11.setText("7");
		button12.setText("8");
		button13.setText("9");
		button21.setText("4");
		button22.setText("5");
		button23.setText("6");
		button31.setText("1");
		button32.setText("2");
		button33.setText("3");
		button41.setText(Constant.CANCEL);
		button42.setText("0");
		button43.setText(Constant.COMPLETE);

		row1.add(button11);
		row1.add(button12);
		row1.add(button13);
		row2.add(button21);
		row2.add(button22);
		row2.add(button23);
		row3.add(button31);
		row3.add(button32);
		row3.add(button33);
		row4.add(button41);
		row4.add(button42);
		row4.add(button43);

		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		keyboard.add(row4);

		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(false);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;
	}

	private ReplyKeyboardMarkup getRelatedStreetKeyPadByDistrictLocation(String districtName, String locationName,
			int firstResultOrder) {
		/*
		 * ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		 * List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>(); KeyboardRow row1 =
		 * new KeyboardRow(); KeyboardRow row2 = new KeyboardRow(); KeyboardRow row3 =
		 * new KeyboardRow(); KeyboardRow row4 = new KeyboardRow(); KeyboardButton
		 * button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button21 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button22 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button23 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button31 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button32 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button33 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button41 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button42 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button43 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		 */

		Region[] region = ongoingReport.getRegion();
		for (int i = 0; i < region.length; i++) {
			District district = region[i].getDistrict(districtName);
			if (district != null) {
				Location[] location = district.getLocation();
				for (int j = 0; j < location.length; j++) {
					if (location != null && location[j].getName().equals(locationName)) {
						/*
						 * button11.setText(Constant.LOCATION_SELECTION_MOVE_LEFT_SYMBOL);
						 * button12.setText(Constant.LOCATION_SELECTION_MOVE_RIGHT_SYMBOL);
						 * button21.setText(location[j].getRelatedStreet().length > firstResultOrder ?
						 * location[j].getRelatedStreet()[firstResultOrder].getName() :
						 * Constant.KEYBOARD_EMPTY);
						 * button22.setText(location[j].getRelatedStreet().length > firstResultOrder + 1
						 * ? location[j].getRelatedStreet()[firstResultOrder + 1].getName() :
						 * Constant.KEYBOARD_EMPTY);
						 * button23.setText(location[j].getRelatedStreet().length > firstResultOrder + 2
						 * ? location[j].getRelatedStreet()[firstResultOrder + 2].getName() :
						 * Constant.KEYBOARD_EMPTY);
						 * button31.setText(location[j].getRelatedStreet().length > firstResultOrder + 3
						 * ? location[j].getRelatedStreet()[firstResultOrder + 3].getName() :
						 * Constant.KEYBOARD_EMPTY);
						 * button32.setText(location[j].getRelatedStreet().length > firstResultOrder + 4
						 * ? location[j].getRelatedStreet()[firstResultOrder + 4].getName() :
						 * Constant.KEYBOARD_EMPTY);
						 * button33.setText(location[j].getRelatedStreet().length > firstResultOrder + 5
						 * ? location[j].getRelatedStreet()[firstResultOrder + 5].getName() :
						 * Constant.KEYBOARD_EMPTY);
						 * button41.setText(location[j].getRelatedStreet().length > firstResultOrder + 6
						 * ? location[j].getRelatedStreet()[firstResultOrder + 6].getName() :
						 * Constant.KEYBOARD_EMPTY);
						 * button42.setText(location[j].getRelatedStreet().length > firstResultOrder + 7
						 * ? location[j].getRelatedStreet()[firstResultOrder + 7].getName() :
						 * Constant.KEYBOARD_EMPTY); button43.setText(Constant.CANCEL);
						 * row1.add(button11); row1.add(button12); row2.add(button21);
						 * row2.add(button22); row2.add(button23); row3.add(button31);
						 * row3.add(button32); row3.add(button33); row4.add(button41);
						 * row4.add(button42); row4.add(button43); keyboard.add(row1);
						 * keyboard.add(row2); keyboard.add(row3); keyboard.add(row4);
						 * replyKeyboardMarkup.setKeyboard(keyboard);
						 * replyKeyboardMarkup.setOneTimeKeyboard(false);
						 * replyKeyboardMarkup.setResizeKeyboard(false);
						 * replyKeyboardMarkup.setSelective(true); return replyKeyboardMarkup;
						 */
						ArrayList<String> relatedStreetList = new ArrayList<String>();
						ArrayList<String> extraOptions = new ArrayList<String>();
						for (int k = 0; k < location[j].getRelatedStreet().length; k++) {
							relatedStreetList.add(location[j].getRelatedStreet()[k].getName());
						}
						return buildDefaultLayout(relatedStreetList, extraOptions, -1, 5);
					}
				}
			}
		}
		return null;
	}

	private ReplyKeyboardMarkup getLocationKeyPadByDistrict(String districtName, int firstResultOrder) {
		/*
		 * ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		 * List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>(); KeyboardRow row1 =
		 * new KeyboardRow(); KeyboardRow row2 = new KeyboardRow(); KeyboardRow row3 =
		 * new KeyboardRow(); KeyboardRow row4 = new KeyboardRow(); KeyboardButton
		 * button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button21 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button22 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button23 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button31 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button32 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button33 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button41 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button42 = new KeyboardButton(Constant.KEYBOARD_EMPTY); KeyboardButton
		 * button43 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		 * 
		 * Region[] region = ongoingReport.getRegion(); for (int i = 0; i <
		 * region.length; i++) { District district =
		 * region[i].getDistrict(districtName); if (district != null &&
		 * district.getLocation() != null) {
		 * button11.setText(Constant.LOCATION_SELECTION_MOVE_LEFT_SYMBOL);
		 * button12.setText(Constant.LOCATION_SELECTION_MOVE_RIGHT_SYMBOL);
		 * button21.setText(district.getLocation().length > firstResultOrder ?
		 * district.getLocation()[firstResultOrder].getName() :
		 * Constant.KEYBOARD_EMPTY); button22.setText(district.getLocation().length >
		 * firstResultOrder + 1 ? district.getLocation()[firstResultOrder + 1].getName()
		 * : Constant.KEYBOARD_EMPTY); button23.setText(district.getLocation().length >
		 * firstResultOrder + 2 ? district.getLocation()[firstResultOrder + 2].getName()
		 * : Constant.KEYBOARD_EMPTY); button31.setText(district.getLocation().length >
		 * firstResultOrder + 3 ? district.getLocation()[firstResultOrder + 3].getName()
		 * : Constant.KEYBOARD_EMPTY); button32.setText(district.getLocation().length >
		 * firstResultOrder + 4 ? district.getLocation()[firstResultOrder + 4].getName()
		 * : Constant.KEYBOARD_EMPTY); button33.setText(district.getLocation().length >
		 * firstResultOrder + 5 ? district.getLocation()[firstResultOrder + 5].getName()
		 * : Constant.KEYBOARD_EMPTY); button41.setText(district.getLocation().length >
		 * firstResultOrder + 6 ? district.getLocation()[firstResultOrder + 6].getName()
		 * : Constant.KEYBOARD_EMPTY); button42.setText(district.getLocation().length >
		 * firstResultOrder + 7 ? district.getLocation()[firstResultOrder + 7].getName()
		 * : Constant.KEYBOARD_EMPTY); button43.setText(Constant.CANCEL);
		 * row1.add(button11); row1.add(button12); row2.add(button21);
		 * row2.add(button22); row2.add(button23); row3.add(button31);
		 * row3.add(button32); row3.add(button33); row4.add(button41);
		 * row4.add(button42); row4.add(button43); keyboard.add(row1);
		 * keyboard.add(row2); keyboard.add(row3); keyboard.add(row4);
		 * replyKeyboardMarkup.setKeyboard(keyboard);
		 * replyKeyboardMarkup.setOneTimeKeyboard(false);
		 * replyKeyboardMarkup.setResizeKeyboard(false);
		 * replyKeyboardMarkup.setSelective(true); return replyKeyboardMarkup; } }
		 * return null;
		 */
		Region[] region = ongoingReport.getRegion();
		ArrayList<String> locationList = new ArrayList<String>();
		ArrayList<String> extraOptions = new ArrayList<String>();
		for (int i = 0; i < region.length; i++) {
			District district = region[i].getDistrict(districtName);
			if (district != null && district.getLocation() != null) {
				for (int j = 0; j < district.getLocation().length; j++) {
					locationList.add(district.getLocation()[j].getName());
				}
			}
		}
		return buildDefaultLayout(locationList, extraOptions, -1, 5);
	}

	private ReplyKeyboardMarkup getDistrictKeyPadByDistrict(String districtName) {
		District district = null;
		for (int i = 0; i < ongoingReport.region.length; i++) {
			district = ongoingReport.region[i].getDistrict(districtName);
			if (district != null) {
				return getDistrictKeyPadByRegion(ongoingReport.region[i].getName(), district.getPosition().getX(),
						district.getPosition().getY(), true);
			}
		}
		return null;
	}

	private ReplyKeyboardMarkup getDistrictKeyPadByRegion(String regionName, int posX, int posY) {
		return getDistrictKeyPadByRegion(regionName, posX, posY, false);
	}

	private ReplyKeyboardMarkup getDistrictKeyPadByRegion(String regionName, int posX, int posY,
			boolean fixedPosition) {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow row1 = new KeyboardRow();
		KeyboardRow row2 = new KeyboardRow();
		KeyboardRow row3 = new KeyboardRow();
		KeyboardRow row4 = new KeyboardRow();
		KeyboardButton button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button13 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button14 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button15 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button21 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button22 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button23 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button24 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button25 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button31 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button32 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button33 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button34 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button35 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button41 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button42 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button43 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button44 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button45 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		Region region = ongoingReport.getRegion(regionName);
		button11.setText(Constant.DISTRICT_SELECTION_MOVE_LEFT_SYMBOL);
		button12.setText(Constant.DISTRICT_SELECTION_MOVE_UP_SYMBOL);
		button13.setText(Constant.DISTRICT_SELECTION_MOVE_DOWN_SYMBOL);
		button14.setText(Constant.DISTRICT_SELECTION_MOVE_RIGHT_SYMBOL);
		button15.setText(Constant.CANCEL);

		button21.setText(region.getDistrictByPos(posX - 2, posY + 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX - 2, posY + 1).getName());
		button22.setText(region.getDistrictByPos(posX - 1, posY + 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX - 1, posY + 1).getName());
		button23.setText(region.getDistrictByPos(posX, posY + 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX, posY + 1).getName());
		button24.setText(region.getDistrictByPos(posX + 1, posY + 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX + 1, posY + 1).getName());
		button25.setText(region.getDistrictByPos(posX + 2, posY + 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX + 2, posY + 1).getName());
		button31.setText(region.getDistrictByPos(posX - 2, posY) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX - 2, posY).getName());
		button32.setText(region.getDistrictByPos(posX - 1, posY) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX - 1, posY).getName());
		button33.setText(region.getDistrictByPos(posX, posY) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX, posY).getName());
		button34.setText(region.getDistrictByPos(posX + 1, posY) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX + 1, posY).getName());
		button35.setText(region.getDistrictByPos(posX + 2, posY) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX + 2, posY).getName());
		button41.setText(region.getDistrictByPos(posX - 2, posY - 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX - 2, posY - 1).getName());
		button42.setText(region.getDistrictByPos(posX - 1, posY - 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX - 1, posY - 1).getName());
		button43.setText(region.getDistrictByPos(posX, posY - 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX, posY - 1).getName());
		button44.setText(region.getDistrictByPos(posX + 1, posY - 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX + 1, posY - 1).getName());
		button45.setText(region.getDistrictByPos(posX + 2, posY - 1) == null ? Constant.KEYBOARD_EMPTY
				: region.getDistrictByPos(posX + 2, posY - 1).getName());
		if (!fixedPosition) {
			row1.add(button11);
			row1.add(button12);
			row1.add(button13);
			row1.add(button14);
			row1.add(button15);

		}
		row2.add(button21);
		row2.add(button22);
		row2.add(button23);
		row2.add(button24);
		row2.add(button25);
		row3.add(button31);
		row3.add(button32);
		row3.add(button33);
		row3.add(button34);
		row3.add(button35);
		row4.add(button41);
		row4.add(button42);
		row4.add(button43);
		row4.add(button44);
		row4.add(button45);
		if (!fixedPosition) {
			keyboard.add(row1);
		}
		keyboard.add(row2);
		keyboard.add(row3);
		keyboard.add(row4);
		if (fixedPosition) {
			KeyboardButton button51 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
			KeyboardButton button52 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
			KeyboardButton button53 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
			KeyboardButton button54 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
			KeyboardButton button55 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
			button51.setText(region.getDistrictByPos(posX - 2, posY - 2) == null ? Constant.KEYBOARD_EMPTY
					: region.getDistrictByPos(posX - 2, posY - 2).getName());
			button52.setText(region.getDistrictByPos(posX - 1, posY - 2) == null ? Constant.KEYBOARD_EMPTY
					: region.getDistrictByPos(posX - 1, posY - 2).getName());
			button53.setText(region.getDistrictByPos(posX, posY - 2) == null ? Constant.KEYBOARD_EMPTY
					: region.getDistrictByPos(posX, posY - 2).getName());
			button54.setText(region.getDistrictByPos(posX + 1, posY - 2) == null ? Constant.KEYBOARD_EMPTY
					: region.getDistrictByPos(posX + 1, posY - 2).getName());
			button55.setText(region.getDistrictByPos(posX + 2, posY - 2) == null ? Constant.KEYBOARD_EMPTY
					: region.getDistrictByPos(posX + 2, posY - 2).getName());
			KeyboardRow row5 = new KeyboardRow();
			row5.add(button51);
			row5.add(button52);
			row5.add(button53);
			row5.add(button54);
			row5.add(button55);
			keyboard.add(row5);
		}
		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(true);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;
	}

	private ReplyKeyboardMarkup getDistrictRegionKeyPad(String lastDistrict) {
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow row1 = new KeyboardRow();
		KeyboardRow row2 = new KeyboardRow();
		KeyboardRow row3 = new KeyboardRow();
		KeyboardButton button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button21 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button22 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button31 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button32 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		Region[] region = ongoingReport.getRegion();
		switch (region.length) {
		case 3:
			button31.setText(region[2].getName());
		case 2:
			button21.setText(region[1].getName());
		case 1:
			button11.setText(region[0].getName());
		}
		button12.setText(Constant.MESSAGE_PLATE_VEHICLE_LICENSE_PLATE);
		if (lastDistrict != null && !lastDistrict.equals("")) {
			button22.setText(lastDistrict);
		}
		button32.setText(Constant.CANCEL);
		row1.add(button11);
		row1.add(button12);
		row2.add(button21);
		row2.add(button22);
		row3.add(button31);
		row3.add(button32);
		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(true);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;
	}

	private ReplyKeyboardMarkup getTimeKeyPad() {
		SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("Asia/Hong_Kong"));
		calendar.setTime(new Date());
		calendar.setTimeInMillis(calendar.getTimeInMillis() + calendar.getTimeZone().getRawOffset());
		ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow row1 = new KeyboardRow();
		KeyboardRow row2 = new KeyboardRow();
		KeyboardRow row3 = new KeyboardRow();
		KeyboardRow row4 = new KeyboardRow();

		KeyboardButton button11 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button12 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button13 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button21 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button22 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button23 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button31 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button32 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button33 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button41 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button42 = new KeyboardButton(Constant.KEYBOARD_EMPTY);
		KeyboardButton button43 = new KeyboardButton(Constant.KEYBOARD_EMPTY);

		button11.setText("7");
		button12.setText("8");
		button13.setText("9");
		button21.setText("4");
		button22.setText("5");
		button23.setText("6");
		button31.setText("1");
		button32.setText("2");
		button33.setText("3");
		button41.setText(formatter.format(calendar.getTime()));
		button42.setText("0");
		button43.setText(Constant.CANCEL);

		row1.add(button11);
		row1.add(button12);
		row1.add(button13);
		row2.add(button21);
		row2.add(button22);
		row2.add(button23);
		row3.add(button31);
		row3.add(button32);
		row3.add(button33);
		row4.add(button41);
		row4.add(button42);
		row4.add(button43);

		keyboard.add(row1);
		keyboard.add(row2);
		keyboard.add(row3);
		keyboard.add(row4);

		replyKeyboardMarkup.setKeyboard(keyboard);
		replyKeyboardMarkup.setOneTimeKeyboard(false);
		replyKeyboardMarkup.setResizeKeyboard(false);
		replyKeyboardMarkup.setSelective(true);
		return replyKeyboardMarkup;
	}

	private ReplyKeyboardRemove resetKeyPad() {
		return new ReplyKeyboardRemove().setSelective(true);
	}

	private void sendMessage(String text, Message message, ReplyKeyboard keypad) throws TelegramApiException {
		SendMessage sendMessage0 = new SendMessage();
		sendMessage0.setChatId(message.getChatId());
		sendMessage0.setReplyMarkup(keypad);
		sendMessage0.setText(text);
		execute(sendMessage0);
	}

	@Override
	public String getBotUsername() {
		return "";
	}

	@Override
	public String getBotToken() {
		 return "";
	}

	enum IdentificationPlateMode {
		ALPHABETICAL, POPULAR
	}
}