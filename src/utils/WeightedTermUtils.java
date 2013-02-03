package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;

import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;

import obj.Term;
import obj.WeightedTerm;

public class WeightedTermUtils {
	
	 public static LinkedList<String> convertWTList2StringList(LinkedList<WeightedTerm> expTermsList){
			LinkedList<String> terms = new LinkedList<String>();
			for (WeightedTerm wt: expTermsList){
				terms.add(wt.getValue());
			}
			return terms;
		}
	 
	 public static LinkedList<WeightedTerm> loadWTFromFile(File statFile, int termsNum, String confName) throws IOException, MorphLemmatizerException {
		 	LinkedList<WeightedTerm> wtList = new LinkedList<WeightedTerm>();
			BufferedReader reader = new BufferedReader(new FileReader(statFile));
			String line = reader.readLine();
			int termsCounter = 0;
			String targetTerm = statFile.getName().substring(0,statFile.getName().indexOf("_")).trim();
			Set<String> AllLemmas = MorphLemmatizer.getAllPossibleLemmas(targetTerm);
	    	AllLemmas.add(targetTerm);
	    	Set<String> BestLemma = MorphLemmatizer.getMostProbableLemma(targetTerm);
	    	BestLemma.add(targetTerm);
			while (line!=null && termsCounter < termsNum){
				String term = line.split("\t")[0].trim();
				double weight = Double.valueOf( line.split("\t")[1].trim());
				term = term.replaceAll("[-+*/=](?![^(]*\\))","");
				if(line.trim().equals("")){
	    			line = reader.readLine();
	    			continue;
	    		}
	    		if(term.length() < 2 || StringUtils.checkIfNumber(term)){
	    			line = reader.readLine();
	    			continue;
	    		}
	    		if(confName.split("_")[0].equals("Surface")){
		    		if(term.equals(targetTerm)){
		    			line = reader.readLine(); 
		    			continue;
		    		}
	    		}
	    		else if(confName.split("_")[0].equals("All")){
		    		if(AllLemmas.contains(term)){
		    			line = reader.readLine(); 
		    			continue;
		    		}
	    		}
	    		else if(confName.split("_")[0].equals("Best")){
		    		if(BestLemma.contains(term)){
		    			line = reader.readLine(); 
		    			continue;
		    		}
	    		}
				wtList.add(new WeightedTerm(new Term(term),weight));
				line = reader.readLine();
				termsCounter ++;
			}
			reader.close();
			return wtList;
		}
}
