package test;

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
import java.util.HashSet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

import utils.StringUtils;

public class CountOldPeriodTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String oldIndex = "/home/ir/liebesc/ResponsaSys/indexes/oldResponsa";
		CountOldPeriodTest pt = new CountOldPeriodTest(oldIndex);
		String inputFolder = "/home/ir/liebesc/ResponsaSys/output/step0/Surface_Surface/clusters5000_SUMSCORE_2";
		String outputFolder = "/home/ir/liebesc/ResponsaSys/output/step0/Surface_Surface/clusters5000_SUMSCORE_2_period";
		File inputDir = new File(inputFolder);
		for(File f:inputDir.listFiles()) {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "CP1255")); 
//			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "CP1255"));
			BufferedReader reader = new BufferedReader(new FileReader(f)); 
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder+"/"+f.getName().replace(".clusters","_modern.clusters")));
			BufferedWriter oldWriter = new BufferedWriter(new FileWriter(outputFolder+"/"+f.getName().replace(".clusters","_old.clusters")));
			String line = reader.readLine();
			while(line!=null){
				if(line.split("\t").length>1) {
					HashSet<String> strLst = StringUtils.convertStringToSet(line.split("\t")[0]);
					int counter = 0;
					for(String str:strLst)
						counter += pt.countOldPeriod(str);
					if (counter > 0)
						oldWriter.write(line + "\n");
					else
						writer.write(line + "\n");
				}
				line = reader.readLine();
			}
			reader.close();
			writer.close();
			oldWriter.close();
		}

	}

	CountOldPeriodTest(String oldIndex) throws CorruptIndexException, IOException{
		m_oldReader = IndexReader.open(FSDirectory.open(new File(oldIndex)));
	}
	/**
	 * Period classification by appearances in the old corpus
	 * @param ngram
	 * @return
	 * @throws IOException
	 */
	public int countOldPeriod(String ngram) throws IOException{
		String cleanNgram = ngram.replaceAll("\\p{Punct}|\\d","");
		int docFreq = m_oldReader.docFreq(new Term("TERM_VECTOR",cleanNgram));
		return docFreq;
	}
	
	private IndexReader m_oldReader;
}
