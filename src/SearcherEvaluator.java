//Name: Kanlayakorn Kesorn (SEC2), Patiphol Pussawong (SEC1), Supakarn Wongnil (SEC1) 
//Section: 1,2
//ID: 6088057, 6088136, 6088137

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class SearcherEvaluator {
	private List<Document> queries = null;				//List of test queries. Each query can be treated as a Document object.
	private  Map<Integer, Set<Integer>> answers = null;	//Mapping between query ID and a set of relevant document IDs
	
	public List<Document> getQueries() {
		return queries;
	}

	public Map<Integer, Set<Integer>> getAnswers() {
		return answers;
	}

	/**
	 * Load queries into "queries"
	 * Load corresponding documents into "answers"
	 * Other initialization, depending on your design.
	 * @param corpus
	 */
	public SearcherEvaluator(String corpus)
	{
		String queryFilename = corpus+"/queries.txt";
		String answerFilename = corpus+"/relevance.txt";
		
		//load queries. Treat each query as a document. 
		this.queries = Searcher.parseDocumentFromFile(queryFilename);
		this.answers = new HashMap<Integer, Set<Integer>>();
		//load answers
		try {
			List<String> lines = FileUtils.readLines(new File(answerFilename), "UTF-8");
			for(String line: lines)
			{
				line = line.trim();
				if(line.isEmpty()) continue;
				String[] parts = line.split("\\t");
				Integer qid = Integer.parseInt(parts[0]);
				String[] docIDs = parts[1].trim().split("\\s+");
				Set<Integer> relDocIDs = new HashSet<Integer>();
				for(String docID: docIDs)
				{
					relDocIDs.add(Integer.parseInt(docID));
				}
				this.answers.put(qid, relDocIDs);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns an array of 3 numbers: precision, recall, F1, computed from the top *k* search results 
	 * returned from *searcher* for *query*
	 * @param query
	 * @param searcher
	 * @param k
	 * @return
	 */
	public double[] getQueryPRF(Document query, Searcher searcher, int k)
	{
		/*********************** YOUR CODE HERE *************************/
		List<SearchResult> returnDoc = searcher.search(query.getRawText(), k);
		HashSet<Integer> returnDocId = new HashSet<Integer>();
		
		for(SearchResult result : returnDoc) {
			returnDocId.add(result.getDocument().getId());
		}
		
		HashSet<Integer> relevance = new HashSet<Integer>(returnDocId);
		relevance.retainAll(answers.get(query.getId()));
		
		double precision = (double) relevance.size()/returnDocId.size();
		double recall = (double) relevance.size()/answers.get(query.getId()).size();
		double f1 = 0;
		if(precision+recall != 0) {
			f1 = (2*precision*recall)/(precision+recall);
		}
		double[] returndata = {precision,recall,f1};
		return returndata;
		/****************************************************************/
	}
	
	/**
	 * Test all the queries in *queries*, from the top *k* search results returned by *searcher*
	 * and take the average of the precision, recall, and F1. 
	 * @param searcher
	 * @param k
	 * @return
	 */
	public double[] getAveragePRF(Searcher searcher, int k)
	{
		/*********************** YOUR CODE HERE *************************/
		double avgprecision = 0.0;
		double avgrecall = 0.0;
		double avgf1 = 0.0;
		
		for(Document doc : this.getQueries()) {
			double[] val = this.getQueryPRF(doc, searcher, k);
			avgprecision += val[0];
			avgrecall += val[1];
			avgf1 += val[2];
		}
		
		avgprecision /= this.getQueries().size();
		avgrecall /= this.getQueries().size();
		avgf1 /= this.getQueries().size();
		
		double[] returndata = {avgprecision, avgrecall, avgf1};
		return returndata;
		/****************************************************************/
	}
}
