package search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.Version;

import utils.StringUtils;



public class QueryGenerator {
	
	public enum InputType{
		Query,
		Raw
	}
	
	public QueryGenerator(Analyzer analyzer, String fieldName) {
		m_cpqp = new ComplexPhraseQueryParser(Version.LUCENE_31, fieldName, analyzer);
		m_type = InputType.Query;
	}
	
	/**
	 * Use ComplexPhraseQueryParser for generating complex queries
	 * @param term
	 * @return
	 * @throws ParseException
	 */
	public Query generate(String term) throws ParseException{
		if(m_type.equals(InputType.Raw))
			return generateFromRaw(term);
		
		BooleanQuery bq = new BooleanQuery();
		for (String t:term.split("\t")){
			if(t.split(" ").length>1) {
				t = StringUtils.fixQuateForSearch(t);
				t = "\"" + t + "\"";
			}
			bq.add(m_cpqp.parse(t),Occur.SHOULD);
		}
		
		return bq;
	}
	
	/**
	 * Generate complex query from a raw line
	 * Get the output of an input file
	 * @param reWritenQuery 
	 * @return
	 * @throws QueryGenerationException
	 */
	public Query generateFromRaw(String line){
		String[] splittedLine = line.split("\t");
		if(splittedLine.length==1) {
			BooleanQuery booleanQuery = new BooleanQuery();
			for(String t: splittedLine[0].split(" ")){
				Query query = new TermQuery(new Term("TERM_VECTOR", t));
				booleanQuery.add(query, BooleanClause.Occur.SHOULD);
	        }
			return booleanQuery;
		}
		else
		{
			SpanQuery[] spanAr = new SpanOrQuery[splittedLine.length];
			for(int i=0; i< splittedLine.length; i++) {
				SpanTermQuery[] termAr = new SpanTermQuery[splittedLine[i].split(" ").length];
				for(int j=0; j< splittedLine[i].split(" ").length; j++){
					termAr[j] = new SpanTermQuery(new Term("TERM_VECTOR", splittedLine[i].split(" ")[j]));
				}
				SpanOrQuery spanOr = new SpanOrQuery(termAr);
				spanAr[i] = spanOr; 
			}
			SpanNearQuery spanNear= new SpanNearQuery(spanAr, 0, true);
			return spanNear;
		}
		
	}
	public Query generateSpanNearQuery(String q){
		Query query;
		if(q.contains("\t"))
			query = generateFromRaw(q);
		else {
			SpanTermQuery[] termAr1 = new SpanTermQuery[q.split(" ").length];
			for(int i=0; i< q.split(" ").length; i++) {
					termAr1[i] = new SpanTermQuery(new Term("TERM_VECTOR", q.split(" ")[i]));
			}
			query = new SpanOrQuery(termAr1);
		}
		return query;
	}
	
	/**
	 * Generate a raw line from complex query
	 * Get the output of the rewrite function
	 * @param reWritenQuery 
	 * @return
	 * @throws QueryGenerationException
	 */
	public String generateToRaw(String reWritenQuery){
		//String patternStr = "(spanOr\\(\\[)([a-zA-Z]).*(\\),|\\]\\)\\].)";
		String patternStr = "(spanOr\\(\\[)([a-zA-Z]).*(\\]|spanOr\\()";
		String inputStr = reWritenQuery;
		// Compile and use regular expression
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(inputStr);
		boolean matchFound = matcher.find();
		String resultLine = "";

		if (matchFound) {
		        String groupStr = matcher.group(0);
		        String[] sp = groupStr.split("(spanOr|\\]\\), )");
		        for(String s:sp) {
		        	if(!s.equals("")) {
			        	s = s.replaceAll("\\(|\\)|\\[|\\]|,|TERM_VECTOR:", "");
			        	resultLine = resultLine + s.trim() + "\t";
		        	}
		        }
		        
		    }
		else{
				resultLine = inputStr.replaceAll("\\(|\\)|\\[|\\]|,|TERM_VECTOR:", "").trim();
	        	//System.out.println(resultLine);
			}
		return resultLine;
		}

		
	public InputType getType() {
		return m_type;
	}

	public void setType(InputType m_type) {
		this.m_type = m_type;
	}
	
	ComplexPhraseQueryParser m_cpqp = null;
	InputType m_type = InputType.Query;
	
	
	
}
