package test;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class GetDocNumInIndex {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException {
		String IndexName = "/home/ir/liebesc/ResponsaSys/indexes/allCorporaBigrams";
		IndexReader reader = IndexReader.open(FSDirectory.open(new File(IndexName)));
		System.out.println("Number of document: "+reader.numDocs());
	}

}
