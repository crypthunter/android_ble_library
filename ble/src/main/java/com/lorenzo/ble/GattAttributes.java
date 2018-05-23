package com.lorenzo.ble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String HC_08_CONF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String HC_08_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb";
    static {
        //servizi hc-08
        attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "Seriale HC - 08");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");
        //caratteristiche hc-08
        attributes.put(HC_08_RX_TX,"RX/TX data");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
