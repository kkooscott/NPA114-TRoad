package npa.gov.tw.mydata.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtil {
	
	private static Logger log = LoggerFactory.getLogger(CommonUtil.class);
	
	public static String getNDaysBeforeNow(int dayDiff) {
        //正數是+dayDiff天，負數-dayDiff天
        String daysBefore;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, dayDiff);
        //cal1.set(2000,1,29);        
        daysBefore = sdf.format(cal.getTime());

        return daysBefore;
    }

    public static String getNDaysAfter(String date, int dayDiff) {
        String daysBefore = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(date));
            cal.add(Calendar.DATE, dayDiff);
            daysBefore = sdf.format(cal.getTime());
//            //System.out.println("getNDaysBeforeN......"+daysBefore);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(ExceptionUtil.toString(e));
        }

        return daysBefore;
    }

    public static String getEncryptionKey() {
        return "please encrypt/decrypt me";
    }

    public static String formatCDttm(String dttm) {
        String str = dttm.trim();
        if (dttm.length() == 14) {
            String tmpDate = dttm.substring(0, 8);
            String tmpTime = dttm.substring(8, 14);
            str = formatE2CDate(tmpDate) + " " + formatTime(tmpTime);
        }
        return str;
    }
    
    public static String formatEDttm(String dttm) {
        String str = dttm.trim();
        if (dttm.length() == 14) {
            String tmpDate = dttm.substring(0, 8);
            String tmpTime = dttm.substring(8, 14);
            str = formatEDate(tmpDate) + " " + formatTime(tmpTime);
        }
        return str;
    }    
    
    public static String formatE2CDate2(String YYYYMMDD){
//        //System.out.println(YYYYMMDD);
        String str = YYYYMMDD.trim();
        if (str.length() == 8) {
            String YYYY = YYYYMMDD.substring(0, 4);
            String YYY = Integer.toString((Integer.parseInt(YYYY) - 1911));
            str = YYY + "年" + YYYYMMDD.substring(4, 6) + "月" + YYYYMMDD.substring(6, 8) + "日";
        }
        return str;
    }    

    public static String formatE2CDate(String YYYYMMDD) {
        String str = YYYYMMDD.trim();
        if (str.length() == 8) {
            String YYYY = YYYYMMDD.substring(0, 4);
            String YYY = Integer.toString((Integer.parseInt(YYYY) - 1911));
            if(YYY.length() == 1)
                YYY = "00"+YYY;
            if(YYY.length()==2)
                YYY = "0"+YYY;
            str = YYY + "/" + YYYYMMDD.substring(4, 6) + "/" + YYYYMMDD.substring(6, 8);
        }
        return str;
    }
    
    public static String formatEDate(String YYYYMMDD) {
        String str = YYYYMMDD.trim();
        if (str.length() == 8) {
            String YYYY = YYYYMMDD.substring(0, 4);
            str = YYYY + "/" + YYYYMMDD.substring(4, 6) + "/" + YYYYMMDD.substring(6, 8);
        }
        return str;
    }    

    public static String formatTime(String hhmmss) {
        String str = hhmmss.trim();
        if(str.length()==6){
            str = hhmmss.substring(0, 2) + ":" + hhmmss.substring(2, 4) + ":" + hhmmss.substring(4, 6);
        }
        return str;
    }

    public static String getDateC2E(String strCDate) {
        int i = 0;
        String strResult = "";

        try {
            if (null == strCDate || strCDate.equals("")) {
                return "";
            } else {
                strCDate = strCDate.trim();
                i = strCDate.length();
            }
            if (i == 5) {
                strResult = String.valueOf(Integer.parseInt(strCDate
                        .substring(0, 1)) + 1911)
                        + strCDate.substring(1);
            } else if (i == 6) {
                strResult = String.valueOf(Integer.parseInt(strCDate
                        .substring(0, 2)) + 1911)
                        + strCDate.substring(2);
            } else if (i == 7) {
                strResult = String.valueOf(Integer.parseInt(strCDate
                        .substring(0, 3)) + 1911)
                        + strCDate.substring(3);
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.toString(e));
            strResult = "19900101";
        }
        
        return strResult;
    }

    public static String getDateE2C(String strEDate) {
        int i = 0;
        String strResult = "";

        try {
            if (null == strEDate || strEDate.equals("")) {
                return "";
            } else {
                strEDate = strEDate.trim();
                i = strEDate.length();
            }
            if (i == 8) {
                strResult = paddingLeft(String.valueOf(Integer.parseInt(strEDate
                        .substring(0, 4)) - 1911), 3, "0")
                        + strEDate.substring(4);
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.toString(e));
            strResult = "19900101";
        }
        
        return strResult;
    }

    public static String getDateBC2E(String strCDate) {
        int i = 0;
        String strResult = "";

        if (null == strCDate || strCDate.equals("")) {
            return "";
        } else {
            strCDate = strCDate.trim();
            i = strCDate.length();
        }
        if (i > 4 && i < 7) {
            String tmp = ("000" + strCDate);
            tmp = tmp.substring(tmp.length() - 7);
            strResult = String.valueOf(1912 - Integer.parseInt(tmp
                    .substring(0, 3)))
                    + tmp.substring(3);
        } else if (i == 7) {
            strResult = String.valueOf(1912 - Integer.parseInt(strCDate
                    .substring(0, 3)))
                    + strCDate.substring(3);
        }
        return strResult;
    }

    public static String getDateE2BC(String strEDate) {
        int i = 0;
        String strResult = "";
        if (null == strEDate || strEDate.equals("")) {
            return "";
        } else {
            strEDate = strEDate.trim();
            i = strEDate.length();
        }
        if (i == 8) {
            strResult = paddingLeft(String.valueOf(1912 - Integer.parseInt(strEDate
                    .substring(0, 4))), 3, "0")
                    + strEDate.substring(4);
        }
        return strResult;
    }

    /**
     * 依據傳入的西元年字串取得轉換後的民國年；只有1912年(含)以後的西元年可轉換為民國年。 回傳的民國年為3碼靠右左補零。如[2005] ==>
     * [094]
     *
     * @param eYear 欲轉換的西元年字串
     * @return 轉換後的民國年；如果轉換的西元年字串無法進行轉換，則傳回空字串
     *
     * @see #getYearC2E(String)
     */
    public static String getYearE2C(String eYear) {
        if (null == eYear) {
            return "";
        }

        String tmpYear = eYear.trim();
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile("^[0-9]{4}$");
        java.util.regex.Matcher mat = pat.matcher(tmpYear);
        if (mat.find()) {
            int nYear = (Integer.valueOf(tmpYear).intValue() - 1911);
            if (nYear < 1) {
                return "";
            }
            return String.format("%1$03d", new Object[]{Integer.valueOf(nYear)});
        } else {
            return "";
        }
    }

    /**
     * 依據傳入的民國年字串取得轉換後的西元年；只有民國元年(含)以後的民國年可轉換為西元年。 回傳的西元年為4碼靠右左補零。如[94] ==>
     * [2005]
     *
     * @param cYear 欲轉換的民國年字串
     * @return 轉換後的西元年；如果轉換的民國年字串無法進行轉換，則傳回空字串
     *
     * @see #getYearE2C(String)
     */
    public static String getYearC2E(String cYear) {
        if (null == cYear) {
            return "";
        }

        String tmpYear = cYear.trim();
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile("^[0-9]{1,3}$");
        java.util.regex.Matcher mat = pat.matcher(tmpYear);
        if (mat.find()) {
            int nYear = (Integer.valueOf(tmpYear).intValue() + 1911);
            if (nYear < 1912) {
                return "";
            }
            return String.format("%1$04d", new Object[]{Integer.valueOf(nYear)});
        } else {
            return "";
        }
    }

    public static String getCurrCDate() {
        Calendar cal = Calendar.getInstance();
        return paddingLeft(Integer.toString(cal.get(Calendar.YEAR) - 1911), 3,
                "0")
                + paddingLeft(Integer.toString(cal.get(Calendar.MONTH) + 1), 2,
                        "0")
                + paddingLeft(Integer.toString(cal.get(Calendar.DATE)), 2, "0");
    }

    public static String getCurrEDate() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return String.valueOf(dateFormat.format(date));
    }
    
    public static String getEDateNextCurr(int calendarUnit, int years) {
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(calendarUnit, years);
        return paddingLeft(Integer.toString(cal.get(Calendar.YEAR)), 4, "0")
                + paddingLeft(Integer.toString(cal.get(Calendar.MONTH) + 1), 2, "0")
                + paddingLeft(Integer.toString(cal.get(Calendar.DATE)), 2, "0");
    }

    public static String getCurrTime() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HHmm");
        return String.valueOf(dateFormat.format(date));
    }
    
    /**
     * For 刑案紀錄表 96.2.1欄位異動，時間改為含秒的6位數值
     */
    public static String getCurrTime6() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HHmmss");
        return String.valueOf(dateFormat.format(date));
    }

    public static String paddingLeft(String strIn, int intDigit,
            String strPaddingChar) {
        if (strIn.length() == 0) {
            return "";
        }
        if (strPaddingChar.equals("")) {
            strPaddingChar = " ";
        }
        StringBuffer sbAdd = new StringBuffer();
        if (strIn.length() < intDigit) {
            for (int i = 0; i < intDigit - strIn.length(); i++) {
                sbAdd.append(strPaddingChar);
            }
            strIn = sbAdd.toString() + strIn;
        }
        return strIn;
    }
}
