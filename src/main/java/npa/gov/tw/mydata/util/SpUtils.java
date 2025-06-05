package npa.gov.tw.mydata.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import npa.gov.tw.mydata.common.ExceptionUtil;
import npa.gov.tw.mydata.common.PropertiesUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * SP與MyData介接工作之的工具物件
 */
public class SpUtils {

    private static Logger logger = LoggerFactory.getLogger(SpUtils.class);

    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // MyData提供給SP的介接網址
    private String spServiceUrl = "/service/client_id";
    // MyData-API的網址
    private String mydataApiUrl = "service/data";

	/**
	 * 密鑰算法
	 */
	public static final String KEY_ALGORITHM = "AES";
	/**
	 * 加密/解密算法/工作模式/填充方式
	 */
	public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
	/**
	 * 測試機sp-example服務，cbc_iv CBC壓密向量值
	 */

    //@Value("${sp.cbc_iv}")
    private String cbc_iv = PropertiesUtil.getProperty("sp_cbc_iv");;
//    @Value("${sp.cbc_iv}")
//    private String cbc_iv_healthCheck;

//    @Value("${sp.cbc_iv}")
//    private String cbc_iv_resume;
//	public static String cbc_iv = "";

    //@Value("${sp.client_secret}")
    private String client_secret = PropertiesUtil.getProperty("sp_client_secret");

    @Value("${sp.client_secret}")
    private String client_secret_healthCheck;

//    @Value("${sp.client_secret}")
//    private String client_secret_resume;
    
    /**
     * MyData-API 請求網址
     * @param mydataHost
     * @return
     */
    public String getMyDataApiUrl(String mydataHost) {
        return mydataHost.endsWith("/") ? (mydataHost+mydataApiUrl) : (mydataHost+"/"+mydataApiUrl);
    }

    /**
     * 組成重導向至MyData的整合介接網址。
     * @param mydataHost
     * @param clientId
     * @param resourceIdArray
     * @param returnUrl
     * @return
     */
    public String getSpIntergrationUrlString(
            String mydataHost,
            String clientId,
            String[] resourceIdArray,
            String returnUrl) {

        StringBuilder sb = new StringBuilder();
        try {
            sb.append(mydataHost.endsWith("/")?mydataHost.substring(0,mydataHost.length()-1):mydataHost)
                .append(spServiceUrl.replaceAll("client_id",clientId))
                .append("/").append(base64EncodedResourceId(resourceIdArray))
                .append("/").append(UUID.randomUUID().toString()).append("?returnUrl=").append(URLEncoder.encode(returnUrl,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        return sb.toString();
    }

    public String getSpIntergrationUrlString(
            String mydataHost,
            String clientId,
            String[] resourceIdArray,
            String returnUrl,
            String pid) {

        StringBuilder sb = new StringBuilder();
        try {
//            if(!clientId.equals("CLI.HVwssWg1zK")){//水電資料
//                getUrl(mydataHost, clientId, resourceIdArray, returnUrl, pid, sb, client_secret_healthCheck, cbc_iv_healthCheck);
//            }else{//財政資料
//                getUrl(mydataHost, clientId, resourceIdArray, returnUrl, pid, sb, client_secret_resume, cbc_iv_resume);
//            }
            getUrl(mydataHost, clientId, resourceIdArray, returnUrl, pid, sb, client_secret, cbc_iv);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(ExceptionUtil.toString(e));
            return null;
        }

        return sb.toString();
    }

    private void getUrl(String mydataHost, String clientId, String[] resourceIdArray, String returnUrl, String pid, StringBuilder sb, String client_secret_pnw, String cbc_iv_pnw) throws Exception {
        byte[] bs = encrypt(pid.getBytes("UTF-8"), (client_secret_pnw + client_secret_pnw), cbc_iv_pnw);
        String encodePid = Base64.getEncoder().encodeToString(bs);
        sb.append(mydataHost.endsWith("/")?mydataHost.substring(0,mydataHost.length()-1):mydataHost)
                .append(spServiceUrl.replaceAll("client_id",clientId))
                .append("/").append(base64EncodedResourceId(resourceIdArray))
                .append("/").append(UUID.randomUUID().toString()).append("?returnUrl=").append(URLEncoder.encode(returnUrl,"UTF-8"))
                .append("&pid=").append(encodePid);
    }

    /**
     * 對resourceId進行Base64編碼。
     * 多個resourceId以分號:串接後再進行Base64編碼。
     * @param resourceIdArray
     * @return
     */
    public String base64EncodedResourceId(String[] resourceIdArray) {
        if(resourceIdArray == null) return null;
        if(resourceIdArray.length == 0) return "";

        final StringJoiner joiner = new StringJoiner(":");

        Stream.of(resourceIdArray).forEach(id -> {
            joiner.add(id);
        });

        return base64EncodeToString(joiner.toString());
    }

    /**
     * 進行 Base64 編碼，以UTF-8。
     * @param source
     * @return
     */
    public String base64EncodeToString(String source) {
        try {
            return base64Encoder.encodeToString(source.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 進行 Base64 解碼。
     * @param base64EncodedString
     * @return
     */
    public byte[] base64DecodeToBytes(String base64EncodedString) {
        return Base64.getUrlDecoder().decode(base64EncodedString);
    }

    public String base64DecodeToString(String base64EncodedString) throws UnsupportedEncodingException {
        return new String(Base64.getUrlDecoder().decode(base64EncodedString),"UTF-8");
    }
    
    /**
     * 將 JSON 字串轉換為 Map
     * @param jsonString
     * @return
     * @throws IOException
     */
    public Map<String, String> parseJsonToMap(String jsonString) throws IOException {
        return objectMapper.readValue(jsonString, HashMap.class);
    }

    /**
     * 驗證 JWT 簽章
     * @param jwtjsonstr
     * @param secretKey
     * @return
     */
    public boolean verifyJwtSignature(String jwtjsonstr, String secretKey) {
        String[] jwtjsonstrList = jwtjsonstr.split("[.]");
        String unsigntoken = jwtjsonstrList[0]+"."+jwtjsonstrList[1];
        String jwtsignature = jwtjsonstrList[2];
        String manualsignature = HMACSHA256(unsigntoken.getBytes(),secretKey.getBytes());
        if(jwtsignature.equalsIgnoreCase(manualsignature)) {
            return true;
        }else {
            return false;
        }
    }

    /**
     * 解密後儲存至指定檔案
     * @param encryptedData
     * @param secretKey
     * @param packFile
     * @throws Exception
     */
    public void decryptToFile(byte[] encryptedData, String secretKey, File packFile) throws Exception {
    		byte[] decryptb = decrypt(encryptedData,secretKey, cbc_iv);
    		FileUtils.writeByteArrayToFile(packFile, decryptb);
    }

    /**
     * 解壓縮
     * @param sourceFile
     * @param targetDir
     * @throws IOException
     */
    public void unzip(File sourceFile, File targetDir) throws IOException {
//        if (!targetDir.exists() || !targetDir.isDirectory()) {
//        		targetDir.mkdirs();
//        }
        ZipEntry entry = null;
        String entryFilePath = null, entryDirPath = null;
        File entryFile = null, entryDir = null;
        int index = 0, count = 0, bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        ZipFile zip = new ZipFile(sourceFile.getAbsoluteFile());
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            entryFilePath = targetDir.getAbsolutePath() + File.separator + entry.getName();
            //可能有/，而非\(window -> \, unix -> /)
            entryFilePath = entryFilePath.replace("/", "\\");
            index = entryFilePath.lastIndexOf(File.separator);
            if (index != -1) {
                entryDirPath = entryFilePath.substring(0, index);
            } else {
                entryDirPath = "";
            }
            entryDir = new File(entryDirPath);
            if (!entryDir.exists() || !entryDir.isDirectory()) {
                entryDir.mkdirs();
            }
            entryFile = new File(entryFilePath);
            if(!entryFile.isDirectory()){
                bos = new BufferedOutputStream(new FileOutputStream(entryFile));
                bis = new BufferedInputStream(zip.getInputStream(entry));
                while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
                    bos.write(buffer, 0, count);
                }
                bos.flush();
                bos.close();
            }

        }
        zip.close();
    }
//    /**
//     * 解壓縮
//     * @param sourceFile
//     * @param targetDir
//     * @throws IOException
//     */
//    public static void unzip(File sourceFile, File targetDir) throws IOException {
//        if (!targetDir.exists() || !targetDir.isDirectory()) {
//            targetDir.mkdirs();
//        }
//        ZipEntry entry = null;
//        String entryFilePath = null, entryDirPath = null;
//        File entryFile = null, entryDir = null;
//        int index = 0, count = 0, bufferSize = 1024;
//        byte[] buffer = new byte[bufferSize];
//        BufferedInputStream bis = null;
//        BufferedOutputStream bos = null;
//        ZipFile zip = new ZipFile(sourceFile.getAbsoluteFile());
//        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
//
//        while (entries.hasMoreElements()) {
//
//            entry = entries.nextElement();
//            logger.debug("entry name -> {}" , entry.getName());
//            entryFilePath = targetDir.getAbsolutePath() + File.separator + entry.getName();
//
//            index = entryFilePath.lastIndexOf(File.separator);
//            if (index != -1) {
//                entryDirPath = entryFilePath.substring(0, index);
//            } else {
//                entryDirPath = "";
//            }
//            entryDir = new File(entryDirPath);
//
//
//            if (!entryDir.exists() || !entryDir.isDirectory()) {
//                entryDir.mkdirs();
//            }
//            entryFile = new File(entryFilePath);
//
//            if(!entryFile.isDirectory()){
//                bos = new BufferedOutputStream(new FileOutputStream(entryFile));
//                bis = new BufferedInputStream(zip.getInputStream(entry));
//                while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
//                    bos.write(buffer, 0, count);
//                }
//                bos.flush();
//                bos.close();
//            }
//        }
//
//    }
    /**
     * 回傳 MyData打包檔中的 manifest.xml 檔案。
     * @param packDir
     * @return
     */
    public File manifestFileOfMyDataPackFile(File packDir) {
        if(packDir.isDirectory()) {
            return new File(packDir, "META-INFO/manifest.xml");
        }else {
            return null;
        }
    }
    
    
    /**
     * 驗證數位簽章是否合法
     * @return
     */
    public boolean verifySignature(File dpPackDir) throws CertificateException, FileNotFoundException, InvalidKeyException, SignatureException {
        File metaInfoDir = new File(dpPackDir,"META-INFO");
        // 憑證檔
        File certFile = new File(metaInfoDir, "certificate.cer");
        // 從憑證檔中取出憑證內容
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        try(FileInputStream fis = new FileInputStream(certFile);) {
            Certificate cert = cf.generateCertificate(fis);

            // TODO 向GCA或其它憑證中心驗證憑證的有效性。因為用於測試的不是合法申請的憑證，所以此處略過驗證憑證有效性的動作。

            // 從憑證檔中取出公鑰
            PublicKey publicKey = cert.getPublicKey();
            // 數位簽章檔
            File signatureFile = new File(metaInfoDir, "manifest.sha256withrsa");
            // 數位簽章檔不存在或檔名錯誤
            if(!signatureFile.exists()){
                logger.debug("數位簽章檔不存在或檔名錯誤.... {}" , signatureFile.getPath());
                return false;
            }
            byte[] signedData = Files.readAllBytes(signatureFile.toPath());
            // 被驗證的原娟檔 manifest.xml
            File manifestFile = new File(metaInfoDir, "manifest.xml");
            // manifest不存在或檔名錯誤
            if(!manifestFile.exists()){
                logger.debug("manifest不存在或檔名錯誤");
                return false;
            }
            byte[] bs = Files.readAllBytes(manifestFile.toPath());

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(bs);
            return signature.verify(signedData);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    
    /**
     * HmacSHA256
     * @param data
     * @param key
     * @return
     */
	public static String HMACSHA256(byte[] data, byte[] key){
	      try  {
	         SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");
	         Mac mac = Mac.getInstance("HmacSHA256");
	         mac.init(signingKey);
	         return byte2hex(mac.doFinal(data));
	      } catch (NoSuchAlgorithmException e) {
	         e.printStackTrace();
	      } catch (InvalidKeyException e) {
	        e.printStackTrace();
	      }
	      return null;
	}
	/**
	 * byteTohex
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b){
	    StringBuilder hs = new StringBuilder();
	    String stmp;
	    for (int n = 0; b!=null && n < b.length; n++) {
	        stmp = Integer.toHexString(b[n] & 0XFF);
	        if (stmp.length() == 1)
	            hs.append('0');
	        hs.append(stmp);
	    }
	    return hs.toString().toUpperCase();
	}
	
	/**
	* 轉換密鑰
	* @param key 二進制密鑰
	* @return Key 密鑰
	*/
	public static Key toKey(String key) throws Exception {
		// decode the base64 encoded string
		byte[] decodedKey = Base64.getDecoder().decode(key);
		// rebuild key using SecretKeySpec
		SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, KEY_ALGORITHM);
		return originalKey;
	}	
	
	public static byte[] encrypt(byte[] data, String key, String ivstr) throws Exception {
		// 密鑰
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"),"AES");
		// 向量鰎值
		IvParameterSpec iv = new IvParameterSpec(ivstr.getBytes("UTF-8"));
		// 實例化
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		// 初始化，設置為加密模式
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		// 執行操作
		return cipher.doFinal(data);
	}
	
	public static byte[] decrypt(byte[] data, String key, String ivstr) throws Exception {
		// 密鑰
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"),"AES");
		// 向量鰎值
		IvParameterSpec iv = new IvParameterSpec(ivstr.getBytes("UTF-8"));
		// 實例化
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		// 初始化，設置為解密模式
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
		// 執行操作
		return cipher.doFinal(data);
	}
		
	
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());
		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();
		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}
		return destFile;
	}
}
