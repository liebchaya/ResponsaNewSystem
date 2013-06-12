package iterativeQE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;

import fo.scorers.DiceScorer;
import fo.scorers.FreqScorer;
import fo.scorers.StatScorer;
import fo.similarity.FeatureVectorExtractor;
import fo.similarity.FeatureVectorExtractor2Measures;

import obj.Pair;
import representation.FeatureRepresentation;
import representation.JewishFeatureRepresentation;
import representation.JewishTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.FeatureRepresentation.FeatureType;
import representation.TargetTermRepresentation.TargetTermType;

import lowFreq.Wikitionary;

import utils.StringUtils;
import utils.TargetTerm2Id;

public class loadInitialStep {
	
	/*
	 * Export the first statistics extraction for judgement
	 */
	public static void ExportInitialData(File f, String outputFolder) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String target_term = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+target_term+".terms"), "CP1255"));
		String line = reader.readLine();
		int lineNum = 1;
		while(line != null) {
			writer.write(target_term+"\t"+line.split("\t")[0]+"\t"+line.split("\t")[1]+"\t"+"-99\t-88\n");
			line = reader.readLine();
			lineNum ++;
		}
		reader.close();
		writer.close();
	}
	
	public static void ExportRunData(File f, String outputFolder) throws IOException, SQLException{
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String target_term = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
//		String type = "modern";
//		if(f.getName().contains("old"))
//			type = "old";
//		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+target_term+"_"+type+".terms"), "CP1255"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+target_term+".terms"), "CP1255"));
		String line = reader.readLine();
		int lineNum = 1;
		LinkedList<String> groups = sql.SelectGroups(target_term);
		HashMap<String, Integer> lemmaSet = sql.Select(target_term);
		while(line != null) {
			int annotation = getAnnotation(lemmaSet,line.split("\t")[1]);
			if (annotation != -99){
//				if (annotation > 0)
//					writer.write("true\n");
//				else
//					writer.write("false\n");
				if(!groups.isEmpty()){
					writer.write(target_term+"\t"+line.split("\t")[0]+"\t"+line.split("\t")[1]+"\t"+annotation+"\t-88\t\t\t\t\t\t"+groups.getFirst()+"\n");
					groups.removeFirst();
				}
				else
					writer.write(target_term+"\t"+line.split("\t")[0]+"\t"+line.split("\t")[1]+"\t"+annotation+"\t-88\n");
			}
			else
				if(!groups.isEmpty()){
					writer.write(target_term+"\t"+line.split("\t")[0]+"\t"+line.split("\t")[1]+"\t"+"-99\t-88\t\t\t\t\t\t"+groups.getFirst()+"\n");
					groups.removeFirst();
				}
				else
					writer.write(target_term+"\t"+line.split("\t")[0]+"\t"+line.split("\t")[1]+"\t"+"-99\t-88\n");
			line = reader.readLine();
			lineNum ++;
		}
		reader.close();
		writer.close();
	}
	
	public static void ExportBaseline(File f, String outputFolder, int num) throws IOException, SQLException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "CP1255"));
		String target_term = f.getName().substring(0,f.getName().indexOf("."));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+target_term+".terms"), "CP1255"));
		String line = reader.readLine();
		int lineNum = 1;
		LinkedList<String> groups = sql.SelectGroups(target_term);
		HashMap<String, Integer> lemmaSet = sql.Select(target_term);
		while(line != null && lineNum < num) {
			if(line.trim().equals("###"))
			{
				writer.write(line + "\n");
				line = reader.readLine();
				continue;
			}
			String[] tokens = line.split("\t");
			System.out.println(line);
			if(Integer.parseInt(tokens[3]) != -99){
				writer.write(line + "\n");
				line = reader.readLine();
				lineNum ++;
				continue;
			}
			else {	
				int annotation = getAnnotation(lemmaSet,line.split("\t")[2]);
				if (annotation == -99){
					if(!groups.isEmpty()){
						writer.write(line+"\t\t\t\t\t\t"+groups.getFirst()+"\n");
						groups.removeFirst();
					}
					else
						writer.write(line + "\n");
				}
				else{
					if(!groups.isEmpty()){
						writer.write(tokens[0]+"\t"+tokens[1]+"\t"+tokens[2]+"\t"+annotation+"\t"+tokens[4]+"\t\t\t\t\t\t"+groups.getFirst()+"\n");
						groups.removeFirst();
					}
					else
						writer.write(tokens[0]+"\t"+tokens[1]+"\t"+tokens[2]+"\t"+annotation+"\t"+tokens[4]+"\n");
				}
				line = reader.readLine();
				lineNum ++;
			}
		}
		reader.close();
		writer.close();
	}
	
	public static Set<String> loadAnnotations(File f, int generation) throws ClassNotFoundException, SQLException, IOException{
		HashSet<String> expansions = new HashSet<String>();
		System.out.println(f.getName());
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "CP1255"));
		String line = reader.readLine();
		int lineNum = 1;
		String target_term = f.getName().substring(0,f.getName().indexOf("."));
		HashMap<String, Integer> lemmaSet = sql.Select(target_term);
		target_term = target_term.replace("_old", "");
		while(line != null && !line.trim().equals("###")) {
			System.out.println(line);
			int annotation = getAnnotation(lemmaSet,line.split("\t")[2]);
			if ((line.split("\t").length>1) && (annotation == -99)) {
				sql.Insert(line.split("\t")[0], line.split("\t")[1], line.split("\t")[2], target_term, generation, lineNum, Integer.parseInt(line.split("\t")[3]));
				if (Integer.parseInt(line.split("\t")[4]) > 0)
					expansions.add(line.split("\t")[5]);
				}
			line = reader.readLine();
			lineNum ++;
		}
		reader.close();
		return expansions;
	}
	
	public static void loadBaselineAnnotations(File f, int generation) throws ClassNotFoundException, SQLException, IOException{
		HashSet<String> expansions = new HashSet<String>();
		System.out.println(f.getName());
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "CP1255"));
		String line = reader.readLine();
		int lineNum = 1;
		String target_term = f.getName().substring(0,f.getName().indexOf("."));
//		HashMap<String, Integer> lemmaSet = sql.Select(target_term);
//		target_term = target_term.replace("_old", "");
		while(line != null && !line.trim().equals("%%%")) {
			System.out.println(line);
//			int annotation = getAnnotation(lemmaSet,line.split("\t")[2]);
			if ((line.split("\t").length>1)){// && (annotation == -99)) {
				sql.Insert(line.split("\t")[0], line.split("\t")[1], line.split("\t")[2], target_term, generation, lineNum, Integer.parseInt(line.split("\t")[3]));
//				if (Integer.parseInt(line.split("\t")[4]) > 0)
//					expansions.add(line.split("\t")[5]);
//				}
				lineNum ++;
			}
			line = reader.readLine();
		}
		reader.close();
//		return expansions;
	}
	
	private static int getAnnotation(HashMap<String, Integer> lemmaSet, String lemma){
		int annotation = -99;
		HashSet<String> lemmaInput = StringUtils.convertStringToSet(lemma);
		for(String l:lemmaInput){
			if (lemmaSet.containsKey(l)){
				annotation = lemmaSet.get(l);
				break;
			}
		}
		return annotation;
	}
	
	
		

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException{
		sql = new SQLAccess();
		String clustersFolder = "C:/ResponsaSys/output/step0/Surface_Surface/clusters200_SUMSCORE_2";
		String outputFolder = "C:/ResponsaSys/output/step0";
		File outputDir = new File(outputFolder+"/judgements30New");
//		outputDir.mkdir();
		
		String inputDir = "C:/ResponsaSys/input";
		String targetTermFile = "C:/ResponsaSys/input/targetTerms_orig.txt";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(targetTermFile));
		Wikitionary wiki = new Wikitionary(new File("C:/wikiRel"));
		
		// step 0
//		File folder = new File(clustersFolder);
//		for (File f:folder.listFiles())
//			loadInitialStep.ExportInitialData(f,outputDir.getAbsolutePath());
		// load step0 judgements
		
		/*for (File f:outputDir.listFiles()) {
			System.out.println("Loadding results for: " + f.getName());
			Set<String> expansions = loadInitialStep.loadAnnotations(f, 0);
			
			System.out.println("Checking results form wikitionary for: " + f.getName());
			InputStreamReader istream = new InputStreamReader(System.in) ;
	        BufferedReader bufRead = new BufferedReader(istream) ;
	        String targetTerm = f.getName().substring(0,f.getName().indexOf("."));
	        HashSet<String> wikiSet = wiki.getRelSet(targetTerm, true, 1);
	        expansions.addAll(wikiSet);
	        for(String expan:wikiSet){
		        System.out.println(expan + " is an expansion to " + targetTerm);
		        String result = bufRead.readLine();
		        if (result.equals("f"))
		        	expansions.remove(expan);
		        	
	        }
	        System.out.println(expansions);
	        targetTerm = targetTerm.replace("_old", "");
	        if (!expansions.isEmpty()){
	        	for(String exp:expansions)
	        		sql.InsertExpansions(targetTerm,exp,targetTerm,1);
	        }
		}*/
		
		// generate new input file
//		int generation = 3;
//		HashMap<Integer, Pair<String, String>> expansionsDetails = sql.SelectExpansions(generation);
//		BufferedWriter writer = new BufferedWriter(new FileWriter(inputDir+"/step"+generation+"New30All_orig.txt"));
//		for (int id:expansionsDetails.keySet())
//			writer.write(id+"\t"+expansionsDetails.get(id).value()+"\n");
//		writer.close();
		
		// merge generation results
//		int generation = 3;
//		HashMap<Integer, Pair<String, String>> expansionsDetails = sql.SelectExpansions(generation);
//		HashMap<Integer,ArrayList<Integer>> mergingMap = new HashMap<Integer, ArrayList<Integer>>();
//		for(int id:expansionsDetails.keySet()){
//			int targetTermId = TargetTerm2Id.getIntDesc(expansionsDetails.get(id).key());
//			if(mergingMap.containsKey(targetTermId))
//				mergingMap.get(targetTermId).add(id);
//			else {
//				ArrayList<Integer> idList = new ArrayList<Integer>();
//				idList.add(id);
//				mergingMap.put(targetTermId, idList);
//			}
//		}
//		System.out.println(mergingMap);
//		String stepInputFolder = "C:/ResponsaSys/output/step3New30All/Surface_Surface/clusters5000_SUMSCORE_2";
//		String stepOutputFolder = "C:/ResponsaSys/output/step3New30All/Surface_Surface/clusters5000_SUMSCORE_2_merged";
//		for (int id:mergingMap.keySet())
//			mergeFiles(id,mergingMap.get(id),stepInputFolder,stepOutputFolder);

//		BufferedReader reader = new BufferedReader(new FileReader("C:/ResponsaSys/input/targetTermsAdd_orig.txt"));
//		String line = reader.readLine();
//		HashSet<Integer> set = new HashSet<Integer>();
//		while(line!=null){
//			set.add(Integer.parseInt(line.split("\t")[0]));
//			line = reader.readLine();
//		}
//		reader.close();
		
			
//		String stepOutputFolder = "C:/ResponsaSys/output/step3New30All/Surface_Surface/clusters5000_SUMSCORE_2_merged";
//		File stepDir = new File(stepOutputFolder);
//		outputFolder = "C:/ResponsaSys/output/step3New30All";
//		outputDir = new File(outputFolder+"/judgements");
//		outputDir.mkdir();
//		for(File f:stepDir.listFiles()){
//			int id = Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_")));
////			if(set.contains(id))
//				loadInitialStep.ExportRunData(f,outputDir.getAbsolutePath());
//		}

//		int generation = 3;
//		outputFolder = "C:/ResponsaSys/output/step3New30All";
//		outputDir = new File(outputFolder+"/judgements");
//		for(File f:outputDir.listFiles()){
////			System.out.println(f.getName());
//			Set<String> expansions = loadInitialStep.loadAnnotations(f, generation);
//			System.out.println(expansions);
//			String targetTerm = f.getName().substring(0,f.getName().indexOf("."));
//	        targetTerm = targetTerm.replace("_old", "");
//	        if (!expansions.isEmpty()){
//	        	for(String exp:expansions)
//	        		sql.InsertExpansions(targetTerm,exp,targetTerm,3);
//	        }
//		}
			
		/*HashSet<Integer> set = new HashSet<Integer>();
		String stepOutputFolder = "C:/ResponsaSys/output/step0/Surface_Surface/clusters200_SUMSCORE_2";
		File stepDir = new File(stepOutputFolder);
		for(File f:stepDir.listFiles())
			set.add(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
		stepOutputFolder = "C:/ResponsaSys/output/step1/Surface_Surface/clusters200_SUMSCORE_2";
		stepDir = new File(stepOutputFolder);
		for(File f:stepDir.listFiles()){
			set.remove(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
		}
		for(int s:set)
			System.out.println(s+"\t"+TargetTerm2Id.getStrDesc(s));*/
		
//		String stepOutputFolder = "C:/ResponsaSys/output/step0/judgements30New";
//		File stepDir = new File(stepOutputFolder);
		outputFolder = "C:/ResponsaSys/output/baseline2";
		outputDir = new File(outputFolder+"/judgementsBaseline2");
//		outputDir.mkdir();
		for(File f:outputDir.listFiles()){
			loadInitialStep.loadBaselineAnnotations(f, 0);
//			String target_term = f.getName().substring(0,f.getName().indexOf("."));
//				LinkedList<String> groups = sql.SelectGroups(target_term);
//				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+target_term+".groups"), "CP1255"));
//				while(!groups.isEmpty()){
//					writer.write(groups.getFirst()+"\n");
//					groups.removeFirst();
//				}
//				writer.close();
		}
		
	}
	
	private static void mergeFiles(int targetTermId, ArrayList<Integer> idList, String inputFolder, String outpuFolder) throws IOException{
		HashSet<String> seenLemmas = new HashSet<String>();
		BufferedWriter writer = new BufferedWriter(new FileWriter(outpuFolder+ "/" + targetTermId +"_Dice.clusters"));
		ArrayList<BufferedReader> readersList = new ArrayList<BufferedReader>();
		for (int id:idList) {
			File idFile = new File(inputFolder+ "/" + id +"_Dice.clusters");
			if(idFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFolder+ "/" + id +"_Dice.clusters"));
				readersList.add(reader);
			}
		}
		
		int activeCounter = readersList.size();
		while (activeCounter > 0) {
			activeCounter = readersList.size();
			for(int i=0; i<readersList.size(); i++ ){
				String line = readersList.get(i).readLine();
				if(line == null)
					activeCounter --;
				else {
					String lemma = line.split("\t")[1];
					HashSet<String> lemmaSet = StringUtils.convertStringToSet(lemma);
					boolean bfound = false;
					for(String l:lemmaSet)
						if(seenLemmas.contains(l))
							bfound = true;
						else
							seenLemmas.add(l);
					if (!bfound)
						writer.write(line+"\n");
				}
			}
		}
	    for(BufferedReader r:readersList)
	    	r.close();
	    writer.close();
	}
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	public static void mainPrev(String[] args) throws SQLException, IOException, ClassNotFoundException, ParseException {
		String targetTerm = "המתת חסד";
		
		//First: extract statistics for the original target_term
		String indexName = "index0";
		String mainOutputDir = "";
		String targetTermFile = "C:\\ResponsaSys\\input\\targetTermsAll_orig.txt";
		String morphTargetTermFile = "C:\\ResponsaSys\\input\\targetTermsAll_morph.txt";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(targetTermFile));
		extractStatistics(morphTargetTermFile, indexName, mainOutputDir);
//		Clustering Generation
//		GroupMWEclustering
		
		// Export original results for annotation
//		loadInitialStep.ExportInitialData(new File("C:\\ResponsaSys\\output\\baseline\\Surface_Surface\\clusters50_SUMSCORE_2\\5_NoModern.clusters"));
		
//		Set<String> expansions = loadInitialStep.loadAnnotations(new File("C:\\export.terms"), 0);
		Wikitionary wiki = new Wikitionary(new File("C:\\wikiRel"));
		InputStreamReader istream = new InputStreamReader(System.in) ;
        BufferedReader bufRead = new BufferedReader(istream) ;
        HashSet<String> wikiSet = wiki.getRelSet(targetTerm, true, 1);
        for(String expan:wikiSet){
	        System.out.println(expan + " is an expansion to " + targetTerm);
	        String result = bufRead.readLine();
	        if (result.equals("false"))
	        	wikiSet.remove(expan);
	        	
        }
        System.out.println(wikiSet);
		
	}
	
	public static void extractStatistics(String termsFile, String index, String outputDir) throws IOException, ParseException{
		TargetTermRepresentation targetRp = new JewishTargetTermRepresentation(TargetTermType.Surface,termsFile,index);
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		DiceScorer scorer  = new DiceScorer();
		FeatureRepresentation featureRp = new JewishFeatureRepresentation(FeatureType.Surface,index);
		FeatureVectorExtractor vectorExtractor = new FeatureVectorExtractor(scorer,featureRp,false);
		vectorExtractor.extractTargetTermVectors(targetDocs, TargetTermType.Surface, new File(outputDir));

	}
	
	private static SQLAccess sql = null;

}
