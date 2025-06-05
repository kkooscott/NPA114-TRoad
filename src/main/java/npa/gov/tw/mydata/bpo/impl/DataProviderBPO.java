/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npa.gov.tw.mydata.bpo.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import npa.gov.tw.mydata.bpo.intf.DataProviderIntf;
import npa.gov.tw.mydata.common.CommonUtil;
import npa.gov.tw.mydata.common.DPConfig;
import npa.gov.tw.mydata.common.ExceptionUtil;
import npa.gov.tw.mydata.common.PropertiesUtil;
import npa.gov.tw.mydata.controller.MyDataDPController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tw.gov.ndc.emsg.mydata.gspclient.bean.IntrospectEntity;
import tw.gov.ndc.emsg.mydata.gspclient.bean.UserInfoEntity;
//import java.util.Base64;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Administrator
 */
public class DataProviderBPO implements DataProviderIntf {
	
	Logger log = LoggerFactory.getLogger(DataProviderBPO.class);
	
    private String DP_SRVC_IP = "";
    private String MYDATA_SRVC_CHKTOKEN = "";
    private String MYDATA_SRVC_GETUSER = "";
    
//    private static final String DP_MOUNTAIN = PropertiesUtil.getProperty("DP_MOUNTAIN");
//    private static final String DP_MOUNTAIN_IP = PropertiesUtil.getProperty("DP_MOUNTAIN_IP");
//    private static final String DP_MOUNTAIN_API_PATH = PropertiesUtil.getProperty("DP_MOUNTAIN_API_PATH");
//    private static final String DP_MOUNTAIN_RESOURCE = PropertiesUtil.getProperty("DP_MOUNTAIN_RESOURCE");
//    
//    private static final String DP_TRAFFIC = PropertiesUtil.getProperty("DP_TRAFFIC");
//    private static final String DP_TRAFFIC_IP = PropertiesUtil.getProperty("DP_TRAFFIC_IP");
//    private static final String DP_TRAFFIC_API_PATH = PropertiesUtil.getProperty("DP_TRAFFIC_API_PATH");
//    private static final String DP_TRAFFIC_RESOURCE = PropertiesUtil.getProperty("DP_TRAFFIC_RESOURCE");
    
    private String ACCESS_TKN_ON = "";
    private String USERINFO_ON = "";
    private String FILE_DONWLOAD_ON = "";
    
    private String TEST_ID = PropertiesUtil.getProperty("TEST_ID");

    @Override
    public ResponseEntity<byte[]> handleResource(String resource, Map<String, String> headers, Boolean heartbeat) throws Exception {
        log.info("resource .......... {}" , resource);
        log.info("heartbeat ......... {}" , heartbeat);
        log.info("headers ........... {}" , headers.size());

        DP_SRVC_IP = DPConfig.DP_SRVC_IP;
        MYDATA_SRVC_CHKTOKEN = DPConfig.MYDATA_SRVC_CHKTOKEN;
        MYDATA_SRVC_GETUSER = DPConfig.MYDATA_SRVC_GETUSER;
        
        log.info("DP_SRVC_IP={}" , DP_SRVC_IP);
        log.info("MYDATA_SRVC_CHKTOKEN={}" , MYDATA_SRVC_CHKTOKEN);
        log.info("MYDATA_SRVC_GETUSER={}" , MYDATA_SRVC_GETUSER);
        
        ACCESS_TKN_ON = DPConfig.ACCESS_TKN_ON;
        USERINFO_ON = DPConfig.USERINFO_ON;
        FILE_DONWLOAD_ON = DPConfig.FILE_DONWLOAD_ON;
        TEST_ID = DPConfig.TEST_ID;
        
        log.info("ACCESS_TKN_ON={}" , ACCESS_TKN_ON);
        log.info("USERINFO_ON={}" , USERINFO_ON);
        log.info("FILE_DONWLOAD_ON={}" , FILE_DONWLOAD_ON);
        
        log.info("print hearder keys");
        headers.keySet().iterator().forEachRemaining(key -> {
            log.info("header -> {} : {}", key, headers.get(key));
        });
        
        /**
         * MyData發出heartbeat請求判斷 請參考：「資料提供者技術文件 捌、DP-API Endpoint 規格準則」 三、DP-API
         * Heartbeat 機制說明
         */
        if (heartbeat) {
        	log.info("do heartbeat request");
            // TODO 實作確認 DP-API 狀態之程式碼。若 DP-API 狀態正常則回覆 200 OK, 若狀態異常則回覆 500 INTERNAL_SERVER_ERROR
            ResponseEntity<byte[]> fileEntity = responseDPFile(resource, "", heartbeat, "");
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.valueOf("application/json"));
            
            if(fileEntity != null)
            	return new ResponseEntity<>(new byte[0], responseHeaders, HttpStatus.OK);
            else
                return new ResponseEntity<>(new byte[0], responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
            
        }

        //access token jobs start---------
        String authorization="";
        String accessToken="";
        String transactionUid="";
        if(ACCESS_TKN_ON.equals("Y")){
        	log.info("do access token request");
            /**
             * HTTP Header 'Authorization' 值為 'Bearer {access_token}'。 因此取得
             * access_token 的方法，即是將 Bearer 字串去除即可。 請參閱「資料提供者技術文件 捌、二、DP-API
             * 請求及回覆規格說明」
             */
            authorization = headers.get("authorization");
            accessToken = authorization.replace("Bearer ", "");
            log.info("access_token ......... {}" , accessToken);

            /**
             * HTTP Header 'transaction_uid' 代表交易鍵值，用於讓DP方便識別資料查詢請求為同一次交易。
             * 請參閱「資料提供者技術文件 捌、二、DP-API 請求及回覆規格說明」
             */
            transactionUid = headers.get("transaction_uid");
            log.info("transaction_uid ...... {}" , transactionUid);

            /**
             * HTTP Header 'custom_param'
             * 代表自訂查詢鍵值，custom_param只是示意用字，實際參數依各自DP資料集查詢需要而不同。 請參閱「資料提供者技術文件
             * 捌、二、DP-API 請求及回覆規格說明」
             */
            String customParam = headers.get("custom_param");
            log.info("custom_param ......... {}" , customParam);

            // ------ Begin, 向GSP驗證access_token及取得user info ------
            /**
             * 驗證access_token，反查access_token. 「資料提供者技術文件 柒、授權主機 API Endpoint 規格說明
             * 三、Introspection Endpoint」
             *
             */

            String resourceid = DPConfig.getInstance(resource).getResourceId();        
            String resourceSecret = DPConfig.getInstance(resource).getResourceSecr();
            log.info("resourceid = {}", resourceid);
            log.info("resourceSecret = {}", resourceSecret);
            if (!isValidAccessToken(accessToken, resourceid, resourceSecret)) {
                log.info("不是有效的 access_token -> {}" , accessToken);
                // 反查失敗！ 回覆 401 SC_UNAUTHORIZED。
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.valueOf("application/json"));
                return new ResponseEntity<>(new byte[0], responseHeaders, HttpStatus.UNAUTHORIZED);
            }
        }
        //access token jobs end---------

        //get user info job start---------
        String userUid = "noUid";
        if(USERINFO_ON.equals("Y")){
        	log.info("do userinfo request.");
            /**
             * 取得用戶基本資料。 「資料提供者技術文件 柒、授權主機 API Endpoint 規格說明 四、UserInfo Endpoint」
             */
            UserInfoEntity userInfoEntity = getUserInfo(accessToken);
            // 示意：取得用戶身份證字號。
            if (userInfoEntity == null) {
                log.info("無法反查 userInfo -> accessToken:{}" , accessToken);
                // 無法取得用戶身份證字號！ 回覆 401 SC_UNAUTHORIZED。
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.valueOf("application/json"));
                return new ResponseEntity<>(new byte[0], responseHeaders, HttpStatus.UNAUTHORIZED);
            } else {
                userUid = userInfoEntity.getUid();
                if (userUid == null || userUid.isEmpty() || userUid.equals("")) {
                    log.info("無法反查 userInfo -> accessToken:{}" , accessToken);
                    // 無法取得用戶身份證字號！ 回覆 401 SC_UNAUTHORIZED。
                    HttpHeaders responseHeaders = new HttpHeaders();
                    responseHeaders.setContentType(MediaType.valueOf("application/json"));
                    return new ResponseEntity<>(new byte[0], responseHeaders, HttpStatus.UNAUTHORIZED);
                }
                log.info("userUid ...... {}" , userUid);
            }
            // ------ End, 向GSP驗證access_token及取得user info ------
        }
        //access token jobs end---------
        
        if(FILE_DONWLOAD_ON.equals("Y")){
            ResponseEntity<byte[]> fileEntity = responseDPFile(resource, accessToken, heartbeat, userUid);
            if(fileEntity != null)
                return fileEntity;
            
            log.info("file entity is null.");
        }
        
                
        JSONObject json = new JSONObject();

        json.put("status", "error");
        json.put("msg", "no return entity");
        
        byte[] data = json.toString().getBytes();
        
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.valueOf("application/json"));

        return new ResponseEntity<>(data, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 示意：以 access_token 向 GSP 反查 token 的有效性。
    private boolean isValidAccessToken(String accessToken, String resourceId, String resourceSecret) throws Exception {
//		return true;
        IntrospectEntity introspectEntity = introspectAccessToken(accessToken, resourceId, resourceSecret);
        if (introspectEntity == null) {
            log.info("無法透過 Introspection Endpoint 順利反查 access_token 的有效性。");
            return false;
        }

        if (introspectEntity.getActive()) {
            /**
             * 示意 access_token 驗證成功
             */
            log.info("sub ........... {}" , introspectEntity.getSub());
            log.info("scop .......... {}" , introspectEntity.getScope());
            log.info("clientId ...... {}" , introspectEntity.getClientId());
            return true;
        } else {
            log.info("經反查 Introspection Endpoint 驗證 access_token 不是有效的token。");
            return false;
        }
    }

    // 示意：以 access_token 向 GSP 反查用戶基本資料。
    private UserInfoEntity getUserInfo(String accessToken) throws IOException {
        // 以access_token去要求user_info
//		UserInfoEntity entity = new UserInfoEntity();
//		entity.setUid("12345");
//		return entity;
        UserInfoEntity userInfoEntity = requestUserInfo(accessToken);
        if (userInfoEntity != null) {
            log.info("sub .............. {}" , userInfoEntity.getSub());         // 與id_token相同
            log.info("account .......... {}" , userInfoEntity.getAccount());     // egov帳號
            log.info("uid .............. {}" , userInfoEntity.getUid());         // 身份證字號
            log.info("is_valid_uid ..... {}" , userInfoEntity.getIsValidUid());  // 身份證字號是否已驗證
            log.info("birthdate ........ {}" , userInfoEntity.getBirthdate());
            log.info("gender ........... {}" , userInfoEntity.getGender());
            log.info("name ............. {}" , userInfoEntity.getName());
            log.info("email ............ {}" , userInfoEntity.getEmail());
            log.info("email_verified ... {}" , userInfoEntity.getEmailVerified());
            log.info("phone_number ..... {}" , userInfoEntity.getPhoneNumber());
        }
        return userInfoEntity;
    }

    /**
     * 反查 access_token
     *
     * @param accessToken
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public IntrospectEntity introspectAccessToken(String accessToken, String clientId, String clientSecret) throws Exception {

        List<NameValuePair> pairList = new ArrayList<>();
//		pairList.add(new BasicNameValuePair("client_id", getClientId()));
//		pairList.add(new BasicNameValuePair("client_secret", getClientSecret()));
        pairList.add(new BasicNameValuePair("token", accessToken));
      //20210506 MYDATA 修正TLS1.2
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(  SSLContexts.createDefault(),  new String[] { "TLSv1.2"}, 
        null,  SSLConnectionSocketFactory.getDefaultHostnameVerifier());
CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
//        CloseableHttpClient httpClient = HttpClientBuilder.create()
//                .build();

        HttpPost post = new HttpPost(MYDATA_SRVC_CHKTOKEN);
        post.setEntity(new StringEntity(URLEncodedUtils.format(pairList, "UTF-8")));
        post.addHeader("Accept", "application/json");
        post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        post.addHeader("Authorization", "Basic " + basicAuthenticationSchema(clientId, clientSecret));

        IntrospectEntity introspectEntity = null;

        HttpResponse response = httpClient.execute(post);
        if (response.getStatusLine().getStatusCode() == 200) {
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.info("反查 access_token 成功！ -> {}" , responseString);
            if (StringUtils.isNotEmpty(responseString)) {
                ObjectMapper om = new ObjectMapper();
                om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);	//避免未知參數造成錯誤
                introspectEntity = om.readValue(responseString, IntrospectEntity.class);
            }
        } else {
            log.info("反查 access_token 失敗！  HttpStatus -> {}" , response.getStatusLine().getStatusCode());
        }

        return introspectEntity;
    }

    /*
     * 符合 HTTP Basic authentication schema 將 clientId:clientSecret 字串以Base64編碼。
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String basicAuthenticationSchema(String clientId, String clientSecret) {
        StringBuilder sb = new StringBuilder();
        sb.append(clientId).append(":").append(clientSecret);
        try {
            //return encoder.encodeToString(sb.toString().getBytes("UTF-8"));
            return Base64.encodeBase64String(sb.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(ExceptionUtil.toString(e));
            return "";
        }
    }

    /**
     * 請求用戶資訊
     *
     * @param accessToken
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public UserInfoEntity requestUserInfo(String accessToken) throws ClientProtocolException, IOException {
//        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    	//20210506 MYDATA 修正TLS1.2
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(  SSLContexts.createDefault(),  new String[] { "TLSv1.2"},null,  SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
	   //CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(MYDATA_SRVC_GETUSER);
        get.addHeader("Content-Type", "application/json");
        get.addHeader("Authorization", "Bearer " + accessToken);

        HttpResponse response = httpClient.execute(get);
        UserInfoEntity userInfo = null;

        if (response.getStatusLine().getStatusCode() == 200) {
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.info("以 access_token 請求 user_info 成功！ -> {}" , responseString);
            if (StringUtils.isNotEmpty(responseString)) {
                ObjectMapper om = new ObjectMapper();
                userInfo = om.readValue(responseString, UserInfoEntity.class);
            }
        } else {
            log.info("以 access_token 請求 user_info 失敗！  HttpStatus -> {}" , response.getStatusLine().getStatusCode());
        }

        return userInfo;
    }

    private ResponseEntity<byte[]> responseDPFile(String resource, String accessTkn, boolean heartbeat, String id) {
        try {
            if(id.equals("55667788"))
                id = TEST_ID;
            String dpIp = DPConfig.getInstance(resource).getDpIp();
            String dpApiPath = DPConfig.getInstance(resource).getApiPath();
            String dpName = DPConfig.getInstance(resource).getDpName();
            String resourceId = DPConfig.getInstance(resource).getResourceId();
            log.info("DP_IP={}", dpIp);
            log.info("DP_API_PATH={}", dpApiPath);
            log.info("DP_NAME={}", dpName);
            log.info("DP_RESOURCE_ID={}", resourceId);
            accessTkn = accessTkn.replaceAll(":", "");
            if(accessTkn.equals("55667788"))
            	accessTkn = CommonUtil.getCurrEDate() + CommonUtil.getCurrTime6();
            String URL = "http://" + dpIp + "/" + dpName + "/" + dpApiPath + "?APLIDN=" + id + "&AccessToken="+ accessTkn + "&heartbeat=" + heartbeat;

            log.info("responseDPFile URL={}", URL);
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(  SSLContexts.createDefault(),  new String[] { "TLSv1.1"}, 
                    null,  SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
//            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(URL);

            HttpResponse response = httpClient.execute(get);


            if (response.getStatusLine().getStatusCode() == 200) {
                log.info("api response 200.");
                Header contentType = response.getEntity().getContentType();
                log.info("response content type={}", contentType);
                String mimeType = contentType.getValue().split(";")[0].trim();
                
                HttpHeaders responseHeaders = new HttpHeaders();
                
                byte[] data = null;
                if(mimeType.contains("json")){
                	String responseStr = EntityUtils.toString( response.getEntity());
                	log.info("response entity : mimeType={} content= {} ", "json", responseStr);
                	
                	data = responseStr.getBytes();	//response.getEntity
//                    data = EntityUtils.toByteArray( response.getEntity());
                	log.info("response getBytes= {}" , data);
                	
                    responseHeaders.setContentType(MediaType.valueOf("application/json"));
                    
                    if(responseStr.contains("成功"))
                    	return new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);
                    else if(responseStr.contains("查無"))
                    	return new ResponseEntity<>(data, responseHeaders, HttpStatus.NO_CONTENT);
                    else
                    	return new ResponseEntity<>(data, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
                    
                }else if(mimeType.contains("pdf")){
                	log.info("response entity mimeType={} ", "pdf");
                	
                    Header header = response.getFirstHeader("Content-Disposition");
                    String cntDisposition = header.getValue();
                    log.info("header s = {}", cntDisposition);
                	
                	String filename = cntDisposition.replaceAll(" ", "").split(";")[1].split("=")[1];
                	log.info("response filename = {}", filename);
                	
                    data = EntityUtils.toByteArray( response.getEntity());
                    responseHeaders.setContentType(MediaType.valueOf("application/pdf"));
                    responseHeaders.setContentLength(data.length);
                    responseHeaders.set("Content-disposition", "attachment; filename=" + filename);

                    log.info("成功回傳DP資料打包檔 ");
                    return new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);
                }else if(mimeType.contains("zip")){
                	log.info("response entity mimeType={} ", "zip");
                	
                    Header header = response.getFirstHeader("Content-Disposition");
                    String cntDisposition = header.getValue();
                    log.info("header s = {}", cntDisposition);

                	String filename = cntDisposition.replaceAll(" ", "").split(";")[1].split("=")[1];
                	log.info("response filename = {}", filename);
                	
                    data = EntityUtils.toByteArray(response.getEntity());
                    responseHeaders.setContentType(MediaType.valueOf("application/zip"));
                    responseHeaders.setContentLength(data.length);
                    responseHeaders.set("Content-disposition", "attachment; filename=" + filename);

                    System.out.println("成功回傳DP資料打包zip檔 ");
                    return new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);
                }
                
            } else {
                log.info("resource API 失敗！  HttpStatus -> {}" , response.getStatusLine().getStatusCode());
                HttpHeaders responseHeaders = new HttpHeaders();
                return new ResponseEntity<>(null, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(ExceptionUtil.toString(e));
        }

        return null;
    }

}
