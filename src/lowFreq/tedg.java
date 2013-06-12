package lowFreq;

public class tedg {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    String s1 = "\"ו\"\"אשרי\"".replaceAll("\\p{Punct}|\\d","");  
	    System.out.println("s1=:"+s1);  
	}

}
