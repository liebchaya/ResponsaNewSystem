package test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import utils.StringUtils;

public class NgramViewer {

	private static String longestSubstring(String str1, String str2) {
		 
		StringBuilder sb = new StringBuilder();
		if (str1 == null || str1.isEmpty() || str2 == null || str2.isEmpty())
		  return "";
		 
		// ignore case
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		 
		// java initializes them already with 0
		int[][] num = new int[str1.length()][str2.length()];
		int maxlen = 0;
		int lastSubsBegin = 0;
		 
		for (int i = 0; i < str1.length(); i++) {
		for (int j = 0; j < str2.length(); j++) {
		  if (str1.charAt(i) == str2.charAt(j)) {
		    if ((i == 0) || (j == 0))
		       num[i][j] = 1;
		    else
		       num[i][j] = 1 + num[i - 1][j - 1];
		 
		    if (num[i][j] > maxlen) {
		      maxlen = num[i][j];
		      // generate substring from str1 => i
		      int thisSubsBegin = i - num[i][j] + 1;
		      if (lastSubsBegin == thisSubsBegin) {
		         //if the current LCS is the same as the last time this block ran
		         sb.append(str1.charAt(i));
		      } else {
		         //this block resets the string builder if a different LCS is found
		         lastSubsBegin = thisSubsBegin;
		         sb = new StringBuilder();
		         sb.append(str1.substring(lastSubsBegin, i + 1));
		      }
		   }
		}
		}}
		 
		return sb.toString();
		}
	
	
	private static String concatenateNgrams1(String n1, String n2) {
		int index;
		String LCS = longestSubstring(n1,n2);
		if (!LCS.isEmpty()) {
//			System.out.println(LCS);
			index = n1.indexOf(LCS);
//			System.out.println(index);
			if (index == 0)
				return n2.substring(0,n2.length()-LCS.length())+ n1;
		}
		
		
		LCS = longestSubstring(n1,n2);
		if (!LCS.isEmpty()) {
//			System.out.println(LCS);
			index = n1.lastIndexOf(LCS);
			if(index+LCS.length()==n1.length() && !checkBrokenWord(LCS,n2))
				return n1 + n2.substring(LCS.length());
		}
		return null;
	}
	
	private static boolean checkBrokenWord(String LCS, String test){
		String[] tTokens = test.split(" ");
		String[] LCSTokens = LCS.split(" ");
		if(tTokens[LCSTokens.length-1].equals(LCSTokens[LCSTokens.length-1]))
			return false;
		return true;
	}
	
	private static String concatenateNgrams(String n1, String n2) {
		if (n1.indexOf(n2) > 0)
			return n1;
		if (n2.indexOf(n1) > 0)
			return n2;
		int index1, index2;
		String LCS = longestSubstring(n1,n2);
		if (!LCS.isEmpty() && LCS.split(" ").length == 3) {
//			System.out.println(LCS);
			index1 = n1.indexOf(LCS);
			index2 = n2.indexOf(LCS);
//			System.out.println(index);
			if (index1 == 0 || index2 == 0){
				if (index2 == 0 && index1+LCS.length() == n1.length())
					return n1.substring(0,index1) +  n2;
				else if (index1 == 0 && index2+LCS.length() == n2.length())
					return n2.substring(0,index2) +  n1;
			}
			return null;
		}
		return null;
	}
	
	public static String mergeNgrams1(HashSet<String> ngrams){
		int maxSize = 0;
		for(String ngram:ngrams)
			if(ngram.split(" ").length>maxSize)
				maxSize = ngram.split(" ").length;
		HashSet<String> ngramSet = new HashSet<String>();
		for(String ngram:ngrams)
			if(ngram.split(" ").length==maxSize)
				ngramSet.add(ngram);
		
		Queue<String> queue = new LinkedList<String>();
		for (String ngram:ngramSet){
			if(queue.isEmpty()){
				queue.add(ngram);
				continue;
			}
			int size = queue.size();
			int counter = 0;
			String elem = null;
			while(counter < size){
				elem = queue.poll();
				String concatenate = concatenateNgrams(ngram,elem);
				if (concatenate != null){
					queue.add(concatenate);
					break;
				}
				else
					queue.add(elem);
				counter++;
			}
			if (counter == size)
				queue.add(ngram);
			
		}
		return queue.toString();
		}
	
	
	public static String reMergeNgrams(HashSet<String> ngramSet){
		
		Queue<String> queue = new LinkedList<String>();
		for (String ngram:ngramSet){
			if(queue.isEmpty()){
				queue.add(ngram);
				continue;
			}
			int size = queue.size();
			int counter = 0;
			String elem = null;
			while(counter < size){
				elem = queue.poll();
				String concatenate = concatenateNgrams(ngram,elem);
				if (concatenate != null){
					queue.add(concatenate);
					break;
				}
				else
					queue.add(elem);
				counter++;
			}
			if (counter == size)
				queue.add(ngram);
			
		}
		return queue.toString();
		}
	
	
	public static String mergeNgrams(HashSet<String> ngrams)
	{
		String merge = mergeNgrams1(ngrams);
		return reMergeNgrams(StringUtils.convertStringToSet(merge));
	}
	
	
	public static void main(String[] args){
		HashSet<String> set = new HashSet<String>();
//		set.add("יוכל להפרד אסור להשמיט");
//		set.add("מכח שאומרים שיש נוצות");
		
		set.add("עצים ואין הנשמה יכולה");
		set.add("להפרד אסור להשמיט הכר");
		
		System.out.println(mergeNgrams1(set));
	}
}