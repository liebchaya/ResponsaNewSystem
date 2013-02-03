package utils;

import java.util.HashSet;
import java.util.Set;

public class StringUtils {

	/**
	 * Enable search for quotes
	 * 
	 * @param str
	 * @return
	 */
	public static String fixQuateForSearch(String str) {
		String newStr = str;
		String s1 = newStr.replace("\"", "\\\'"); // changes: " to: \"
		String s2 = s1.replace("\'", "\\\'"); // changes: ' to: \'
		return s2;
	}
	
	/**
	 * Clean string after annotation
	 * 
	 * @param str
	 * @return
	 */
	 public static String fixAnnotatedTerm(String term){
			String newTerm=term;
			if (term.startsWith("\""))
				newTerm = term.substring(1);
			if (term.endsWith("\""))
				newTerm = newTerm.substring(0,newTerm.length()-1);
			newTerm = newTerm.replaceAll("\"\"", "\"");
			return newTerm;
				
		}

	/**
	 * Validates if input String is a number
	 * 
	 * @param in
	 * @return
	 */
	public static boolean checkIfNumber(String in) {
		try {
			Long.parseLong(in);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}
	
	public static HashSet<String> convertStringToSet(String str){
		HashSet<String> set = new HashSet<String>();
		for(String s:str.split(",")){
			s= s.replaceAll("\\[|\\]", "");
			if(!s.isEmpty())
				set.add(s.trim());
		}
		return set;
	}
	
	public static String convertSetToString(Set<String> set){
		String str = "[";
		for(String s:set){
			str= str + s.trim() + ",";
		}
		if(str.endsWith(",")){
			str = str.substring(0,str.length()-1);
		}
		str = str + "]";
		return str;
	}
}
