package npa.gov.tw.mydata.util;

import org.apache.commons.io.FileUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Base64;

/**
 * DESede對稱加密演算法
 * 
 * @author Weder
 */
public class DESedeCoder {
	/**
	 * 密鑰算法
	 */
	public static final String KEY_ALGORITHM = "DESede";

	/**
	 * 加密/解密算法/工作模式/填充方式
	 */
	public static final String CIPHER_ALGORITHM = "DESede/ECB/PKCS5Padding";

	/**
	 * 
	 * 生成密钥
	 * 
	 * @return byte[] 二进制密钥
	 */
	public static byte[] initkey() throws Exception {

		// 實例化密鑰生成器
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		// 初始化密鑰生成器
		kg.init(168);
		// 生成密鑰
		SecretKey secretKey = kg.generateKey();
		// 獲取二進制密鑰編碼形式

		byte[] key = secretKey.getEncoded();
		/*BufferedOutputStream keystream = new BufferedOutputStream(new FileOutputStream("DESedeKey.dat"));
		keystream.write(key, 0, key.length);
		keystream.flush();
		keystream.close();*/

		return key;
	}

	/**
	* 轉換密鑰
	* @param key 二進制密鑰
	* @return Key 密鑰
	*/
	public static Key toKey(byte[] key) throws Exception {
		// 實例化Des密鑰
		DESedeKeySpec dks = new DESedeKeySpec(key);
		// 實例化密鑰工廠
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		// 生成密鑰
		SecretKey secretKey = keyFactory.generateSecret(dks);
		return secretKey;
	}

	/**
	* 加密數據
	* @param data 待加密數據
	* @param key 密鑰
	* @return byte[] 加密後的數據
	*/
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 還原密鑰
		Key k = toKey(key);
		// 實例化
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		// 初始化，設置為加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 執行操作
		return cipher.doFinal(data);
	}
	
	public static File encrypt(File sourceFile, byte[] key) throws Exception {
		File encryptFiles=null;
		Path path = Paths.get(sourceFile.getAbsolutePath());
		byte[] data = Files.readAllBytes(path);
		// 還原密鑰
		Key k = toKey(key);
		// 實例化
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		// 初始化，設置為加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 執行操作
		byte[] encryptByres =cipher.doFinal(data);
		FileUtils.writeByteArrayToFile(new File("pathname"), encryptByres);
		
		return encryptFiles;
	}
	

	/**
	* 解密數據
	* @param data 待解密數據
	* @param key 密鑰
	* @return byte[] 解密後的數據
	*/
	public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 歡迎密鑰
		Key k = toKey(key);
		// 實例化
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		// 初始化，設置為解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);
		// 執行操作
		return cipher.doFinal(data);
	}
	
	
	public static File decrypt(File sourceFile, byte[] key) throws Exception {
		File encryptFiles=null;
		Path path = Paths.get(sourceFile.getAbsolutePath());
		byte[] data = Files.readAllBytes(path);
		// 歡迎密鑰
		Key k = toKey(key);
		// 實例化
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		// 初始化，設置為解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);
		// 執行操作
		byte[] encryptByres =cipher.doFinal(data);
		FileUtils.writeByteArrayToFile(new File("pathname"), encryptByres);
		return encryptFiles;
	}	
	

	/**
	 * 进行加解密的测试
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//String str = "DESede";
		String str = readFile("/Users/mac/Desktop/tmp/mydatalog.json");
		System.out.println("原文：" + str);
		// 初始化密鑰
		byte[] key = DESedeCoder.initkey();
		System.out.println("密鑰：" + Base64.getEncoder().encode(key));
		// 加密數據
		byte[] data = DESedeCoder.encrypt(str.getBytes(), key);
		System.out.println("加密後：" + Base64.getDecoder().decode(data));
		// 解密數據
		data = DESedeCoder.decrypt(data, key);
		System.out.println("解密後：" + new String(data));
	}
	
	public static String readFile(String file) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    String line = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    String ls = System.getProperty("line.separator");
	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }
	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}	
	
	
}