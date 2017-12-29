package redis;

/**
 * Created by Y.B.H(mium2) on 2016. 2. 5..
 */
public class BasicPushUserBean {

    private String PUSHKEY = "";
    private String CUID = "";
    private String CNAME = "";
    private String APPID = "";
    private String PNSID = "";
    private String DEVICEID = "";
    private String PSID = "";
    private String UPNSID= "";
    private String APNS_MODE = "";
    private String ORGUPNSID = "";

    public String getPUSHKEY() {
        return PUSHKEY;
    }

    public void setPUSHKEY(String PUSHKEY) {
        this.PUSHKEY = PUSHKEY;
    }

    public String getCUID() {
        return CUID;
    }

    public void setCUID(String CUID) {
        this.CUID = CUID;
    }

    public String getCNAME() {
        return CNAME;
    }

    public void setCNAME(String CNAME) {
        this.CNAME = CNAME;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public String getPNSID() {
        return PNSID;
    }

    public void setPNSID(String PNSID) {
        this.PNSID = PNSID;
    }

    public String getDEVICEID() {
        return DEVICEID;
    }

    public void setDEVICEID(String DEVICEID) {
        this.DEVICEID = DEVICEID;
    }

    public String getPSID() {
        return PSID;
    }

    public void setPSID(String PSID) {
        this.PSID = PSID;
    }

    public String getUPNSID() {
        return UPNSID;
    }

    public void setUPNSID(String UPNSID) {
        this.UPNSID = UPNSID;
    }

    public String getAPNS_MODE() {
        return APNS_MODE;
    }

    public void setAPNS_MODE(String APNS_MODE) {
        this.APNS_MODE = APNS_MODE;
    }

    public String getORGUPNSID() {
        return ORGUPNSID;
    }

    public void setORGUPNSID(String ORGUPNSID) {
        this.ORGUPNSID = ORGUPNSID;
    }

    @Override
    public String toString() {
        return "{" +
                "\"PUSHKEY\":\"" + PUSHKEY + "\"" +
                ", \"CUID\":\"" + CUID + "\"" +
                ", \"CNAME\":\"" + CNAME + "\"" +
                ", \"APPID\":\"" + APPID + "\"" +
                ", \"PNSID\":\"" + PNSID + "\"" +
                ", \"DEVICEID\":\"" + DEVICEID + "\"" +
                ", \"psid\":\"" + PSID + "\"" +
                ", \"UPNSID\":\"" + UPNSID + "\"" +
                ", \"APNS_MODE\":\"" + APNS_MODE + "\"" +
                ", \"ORGUPNSID\":\"" + ORGUPNSID + "\"" +
                "}";
    }
}
