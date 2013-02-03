package index;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;


public class Indexer {
	
	public enum DocField
	{
		ID,
		TEXT,
		TERM_VECTOR,
		PERIOD,
		SOURCE
	}
	
	/**
	 * Index the contents of the DocReader into the dir indexDir. Each doc will be indexed with the fields specified in combo 
	 * 
	 * @param analyzer
	 * @param docReader
	 * @param indexDir
	 * @param fieldsToIndex
	 * @param dontModifyExistingIndex if true, this method will require indexDir to be empty, and will otherwise throw an exception
	 * @param overwriteNotApped in case there already is an index there, true means overwrite it; false means append
	 * @return the number of docs indexed
	 * @throws IndexerException
	 */
	public int index(Analyzer analyzer, DocReader docReader, File indexDir, Set<DocField> fieldsToIndex, boolean dontModifyExistingIndex, 
			boolean overwriteNotApped) throws IndexerException {
		if (analyzer == null)
			throw new IndexerException("null analyzer");
		
		try
		{
			Directory fsDir = FSDirectory.open(indexDir);
			if ( dontModifyExistingIndex && fsDir.listAll().length > 0)
				throw new IndexerException("Failed creating index " + indexDir + ": Index already exists and write protection is turned on");

			// in case the index dir doesn't have a working index in it, overwriteNotApped  must be lit. otherwise, the indexWriter fails
			if (!indexDir.exists() || indexDir.listFiles().length == 0)		
				overwriteNotApped = true;
			
			IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_31, analyzer);
			conf.setOpenMode(overwriteNotApped ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND);
			IndexWriter indexWriter = new IndexWriter(fsDir, conf);
			int docCounter = 0;
			while (docReader.next()) 
			{
				Document doc = new Document();

				try {
					if (fieldsToIndex.contains(DocField.ID))
						doc.add(new Field(DocField.ID.name(), docReader.docId(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));

					if (fieldsToIndex.contains(DocField.TEXT))
						doc.add(new Field(DocField.TEXT.name(), docReader));

					if (fieldsToIndex.contains(DocField.TERM_VECTOR))
						doc.add(new Field(DocField.TERM_VECTOR.name(), docReader.doc(), Store.YES, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
//					doc.add(new Field(DocField.TERM_VECTOR.name(), docReader, TermVector.WITH_POSITIONS_OFFSETS));
					
					if (fieldsToIndex.contains(DocField.PERIOD))
						doc.add(new Field(DocField.PERIOD.name(), docReader.period(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));

					if (fieldsToIndex.contains(DocField.SOURCE))
						doc.add(new Field(DocField.SOURCE.name(), docReader.source(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
				} catch (Exception e) 
				{
					throw new IndexerException("Problem with docReader. Might have returned a null field value", e);
				}
				docCounter++;
				indexWriter.addDocument(doc);
				if (docCounter%1000==0)
					System.out.println("Doc number: " + docCounter);
			}
			indexWriter.optimize();
			indexWriter.close();
			
			return indexWriter.numDocs();
		} catch (CorruptIndexException e)
		{
			throw new IndexerException("corrupt index " + indexDir,e);
		} catch (LockObtainFailedException e)
		{
			throw new IndexerException("Error obtaining lock on index " + indexDir,e);
		} catch (IOException e)
		{
			throw new IndexerException("IO exception occured with index " + indexDir,e);
		}

	}
			
}
