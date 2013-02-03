package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;

import utils.StringUtils;

public class CountOldPeriod {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException {
		File dir = new File("/home/ir/liebesc/ResponsaSys/output/modernJewishOnly/Surface_Surface/");
		IndexReader reader = IndexReader.open(FSDirectory.open(new File("/home/ir/liebesc/ResponsaSys/indexes/oldResponsa")));
		for (File f:dir.listFiles()) {
			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith("_Dice.txt")) {
				BufferedReader fileReader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + f.getName().replace("_Dice.txt","_OldCounts.txt")));
				System.out.println("New file name: " +dir.getAbsolutePath() + "/" + f.getName().replace("_Dice.txt","_OldCounts.txt"));
				String line = fileReader.readLine();
				while (line!=null){
					String[] tokens = line.split("\t");
					String query = tokens[0];
					int docFreq = reader.docFreq(new Term("TERM_VECTOR",query));
					writer.write(line + "\t" + docFreq + "\n");
					line = fileReader.readLine();
				}
				fileReader.close();
				writer.close();
			}
		}
		
		

	}

}
