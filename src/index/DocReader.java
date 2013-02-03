package index;

import java.io.Reader;



/**
 * @author Amnon Lotan
 *
 * @since 08/03/2011
 */
public abstract class DocReader extends Reader 
{
	/**
	 * Progresses the reader's head to the next doc to read
	 * (either sentence or paragraph depending on the implementation).
	 * @return false iff reached the end of corpus. 
	 */
	public abstract boolean next() throws IndexerException;

	/**
	 * @return the current doc's ID
	 * @throws IndexerException
	 */
	public abstract String docId() throws IndexerException;
	
	/**
	 * @return the current doc's contents
	 * @throws IndexerException
	 */
	public abstract String doc() throws IndexerException;

	/**
	 * @return the next token, or null when finished current text.
	 * @throws IndexerException
	 */
	public abstract String readToken() throws IndexerException;
	
	/**
	 * get the period in which the doc was written (if applicable)
	 * @return
	 * @throws IndexerException
	 */
	public abstract String period() throws IndexerException;
	
	/**
	 * get the historical source of the doc (if applicable)
	 * @return
	 * @throws IndexerException
	 */
	public abstract String source() throws IndexerException;
}
