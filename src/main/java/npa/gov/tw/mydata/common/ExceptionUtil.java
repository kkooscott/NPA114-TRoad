package npa.gov.tw.mydata.common;

public class ExceptionUtil {

    /**
     * 將Exception內容轉成字串
     * @param e
     * @return
     */
    public static String toString(Exception e){
	String tmp = e.getMessage();//e.getLocalizedMessage();
	tmp = tmp +"\n"+ getStackTrace(e.getStackTrace());
	return tmp;
    }
    /**
     * 請改成呼叫toString
     * @param ste
     * @return
     */
    public static String getStackTrace(StackTraceElement[] ste) {
	StringBuilder s = new StringBuilder();

	for (StackTraceElement e : ste) {
	    s.append(e + "\n");
	}
	return s.toString();
    }

    
    //get from org.apache.axis2.util.JavaUtils
    /**
     * Get a string containing the stack of the specified exception
     * 
     * @param e
     * @return
     */
    public static String stackToString(Throwable e) {
	java.io.StringWriter sw = new java.io.StringWriter();
	java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
	java.io.PrintWriter pw = new java.io.PrintWriter(bw);
	e.printStackTrace(pw);
	pw.close();
	String text = sw.getBuffer().toString();
	// Jump past the throwable
	text = text.substring(text.indexOf("at"));
	text = replace(text, "at ", "DEBUG_FRAME = ");
	return text;
    }
    /**
     * replace: Like String.replace except that the old new items are strings.
     *
     * @param name string
     * @param oldT old text to replace
     * @param newT new text to use
     * @return replacement string
     */
    public static final String replace(String name,
                                       String oldT, String newT) {

        if (name == null) return "";

        // Create a string buffer that is twice initial length.
        // This is a good starting point.
        StringBuffer sb = new StringBuffer(name.length() * 2);

        int len = oldT.length();
        try {
            int start = 0;
            int i = name.indexOf(oldT, start);

            while (i >= 0) {
                sb.append(name.substring(start, i));
                sb.append(newT);
                start = i + len;
                i = name.indexOf(oldT, start);
            }
            if (start < name.length())
                sb.append(name.substring(start));
        } catch (NullPointerException e) {
            // No FFDC code needed
        }

        return new String(sb);
    }
    /**
     * Get a string containing the stack of the current location.
     * Note This utility is useful in debug scenarios to dump out 
     * the call stack.
     *
     * @return String
     */
    public static String callStackToString() {
        return stackToString(new RuntimeException());
    }
}
