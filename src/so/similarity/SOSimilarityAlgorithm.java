package so.similarity;


import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;


import com.aliasi.util.BoundedPriorityQueue;


import obj.DoubleContainer;
import obj.DoubleMap;
import obj.Pair;
import so.DistSimRule;
import so.DistSimRuleComparator;

/**
 * Input: Two matrices A and B of feature vectors. 
 * ## Build an inverted index for A (optimization for data sparseness) 
 * AA = an empty hash-table
 * for i (1..n): 
 * 		F2[i] = f2(A[i]) ## cache values of f2(x)
 * 		for k in non-zero features of A[i]:
 * 			if k not in AA: 
 * 				AA[k] = empty-set 
 * 		## append <vector-id,feature-value> pairs to the set of non-zero values for feature k
 * 				AA[k].append( (i,A[i,k]) ) 
 * ## Process the elements of B 
 * for b in B:
 * 		F1 = {} ## the set of Ai that have nonzero similarity with b 
 * 		for k in non-zero features of b:
 * 			for i in AA[k]: 
 * 				if i not in sim:  sim[i] = 0 
 * 				F1[i] += f1(AA[k][i], b[k]) 
 * 		F3 = f3(b) 
 * 		for i in sim: 
 * 			print i, b, f0( F1[i], F2[i], F3)
 * Output: A matrix containing the similarity between all elements in A and in B.
 * 
 * @author Chaya Liebeskind
 * 
 */
public class SOSimilarityAlgorithm {
	
	/**
	 * Generate inverted index from a feature vector file
	 * @param vectorsFile
	 * @return
	 * @throws IOException
	 */
	private TIntObjectMap<List<Pair<Integer,Float>>> generateInvertedIndex(File vectorsFile) throws IOException {

		TIntObjectMap<List<Pair<Integer,Float>>> feature2ElementListMap = new TIntObjectHashMap<List<Pair<Integer,Float>>>();

		BufferedReader reader = new BufferedReader(new FileReader(vectorsFile));
		String line;
		String[] tokens;
		int i = 0;
		while((line=reader.readLine())!=null) {

			tokens = line.split("\t");
			Integer elementId = Integer.parseInt(tokens[0]);
			Integer featureId = Integer.parseInt(tokens[2]);
			Float pmi = Float.parseFloat(tokens[3]);

			if(feature2ElementListMap.get(featureId)==null) {
				List<Pair<Integer,Float>> elementPmiList = new LinkedList<Pair<Integer,Float>>();
				elementPmiList.add(new Pair<Integer, Float>(elementId,pmi));
				feature2ElementListMap.put(featureId, elementPmiList);
			}
			else 
				feature2ElementListMap.get(featureId).add(new Pair<Integer, Float>(elementId, pmi));
			i++;
			if(i % 1000000==0)
				System.out.println("Lines: " + i);
		}
		reader.close();
		System.out.println("Done, uploaded");
		return feature2ElementListMap;
	}
	
	/**
	 * Compute vector normalization
	 * @param vectorFile
	 * @throws Exception
	 */
	public void computeElementNormalizer(File vectorFile) throws Exception {
			
			DoubleMap<Integer> elementNorm = new DoubleMap<Integer>();
			
			BufferedReader reader = new BufferedReader(new FileReader(vectorFile));
			String line;
	
			int i = 0;
			while((line=reader.readLine())!=null) {
				String[] tokens = line.split("\t");
				elementNorm.inc(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[3]));
				i++;
				if (i % 1000000 ==0)
					System.out.println("lines: " + i);
			}
			reader.close();
			System.out.println("Finish loading elements");
			
			String vectorFileString = vectorFile.getAbsolutePath();
			File normFile = new File(vectorFileString.substring(0,vectorFileString.lastIndexOf("."))+".norm");
			PrintWriter writer = new PrintWriter(new FileOutputStream(normFile));
			for(Integer elem:elementNorm.keySet()) {
				writer.println(elem +"\t"+"D"+"\t"+elementNorm.getValue(elem));
			}
			writer.close();
		}
	/**
	 * Compute rule scores
	 * @param targetVectorsFile
	 * @param vectorsFile
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public void computeRuleSimilarityScores(File targetVectorsFile, File vectorsFile) throws IOException, NumberFormatException {

		//upload the element norm map
		TIntDoubleHashMap entailedElement2NormMap = new TIntDoubleHashMap();
		TIntDoubleHashMap entailingElement2NormMap = new TIntDoubleHashMap();
		
		String vectorFileString = targetVectorsFile.getAbsolutePath();
		File normFile = new File(vectorFileString.substring(0,vectorFileString.lastIndexOf("."))+".norm");
		BufferedReader reader = new BufferedReader(new FileReader(normFile));
		String line;
		System.out.println("Uploading target terms norm:");
		while((line=reader.readLine())!=null) {

			String[] tokens = line.split("\t");
			entailedElement2NormMap.put(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[2]));		
		}
		reader.close();
		
		vectorFileString = vectorsFile.getAbsolutePath();
		normFile = new File(vectorFileString.substring(0,vectorFileString.lastIndexOf("."))+".norm");
		reader = new BufferedReader(new FileReader(normFile));
		System.out.println("Uploading elements norm:");
		while((line=reader.readLine())!=null) {

			String[] tokens = line.split("\t");
			entailingElement2NormMap.put(Integer.parseInt(tokens[0]), Double.parseDouble(tokens[2]));		
		}
		reader.close();
		System.out.println("Done, uploaded " + entailingElement2NormMap.size() 
				+ " elements");
		//inverted index

		System.out.println("Uploading inverted index");
		TIntObjectMap<List<Pair<Integer,Float>>> invertedIndex = generateInvertedIndex(vectorsFile);
		System.out.println("Done");
		//compute rule scores
		computeRuleScores(invertedIndex,entailedElement2NormMap,entailingElement2NormMap,targetVectorsFile,vectorsFile);
	}

	/**
	 * Compute rule scores after generating an inverted index an loading normalization data
	 * @param invertedIndex
	 * @param entailedElement2NormMap
	 * @param entailingElement2NormMap
	 * @param targetVectorsFile
	 * @param vectorsFile
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void computeRuleScores(TIntObjectMap<List<Pair<Integer,Float>>> invertedIndex,
			TIntDoubleHashMap entailedElement2NormMap, TIntDoubleHashMap entailingElement2NormMap, File targetVectorsFile, File vectorsFile) throws NumberFormatException, IOException {

		BufferedReader reader= new BufferedReader(new FileReader(targetVectorsFile));
		String vectorFileString = vectorsFile.getAbsolutePath();
		File rulesFile = new File(vectorFileString.substring(0,vectorFileString.lastIndexOf("."))+".rules");
		PrintWriter writer = new PrintWriter(new FileOutputStream(rulesFile));
		String line;

		Integer currEntailedElementId=null;
		TIntObjectMap<Pair<DoubleContainer,DoubleContainer>> entailingElements = new TIntObjectHashMap<Pair<DoubleContainer,DoubleContainer>>();				
		int i = 0;
		int missingFeaturesCount = 0;
		while((line=reader.readLine())!=null) {

			String[] tokens = line.split("\t");
			Integer entailedElement = Integer.parseInt(tokens[0]);
			Integer featureId = Integer.parseInt(tokens[2]);
			Double entailedPmi = Double.parseDouble(tokens[3]);

			//if there are no features for this element - then it is not similar to any other and we can move on
			if(entailedElement2NormMap.get(entailedElement)==0.0)
				continue;

			if(currEntailedElementId!=null && !currEntailedElementId.equals(entailedElement)) {
				i++;
				if (i % 1000 ==0) {
					System.out.println("Entailed element Id: " + entailedElement+ ". Number of entailing elements: " + entailingElements.size());
					System.out.println("Number of elements gone through: " + i);
				}
				writeEntailedElementScores(writer,currEntailedElementId,entailingElements,entailedElement2NormMap,entailingElement2NormMap);
			}

			List<Pair<Integer,Float>> elementPmiList = invertedIndex.get(featureId);
			if(elementPmiList == null){
				missingFeaturesCount ++;
				System.out.println("line: " + i + " " + tokens[2]);
				System.out.println("feature id: " + featureId);
				continue;
			}

			for(Pair<Integer,Float> elementPmiPair:elementPmiList) {
//				if(!entailedElement.equals(elementPmiPair.key())) {

					int entailingId = elementPmiPair.key();
					double entailingPmi = elementPmiPair.value();
					double linScore = entailedPmi + entailingPmi;
					double coverScore = entailingPmi;

					Pair<DoubleContainer, DoubleContainer>  scoresMap = entailingElements.get(entailingId);
					if(scoresMap==null) {
						scoresMap = new Pair<DoubleContainer, DoubleContainer>(new DoubleContainer(linScore),new DoubleContainer(coverScore));
						entailingElements.put(entailingId, scoresMap);
					}
					else {
						scoresMap.key().add(linScore);
						scoresMap.value().add(coverScore);
						}

//				}
			}
			currEntailedElementId=entailedElement;
		}
		writeEntailedElementScores(writer,currEntailedElementId,entailingElements,entailedElement2NormMap,entailingElement2NormMap);
		reader.close();
		writer.close();
		System.out.println("Num of missing features: " +missingFeaturesCount);
	}

	/**
	 * Write top lin and balanced inclusion (cover) scores to a file
	 * @param writer
	 * @param entailedElementId
	 * @param entailingElements
	 * @param entailedElement2NormMap
	 * @param entailingElement2NormMap
	 */
	private void writeEntailedElementScores(PrintWriter writer, Integer entailedElementId, 
			TIntObjectMap<Pair<DoubleContainer,DoubleContainer>> entailingElements, TIntDoubleHashMap entailedElement2NormMap, TIntDoubleHashMap entailingElement2NormMap) {

		BoundedPriorityQueue<DistSimRule> linScores = new BoundedPriorityQueue<DistSimRule>(new DistSimRuleComparator(), m_maxRulesPerElement);
		BoundedPriorityQueue<DistSimRule> coverScores = new BoundedPriorityQueue<DistSimRule>(new DistSimRuleComparator(), m_maxRulesPerElement);
		
		double entailedElementNorm = entailedElement2NormMap.get(entailedElementId);

		TIntIterator iter = entailingElements.keySet().iterator();
		while(iter.hasNext()) {
			Integer entailingElementId = iter.next();

			//if there are no features for this element - then it is not similar to any other and we can move on
			if(entailingElement2NormMap.get(entailingElementId)==0.0)
				continue;

			double entailingElementNorm = entailingElement2NormMap.get(entailingElementId);

			Pair<DoubleContainer,DoubleContainer> scores = entailingElements.get(entailingElementId);

			Double linNominator = scores.key().value();
			Double coverNominator = scores.value().value();

			double linScore = linNominator / (entailedElementNorm+entailingElementNorm);
			double coverScore = coverNominator / entailingElementNorm;
			coverScore = Math.sqrt(linScore*coverScore);

			if(linScore>0.0){
					linScores.offer(new DistSimRule(entailedElementId, entailingElementId, linScore));
			}
			
			if(coverScore>0.0){
					coverScores.offer(new DistSimRule(entailedElementId, entailingElementId, coverScore));
			}
		}
		while(!linScores.isEmpty()) {
			DistSimRule linRule = linScores.poll();
			writer.println("LIN\t"+linRule);
		}
		while(!coverScores.isEmpty()) {
			DistSimRule coverRule = coverScores.poll();
			writer.println("COVER\t"+coverRule);
		}
		
		//we are done with the left element so we clear rightElements
		entailingElements.clear();
	}
	
	public void mergeRules(File vectorsDir, int maxVectorLen) throws NumberFormatException, IOException {
		TIntObjectHashMap<BoundedPriorityQueue<Pair<Integer, Double>>> linScores = new TIntObjectHashMap<BoundedPriorityQueue<Pair<Integer,Double>>>();
		TIntObjectHashMap<BoundedPriorityQueue<Pair<Integer, Double>>> balScores = new TIntObjectHashMap<BoundedPriorityQueue<Pair<Integer,Double>>>();
		String line;
		BufferedReader reader = null;
		
		for(String fileName:vectorsDir.list()){
			if(fileName.endsWith(".rules")&& fileName.contains("Trunc"+maxVectorLen)) {
				System.out.println("Reading: " + fileName);
				reader = new BufferedReader(new FileReader(vectorsDir.getAbsolutePath()+"/"+fileName));
				while((line=reader.readLine())!=null) {
		
					String[] tokens = line.split("\t");
					String scoreType = tokens[0];
					Integer entailedElement = Integer.parseInt(tokens[1]);
					Integer entailingElement = Integer.parseInt(tokens[2]);
					Double score = Double.parseDouble(tokens[3]);
					if(scoreType.equals("LIN")){
						if(linScores.contains(entailedElement))
							linScores.get(entailedElement).offer(new Pair<Integer, Double>(entailingElement,score));
						else {
							linScores.put(entailedElement,new BoundedPriorityQueue<Pair<Integer,Double>>(new obj.PairComparator(), m_maxRulesPerElement));
							linScores.get(entailedElement).offer(new Pair<Integer, Double>(entailingElement,score));
						}
					}
					
					else if(scoreType.equals("COVER"))
						if(balScores.contains(entailedElement))
							balScores.get(entailedElement).offer(new Pair<Integer, Double>(entailingElement,score));
						else {
							balScores.put(entailedElement,new BoundedPriorityQueue<Pair<Integer,Double>>(new obj.PairComparator(), m_maxRulesPerElement));
							balScores.get(entailedElement).offer(new Pair<Integer, Double>(entailingElement,score));
						}
			}
			reader.close();
			}
		}
		
		System.out.println("Uploading elements");
		TIntObjectMap<String> id2elementDesc = new TIntObjectHashMap<String>();
		TIntObjectMap<String> id2targetElementDesc = new TIntObjectHashMap<String>();
		
		reader = new BufferedReader(new FileReader(vectorsDir.getAbsolutePath()+"/elements.txt"));
		while((line=reader.readLine())!=null) {
			id2elementDesc.put(Integer.parseInt(line.split("\t")[1]), line.split("\t")[0]);
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader(vectorsDir.getAbsolutePath()+"/targetElements.txt"));
		while((line=reader.readLine())!=null) {
			id2targetElementDesc.put(Integer.parseInt(line.split("\t")[1]), line.split("\t")[0]);
		}
		reader.close();

		PrintWriter writer = new PrintWriter(new FileOutputStream(vectorsDir.getAbsolutePath()+"/linRules.txt"));
		
		TIntIterator iter =linScores.keySet().iterator();
		while(iter.hasNext()) {
			Integer entailedElementId = iter.next();
			String entailedStr = id2targetElementDesc.get(entailedElementId);
//			if (entailedStr.contains("_")) {
//				String confDir = entailedStr.substring(entailedStr.indexOf("_")+1);
//				File resultsDir = new File(f.getAbsolutePath()+"/" + confDir);
//				if(!resultsDir.exists())
//					resultsDir.mkdir();
//				writer = new PrintWriter(new FileOutputStream(resultsDir.getAbsolutePath()+"/"+entailedStr.substring(0,entailedStr.indexOf("_"))));
//				writer = new PrintWriter(new FileOutputStream(resultsDir.getAbsolutePath()+"/"+entailedElementId));
//			}
//			else
//				writer = new PrintWriter(new FileOutputStream(f.getAbsoluteFile()+"/"+entailedStr));
//				writer = new PrintWriter(new FileOutputStream(f.getAbsoluteFile()+"/"+entailedElementId));
			
			while(!linScores.get(entailedElementId).isEmpty()) {
				Pair<Integer,Double> linRule = linScores.get(entailedElementId).poll();
				writer.println(entailedStr+ "\t" + id2elementDesc.get(linRule.key())+ "\t" + linRule.value());
			}
		}
		if (writer != null)
			writer.close();
		writer = new PrintWriter(new FileOutputStream(vectorsDir.getAbsolutePath()+"/balRules.txt"));
		iter =balScores.keySet().iterator();
		while(iter.hasNext()) {
			Integer entailedElementId = iter.next();
			String entailedStr = id2targetElementDesc.get(entailedElementId);
//			if (entailedStr.contains("_")) {
//				String confDir = entailedStr.substring(entailedStr.indexOf("_")+1);
//				File resultsDir = new File(f.getAbsolutePath()+"/" + confDir);
//				if(!resultsDir.exists())
//					resultsDir.mkdir();
//				writer = new PrintWriter(new FileOutputStream(resultsDir.getAbsolutePath()+"/"+entailedStr.substring(0,entailedStr.indexOf("_"))));
//				writer = new PrintWriter(new FileOutputStream(resultsDir.getAbsolutePath()+"/"+entailedElementId));
//			}
//			else
//				writer = new PrintWriter(new FileOutputStream(f.getAbsoluteFile()+"/"+entailedStr));
//				writer = new PrintWriter(new FileOutputStream(f.getAbsoluteFile()+"/"+entailedElementId));
			while(!balScores.get(entailedElementId).isEmpty()) {
				Pair<Integer,Double> balRule = balScores.get(entailedElementId).poll();
				writer.println(entailedStr+ "\t" + id2elementDesc.get(balRule.key())+ "\t" + balRule.value());
			}
		}
		writer.close();

	}
	

	private int m_maxRulesPerElement = 200;

}
