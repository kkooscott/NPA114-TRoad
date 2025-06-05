package npa.gov.tw.mydata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import npa.gov.tw.mydata.common.DPConfig;
import npa.gov.tw.mydata.common.ExceptionUtil;
import npa.gov.tw.mydata.common.PropertiesUtil;
import npa.gov.tw.mydata.util.JWEUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import npa.gov.tw.mydata.util.SpUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
//202505 昇SPRING BOOT 2.3至3.2
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class SpController {

    private static final Logger logger = LoggerFactory.getLogger(SpController.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    private final Base64.Encoder encoder = Base64.getEncoder();
    private static Base64.Decoder base64Decoder =  Base64.getDecoder();


//    @Value("${healthCheck.sp.temp.directory}")

    private String spTempDir= PropertiesUtil.getProperty("sp_temp_directory");       // 打包路徑
    private String mydataHost = PropertiesUtil.getProperty("mydata_url");       // MyData主機網址
//    @Value("${resume.sp.client_id}")
//    private String spResumeClientId;      // SP client_id
    private String spClientId = PropertiesUtil.getProperty("sp_client_id");
//    @Value("${resume.sp.returnUrl}")
//    private String spResumeReturnUrl;     // SP return url
    private String spReturnUrl = PropertiesUtil.getProperty("sp_returnUrl");
//    @Value("${resume.dp.resource_id}")
//    private String dpResumeResourceId;    // DP resource_id
    private String dpResourceId = PropertiesUtil.getProperty("dp_resource_id");
//    @Value("${resume.sp.client_secret}")
//    private String client_secret_resume;
    private String client_secret = PropertiesUtil.getProperty("sp_client_secret");
//    @Value("${resume.sp.cbc_iv}")
//    private String cbc_iv_resume;
    //@Value("${sp.cbc_iv}")
    private String cbc_iv = PropertiesUtil.getProperty("sp_cbc_iv");


//    @Value("${healthCheck.sp.client_id}")
//    private String spHealthCheckClientId;
//    @Value("${healthCheck.sp.returnUrl}")
//    private String spHealthCheckReturnUrl;
//    @Value("${healthCheck.dp.resource_id}")
//    private String dpHealthCheckResourceId;
//    @Value("${healthCheck.sp.client_secret}")
//    private String client_secret_healthCheck;
//    @Value("${healthCheck.sp.cbc_iv}")
//    private String cbc_iv_healthCheck;

    private String permissionTicket = "";
    private String secret_key = "";


 //   @Autowired
 // private RestTemplate restTemplate;
 // 202505 昇SPRING BOOT 2.3至3.2
    // Spring Boot 3.x 預設不再允許 Bean 的循環依賴
    private final RestTemplate restTemplate = new RestTemplate();

  //@Autowired
  //private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
//    @Autowired
//    MyDataLogService myDataLogService;

//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Do any additional configuration here
        return builder.build();
    }

    /**
     * SP服務說明頁 [流程示意圖]
     * <p>
     * 柒、MyData整合方式說明 二、MyData整合網址及參數說明
     * <p>
     * 欲進入「服務申請頁」，/service/{client_id}/{服務篩選參數}
     */
//    @GetMapping("/service_description")
    @RequestMapping(value = "/service_description", method = RequestMethod.GET)
    public String getSpChBookDesc(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
        HttpSession session = request.getSession();

        // SP協助工具
        SpUtils utils = new SpUtils();
        // SP所綁定的 resourceId。
        String[] resourceIdArray = dpResourceId.split(",");

        //String pid = request.getParameter("PID");
        // 重導向至MyData的整合介接網址
        String spIntergrationUrl = utils.getSpIntergrationUrlString(mydataHost, spClientId, resourceIdArray, spReturnUrl);

        model.addAttribute("spIntergrationUrl", spIntergrationUrl);

        //return "service_description";
        return spIntergrationUrl;
    }

    //202505 試加!!
    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String pingXXXX() {
        LocalDateTime now = LocalDateTime.now();
        String result = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        //System.out.println("ping return : {}", result);
        return result;
    }    
    
    /**
     * SP指定之服務跳轉網址[流程示意圖] sp_return_url
     * <p>
     * 柒、MyData整合方式說明 三、正常返回SP網址之處理方式說明 四、異常返回SP網址之處理方式說明
     * <p>
     * 正常時，傳送 permission_ticket
     * 錯誤時，傳送 code
     * 原有sp_return_url的參數會全數帶回方便使用
     */
//    @GetMapping("/service_apply")
    @RequestMapping(value = "/service_apply", method = RequestMethod.GET)
    public String getServiceapply(
    			@RequestParam(name = "tx_id", required = false) String tx_id,
            @RequestHeader(name = "code", required = false) String code,
            HttpServletRequest request,
            HttpServletResponse response,
            ModelMap model) {
        /**
         * 此處根據SP使用需求可自行填寫邏輯
         */
        System.out.println("code=" + code);
        model.addAttribute("code", code);

        System.out.println("tx_id=" + tx_id);
        model.addAttribute("tx_id", tx_id);

        System.out.println("request.getQueryString()=" + request.getQueryString());
        model.addAttribute("querystr", request.getQueryString());
        return "service_apply";
    }

    /**
     * SP-API
     * 捌、SP-API Endpoint規格說明（一）MyData發出請求 - 告知SP準備來捉取資料檔
     * Content-Type: application/json
     *
     * @param params permission_ticket - String
     * @param params secret_key - String
     * @param params unable_to_deliver - ArrayList
     *
     * 成功 HTTP/1.1 200 OK
     * 失敗 HTTP/1.1 403 Forbidden
     * @throws Exception
     */
//    @PostMapping
//    @RequestMapping(value = "/notification", method = RequestMethod.POST)
//    @RequestMapping(value = "/sp-test-api", method = RequestMethod.POST)
//    public void postNotify(@RequestBody Map<String,Object> params,
//                           HttpServletResponse httpResponse) throws Exception {
//
//        String permissionTicket = (String)params.get("permission_ticket");
//        this.permissionTicket = permissionTicket;
//        String txId = (String)params.get("tx_id");
//        String secret_key = (String)params.get("secret_key");
//        this.secret_key = secret_key;
//        List<String> unableToDeliver = (ArrayList<String>)params.get("unable_to_deliver");
//
////        String secretKey = new String(decrypt_cbc(base64Decoder.decode(secret_key),client_secret_healthCheck+client_secret_healthCheck, cbc_iv_healthCheck));
//        String secretKey = new String(decrypt_cbc(base64Decoder.decode(secret_key),client_secret+client_secret, cbc_iv));
//
//        logger.warn("permission_ticket ... {}", permissionTicket);
//        logger.warn("txId ................ {}", txId);
//        logger.warn("secret_key .......... {}", secretKey);
//        logger.warn("unable_to_deliver ... {}", unableToDeliver);
//
//
//        if(unableToDeliver!=null&&unableToDeliver.size()>0) {
//            // TODO 處理異常狀況，有部份資料集無法取得。
//            String unableToDeliverResourceId = unableToDeliver.stream().collect(Collectors.joining(","));
//            logger.warn("unableToDeliver resourceId -> {}", unableToDeliverResourceId);
//            //20220809 紀錄mydataLog
////            MyDataLog myDataLog = new MyDataLog();
////            myDataLog.setPermissionTicket(permissionTicket);
////            myDataLog.setTxid(txId);
////            myDataLog.setSecretKey(secretKey);
////            myDataLog.setUnableToDeliver(unableToDeliverResourceId);
////            myDataLog.setHttpCode("000");
////            myDataLog.setDateTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
////            myDataLogService.addMyDataLog(myDataLog);
//            return;
//        }
//
//
//        //ResponseEntity<String> restTemplate.getForEntity(mydataApiUrl, String.class, );
//
//
//        /**
//         * SP需自行紀錄，以待驗證用
//         * 1. permission_ticket
//         * 2. secret_key
//         * 3. unable_to_deliver
//         */
//
//        /**
//         * 服務狀態回復
//         * 正常 200
//         * 錯誤 403
//         */
//        boolean check = true;
//
//        if(check) {
//            httpResponse.setStatus(HttpServletResponse.SC_OK);
//            /**
//             * 另起執行緒
//             * MyData平台為了確認有正確收到SP-API的回應
//             * 故要求此支程式先有回應，才可進行MyData-API抓取JWE資料
//             * 此處故意用thread暫等三秒，以確定讓MyData能先收到SP-API回應的http status code 200
//             * 實作時，SP端可將Thread內程式放置於另外程式，於SP-API程式回應後，另行呼叫
//             */
//            threadPoolTaskExecutor.execute(() -> {
//                /**
//                 * 此處故意等3秒後執行以待，檔案處理
//                 */
//                try {
//                    int repeat = 0;
//                    long wait = 15000l;
//                    while (true){
//                        Thread.sleep(wait);
//                        String returnString = downloadMyData(permissionTicket, secretKey, txId);
//                        logger.warn("回傳:" + returnString);
//                        if(!returnString.trim().startsWith("429") || repeat == 2){
//                            break;
//                        }
//                        if(returnString.trim().startsWith("429")){
//                            if(returnString.contains("|")){
//                                wait = Long.parseLong(returnString.split("\\|")[1]) * 1000;
//                            }
//                            repeat++;
//                        }
//                        //20220809 紀錄mydataLog
//                        if(!returnString.trim().startsWith("200")){
////                            MyDataLog myDataLog = new MyDataLog();
////                            myDataLog.setPermissionTicket(permissionTicket);
////                            myDataLog.setTxid(txId);
////                            myDataLog.setSecretKey(secretKey);
////                            myDataLog.setHttpCode(returnString.split("\\|")[0]);
////                            myDataLog.setDateTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
////                            myDataLogService.addMyDataLog(myDataLog);
//                        }
//                    }
//                } catch (InterruptedException e1) {
//                    logger.error(ExceptionUtil.toString(e1));
//                    e1.printStackTrace();
//                }
//            });
//        }else {
//            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        }
//    }
    
@RequestMapping(value = "/sp-test-api", method = RequestMethod.POST)
public void postNotify(@RequestBody Map<String,Object> params,
                       HttpServletResponse httpResponse) throws Exception {

    String permissionTicket = (String) params.get("permission_ticket");
    this.permissionTicket = permissionTicket;

    String txId = (String) params.get("tx_id");
    String secret_key = (String) params.get("secret_key");
    this.secret_key = secret_key;

    List<String> unableToDeliver = (ArrayList<String>) params.get("unable_to_deliver");

    // 🩹 PATCH: 模擬解密 (略過 decode 與 AES)
    String secretKey = "mock-secretKey-for-testing";

    logger.warn("permission_ticket ... {}", permissionTicket);
    logger.warn("txId ................ {}", txId);
    logger.warn("secret_key .......... {}", secretKey);
    logger.warn("unable_to_deliver ... {}", unableToDeliver);

    // 🟨 模擬成功處理流程
    boolean check = true;

    if (check) {
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        // 模擬延遲處理下載動作（真實環境用 Thread）
        threadPoolTaskExecutor.execute(() -> {
            try {
                Thread.sleep(3000);
                String returnString = downloadMyData(permissionTicket, secretKey, txId);
                logger.warn("回傳: " + returnString);
            } catch (InterruptedException e) {
                logger.error(ExceptionUtil.toString(e));
            }
        });
    } else {
        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}


    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public void test(@RequestBody Map<String,Object> params,
                           HttpServletResponse httpResponse) throws Exception {
        String permissionTicket = (String)params.get("permissionTicket");
        String txId = (String)params.get("tx_id");
        String secretKey = (String)params.get("secretKey");
        String returnString = downloadMyData(permissionTicket, secretKey, txId);
        httpResponse.setStatus(HttpServletResponse.SC_OK);
    }

//    @RequestMapping(value = "/form-test", method = RequestMethod.GET)
//    public void formTest(@RequestParam(name = "tx_id", required = false) String tx_id,
//                         @RequestHeader(name = "code", required = false) String code,
//                         HttpServletRequest request,
//                     HttpServletResponse httpResponse) throws Exception {
//        if(code.equals("200")){
////        String permissionTicket = this.permissionTicket;
////        String secretKey = this.secret_key;
////        String returnString = downloadMyData(permissionTicket, secretKey);
//            // MyData打包檔暫存路徑
//            File packFile = Paths.get(spTempDir + "/" + tx_id,spClientId + ".zip").toFile();
////        File packFile = Paths.get(spTempDir + "/" + tx_id,spHealthCheckClientId + ".zip").toFile();
//            SpUtils utils = new SpUtils();
//            // 解壓縮 MyData 打包檔
//            File packDir = packFile.getParentFile();
//            try {
//                utils.unzip(packFile, packFile.getParentFile());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            // MyData打包檔中的 manifest.xml
//            File manifestFile = utils.manifestFileOfMyDataPackFile(packDir);
//            logger.warn("MyData打包檔中的 manifest.xml -> {}", manifestFile.getAbsolutePath());
//
//            // 解壓縮 DP打包檔
//            File[] dpPackFiles = packDir.listFiles(new FileFilter() {
//                @Override
//                public boolean accept(File pathname) {
//                    return pathname.isFile() && pathname.getName().toLowerCase().endsWith("zip")
//                            //&& !pathname.getName().startsWith(spHealthCheckClientId)
//                            && !pathname.getName().startsWith(spClientId)
//                            && !pathname.getName().contentEquals(packFile.getName());
//                }
//            });
//            Arrays.stream(dpPackFiles).forEach(dpPackFile -> {
//                File dpPackDir = dpPackFile.getParentFile();
//                try {
//                    utils.unzip(dpPackFile, dpPackDir);
//                    // TODO 驗證DP打包檔內的憑證檔及數位簽章。
//                    boolean verifyDpSignatur = utils.verifySignature(dpPackDir);
//                    if(verifyDpSignatur) {
//                        logger.warn("DP打包檔數位簽章驗證成功");
//                    }else {
//                        logger.warn("DP打包檔數位簽章驗證失敗");
//                    }
//                } catch (IOException | InvalidKeyException | CertificateException | SignatureException e) {
//                    e.printStackTrace();
//                    logger.error(ExceptionUtil.toString(e));
//                }
//            });
//
//            //TODO 用解出來的json資料接續執行
//            Object ob = new JSONParser().parse(new FileReader("JSONFile.json"));
//            JSONObject js = (JSONObject) ob;
//            ObjectMapper mapper = new ObjectMapper();
//            String json = mapper.writeValueAsString(js);
//        }
//        //刪除資料
//        File packFile = Paths.get(spTempDir + "/" + tx_id).toFile();
//        packFile.delete();
//        httpResponse.setStatus(HttpServletResponse.SC_OK);
//    }

    //202505 
@RequestMapping(value = "/form-test", method = RequestMethod.GET)
public void formTest(@RequestParam(name = "tx_id", required = false) String tx_id,
                     @RequestHeader(name = "code", required = false) String code,
                     HttpServletRequest request,
                     HttpServletResponse httpResponse) throws Exception {
    try {
        logger.warn("==[form-test] tx_id: {}, code: {}==", tx_id, code);

        if (code == null || !code.equals("200")) {
            logger.error("Invalid or missing code header!");
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 模擬打包檔路徑
        File packFile = Paths.get(spTempDir + "/" + tx_id, spClientId + ".zip").toFile();
        logger.warn("找打包檔位置：{}", packFile.getAbsolutePath());

        if (!packFile.exists()) {
            logger.error("找不到 zip 檔案！" + packFile.getAbsolutePath());
            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 解壓縮
        SpUtils utils = new SpUtils();
        utils.unzip(packFile, packFile.getParentFile());

        logger.warn("已解壓縮完成！");
        httpResponse.setStatus(HttpServletResponse.SC_OK);

    } catch (Exception e) {
        logger.error("form-test 發生錯誤", e);
        httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}


    public String downloadMyData(String permissionTicket, String secretKey, String txId){
        String result = "";
        try{
            SpUtils utils = new SpUtils();

            // 呼叫 MyData-API
            String mydataApiUrl = utils.getMyDataApiUrl(mydataHost);
            logger.warn("mydataApiUrl -> {}", mydataApiUrl);
            logger.warn("permissionTicket -> {}", permissionTicket);
            logger.warn("secretKey -> {}", secretKey);
            logger.warn("txId -> {}", txId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.ALL.APPLICATION_JSON);
            headers.set("permission_ticket", permissionTicket);
            //headers.set("tx_id", txId);
            HttpEntity entity = new HttpEntity(headers);
            
            //202505 昇SPRING BOOT 2.3至3.2
         // HttpStatus     statusCode  = null;
            HttpStatusCode statusCode  = null;

            ResponseEntity<String> response = null;
            try {
                response = restTemplate.exchange(mydataApiUrl, HttpMethod.GET, entity, String.class);
                statusCode = response.getStatusCode();
            }catch (HttpStatusCodeException exception) {
                statusCode = exception.getStatusCode();
            }
            logger.warn("Http Status Code -> {}", statusCode);
            result += statusCode;

            if(HttpStatus.TOO_MANY_REQUESTS == statusCode) {
                Integer retryAfter = 60;
                if(response != null){
                    HttpHeaders hs = response.getHeaders();
                    if(hs.containsKey("Retry-After")) {
                        retryAfter = Integer.valueOf(hs.get("Retry-After").get(0));
                        logger.warn("MyData回覆429  Retry-After -> {}", retryAfter);
                    }else {
                        logger.warn("MyData回覆429  但無 Retry-After 值！");
                    }
                }else{
                    logger.warn("MyData回覆429  但無 response 值！");
                }

                // TODO 等待後再發動請求。

                logger.warn("Http Status Code -> {}", statusCode);
                result = statusCode + "|" + retryAfter;
                logger.warn("result-> {}", result);
                return result;
            }else if(HttpStatus.OK == statusCode || HttpStatus.CREATED == statusCode) {
                //UNDO
                logger.warn("Http Status Code -> {}", statusCode);
            }else {
                logger.warn("Http Status Code -> {}", statusCode);
                return result;
            }

            String mydataJwe = response.getBody();
            // decrypt mydataJwe
            String jweInfo = JWEUtil.decrypt(secretKey, mydataJwe);

            // 拆解 JWE payload
            Map<String,String> payload = null;
            try {
                payload = utils.parseJsonToMap(jweInfo);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(ExceptionUtil.toString(e));
                return ExceptionUtil.toString(e);
            }
            String fileName = payload.get("filename");
            String dataString = payload.get("data");
//            logger.warn("dataString -> {}",dataString);
            // Base64編碼後的資料檔內容
            String base64EncodedData = dataString.substring("application/zip;data:".length());
//            logger.warn("base64EncodedData -> {}",base64EncodedData);
            byte[] encryptedData = utils.base64DecodeToBytes(base64EncodedData);
            // MyData打包檔暫存路徑
            //20220811 新增txId為名的資料夾
            Paths.get(spTempDir + "/" + txId).toFile().mkdirs();
            File packFile = Paths.get(spTempDir + "/" + txId,fileName).toFile();
            if(packFile.exists()) packFile.delete();
            logger.warn("MyData打包檔暫存路徑 -> {}", packFile.getAbsolutePath());

            // 解密後並儲存資料檔
            try {
                FileUtils.writeByteArrayToFile(packFile, encryptedData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!packFile.exists() || packFile.length() == 0) {
                logger.warn("MyData資料打包檔解密失敗！");
                return "MyData資料打包檔解密失敗！";
            }
            if(!FilenameUtils.getExtension(packFile.getName()).equalsIgnoreCase("zip")) {
                logger.warn("MyData打包檔格式不正確！");
                return "MyData打包檔格式不正確！";
            }

            logger.warn("MyData打包檔下載成功 -> {}", packFile.getAbsolutePath());

//            // 解壓縮 MyData 打包檔
//            File packDir = packFile.getParentFile();
//            try {
//                utils.unzip(packFile, packFile.getParentFile());
//            } catch (IOException e) {;
//                e.printStackTrace();
//            }
//            // MyData打包檔中的 manifest.xml
//            File manifestFile = utils.manifestFileOfMyDataPackFile(packDir);
//            logger.warn("MyData打包檔中的 manifest.xml -> {}", manifestFile.getAbsolutePath());
//
//            // 解壓縮 DP打包檔
//            File[] dpPackFiles = packDir.listFiles(new FileFilter() {
//                @Override
//                public boolean accept(File pathname) {
//                    return pathname.isFile() && pathname.getName().toLowerCase().endsWith("zip")
//                            && !pathname.getName().contentEquals(packFile.getName());
//                }
//            });
//            Arrays.stream(dpPackFiles).forEach(dpPackFile -> {
//                File dpPackDir = dpPackFile.getParentFile();
//                try {
//                    utils.unzip(dpPackFile, dpPackDir);
//                    // TODO 驗證DP打包檔內的憑證檔及數位簽章。
//                    boolean verifyDpSignatur = utils.verifySignature(dpPackDir);
//                    if(verifyDpSignatur) {
//                        logger.warn("DP打包檔數位簽章驗證成功");
//                    }else {
//                        logger.warn("DP打包檔數位簽章驗證失敗");
//                    }
//                } catch (IOException | InvalidKeyException | CertificateException | SignatureException e) {
//                    e.printStackTrace();
//                    logger.error(ExceptionUtil.toString(e));
//                }
//            });
            return result;
        }catch (Exception e){
            result += ExceptionUtil.toString(e);
            logger.error(ExceptionUtil.toString(e));
        }finally {
            return result;
        }
    }

    public static byte[] decrypt_cbc(byte[] data, String key, String ivstr) throws Exception {
        // 密鑰
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"),"AES");
        // 向量鰎值
        IvParameterSpec iv = new IvParameterSpec(ivstr.getBytes("UTF-8"));
        // 實例化
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        // 初始化，設置為解密模式
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        // 執行操作
        return cipher.doFinal(data);
    }
}
