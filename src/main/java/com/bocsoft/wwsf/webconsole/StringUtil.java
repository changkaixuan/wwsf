package com.bocsoft.wwsf.webconsole;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.Clob;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

public class StringUtil {

	private static final DecimalFormat decimalFormat;
	static {
		NumberFormat numberFormat = NumberFormat
				.getNumberInstance(Locale.ENGLISH);
		decimalFormat = (DecimalFormat) numberFormat;
		decimalFormat.applyPattern("#.##");
	}

	public static String stringifyException(Throwable e) {
		StringWriter stm = new StringWriter();
		PrintWriter wrt = new PrintWriter(stm);
		e.printStackTrace(wrt);
		wrt.close();
		return stm.toString();
	}

	/**
	 * Given a full hostname, return the word upto the first dot.
	 * 
	 * @param fullHostname
	 *            the full hostname
	 * @return the hostname to the first dot
	 */
	public static String simpleHostname(String fullHostname) {
		int offset = fullHostname.indexOf('.');
		if (offset != -1) {
			return fullHostname.substring(0, offset);
		}
		return fullHostname;
	}

	private static DecimalFormat oneDecimal = new DecimalFormat("0.0");

	/**
	 * Given an integer, return a string that is in an approximate, but human
	 * readable format. It uses the bases 'k', 'm', and 'g' for 1024, 1024**2,
	 * and 1024**3.
	 * 
	 * @param number
	 *            the number to format
	 * @return a human readable form of the integer
	 */
	public static String humanReadableInt(long number) {
		long absNumber = Math.abs(number);
		double result = number;
		String suffix = "";
		if (absNumber < 1024) {
			// nothing
		} else if (absNumber < 1024 * 1024) {
			result = number / 1024.0;
			suffix = "k";
		} else if (absNumber < 1024 * 1024 * 1024) {
			result = number / (1024.0 * 1024);
			suffix = "m";
		} else {
			result = number / (1024.0 * 1024 * 1024);
			suffix = "g";
		}
		return oneDecimal.format(result) + suffix;
	}
	
	public static String formatSeparator(long number) {
		DecimalFormat percentFormat = new DecimalFormat("#,###");
		return percentFormat.format(number);
	}

	/**
	 * Format a percentage for presentation to the user.
	 * 
	 * @param done
	 *            the percentage to format (0.0 to 1.0)
	 * @param digits
	 *            the number of digits past the decimal point
	 * @return a string representation of the percentage
	 */
	public static String formatPercent(double done, int digits) {
		DecimalFormat percentFormat = new DecimalFormat("0.00%");
		double scale = Math.pow(10.0, digits + 2);
		double rounded = Math.floor(done * scale);
		percentFormat.setDecimalSeparatorAlwaysShown(false);
		percentFormat.setMinimumFractionDigits(digits);
		percentFormat.setMaximumFractionDigits(digits);
		return percentFormat.format(rounded / scale);
	}

	/**
	 * Given an array of strings, return a comma-separated list of its elements.
	 * 
	 * @param strs
	 *            Array of strings
	 * @return Empty string if strs.length is 0, comma separated list of strings
	 *         otherwise
	 */

	public static String arrayToString(String[] strs) {
		if (strs.length == 0) {
			return "";
		}
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(strs[0]);
		for (int idx = 1; idx < strs.length; idx++) {
			sbuf.append(",");
			sbuf.append(strs[idx]);
		}
		return sbuf.toString();
	}

	/**
	 * 
	 * @param uris
	 */
	public static String uriToString(URI[] uris) {
		if (uris == null) {
			return null;
		}
		StringBuffer ret = new StringBuffer(uris[0].toString());
		for (int i = 1; i < uris.length; i++) {
			ret.append(",");
			ret.append(uris[i].toString());
		}
		return ret.toString();
	}

	/**
	 * 
	 * @param str
	 */
	public static URI[] stringToURI(String[] str) {
		if (str == null)
			return null;
		URI[] uris = new URI[str.length];
		for (int i = 0; i < str.length; i++) {
			try {
				uris[i] = new URI(str[i]);
			} catch (URISyntaxException ur) {
				System.out.println("Exception in specified URI's "
						+ StringUtil.stringifyException(ur));
				// making sure its asssigned to null in case of an error
				uris[i] = null;
			}
		}
		return uris;
	}

	/**
	 * 
	 * Given a finish and start time in long milliseconds, returns a String in
	 * the format Xhrs, Ymins, Z sec, for the time difference between two times.
	 * If finish time comes before start time then negative valeus of X, Y and Z
	 * wil return.
	 * 
	 * @param finishTime
	 *            finish time
	 * @param startTime
	 *            start time
	 */
	public static String formatTimeDiff(long finishTime, long startTime) {
		long timeDiff = finishTime - startTime;
		return formatTime(timeDiff);
	}

	/**
	 * 
	 * Given the time in long milliseconds, returns a String in the format Xhrs,
	 * Ymins, Z sec.
	 * 
	 * @param timeDiff
	 *            The time difference to format
	 */
	public static String formatTime(long timeDiff) {
		StringBuffer buf = new StringBuffer();
		long days = timeDiff / (24 * 60 * 60 * 1000);
		long rem = (timeDiff % (24 * 60 * 60 * 1000));
		long hours = rem / (60 * 60 * 1000);
		rem = (rem % (60 * 60 * 1000));
		long minutes = rem / (60 * 1000);
		rem = rem % (60 * 1000);
		long seconds = rem / 1000;
		
		boolean kk = false;
		
		if (days != 0) {
			if (!kk) kk = true;
			buf.append(days);
			buf.append(" ");
		}
		if (hours != 0 || kk) {
			if (!kk) kk = true;
			buf.append(hours);
			buf.append(":");
		}
		if (minutes != 0 || kk) {
			if (!kk) kk = true;
			buf.append(minutes);
			buf.append(":");
		}
		// return "0sec if no difference
		buf.append(seconds);
		return buf.toString();
	}

	/**
	 * Formats time in ms and appends difference (finishTime - startTime) as
	 * returned by formatTimeDiff(). If finish time is 0, empty string is
	 * returned, if start time is 0 then difference is not appended to return
	 * value.
	 * 
	 * @param dateFormat
	 *            date format to use
	 * @param finishTime
	 *            fnish time
	 * @param startTime
	 *            start time
	 * @return formatted value.
	 */
	public static String getFormattedTimeWithDiff(DateFormat dateFormat,
			long finishTime, long startTime) {
		StringBuffer buf = new StringBuffer();
		if (0 != finishTime) {
			buf.append(dateFormat.format(new Date(finishTime)));
			if (0 != startTime) {
				buf.append(" (" + formatTimeDiff(finishTime, startTime) + ")");
			}
		}
		return buf.toString();
	}

	/**
	 * Returns an arraylist of strings.
	 * 
	 * @param str
	 *            the comma seperated string values
	 * @return the arraylist of the comma seperated string values
	 */
	public static String[] getStrings(String str) {
		Collection<String> values = getStringCollection(str);
		if (values.size() == 0) {
			return null;
		}
		return values.toArray(new String[values.size()]);
	}

	/**
	 * Returns a collection of strings.
	 * 
	 * @param str
	 *            comma seperated string values
	 * @return an <code>ArrayList</code> of string values
	 */
	public static Collection<String> getStringCollection(String str) {
		List<String> values = new ArrayList<String>();
		if (str == null)
			return values;
		StringTokenizer tokenizer = new StringTokenizer(str, ",");
		values = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			values.add(tokenizer.nextToken().trim());
		}
		return values;
	}

	final public static char COMMA = ',';
	final public static String COMMA_STR = ",";
	final public static char ESCAPE_CHAR = '\\';

	/**
	 * Split a string using the default separator
	 * 
	 * @param str
	 *            a string that may have escaped separator
	 * @return an array of strings
	 */
	public static String[] split(String str) {
		return split(str, ESCAPE_CHAR, COMMA);
	}
	
	public static String[] split(String toSplit, String delimiter) {
		if (isNullOrEmpty(toSplit) || isNullOrEmpty(delimiter)) {
			return null;
		}
		int offset = toSplit.indexOf(delimiter);
		if (offset < 0) {
			return null;
		}

		String beforeDelimiter = toSplit.substring(0, offset);
		String afterDelimiter = toSplit.substring(offset + delimiter.length());
		return new String[] {beforeDelimiter, afterDelimiter};
	}

	/**
	 * Split a string using the given separator
	 * 
	 * @param str
	 *            a string that may have escaped separator
	 * @param escapeChar
	 *            a char that be used to escape the separator
	 * @param separator
	 *            a separator char
	 * @return an array of strings
	 */
	public static String[] split(String str, char escapeChar, char separator) {
		if (str == null) {
			return null;
		}
		ArrayList<String> strList = new ArrayList<String>();
		StringBuilder split = new StringBuilder();
		int index = 0;
		while ((index = findNext(str, separator, escapeChar, index, split)) >= 0) {
			++index; // move over the separator for next search
			strList.add(split.toString());
			split.setLength(0); // reset the buffer
		}
		strList.add(split.toString());
		// remove trailing empty split(s)
		int last = strList.size(); // last split
		while (--last >= 0 && "".equals(strList.get(last))) {
			strList.remove(last);
		}
		return strList.toArray(new String[strList.size()]);
	}

	/**
	 * Finds the first occurrence of the separator character ignoring the
	 * escaped separators starting from the index. Note the substring between
	 * the index and the position of the separator is passed.
	 * 
	 * @param str
	 *            the source string
	 * @param separator
	 *            the character to find
	 * @param escapeChar
	 *            character used to escape
	 * @param start
	 *            from where to search
	 * @param split
	 *            used to pass back the extracted string
	 */
	public static int findNext(String str, char separator, char escapeChar,
			int start, StringBuilder split) {
		int numPreEscapes = 0;
		for (int i = start; i < str.length(); i++) {
			char curChar = str.charAt(i);
			if (numPreEscapes == 0 && curChar == separator) { // separator
				return i;
			} else {
				split.append(curChar);
				numPreEscapes = (curChar == escapeChar) ? (++numPreEscapes) % 2
						: 0;
			}
		}
		return -1;
	}

	/**
	 * Escape commas in the string using the default escape char
	 * 
	 * @param str
	 *            a string
	 * @return an escaped string
	 */
	public static String escapeString(String str) {
		return escapeString(str, ESCAPE_CHAR, COMMA);
	}

	/**
	 * Escape <code>charToEscape</code> in the string with the escape char
	 * <code>escapeChar</code>
	 * 
	 * @param str
	 *            string
	 * @param escapeChar
	 *            escape char
	 * @param charToEscape
	 *            the char to be escaped
	 * @return an escaped string
	 */
	public static String escapeString(String str, char escapeChar,
			char charToEscape) {
		return escapeString(str, escapeChar, new char[] { charToEscape });
	}

	// check if the character array has the character
	private static boolean hasChar(char[] chars, char character) {
		for (char target : chars) {
			if (character == target) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param charsToEscape
	 *            array of characters to be escaped
	 */
	public static String escapeString(String str, char escapeChar,
			char[] charsToEscape) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		// Let us specify good enough capacity to constructor of StringBuilder
		// sothat
		// resizing would not be needed(to improve perf).
		StringBuilder result = new StringBuilder((int) (len * 1.5));

		for (int i = 0; i < len; i++) {
			char curChar = str.charAt(i);
			if (curChar == escapeChar || hasChar(charsToEscape, curChar)) {
				// special char
				result.append(escapeChar);
			}
			result.append(curChar);
		}
		return result.toString();
	}

	/**
	 * Unescape commas in the string using the default escape char
	 * 
	 * @param str
	 *            a string
	 * @return an unescaped string
	 */
	public static String unEscapeString(String str) {
		return unEscapeString(str, ESCAPE_CHAR, COMMA);
	}

	/**
	 * Unescape <code>charToEscape</code> in the string with the escape char
	 * <code>escapeChar</code>
	 * 
	 * @param str
	 *            string
	 * @param escapeChar
	 *            escape char
	 * @param charToEscape
	 *            the escaped char
	 * @return an unescaped string
	 */
	public static String unEscapeString(String str, char escapeChar,
			char charToEscape) {
		return unEscapeString(str, escapeChar, new char[] { charToEscape });
	}

	/**
	 * @param charsToEscape
	 *            array of characters to unescape
	 */
	public static String unEscapeString(String str, char escapeChar,
			char[] charsToEscape) {
		if (str == null) {
			return null;
		}
		StringBuilder result = new StringBuilder(str.length());
		boolean hasPreEscape = false;
		for (int i = 0; i < str.length(); i++) {
			char curChar = str.charAt(i);
			if (hasPreEscape) {
				if (curChar != escapeChar && !hasChar(charsToEscape, curChar)) {
					// no special char
					throw new IllegalArgumentException(
							"Illegal escaped string " + str + " unescaped "
									+ escapeChar + " at " + (i - 1));
				}
				// otherwise discard the escape char
				result.append(curChar);
				hasPreEscape = false;
			} else {
				if (hasChar(charsToEscape, curChar)) {
					throw new IllegalArgumentException(
							"Illegal escaped string " + str + " unescaped "
									+ curChar + " at " + i);
				} else if (curChar == escapeChar) {
					hasPreEscape = true;
				} else {
					result.append(curChar);
				}
			}
		}
		if (hasPreEscape) {
			throw new IllegalArgumentException("Illegal escaped string " + str
					+ ", not expecting " + escapeChar + " in the end.");
		}
		return result.toString();
	}

	/**
	 * Return hostname without throwing exception.
	 * 
	 * @return hostname
	 */
	public static String getHostname() {
		try {
			return "" + InetAddress.getLocalHost();
		} catch (UnknownHostException uhe) {
			return "" + uhe;
		}
	}

	/**
	 * Return a message for logging.
	 * 
	 * @param prefix
	 *            prefix keyword for the message
	 * @param msg
	 *            content of the message
	 * @return a message for logging
	 */
	public static String toStartupShutdownString(String prefix, String[] msg) {
		StringBuffer b = new StringBuffer(prefix);
		b.append("\n/************************************************************");
		for (String s : msg)
			b.append("\n" + prefix + s);
		b.append("\n************************************************************/");
		return b.toString();
	}

	/**
	 * Escapes HTML Special characters present in the string.
	 * 
	 * @param string
	 * @return HTML Escaped String representation
	 */
	public static String escapeHTML(String string) {
		if (string == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		boolean lastCharacterWasSpace = false;
		char[] chars = string.toCharArray();
		for (char c : chars) {
			if (c == ' ') {
				if (lastCharacterWasSpace) {
					lastCharacterWasSpace = false;
					sb.append("&nbsp;");
				} else {
					lastCharacterWasSpace = true;
					sb.append(" ");
				}
			} else {
				lastCharacterWasSpace = false;
				switch (c) {
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				default:
					sb.append(c);
					break;
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Return an abbreviated English-language desc of the byte length
	 */
	public static String byteDesc(long len) {
		double val = 0.0;
		String ending = "";
		if (len < 1024 * 1024) {
			val = (1.0 * len) / 1024;
			ending = " KB";
		} else if (len < 1024 * 1024 * 1024) {
			val = (1.0 * len) / (1024 * 1024);
			ending = " MB";
		} else if (len < 1024L * 1024 * 1024 * 1024) {
			val = (1.0 * len) / (1024 * 1024 * 1024);
			ending = " GB";
		} else if (len < 1024L * 1024 * 1024 * 1024 * 1024) {
			val = (1.0 * len) / (1024L * 1024 * 1024 * 1024);
			ending = " TB";
		} else {
			val = (1.0 * len) / (1024L * 1024 * 1024 * 1024 * 1024);
			ending = " PB";
		}
		return limitDecimalTo2(val) + ending;
	}

	public static synchronized String limitDecimalTo2(double d) {
		return decimalFormat.format(d);
	}

	/**
	 * Concatenates strings, using a separator.
	 * 
	 * @param separator
	 *            Separator to join with.
	 * @param strings
	 *            Strings to join.
	 * @return the joined string
	 */
	public static String join(CharSequence separator, Iterable<String> strings) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : strings) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Concatenates strings, using a separator.
	 * 
	 * @param separator
	 *            to join with
	 * @param strings
	 *            to join
	 * @return the joined string
	 */
	public static String join(CharSequence separator, String[] strings) {
		// Ideally we don't have to duplicate the code here if array is
		// iterable.
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : strings) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Concatenates objects, using a separator.
	 * 
	 * @param separator
	 *            to join with
	 * @param objects
	 *            to join
	 * @return the joined string
	 */
	public static String join(CharSequence separator, Object[] objects) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object obj : objects) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(obj);
		}
		return sb.toString();
	}

	/**
	 * Capitalize a word
	 * 
	 * @param s
	 *            the input string
	 * @return capitalized string
	 */
	public static String capitalize(String s) {
		int len = s.length();
		if (len == 0)
			return s;
		return new StringBuilder(len)
				.append(Character.toTitleCase(s.charAt(0)))
				.append(s.substring(1)).toString();
	}

	/**
	 * Convert SOME_STUFF to SomeStuff
	 * 
	 * @param s
	 *            input string
	 * @return camelized string
	 */
	public static String camelize(String s) {
		StringBuilder sb = new StringBuilder();
		String[] words = split(s.toLowerCase(Locale.US), ESCAPE_CHAR, '_');

		for (String word : words)
			sb.append(capitalize(word));

		return sb.toString();
	}
	
	public static boolean isNullOrEmpty(Object str){
		if(str == null || "".equals(str.toString().trim())){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean hasText(String str) {
		return (str != null && !str.isEmpty() && containsText(str));
	}

	private static boolean containsText(CharSequence str) {
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	
	public static String[] delimitedListToStringArray(String str, String delimiter) {
		return delimitedListToStringArray(str, delimiter, null);
	}
	
	public static String deleteAny(String inString, String charsToDelete) {
		if (!StringUtil.isNullOrEmpty(inString) || !StringUtil.isNullOrEmpty(charsToDelete)) {
			return inString;
		}

		StringBuilder sb = new StringBuilder(inString.length());
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String[] delimitedListToStringArray(
			String str, String delimiter, String charsToDelete) {

		if (str == null) {
			return new String[0];
		}
		if (delimiter == null) {
			return new String[] {str};
		}

		List<String> result = new ArrayList<>();
		if ("".equals(delimiter)) {
			for (int i = 0; i < str.length(); i++) {
				result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
			}
		}
		else {
			int pos = 0;
			int delPos;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(deleteAny(str.substring(pos), charsToDelete));
			}
		}
		return result.toArray(new String[0]);
	}
	
	public static String[] commaDelimitedListToStringArray(String str) {
		return delimitedListToStringArray(str, ",");
	}
	
	public static Set<String> commaDelimitedListToSet(String str) {
		Set<String> set = new LinkedHashSet<>();
		String[] tokens = commaDelimitedListToStringArray(str);
		for (String token : tokens) {
			set.add(token);
		}
		return set;
	}

	/**
	 * Convert a {@link Collection} to a delimited {@code String} (e.g. CSV).
	 * <p>Useful for {@code toString()} implementations.
	 * @param coll the {@code Collection} to convert
	 * @param delim the delimiter to use (typically a ",")
	 * @param prefix the {@code String} to start each element with
	 * @param suffix the {@code String} to end each element with
	 * @return the delimited {@code String}
	 */
	public static String collectionToDelimitedString(
			Collection<?> coll, String delim, String prefix, String suffix) {

		if (coll == null || coll.size() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		Iterator<?> it = coll.iterator();
		while (it.hasNext()) {
			sb.append(prefix).append(it.next()).append(suffix);
			if (it.hasNext()) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}

	/**
	 * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
	 * <p>Useful for {@code toString()} implementations.
	 * @param coll the {@code Collection} to convert
	 * @param delim the delimiter to use (typically a ",")
	 * @return the delimited {@code String}
	 */
	public static String collectionToDelimitedString(Collection<?> coll, String delim) {
		return collectionToDelimitedString(coll, delim, "", "");
	}

	/**
	 * Convert a {@code Collection} into a delimited {@code String} (e.g., CSV).
	 * <p>Useful for {@code toString()} implementations.
	 * @param coll the {@code Collection} to convert
	 * @return the delimited {@code String}
	 */
	public static String collectionToCommaDelimitedString(Collection<?> coll) {
		return collectionToDelimitedString(coll, ",");
	}

	public static String fillSpace(String src,int length){
		StringBuffer space= new StringBuffer();
		if(src==null||"".equals(src.trim())){
			for(int i= 0;i<length;i++) space.append(" ");
		}else{
			if(src.length() == length){
				space.append(src);
			}else if(src.length() < length){
				space.append(src);
				for(int i= 0;i<length-src.length();i++) space.append(" ");
			}else{
				space.append(src.substring(0,length-3))
				.append("...");
			}
		}
		return space.toString();
	}
	
	public static String leftFillSpace(String src,int length){
		StringBuffer space= new StringBuffer();
		if(src==null||"".equals(src.trim())){
			for(int i= 0;i<length;i++) space.append(" ");
		}else{
			if(src.length() == length){
				space.append(src);
			}else if(src.length() < length){
				for(int i= 0;i<length-src.length();i++) space.append(" ");
				space.append(src);
			}else{
				space.append(src.substring(0,length-3))
				.append("...");
			}
		}
		return space.toString();
	}
	
	/**
	 * ************************************** 字符转码相关  **************************************
	 */
	//字母汉字都转
	public static String stringToAscii(String v) {
		if(null == v || v.equals("")) {
			return "";
		}
		char[] utfBytes = v.toCharArray();
		String unicodeBytes = "";
		for(int byteIndex=0;byteIndex<utfBytes.length;byteIndex++) {
			String hexB = Integer.toHexString(utfBytes[byteIndex]);
			if(hexB.length() <= 2) {
				hexB = "00" + hexB;
			}
			//unicodeBytes = unicodeBytes + "\\u" + hexB.toUpperCase();
			unicodeBytes = unicodeBytes + "\\u" + hexB;
		}
		return unicodeBytes;
	}
	
	//不转字母只转汉字
	public static String stringToAscii2(String v) {
		if(null == v || v.equals("")) {
			return "";
		}
		String result = "";
		for(int i=0;i<v.length();i++) {
			int chr1 = (char)v.charAt(i);
			if(chr1 >= 19968 && chr1 <= 171941) {//汉字范围 \u4e00-\u9fa5 (中文)
				result += "\\u" + Integer.toHexString(chr1);
			}else {
				result += v.charAt(i);
			}
		}
		return result;
	}
	
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if(ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}
	
	public static String asciiToString(String v) {
		if(null == v || v.equals("")) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		String hex[] = v.split("\\\\u");
		for(int i=0;i<hex.length;i++) {
			try {
				//汉字范围 \u4e00-\u9fa5 (中文)
				if(hex[i].length() >= 4) {//取前四个，判断是否是汉字
					String chinese = hex[i].substring(0,4);
					try {
						int chr = Integer.parseInt(chinese,16);
						//boolean isChinese = isChinese((char)chr);
						//转化成功，判断是否在 汉字范围内
					    //if(isChinese) {//在汉字范围内
							//追加
							sb.append((char)chr);
							String behindString = hex[i].substring(4);
							//并且追加 后面的字符
							sb.append(behindString);
						//}else {
						//	sb.append(hex[i]);
						//}
					}catch(NumberFormatException e1) {
						sb.append(hex[i]);
					}
				}else {
					sb.append(hex[i]);
				}
			}catch(NumberFormatException e1) {
				sb.append(hex[i]);
			}
		}
		return sb.toString();
	}
	
	public static int getStringLength(String str) {
		if(null == str || str.equals("")) {
			return 0;
		}
		int retLength = 0;
		for(int i=0;i<str.length();i++) {
			int chr1 = (char)str.charAt(i);
			if(chr1 >= 19968 && chr1 <= 171941) {//汉字范围 \u4e00-\u9fa5 (中文)
				retLength += 2;
			}else {
				retLength += 1;
			}
		}
		return retLength;
	}
	
	public static String clobToString(Clob clob) throws Exception{
		String retStr = "";
		Reader is = clob.getCharacterStream();
		BufferedReader br = new BufferedReader(is);
		String tStr = br.readLine();
		StringBuffer sb = new StringBuffer();
		while(tStr != null){
			sb.append(tStr+"\r\n");
			tStr = br.readLine();
		}
		retStr = sb.toString();
		return retStr;
	}
	
	public static void validateTabColNullAndLength(String sheetName,int rowNum,String colName,boolean isNotNull,String colValue,int length) throws Exception{
		if(isNotNull) {
			if(null == colValue || colValue.equals("")) {
				throw new Exception(sheetName+": "+"第"+rowNum+"行,"+colName+"不能为空");
			}
		}
		int tLength = getStringLength(colValue);
		if(tLength > length) {
			throw new Exception(sheetName+": "+"第"+rowNum+"行,"+colName+"长度不能超"+length);
		}
	}
	
	public static int getIntValue(String sheetName,int rowNum,String colName,String pValue) throws Exception{
		try {
			return Integer.parseInt(pValue);
		}catch(Exception e) {
			throw new Exception(sheetName+": "+"第"+rowNum+"行,"+colName+"应是数字");
		}
	}
	
	public static Date getDateValue(String sheetName,int rowNum,String colName,String pValue) throws Exception{
		Date rDate = null;
		try {
			rDate = dateStringToDate(pValue);
		}catch(Exception e) {
			throw new Exception(sheetName+": "+"第"+rowNum+"行,"+colName+"格式应为yyyy-MM-dd或yyyy-MM-dd HH:mm:ss");
		}
		return rDate;
	}
	
	public static Date dateStringToDate(String pDate) throws Exception{
		Date rDate = null;
		try{
			if(null != pDate && !pDate.equals("")) {
				pDate = pDate.replaceAll("/", "-");
				String format = "yyyy-MM-dd";
				if(pDate.length() > 10) {
					format = "yyyy-MM-dd HH:mm:ss";
				}
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				rDate = sdf.parse(pDate);
				//System.out.println("pDate+++++++++++++:"+sdf.format(rDate));
			}
		}catch(Exception e){
			throw e;
		}
		return rDate;
	}
	
}
