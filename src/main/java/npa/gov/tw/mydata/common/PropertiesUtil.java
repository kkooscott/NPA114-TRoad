/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npa.gov.tw.mydata.common;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public final class PropertiesUtil implements Serializable {

    private static Properties prop = new Properties();

	//private static Map<String, Object> object = new HashMap<String, Object>();
    static {
        InputStream inputStream = null;
        try {
            prop = new Properties();
            inputStream = new PropertiesUtil().getClass().getClassLoader().getResourceAsStream("config/Env.properties");
            prop.load(inputStream);

        } catch (Exception e) {
            System.err.println("PropertiesUtil 讀取環境變數檔發生錯誤，錯誤訊息:" + e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
            }
        }
    }

    private PropertiesUtil() {
    }

    /**
     * ******************************************************************************
     * 回傳 Properties 型態的環境變數物件。
     *
     * @return 回傳 Properties 型態的環境變數物件。
	 *******************************************************************************
     */
    public static Properties loadProperties() {
        return prop;
    }

    /**
     * ******************************************************************************
     * 回傳 sKey 值所指定的環境變數內容。
     *
     * @param sKey 欲取回的環境變數鍵值。
     * @return 回傳 sKey 值所指定的環境變數內容。
	 *******************************************************************************
     */
    public static String getProperty(String sKey) {

        //return (Map<String, Object>)object.get(sKey);
        return prop.getProperty(sKey);
    }
}
