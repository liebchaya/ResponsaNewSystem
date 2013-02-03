package so;

import fo.scorers.StatScorer;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import representation.FeatureRepresentation;
import representation.SORelatedTermRepresentation;
import representation.TargetTermRepresentation.TargetTermType;
import utils.StringUtils;

import ac.biu.nlp.nlp.general.SimpleBidirectionalMap;

public class SOFeatureVectorExtractor {

	public SOFeatureVectorExtractor(StatScorer scorer,
			FeatureRepresentation featureRp) {
		m_scorer = scorer;
		m_featureRp = featureRp;
		try {
			m_reader = IndexReader.open(FSDirectory.open(new File(m_featureRp
					.getIndexNameByRepresentation())));
			loadFeatureDataFromIndex();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Ctor for vectors truncating
	 * @param scorer
	 */
	public SOFeatureVectorExtractor(StatScorer scorer) {
		m_scorer = scorer;
	}

	/**
	 * Print elements mapping
	 * 
	 * @param outputDir
	 * @param elements
	 *            file name
	 * @throws FileNotFoundException
	 */
	private void printElementsMapping(String outputDir, String elementsFileName)
			throws FileNotFoundException {
		File file = null;
		PrintWriter writer = null;
		file = new File(outputDir + "/" + elementsFileName);
		writer = new PrintWriter(new FileOutputStream(file));
		for (String element : m_soElementDesc2Id.leftSet())
			writer.println(element + "\t" + m_soElementDesc2Id.leftGet(element));
		writer.close();
	}

	private void printFeaturesMapping(String outputDir)
			throws FileNotFoundException {
		File file = new File(outputDir + "/features.txt");
		PrintWriter writer = new PrintWriter(new FileOutputStream(file));
		for (String element : m_featureDesc2Id.leftSet())
			writer.println(element + "\t" + m_featureDesc2Id.leftGet(element));
		writer.close();

		file = new File(outputDir + "/featuresFreq.txt");
		writer = new PrintWriter(new FileOutputStream(file));
		for (int freq : m_featureFreq.keys())
			writer.println(freq + "\t" + m_featureFreq.get(freq));
		writer.close();
	}

	/**
	 * Load features' data from index Feature filtering: features of freq<2
	 * 
	 * @throws IOException
	 */
	private void loadFeatureDataFromIndex() throws IOException {
		int featureId = 0;
		m_featureDesc2Id = new SimpleBidirectionalMap<String, Integer>();
		m_featureFreq = new TIntIntHashMap();

		/**
		 * @TODO Assign the correct value of m_totalElementsCount when used e.g.
		 *       PMI first order measure m_totalElementsCount =
		 *       reader.numDocs();
		 */
		TermEnum features = m_reader.terms();
		while (features.next()) {
			if (!features.term().field().equals("TERM_VECTOR"))
				continue;

			/**
			 * @TODO Add feature filter
			 */
			if (features.docFreq() < 2)
				continue;

			if (m_featureRp.isRemoveMarkedFeatures()
					&& features.term().text().contains("$"))
				continue;

			m_featureDesc2Id.put(features.term().text(), featureId);
			m_featureFreq.put(featureId, features.docFreq());
			featureId++;
		}
	}

	/**
	 * Extract target term vectors to a new elements file using an existing
	 * features mapping
	 * 
	 * @param targetDocs
	 * @param targetType
	 * @param outputDir
	 * @return int - number of target terms which were added
	 * @throws IOException
	 */
	public int extractTargetTermVectorsForSO(
			HashMap<String, ArrayList<ScoreDoc>> targetDocs,
			TargetTermType targetType, File outputDir) throws IOException {
		m_soElementDesc2Id = new SimpleBidirectionalMap<String, Integer>();
		m_soElementFreq = new TIntIntHashMap();

		BufferedWriter writer = null;
		File targetFile = new File(outputDir + "/" + "B_" + m_scorer.getName()
				+ ".txt");
		if (targetFile.exists()) {
			System.out.println("Target term file " + targetFile.getName()
					+ " already exist");
			return 0;
		}
		loadFeaturesFromFile(outputDir.getAbsolutePath());

		writer = new BufferedWriter(new FileWriter(targetFile));
		for (String target : targetDocs.keySet()) {
			m_soElementDesc2Id.put(target, m_soElementId);
			m_soElementFreq.put(m_soElementId, targetDocs.get(target).size());
			getTargetTermVectorForSO(writer, targetDocs.get(target), m_soElementId);
			m_soElementId++;
		}
		writer.close();
		printElementsMapping(outputDir.getAbsolutePath(), "targetElements.txt");
		return m_soElementDesc2Id.size();

	}

	/**
	 * 
	 * @param soRelatedTermRp
	 * @param outputDir
	 * @param filesNum - Number of files (splitting)
	 * @return int - Number of related terms
	 * @throws IOException
	 */
	public int extractSORelatedTermVectors(
			SORelatedTermRepresentation soRelatedTermRp, File outputDir, int filesNum)
			throws IOException {
		m_soElementDesc2Id = new SimpleBidirectionalMap<String, Integer>();
		m_soElementFreq = new TIntIntHashMap();
		BufferedWriter writer = null;
		int splitNum = 1;
		if (!outputDir.exists())
			outputDir.mkdir();
		File configDir = new File(outputDir.getAbsolutePath() + "/"
				+ m_featureRp.getFeatureType() + "_"
				+ soRelatedTermRp.getSoRelatedTermType());
		if (!configDir.exists())
			configDir.mkdir();
		File targetFile = new File(configDir + "/" + "A_" + m_scorer.getName()
				+ splitNum + ".txt");
		if (targetFile.exists()) {
			System.out.println("Target term file " + targetFile.getName()
					+ " already exist in " + configDir.getAbsolutePath());
			return 0;
		}
		writer = new BufferedWriter(new FileWriter(new File(configDir + "/"
				+ "A" + splitNum + "_" + m_scorer.getName() + ".txt")));
		IndexReader soRelatedTermsReader = IndexReader
				.open(FSDirectory.open(new File(soRelatedTermRp
						.getIndexNameByRepresentation())));
		TermEnum elements = soRelatedTermsReader.terms();
		while (elements.next()) {
			if (!elements.term().field().equals("TERM_VECTOR"))
				continue;

			if (soRelatedTermRp.isRemoveMarkedFeatures()
					&& elements.term().text().contains("$"))
				continue;

			if (elements.docFreq() < 2)
				continue;
			
			if((StringUtils.checkIfNumber(elements.term().text())))
				continue;
			
//			if (elements.term().text().length() < 2)
//				continue;

			m_soElementDesc2Id.put(elements.term().text(), m_soElementId);
			m_soElementFreq.put(m_soElementId, elements.docFreq());
			m_soElementId++;

		}
		int counter = 0;
		int elementPerSplit = m_soElementDesc2Id.size() / (filesNum-1);
		int splitCount = 0;

		for (String element : m_soElementDesc2Id.leftSet()) {

			if (counter % 1000 == 0)
				System.out.println("Element Num: " + counter);
			++counter;

			// the related term representation is separated from feature
			// representation
			
//			TermDocs docs = soRelatedTermsReader
//					.termDocs(new org.apache.lucene.index.Term("TERM_VECTOR",
//							element));
//			
//			getTermVectorForSO(writer, docs, m_soElementDesc2Id.leftGet(element));
			/**/
			IndexSearcher searcher = new IndexSearcher(soRelatedTermsReader);
			ScoreDoc[] docs = searcher.search(new TermQuery(new org.apache.lucene.index.Term("TERM_VECTOR",element)), 1000).scoreDocs;
			
			getTermVectorForSO(writer, docs, m_soElementDesc2Id.leftGet(element));
			/**/
			splitCount++;
			if (splitCount == elementPerSplit) {
				writer.close();
				splitNum++;
				writer = new BufferedWriter(new FileWriter(new File(configDir
						+ "/" + "A" + splitNum + "_" + m_scorer.getName()
						+ ".txt")));
				splitCount = 0;
			}
		}

		writer.close();

		// print element and features mapping
		printElementsMapping(configDir.getAbsolutePath(), "elements.txt");
		printFeaturesMapping(configDir.getAbsolutePath());

		return m_soElementDesc2Id.size();
	}

	/**
	 * Truncate vectors to vectors of length newVectorSize FEATURE_TRUNCATION is
	 * fixed - should not effects results
	 * 
	 * @param newVectorSize
	 * @param vectorsDir
	 * @throws IOException
	 */
	public void truncateVectors(int newVectorSize, File vectorsDir)
			throws IOException {
		BufferedReader reader;
		PrintWriter writer;

		System.out.println("Truncating");
		for (File f : vectorsDir.listFiles()) {
			if (f.getAbsolutePath().endsWith("_" + m_scorer.getName() + ".txt")&& !f.getAbsolutePath().contains("Trunc")) {
				reader = new BufferedReader(new FileReader(f));
				File truncFile = new File(vectorsDir + "/" + "Trunc"
						+ newVectorSize + f.getName());
				writer = new PrintWriter(new FileOutputStream(truncFile));

				String line = null;
				int currElementId = -1;
				List<FeatureScore> featureScores = new ArrayList<FeatureScore>();

				while ((line = reader.readLine()) != null) {

					String[] tokens = line.split("\t");
					int elementId = Integer.parseInt(tokens[0]);
					int featureId = Integer.parseInt(tokens[2]);
					double score = Double.parseDouble(tokens[3]);

					if (currElementId != -1 && (elementId != currElementId)) {
						int numOfFeatures = (int) Math.min(newVectorSize,
								Math.ceil(featureScores.size()
										* FEATURE_TRUNCATION));
						Collections.sort(featureScores);
						for (int j = 0; j < numOfFeatures; j++) {
							FeatureScore featureScore = featureScores.get(j);
							writer.println(currElementId + "\t" + "D" + "\t"
									+ featureScore.getFeatureId() + "\t"
									+ featureScore.getScore());
						}
						featureScores.clear();
					}

					featureScores.add(new FeatureScore(featureId, score));
					currElementId = elementId;
				}

				int numOfFeatures = (int) Math.min(newVectorSize, Math
						.ceil(featureScores.size() * FEATURE_TRUNCATION));
				Collections.sort(featureScores);
				for (int j = 0; j < numOfFeatures; j++) {
					FeatureScore featureScore = featureScores.get(j);
					writer.println(currElementId + "\t" + "D" + "\t"
							+ featureScore.getFeatureId() + "\t"
							+ featureScore.getScore());
				}
				featureScores.clear();

				reader.close();
				writer.close();
			}
		}
	}

	/**
	 * Truncate target vectors to vectors of length newVectorSize FEATURE_TRUNCATION is
	 * fixed - should not effects results
	 * 
	 * @param newVectorSize
	 * @param vectorsDir
	 * @throws IOException
	 */
	public void truncateTargetVectors(int newVectorSize, File vectorsDir)
			throws IOException {
		BufferedReader reader;
		PrintWriter writer;

		System.out.println("Truncating target vectors");
		File f = new File(vectorsDir +"/B_" + m_scorer.getName() + ".txt");
		reader = new BufferedReader(new FileReader(f));
		File truncFile = new File(vectorsDir + "/" + "Trunc"
						+ newVectorSize + f.getName());
		System.out.println("Truncated file:" + truncFile);
		writer = new PrintWriter(new FileOutputStream(truncFile));

		String line = null;
		int currElementId = -1;
		List<FeatureScore> featureScores = new ArrayList<FeatureScore>();

		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split("\t");
			int elementId = Integer.parseInt(tokens[0]);
			int featureId = Integer.parseInt(tokens[2]);
			double score = Double.parseDouble(tokens[3]);

			if (currElementId != -1 && (elementId != currElementId)) {
				int numOfFeatures = (int) Math.min(newVectorSize,
						Math.ceil(featureScores.size()
								* FEATURE_TRUNCATION));
				Collections.sort(featureScores);
				for (int j = 0; j < numOfFeatures; j++) {
					FeatureScore featureScore = featureScores.get(j);
					writer.println(currElementId + "\t" + "D" + "\t"
							+ featureScore.getFeatureId() + "\t"
							+ featureScore.getScore());
				}
				featureScores.clear();
			}

			featureScores.add(new FeatureScore(featureId, score));
			currElementId = elementId;
		}

		int numOfFeatures = (int) Math.min(newVectorSize, Math
				.ceil(featureScores.size() * FEATURE_TRUNCATION));
		Collections.sort(featureScores);
		for (int j = 0; j < numOfFeatures; j++) {
			FeatureScore featureScore = featureScores.get(j);
			writer.println(currElementId + "\t" + "D" + "\t"
					+ featureScore.getFeatureId() + "\t"
					+ featureScore.getScore());
		}
		featureScores.clear();

		reader.close();
		writer.close();
	}

	private void loadFeaturesFromFile(String confDir) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				confDir + "/" + "features.txt")));
		m_featureDesc2Id = new SimpleBidirectionalMap<String, Integer>();
		m_featureFreq = new TIntIntHashMap();
		String line = reader.readLine();
		while (line != null) {
			m_featureDesc2Id.put(line.split("\t")[0], Integer.parseInt(line
					.split("\t")[1]));
			line = reader.readLine();
		}
		reader.close();
		reader = new BufferedReader(new FileReader(new File(confDir + "/"
				+ "featuresFreq.txt")));
		line = reader.readLine();
		while (line != null) {
			m_featureFreq.put(Integer.parseInt(line.split("\t")[0]), Integer
					.parseInt(line.split("\t")[1]));
			line = reader.readLine();
		}
		reader.close();

	}

	/**
	 * Write a term feature vector to a file
	 * 
	 * @param writer
	 * @param targetDocs
	 * @throws IOException
	 */
	private void getTargetTermVectorForSO(BufferedWriter writer,
			List<ScoreDoc> targetDocs, int elementId) throws IOException {
		if (targetDocs.size() == 0)
			return;

		TIntLongHashMap countsMap = new TIntLongHashMap();
		for (ScoreDoc doc : targetDocs) {
			TermFreqVector tfv = m_reader.getTermFreqVector(doc.doc,
					"TERM_VECTOR");
			for (String term : tfv.getTerms()) {
				if (!m_featureDesc2Id.leftContains(term))
					continue;

				if (!countsMap.containsKey(m_featureDesc2Id.leftGet(term)))
					countsMap.put(m_featureDesc2Id.leftGet(term), 1);
				else {
					Long score = countsMap.get(m_featureDesc2Id.leftGet(term));
					countsMap.put(m_featureDesc2Id.leftGet(term), score + 1);
				}

			}
		}
		for (int featureId : countsMap.keys()) {
			double score = m_scorer.score(m_totalElementsCount, targetDocs
					.size(), m_featureFreq.get(featureId), countsMap
					.get(featureId));

			if (score > 0) {
				writer.write(m_soElementId + "\t" + "D" + "\t" + featureId
						+ "\t" + score + "\n");
			}
		}
	}

	/**
	 * Write a term feature vector to a file
	 * 
	 * @param writer
	 * @param termsDocs
	 * @throws IOException
	 */
	private void getTermVectorForSO(BufferedWriter writer,
			TermDocs termsDocs, int elementId) throws IOException {

		TIntLongHashMap countsMap = new TIntLongHashMap();
		int docsNum = 0;
		while (termsDocs.next()) {
			docsNum++;
			TermFreqVector tfv = m_reader.getTermFreqVector(termsDocs.doc(),
					"TERM_VECTOR");
			for (String term : tfv.getTerms()) {
				if (!m_featureDesc2Id.leftContains(term))
					continue;

				if (!countsMap.containsKey(m_featureDesc2Id.leftGet(term)))
					countsMap.put(m_featureDesc2Id.leftGet(term), 1);
				else {
					Long score = countsMap.get(m_featureDesc2Id.leftGet(term));
					countsMap.put(m_featureDesc2Id.leftGet(term), score + 1);
				}

			}
		}
		for (int featureId : countsMap.keys()) {
			double score = m_scorer.score(m_totalElementsCount, docsNum,
					m_featureFreq.get(featureId), countsMap.get(featureId));

			if (score > 0) {
				writer.write(elementId + "\t" + "D" + "\t" + featureId
						+ "\t" + score + "\n");
			}
		}
	}

	/**
	 * Write a term feature vector to a file
	 * 
	 * @param writer
	 * @param termsDocs
	 * @throws IOException
	 */
	private void getTermVectorForSO(BufferedWriter writer,
			ScoreDoc[] termsDocs, int elementId) throws IOException {

		TIntLongHashMap countsMap = new TIntLongHashMap();
		int docsNum = 0;
		for (ScoreDoc doc:termsDocs) {
			docsNum++;
			TermFreqVector tfv = m_reader.getTermFreqVector(doc.doc,
					"TERM_VECTOR");
			for (String term : tfv.getTerms()) {
				if (!m_featureDesc2Id.leftContains(term))
					continue;

				if (!countsMap.containsKey(m_featureDesc2Id.leftGet(term)))
					countsMap.put(m_featureDesc2Id.leftGet(term), 1);
				else {
					Long score = countsMap.get(m_featureDesc2Id.leftGet(term));
					countsMap.put(m_featureDesc2Id.leftGet(term), score + 1);
				}

			}
		}
		for (int featureId : countsMap.keys()) {
			double score = m_scorer.score(m_totalElementsCount, docsNum,
					m_featureFreq.get(featureId), countsMap.get(featureId));

			if (score > 0) {
				writer.write(elementId + "\t" + "D" + "\t" + featureId
						+ "\t" + score + "\n");
			}
		}
	}

	private SimpleBidirectionalMap<String, Integer> m_featureDesc2Id;
	private SimpleBidirectionalMap<String, Integer> m_soElementDesc2Id;
	private StatScorer m_scorer;
	private FeatureRepresentation m_featureRp;
	private TIntIntHashMap m_featureFreq;
	private TIntIntHashMap m_soElementFreq;
	private int m_soElementId = 0;
	private int m_totalElementsCount = 0;
	private IndexReader m_reader = null;
	final double FEATURE_TRUNCATION = 0.75;
}
