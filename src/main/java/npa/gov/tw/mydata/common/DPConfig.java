/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npa.gov.tw.mydata.common;

/**
 *
 * @author Administrator
 */

public class DPConfig {
    public static final String DP_SRVC_IP = PropertiesUtil.getProperty("DP_SRVC_IP");
    public static final String MYDATA_SRVC_CHKTOKEN = PropertiesUtil.getProperty("MYDATA_SRVC_CHKTOKEN");
    public static final String MYDATA_SRVC_GETUSER = PropertiesUtil.getProperty("MYDATA_SRVC_GETUSER");
    public static final String ACCESS_TKN_ON = PropertiesUtil.getProperty("ACCESS_TKN_ON");
    public static final String USERINFO_ON = PropertiesUtil.getProperty("USERINFO_ON");
    public static final String FILE_DONWLOAD_ON = PropertiesUtil.getProperty("FILE_DONWLOAD_ON");
    public static final String TEST_ID = PropertiesUtil.getProperty("TEST_ID");
    
    private static final String DP_MOUNTAIN = PropertiesUtil.getProperty("DP_MOUNTAIN");
    private static final String DP_MOUNTAIN_IP = PropertiesUtil.getProperty("DP_MOUNTAIN_IP");
    private static final String DP_MOUNTAIN_API_PATH = PropertiesUtil.getProperty("DP_MOUNTAIN_API_PATH");
    private static final String DP_MOUNTAIN_RESOURCE = PropertiesUtil.getProperty("DP_MOUNTAIN_RESOURCE");
    private static final String DP_MOUNTAIN_RESOURCE_SECR = PropertiesUtil.getProperty("DP_MOUNTAIN_RESOURCE_SECR");
    
    private static final String DP_TRAFFIC = PropertiesUtil.getProperty("DP_TRAFFIC");
    private static final String DP_TRAFFIC_IP = PropertiesUtil.getProperty("DP_TRAFFIC_IP");
    private static final String DP_TRAFFIC_API_PATH = PropertiesUtil.getProperty("DP_TRAFFIC_API_PATH");
    private static final String DP_TRAFFIC_RESOURCE = PropertiesUtil.getProperty("DP_TRAFFIC_RESOURCE");
    private static final String DP_TRAFFIC_RESOURCE_SECR = PropertiesUtil.getProperty("DP_TRAFFIC_RESOURCE_SECR");
    
    private String dpName = "";
    private String dpIp="";
    private String apiPath="";
    private String resourceId="";
    private String resourceSecr="";

    public String getResourceSecr() {
        return resourceSecr;
    }

    public void setResourceSecr(String resourceSecr) {
        this.resourceSecr = resourceSecr;
    }

    public String getDpName() {
        return dpName;
    }

    public void setDpName(String dpName) {
        this.dpName = dpName;
    }

    public String getDpIp() {
        return dpIp;
    }

    public void setDpIp(String dpIp) {
        this.dpIp = dpIp;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
       this.resourceId = resourceId;
    }

    DPConfig(String source){
        if(source.equals("mountain")){
            dpName = DP_MOUNTAIN;
            dpIp = DP_MOUNTAIN_IP;
            apiPath = DP_MOUNTAIN_API_PATH;
            resourceId = DP_MOUNTAIN_RESOURCE;
            resourceSecr = DP_MOUNTAIN_RESOURCE_SECR;
        }
        if(source.equals("traffic")){
            dpName = DP_TRAFFIC;
            dpIp = DP_TRAFFIC_IP;
            apiPath = DP_TRAFFIC_API_PATH;
            resourceId = DP_TRAFFIC_RESOURCE;
            resourceSecr = DP_TRAFFIC_RESOURCE_SECR;
        }
    }
    
    DPConfig(){}
    
    public static DPConfig getInstance(String source){
        return new DPConfig(source);
    }
    
    public static DPConfig getInstance(){
        return new DPConfig();
    }
}
