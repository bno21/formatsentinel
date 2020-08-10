package sentinelFormatter.bot;

public class Constant {

	public static final int TIME_OUT = 240000;
	public static final int VALID = 0;
	public static final String CANCEL = "重新輸入";
	public static final String COMPLETE = "完成 / 下一步";
	public static final String EMPTY = "123456"; /* \u200f\u200f\u200e\u0020\u200e */
	public static final String KEYBOARD_EMPTY = "不適用";
	public static final int TIME_VALUE_MISSING = 199;
	public static final int TIME_VALUE_EMPTY = 198;
	public static final int TIME_VALUE_INCOMPLETE = 101;
	public static final int TIME_FIRST_VALUE_INVALID = 110;
	public static final int TIME_THIRD_VALUE_INVALID = 130;
	public static final int DISTRICT_REGION_VALID = 210;
	public static final int DISTRICT_REGION_VALID_NT = 212;
	public static final int DISTRICT_UNKNOWN = 220;
	public static final int DISTRICT_VALUE_MISSING = 299;
	public static final int DISTRICT_VALUE_EMPTY = 298;
	public static final String DISTRICT_SELECTION_MOVE_RIGHT_SYMBOL = ">";
	public static final String DISTRICT_SELECTION_MOVE_LEFT_SYMBOL = "<";
	public static final String DISTRICT_SELECTION_MOVE_UP_SYMBOL = "^";
	public static final String DISTRICT_SELECTION_MOVE_DOWN_SYMBOL = "v";
	public static final String DISTRICT_REGION_NT_MSG = "新界";
	public static final int LOCATION_ONE_STREETNAME_ONLY = 310;
	public static final int LOCATION_UNKNOWN = 320;
	public static final int LOCATION_VALUE_MISSING = 399;
	public static final int LOCATION_VALUE_EMPTY = 398;
	public static final int LOCATION_RESULT_STEP = 8;
	public static final String LOCATION_SELECTION_MOVE_LEFT_SYMBOL = "<";
	public static final String LOCATION_SELECTION_MOVE_RIGHT_SYMBOL = ">";
	public static final int DEPLOYMENT_MISSING = 499;
	public static final int DEPLOYMENT_EMPTY = 498;
	public static final int DEPLOYMENT_INCOMPLETE = 410;
	public static final int DEPLOYMENT_READY = 420;
	public static final int ACTION_MISSING = 499;
	public static final int ACTION_EMPTY = 498;
	public static final int ACTION_INCOMPLETE = 410;
	public static final int ACTION_READY = 420;
	public static final String ACTION_LOCATION_LABEL = "{loc}";
	public static final String ACTION_STREET_LABEL = "{street}";
	public static final int ACTION_INCOMPLETE_REQUIRE_LOCATION_INPUT = 411;
	public static final int ACTION_INCOMPLETE_REQUIRE_STREET_INPUT = 412;
	public static final int PLATE_MISSING = 50099;
	public static final int PLATE_INVALID = 50097;
	public static final int PLATE_INCOMPLETE = 50010;
	public static final int PLATE_INCOMPLETE_LICENSE_PLATE = 50011;
	public static final int PLATE_INCOMPLETE_MODEL = 50012;
	public static final int PLATE_INCOMPLETE_IDENTIFICATION_PLATE = 50013;
	public static final String PLATE_PLATOON_LABEL = "Platoon";
	public static final int PLATE_INCOMPLETE_REMARK = 50014;
	public static final int PLATE_READY = 50020;
	public static final int PLATE_IDENTIFICATION_PLATE_RESULT_STEP = 25;
	
	public static final String MESSAGE_ENTER_TIME = "請輸入時間 (格式：hhmm)";
	public static final String MESSAGE_INVALID_TIME = "時間格式不正確！請重新輸入！";
	public static final String MESSAGE_ENTER_REGION = "請輸入地區";
	public static final String MESSAGE_ENTER_DISTRICT = "請輸入地區\n如有需要可用箭嘴尋找相關地區\n若果找不到相關地區，請自行輸入";
	public static final String MESSAGE_ENTER_LOCATION_STREET = "請輸入有關街道\n如有需要可用箭嘴尋找有關街道\n若果找不到有關街道，請自行輸入\n但輸入內容必須為街道準確位置！";
	public static final String MESSAGE_ENTER_LOCATION_STREET_BUILDING = "請輸入有關街道或該街道上的地標";
	public static final String MESSAGE_ENTER_LOCATION_DISTRICT_UNKNOWN = "請輸入街道準確位置";
	public static final String MESSAGE_ENTER_DEPLOYMENT_QUANTITY = "請輸入數量";
	public static final String MESSAGE_ENTER_DEPLOYMENT_TYPE = "請輸入種類";
	public static final String MESSAGE_ENTER_ACTION_TYPE = "請輸入事件";
	public static final String MESSAGE_ENTER_ACTION_MOVING = "請輸入前進 / 推進 方向";
	public static final String MESSAGE_REMARK = "請輸入是次事件的附加資訊\n如需輸入發生於相同時間及地點資訊\n可按新增資料\n如要輸入另一項事件，請輸入/new";
	public static final String MESSAGE_REMARK_NEW_INFO = "新增附加資訊";
	public static final String MESSAGE_ENTER_VEHICLE_FAST_INPUT_MODE = "⎆";
	public static final String MESSAGE_ENTER_VEHICLE_LICENSE_PLATE = "請輸入車牌資訊\n如有關車輛車牌為AM字頭，請輸入AM車牌後的四位數字";
	public static final String MESSAGE_ENTER_VEHICLE_LICENSE_PLATE_NOT_AM = "請輸入車牌";
	public static final String MESSAGE_PLATE_VEHICLE_LICENSE_PLATE = "新增車牌資訊";
	public static final String MESSAGE_PLATE_VEHICLE_LICENSE_PLATE_NOT_AM = "車牌不是AM字頭";
	public static final String MESSAGE_ENTER_VEHICLE_IDENTIFICATION_PLATE = "請輸入區牌資訊";
	public static final String MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_IN_ALPHABETICAL_ORDER = "顯示所有";
	public static final String MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_IN_POPULARITY_ORDER = "顯示熱門";
	public static final String MESSAGE_SELECT_VEHICLE_IDENTIFICATION_PLATE_NO_PLATE = "沒有區牌";
	public static final String MESSAGE_SELECT_VEHICLE_MODEL = "請輸入車款";
	public static final String MESSAGE_PLATE_RETURN_OPTIONS = "請選擇以下選項";
	public static final String MESSAGE_PLATE_NEW_PLATE = "新增一項車牌資訊";
	public static final String MESSAGE_PLATE_EXIT_PLATE = "繼續輸入其他資料";
	public static final String MESSAGE_PLATE_FAST_PLATE_INPUT_COMPLETE = "輸入完成";
	public static final String MESSAGE_REMARK_FORWARD = "完成輸入及報哨";
	public static final String MESSAGE_FORWARD_BOT_NAME = "1292991056:AAHRowVIo47VW5pjhYfAeklMBud_fHxpPnA";
}