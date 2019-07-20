package dataserver;

import java.util.HashMap;

public interface SystemConstant {
    
    public static HashMap serverParameters = new HashMap();

    //costanti server da file
    public static final String SERVER_PORT = "SERVER_PORT";
    public static final String T_ACK = "T_ACK";
    public static final String T_SES = "T_SES";
    public static final String MAX_RECOVERY = "MAX_RECOVERY";
    public static final String CONFIG_FILE_PATH = "CONFIG_FILE_PATH";
    
    //costanti network
    public static final int MAXIMUM_PACKET_SIZE = 1386;
    public static final int DATA_SIZE = MAXIMUM_PACKET_SIZE - 6;
    
    //tipi comando
    public static final int COMMAND_TYPE_WM_FIRMWARE_UPLOAD = 1;
    public static final int COMMAND_TYPE_SAT_FIRMWARE_UPLOAD = 2;
    public static final int COMMAND_TYPE_TREATEMENT_DATABASE_UPLOAD = 3;
    public static final int COMMAND_TYPE_SINGLE_TREATEMENT_UPLOAD = 4;
    public static final int COMMAND_TYPE_TREATEMENT_ENABLING = 5;
    public static final int COMMAND_TYPE_PARAMETER_UPLOAD = 6;
    public static final int COMMAND_TYPE_PARAMETER_DOWNLOAD = 7;
    public static final int COMMAND_TYPE_LOG_DOWNLOAD = 8;
    public static final int COMMAND_TYPE_NOTHING_TODO = 9;
    public static final int COMMAND_TYPE_TIME_UPDATE = 20;
    
    public static final int COMMAND_REQUEST_LOG_DOWNLOAD = 1;
    public static final int COMMAND_REQUEST_PARAMETER_DOWNLOAD = 2;
    public static final int COMMAND_REQUEST_TIME_UPDATE = 3;
    
    public static final int DATA_TYPE_PARAMETER_DOWNLOAD = 100;
    public static final int DATA_TYPE_LOG_DOWNLOAD = 101;
    
    public static final int ERROR_TYPE_CRC = 1;
    public static final int ERROR_TYPE_INCOMPLETE_DATA = 2;
    
    //costanti database
    public static final String ACTIVITY_TYPE_FIRMWARE_UPLOAD_WM = "1";
    public static final String ACTIVITY_TYPE_FIRMWARE_UPLOAD_SAT = "2";
    public static final String ACTIVITY_TYPE_TREATMENT_DATABASE_UPLOAD = "3";
    public static final String ACTIVITY_TYPE_SINGLE_TREATMENT_UPLOAD = "4";
    public static final String ACTIVITY_TYPE_TREATMENT_ENABLING = "5";
    public static final String ACTIVITY_TYPE_PARAMETER_UPLOAD = "6";    
    public static final String ACTIVITY_TYPE_PARAMETER_DOWLOAD = "7";
    public static final String ACTIVITY_TYPE_LOG_DOWLOAD = "8";
    public static final String ACTIVITY_TYPE_NO_TODO = "9";
    
    public static final int ACTIVITY_STATUS_PENDING = 1;
    public static final int ACTIVITY_STATUS_EXECUTED = 2;
    public static final int ACTIVITY_STATUS_ERROR = 3;
    
    public static final int ERROR_TYPE_MAX_RECOVERY_REACHED = 1;
    
    
    public static final char GATEWAY_TIMER_OFF = 255;
    public static final char GATEWAY_OPT_OFF = 0;
    public static final char GATEWAY_MEDIA_SENSORI_MIN = 1;
    public static final char GATEWAY_MEDIA_SENSORI_MAX = 50;
    public static final char GATEWAY_TIMER_SENSORI_MIN = 10;
    public static final char GATEWAY_TIMER_SENSORI_MAX = 255;
    
}