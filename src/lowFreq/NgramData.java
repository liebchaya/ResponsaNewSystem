package lowFreq;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import obj.Pair;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;

import utils.StringUtils;

public class NgramData {
	
	public NgramData(String oldIndex, String modernJewishIndex, String modernIndex) throws CorruptIndexException, IOException{
		m_oldReader = IndexReader.open(FSDirectory.open(new File(oldIndex)));
		m_modernJewishReader = IndexReader.open(FSDirectory.open(new File(modernJewishIndex)));
		m_modernSearcher = new IndexSearcher(FSDirectory.open(new File(modernIndex)));
	}
	
	/**
	 * 
	 * @param ngram
	 * @param targetTerm
	 * @param termsData
	 * @return Pair<Integer,Integer> - Added documents by the ngram, Intersecting documents 
	 * @throws IOException
	 */
	public Pair<Integer,Integer> NgramPossibleContribution(String ngram, String targetTerm, HashMap<String,HashSet<Integer>>termsData) throws IOException{
		TermDocs docs = m_modernJewishReader.termDocs(new Term("TERM_VECTOR",ngram));
		int addDocsNum = 0;
		int comDocsNum = 0;
		while(docs.next()){
			if(!termsData.get(targetTerm).contains(docs.doc()))
				addDocsNum++;
			else 
				comDocsNum++;
		}
		return new Pair<Integer, Integer>(addDocsNum,comDocsNum);
	}
	
	/**
	 * Period classification by appearances in the old corpus
	 * @param ngram
	 * @return
	 * @throws IOException
	 */
	public int countOldPeriod(String ngram) throws IOException{
		int docFreq = m_oldReader.docFreq(new Term("TERM_VECTOR",ngram));
		return docFreq;
	}
	
	/**
	 * Counts the intersection between the ngram and the target term in the modern corpus
	 * @param ngram
	 * @param targetTerm
	 * @return
	 * @throws IOException
	 */
	public int countModernIntersaction(String ngram, String targetTerm) throws IOException{
		BooleanQuery bq = new BooleanQuery();
		bq.add(new TermQuery(new Term("TERM_VECTOR",StringUtils.fixQuateForSearch(ngram))),Occur.MUST);
		bq.add(new TermQuery(new Term("TERM_VECTOR",StringUtils.fixQuateForSearch(targetTerm))),Occur.MUST);
		TopDocs docs = m_modernSearcher.search(bq, 100000);
		return docs.totalHits;
	}

	private IndexReader m_oldReader;
	private IndexReader m_modernJewishReader;
	private IndexSearcher m_modernSearcher;

}
