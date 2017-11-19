package net.macdidi.google_api_o_i_o_i;

/**
 * Created by user on 2017/11/7.
 */

public class GlobalVariable {
    public static boolean is_simulateGPS = false;
    public static int machineID = 1;
    public static float speakRate = 1.75f;
    public static int cur_stat = -1;//當前狀態對應的圓形按鈕顏色，-1代表未啟動，0代表正常(GREEN)，1代表有救護車經過(RED)
    public static boolean is_MapActivity_open = false;//判斷地圖有沒有被開啟過(service判斷方向用)
    public static String hintText;
    public static String hintContentTitle;
    public static String hintContentText;
    public static String IP;
    public static int port;
    public static double tolerance;
}
