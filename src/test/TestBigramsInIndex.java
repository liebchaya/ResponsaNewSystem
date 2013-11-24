package test;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class TestBigramsInIndex {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException {
		String IndexName = "/home/ir/liebesc/ResponsaSys/indexes/allCorporaBigrams";
		IndexReader reader = IndexReader.open(FSDirectory.open(new File(IndexName)));
		TermEnum terms = reader.terms();
		int countPos = 0, countAll = 0;
		while (terms.next()) {
			if (terms.term().field().equals("TERM_VECTOR")){
				int freq = terms.docFreq();
				if (freq > 1){
	//				System.out.println(terms.term().text());
					if (terms.term().text().split(" ").length == 2) {
						countAll ++;
						String[] tokens = terms.term().text().split(" ");
						String convBigram = tokens[1] + " " + tokens[0];
						int convFreq = reader.docFreq(new Term("TERM_VECTOR", convBigram));
						if(convFreq > 0){
							System.out.println(terms.term().text()+"\t"+freq+"\t"+convFreq);
							countPos ++;
						}
					}
				}
			}
	}
		System.out.println(countAll+"\t" + countPos);
	}
}

