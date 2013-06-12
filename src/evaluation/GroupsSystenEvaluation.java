package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import obj.Judgement;
import obj.TermsJudgements;

import utils.StringUtils;
import utils.TargetTerm2Id;


public class GroupsSystenEvaluation {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MorphLemmatizerException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		
		
		/**/    String targetTermFile = "C:\\ResponsaSys\\input\\targetTermsAll_orig.txt";
		/**/    String judgementsDir = "C:\\ResponsaSys\\judgements";
		/**/	File outputDir = new File("C:\\ResponsaSys\\output");
		/**/	int clustersNum = 10;
		
		/**/ 	String clusteringType = "clusters50_SUMSCORE_2";
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(targetTermFile));
		boolean bMissing = false;
		// Export un-judged terms
		for(File conf: outputDir.listFiles()) {
			if (conf.isDirectory()) {
				System.out.println(conf.getAbsolutePath());
				
				File clsDir = new File(conf.getAbsoluteFile()+ "\\Surface_Surface\\" + clusteringType);
				String sep = "_";
				for (File f:clsDir.listFiles()) {
					if (f.getName().endsWith(".clustersNew")) {
						if (!f.getName().contains(sep))
							sep = ".";
						String targetTerm = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf(sep))));
						TermsJudgements termsJudgements = new TermsJudgements(targetTerm,judgementsDir);
						int noTermsJudgesCounter = 0;
						noTermsJudgesCounter = termsJudgements.append(loadClusterAsStringFromFile(f,clustersNum));
						if (noTermsJudgesCounter > 0)
							bMissing = true;
						termsJudgements.exportJFile();
					}
				}
			}
		}
		if (bMissing){
			System.out.println("Cannot complete evaluation, there are still missing judgements");
			System.exit(0);
		}
		
		
		Evaluation<String> clusterEval = null;
		
		for(File conf: outputDir.listFiles()) {
			if (conf.isDirectory()) {
				System.out.println(conf.getAbsolutePath());
				BufferedWriter evalWriter = new BufferedWriter(new FileWriter(conf.getAbsolutePath() + "\\Surface_Surface\\" + clusteringType + "\\eval.txt"));
				
				File clsDir = new File(conf.getAbsoluteFile()+ "\\Surface_Surface\\" + clusteringType);
				String sep = "_";
				for (File f:clsDir.listFiles()) {
					if (f.getName().endsWith(".clustersNew")) {
					if (!f.getName().contains(sep))
						sep = ".";
					String targetTerm = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf(sep))));
					TermsJudgements termsJudgements = new TermsJudgements(targetTerm,judgementsDir);
					
					//objects for grouped results evaluation: contains numeric judgments as strings
					HashSet<String> relevantTermJudges = termsJudgements.getStrLstRelJudeges();//=getRelevant(relevantTerms,termsJudgements);
					HashSet<String> nonRelevantTermJudges = termsJudgements.getStrLstNonRelJudeges();//=getNonRelevant(nonRelevantTerms,termsJudgements);
					LinkedList<String> termsList = loadClusterAsStringFromFile(f,clustersNum);
					LinkedList<String> groupList = getGroupList(termsList,termsJudgements,targetTerm);//list of results' numeric judgments as string, with no duplications

					if (!missingJudgements(groupList,relevantTermJudges,nonRelevantTermJudges)){
						clusterEval = new Evaluation<String>(groupList,relevantTermJudges,"terms");//for grouped results evaluation
						evalWriter.write(targetTerm+"\tgroupJudges\t"+clusterEval.getShortEvalString()+"\t"+getJudgementsPosGroupsNum(termsList, termsJudgements)+"\t\t"+getGjudgements(termsList,termsJudgements)+"\t"+getJudgementsPosGroups(termsList, termsJudgements)/*+"\t"+getTermsJudgementsString(expTermsList,termsJudgements)*/+"\n");

					}
					else {
						System.out.println("no terms evaluation for query : "+targetTerm+" , reason: terms judgements are missing");
						evalWriter.write("\t"+"no evaluation:terms judgements are missing\t\t\t\t\t"+getJudgementsPosGroups(termsList, termsJudgements)+"\t"+getTermsJudgementsString(termsList,termsJudgements)+"\n");
					}
					}
				}
				evalWriter.close();
				}
		}
	}
		
		
	public static String getTermsJudgementsString(LinkedList<String> expTermsList,
			TermsJudgements termsJudgements) {

		String str="";
		for (String t :expTermsList){
			str=str+t+"("+termsJudgements.getJudge(t)+")"+"\t";
		}
		return str;
	}
	
	public static LinkedList<String> loadClusterAsStringFromFile(File clustersFile, int clustersNum) throws IOException{
		LinkedList<String> clusters = new LinkedList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(clustersFile));
		String line = reader.readLine();
		int counter = 0;
		while (line != null && counter < clustersNum) {
			String [] tokens = line.split("\t");
			clusters.add(tokens[0]);
			line = reader.readLine();
			counter++;
		}
		reader.close();
		return clusters;
	}
	
	public static LinkedList<String> getGroupList(LinkedList<String> expTermsList,
			TermsJudgements termsJudgements,String query) {
	
		LinkedList<String> gList = new LinkedList<String>();
		for (String t: expTermsList){
			String text = t;
			Judgement j = new Judgement(query,text);
			if (termsJudgements.getJudgements().contains(j)){
				int judge = termsJudgements.getJudge(text);
				if (!gList.contains(String.valueOf(judge)))
					gList.add(String.valueOf(judge));
			}
		}
		return gList;
	}
	
	public static boolean missingJudgements(LinkedList<String> list, 
			HashSet<String> relevantSet,
			HashSet<String> nonRelevantSet/*,
			HashSet<String> doubleRelevantTerms*/) {
		boolean missing = false;
		HashSet<String> union = new HashSet<String>(relevantSet);
		union.addAll(nonRelevantSet);
//24.05.10
//		union.addAll(doubleRelevantTerms);
		for (String str:list){
			if (!union.contains(StringUtils.fixAnnotatedTerm(str))){
				System.out.println("\tno current judgement for:\t"+str);
				missing = true;
			}
		}
		return missing;
	}

/**
 * Chaya 5/9/12
 * @param expTermsList
 * @param termsJudgements
 * @return
 */
public static int getJudgementsPosGroupsNum(LinkedList<String> expTermsList,
		TermsJudgements termsJudgements) {
	int num = 0;
	HashSet<Integer> groupsSet=new HashSet<Integer>();
	for (String t :expTermsList){
		int g = termsJudgements.getJudge(t);
		if (g>0)
			if (!groupsSet.contains(g)){
				groupsSet.add(g);
				num++;
			}
	}
	return num;
}

public static String getJudgementsPosGroups(LinkedList<String> expTermsList,
		TermsJudgements termsJudgements) {

	String str="";
	for (String t :expTermsList){
		int g = termsJudgements.getJudge(t);
		if (g>0)
			if (!str.contains(Integer.toString(g)+"\t"))
				str=str+termsJudgements.getJudge(t)+"\t";
	}
	return str;
}

public static String getGjudgements(LinkedList<String> expTermsList,
		TermsJudgements tj) {
String str = "";
for (String t: expTermsList){
	str=str+Integer.toString(tj.getJudge(t))+"\t";
}
	return str;
}

}
