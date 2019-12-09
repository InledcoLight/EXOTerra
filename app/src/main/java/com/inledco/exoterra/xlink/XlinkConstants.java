package com.inledco.exoterra.xlink;

import java.util.ArrayList;
import java.util.List;

public class XlinkConstants {
    public static final String HOST_API_FORMAL = "https://api2.xlink.cn";
    public static final String HOST_CM_FORMAL = "mqtt.xlink.cn";
    public static final int PORT_API_FORMAL = 443;
    public static final int PORT_CM_FORMAL = 1884;

//    public static final String HOST_API_TEST = "http://dev-api.xlink.cn";
//    public static final String HOST_CM_TEST = "dev-cm.xlink.cn";
//    public static final int PORT_API_TEST = 80;
//    public static final int PORT_CM_TEST = 1884;

    public static final String CORPORATION_ID = "100fa8b8584bdc00";

    public static final String PRODUCT_ID_LEDSTRIP = "160fa2b85ae803e9160fa2b85ae84e01";
    public static final String PRODUCT_ID_MONSOON = "160fa2b88b1903e9160fa2b88b19a401";
    public static final String PRODUCT_ID_SOCKET = "160fa2b95aac03e9160fa2b95aac5e01";

    public static final String STRIP_AP_SSID = "EXOTerraStrip_XXXXXX";
    public static final String SOCKET_AP_SSID = "EXOTerraSocket_XXXXXX";
    public static final String MONSOON_AP_SSID = "EXOTerraMonsoon_XXXXXX";

    public static final String CURRENT_HOME_ID = "current_homeid";

    public static final List<String> XLINK_PRODUCTS;

    static {
        XLINK_PRODUCTS = new ArrayList<>();
        XLINK_PRODUCTS.add(PRODUCT_ID_LEDSTRIP);
        XLINK_PRODUCTS.add(PRODUCT_ID_SOCKET);
        XLINK_PRODUCTS.add(PRODUCT_ID_MONSOON);
    }
}