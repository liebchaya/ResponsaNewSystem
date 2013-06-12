package clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import utils.StringUtils;

public class GroupMweClusters {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File inputFolder = new File("/home/ir/liebesc/ResponsaSys/output/oldExpShmulik100MWEBaseline/Surface_Surface/clusters50_SUMSCORE_2/");
//		File inputFolder = new File("C:/ResponsaSys/output/qeWikiAfterWiki/Surface_Surface/clusters50_SUMSCORE_2/");
		int minClusterNum = Integer.MAX_VALUE;
		for (File f:inputFolder.listFiles()){
			if (f.getAbsolutePath().endsWith(".clusters")) {
				ArrayList<Double> scores = new ArrayList<Double>();
				ArrayList<HashSet<String>> terms = new ArrayList<HashSet<String>>();
				ArrayList<HashSet<String>> lemmas = new ArrayList<HashSet<String>>();
				BufferedReader reader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter(f.getAbsolutePath().replace(".clusters", ".clustersNew")));
				String line = reader.readLine();
				while (line != null){
					String[] tokens = line.split("\t");
					HashSet<String> input = StringUtils.convertStringToSet(tokens[0]);
					HashSet<String> lemmaInput = StringUtils.convertStringToSet(tokens[1]);
					
					boolean found = false;
					for(int i=0; i<terms.size();i++)
						if (containOverlapMWE(input, terms.get(i))){
							terms.get(i).addAll(input);
							lemmas.get(i).addAll(lemmaInput);
							found = true;
							break;
						}
					if (!found) {
						terms.add(input);
						lemmas.add(lemmaInput);
						scores.add(Double.parseDouble(tokens[2]));
					}	
					line = reader.readLine();
				}
				reader.close();
				if (terms.size()<minClusterNum)
					minClusterNum =terms.size();
				for(int i=0; i<terms.size();i++){
					boolean skip = false;
					for (String t:terms.get(i)){
						if (t.contains("יש מי")||t.contains("מי שכתב")||t.contains("מי שסבור")||t.contains("הפוסקים")||m_termSet.contains(t)||isLenOne(t))
							skip = true;
					}
					if (!skip)
						writer.write(StringUtils.convertSetToString(terms.get(i))+"\t" +StringUtils.convertSetToString(lemmas.get(i)) + "\t" + scores.get(i) + "\n");
				}
				writer.close();
			}
		}
		System.out.println("Minimum cluster number : " +minClusterNum);
		
					
		}
	
	private static boolean containOverlapMWE(HashSet<String> input, HashSet<String> termSet){
		for(String s1:input){
			for(String s2:termSet){
				if (isOverlap(s1,s2))
					return true;
				
			}
		}
		return false;
	}
	
	private static boolean isOverlap(String s1, String s2){
		String[] tokens1 = s1.split(" ");
		String[] tokens2 = s2.split(" ");
		if (tokens1.length==tokens2.length){
			if (tokens1.length > 2)
				for(int i=0; i<tokens1.length-2; i++){
					for(int j=0; j< tokens2.length-2; j++)
						if(tokens1[i].equals(tokens2[j]) && tokens1[i+1].equals(tokens2[j+1]) && tokens1[i+2].equals(tokens2[j+2]))
							return true;
				}
		}
		String largerS = s1;
		String smallerS = s2;

		if(tokens2.length>tokens1.length){
			largerS = s2;
			smallerS = s1;
		}
		int largerLen = largerS.split(" ").length;
		int smallerLen = smallerS.split(" ").length;
		
		if(largerLen==smallerLen+1)
			if(largerS.indexOf(smallerS)!= -1)
				return true;
			
		
//		for(int i=0; i<tokens1.length-1; i++){
//			for(int j=0; j< tokens2.length-1; j++)
//				if(tokens1[i].equals(tokens2[j]) && tokens1[i+1].equals(tokens2[j+1]))
//					return true;
//		}
		return false;
			
	}
		
	private static HashSet<String> m_termSet = new HashSet(Arrays.asList(new String[] { "סי'", "סי","סעי'", "ע\"ש", "ע\"ע" ,"שו\"ת", "עי'", "ב\"מ", "תוס'", "צ\"ע", "או\"ח"}));
	
	private static boolean isLenOne(String s){
		String[] tokens = s.split(" ");
		for(String word:tokens) {
			String candTerm = word.replaceAll("\\p{Punct}|\\d","");
			if(candTerm.length() < 2)
				return true;
		}
		return false;
	}

	}


