package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import utils.StringUtils;

public class EvalCombPotential {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		int FoCandidatesNum = 2000;
		HashMap<String, HashSet<String>> FOmap = new HashMap<String, HashSet<String>>();
		String statDir = "C:\\SONewStatisticsFolder\\Best_Surface";
		File statFolder = new File(statDir);
		for(File f:statFolder.listFiles()){
			if(f.getName().endsWith("_Dice.txt")){
				String target_term = f.getName().substring(0, f.getName().indexOf("_Dice.txt"));
				HashSet<String> candidates = new HashSet<String>();
				int counter = 0;
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = reader.readLine();
				while (line != null && counter < FoCandidatesNum){
					candidates.add(line.split("\t")[0]);
					line = reader.readLine();
					counter ++;
				}
				reader.close();
				FOmap.put(target_term, candidates);
			}
		}
		
		String clustersFile = "C:\\SONewStatisticsFolder\\All_Surface_Surface_LIN\\clusters50_SUMSCORE_2\\clustersTop15RGSSO.txt";
		BufferedReader reader = new BufferedReader(new FileReader(clustersFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Documents and Settings\\HZ\\Desktop\\AnalJournalMorphology\\final\\CombPotential" + FoCandidatesNum + ".txt"));
		writer.write("target_term\tcounterExist\tcounterAppear\n");
		String line = reader.readLine();
		String target_term = null;
		int counterExist = 0, counterAppear = 0;
		int totalCounterExist = 0, totalCounterAppear = 0;
		while (line != null){
			String[] tokens = line.split("\t");
			if (tokens.length < 2){
				if (target_term != null){
					writer.write(target_term+"\t"+counterExist+"\t"+counterAppear+"\n");
					totalCounterExist += counterExist;
					totalCounterAppear += counterAppear;
					}
				target_term = tokens[0];
				counterExist = 0; 
				counterAppear = 0;
			}
			else {
				if (tokens[2].trim().equals("true")){
					counterExist ++;
					if(FOmap.containsKey(target_term)){
						HashSet<String> cluster = StringUtils.convertStringToSet(tokens[0].trim());
						boolean bFound = false;
						for(String s:cluster)
							if(FOmap.get(target_term).contains(s)){
								bFound = true;
								break;
							}
						if (bFound)
							counterAppear ++;
					}		
				}
			}
			line = reader.readLine();
		}
		// add the last target_term
		writer.write(target_term+"\t"+counterExist+"\t"+counterAppear+"\n");
		totalCounterExist += counterExist;
		totalCounterAppear += counterAppear;
		
		writer.write("\n\t"+totalCounterExist+"\t"+totalCounterAppear+"\n");
		writer.close();

	}
	
	public static void main2(String[] args) throws IOException {
		HashMap<String, HashSet<String>> FOmap = new HashMap<String, HashSet<String>>();
		String statFile = "C:\\SONewStatisticsFolder\\Best_Surface\\clusters50_SUMSCORE_2\\clustersTop15RGSFO.txt";
		BufferedReader reader = new BufferedReader(new FileReader(statFile));
		String target_term = null;
		HashSet<String> candidates = null;
		String line = reader.readLine();
		while (line != null){
			String[] tokens = line.split("\t");
			if (tokens.length < 2){
				if (target_term != null){
					FOmap.put(target_term, candidates);
				}
				target_term = tokens[0];
				candidates = new HashSet<String>();
			}
			else {
				candidates.addAll(StringUtils.convertStringToSet(tokens[0].trim()));
			}
			line = reader.readLine();
			
		}
		reader.close();
		// add the last target_term
		FOmap.put(target_term, candidates);
		
		String clustersFile = "C:\\SONewStatisticsFolder\\All_Surface_Surface_LIN\\clusters50_SUMSCORE_2\\clustersTop15RGSSO.txt";
		reader = new BufferedReader(new FileReader(clustersFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Documents and Settings\\HZ\\Desktop\\AnalJournalMorphology\\final\\CombPotential.orig" +  ".txt"));
		writer.write("target_term\tcounterExist\tcounterAppear\n");
		line = reader.readLine();
		target_term = null;
		int counterExist = 0, counterAppear = 0;
		int totalCounterExist = 0, totalCounterAppear = 0;
		while (line != null){
			String[] tokens = line.split("\t");
			if (tokens.length < 2){
				if (target_term != null){
					writer.write(target_term+"\t"+counterExist+"\t"+counterAppear+"\n");
					totalCounterExist += counterExist;
					totalCounterAppear += counterAppear;
					}
				target_term = tokens[0];
				counterExist = 0; 
				counterAppear = 0;
			}
			else {
				if (tokens[2].trim().equals("true")){
					counterExist ++;
					if(FOmap.containsKey(target_term)){
						HashSet<String> cluster = StringUtils.convertStringToSet(tokens[0].trim());
						boolean bFound = false;
						for(String s:cluster)
							if(FOmap.get(target_term).contains(s)){
								bFound = true;
								break;
							}
						if (bFound)
							counterAppear ++;
					}		
				}
			}
			line = reader.readLine();
		}
		// add the last target_term
		writer.write(target_term+"\t"+counterExist+"\t"+counterAppear+"\n");
		totalCounterExist += counterExist;
		totalCounterAppear += counterAppear;
		
		writer.write("\n\t"+totalCounterExist+"\t"+totalCounterAppear+"\n");
		writer.close();

	}


}
