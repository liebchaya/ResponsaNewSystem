package index;

import hebmorph.lemmafilters.FilterValue;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.util.Version;

import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;




	/**
 * @author Chaya Liebeskind
 *
 * @since 03/05/2011
 */
public class IndexCorpora
{		
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		
		long start = new Date().getTime();
		long end = new Date().getTime();
		
		System.out.println(args[0]);
		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
		ConfigurationParams params = conf.getModuleConfiguration("Experiment");
		
		System.out.println("start :"+start);
		String indexFolder = params.get("index-dir");
		File corpusDir = params.getDirectory("corpus-dir");
		Class<?> cls;
		String docReaderClass =params.get("docReader-class");
		cls = Class.forName(docReaderClass);
		DocReader reader = (DocReader) cls.getDeclaredConstructor(File.class).newInstance(corpusDir);
		String analyzerClass =params.get("analyzer-class");
		cls = Class.forName(analyzerClass);
		Analyzer analyzer = (Analyzer) cls.getDeclaredConstructor(Version.class).newInstance(Version.LUCENE_31);
		Float filterPercent = params.getFloat("filter-percent");
		FilterValue.filterPercent = filterPercent;

		
		File indexDir = new File(indexFolder);
		Indexer manager = new Indexer();
		Set<Indexer.DocField> fields = new HashSet<Indexer.DocField>();
		fields.add(Indexer.DocField.ID);
		//fields.add(SearchManager.DocField.TEXT);
		fields.add(Indexer.DocField.TERM_VECTOR);
		fields.add(Indexer.DocField.PERIOD);
		fields.add(Indexer.DocField.SOURCE);
		ShingleAnalyzerWrapper shingleAnalyzer = new ShingleAnalyzerWrapper(new WhitespaceAnalyzer(Version.LUCENE_31), 4);
		shingleAnalyzer.setOutputUnigrams(true);
//		MilaMorphAnalyzer analyzer = new MilaMorphAnalyzer(Version.LUCENE_31);
//		manager.index(analyzer, reader, indexDir , fields, false, true);
		manager.index(shingleAnalyzer, reader, indexDir , fields, false, true);
		
		analyzer.close();
		
		end = new Date().getTime();
		System.out.println("total run time : "+(end-start)/1000+" seconds"+"("+(end-start)/60000+" minutes)");
	}

}
