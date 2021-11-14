//Name: Kanlayakorn Kesorn (SEC2), Patiphol Pussawong (SEC1), Supakarn Wongnil (SEC1) 
//Section: 1,2
//ID: 6088057, 6088136, 6088137

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

public class TFIDFSearcher extends Searcher
{
	LinkedHashMap<String, Double> idf = new LinkedHashMap<String, Double>();
	List<String> termIndex = null;
	HashMap<Integer, double[]> docVector = new HashMap<Integer, double[]>();
	HashMap<Integer, Double> docLength = new HashMap<Integer, Double>();
	
	public TFIDFSearcher(String docFilename) {
		super(docFilename);
		/************* YOUR CODE HERE ******************/
		HashSet<String> allTerms = new HashSet<String>();
		for (Document doc : this.documents) {
			allTerms.addAll(doc.getTokens());
		}
		//System.out.println(allTerms.size());
		
		
		//Getting DF -> how many occurrences of this term in all documents
		
		for (Document doc : this.documents) {
			HashSet<String> uniq = new HashSet<String>(doc.getTokens());
			for(String term : uniq) {
				if(idf.containsKey(term)) {
					idf.replace(term, idf.get(term)+1.0);
				} else {
					idf.put(term, 1.0);
				}
			}
		}
		
		//TODO: Fix this bug after https://github.com/mockito/mockito/issues/1899 is fixed
		for (String term : idf.keySet()) {
			double idfVal = Math.log10((this.documents.size()/idf.get(term))+1);
			idf.replace(term, idfVal);
		}
		
		//System.out.println(idf);
		
		//Generating term index for indicate location of each terms in an array
		termIndex = new ArrayList<String>(idf.keySet());
		
		//calculating each document tfidf array
		for (Document doc : this.documents) {
			double tfidf[] = new double[idf.size()];
			for(String term : doc.getTokens()) {
				double tf = findTf(term, doc.getTokens());
				tfidf[termIndex.indexOf(term)] = tf*idf.get(term);
			}
			
			docVector.put(doc.getId(), tfidf);
		}
		
		
		//calculating each document length
		for (Document doc : this.documents) {
			double sum = 0;
			for(double val : docVector.get(doc.getId())) {
				sum += Math.pow(val, 2);
			}
			sum = Math.sqrt(sum);
			docLength.put(doc.getId(), sum);
		}
		
		//System.out.println(docLength);
		
		//System.out.println("TF"+findTf("cancel", this.documents.get(0).getTokens()));
		//System.out.println(idf.get("cancel"));
		//System.out.println(tfidf.get("cancel"));
		/***********************************************/
	}
	
	@Override
	public List<SearchResult> search(String queryString, int k) {
		/************* YOUR CODE HERE ******************/
		Queue<SearchResult> result = new PriorityQueue<>(k, Collections.reverseOrder());
		List<String> qTokens = tokenize(queryString);
		
		
		
		//System.out.println(sum);
		
		for(Document doc : this.documents) {
			//calculating query length and query vector
			double queryTfidf[] = new double[idf.size()];
			for(String term : qTokens) {
				double tf = findTf(term, qTokens);
				if(idf.get(term) != null) {
					queryTfidf[termIndex.indexOf(term)] = tf*idf.get(term);
				} else {
					if(termIndex.indexOf(term) != -1) {
						queryTfidf[termIndex.indexOf(term)] = 0.0;
					}
				}
			}
			double sum = 0.0;
			for(double val : queryTfidf) {
				sum += Math.pow(val, 2);
			}
			sum = Math.sqrt(sum);
			
			double queryLength = sum;
			
			//get document vector
			double docTfidf[] = docVector.get(doc.getId());
			double sumup = 0;
			
			//doing dot product
			for(int i=0;i<docTfidf.length;i++) {
				sumup += docTfidf[i]*queryTfidf[i];
			}
			
			double cos = sumup / (queryLength*docLength.get(doc.getId()));
			
			SearchResult temp = new SearchResult(doc, cos);
			
			if(result.size() < k || result.peek().getScore() < temp.getScore()) {
				if(result.size() == k) {
					SearchResult rem = result.remove();
					//System.out.println("Remove : "+rem.getScore());
				}
				result.add(temp);
				//System.out.println("Adding : "+temp.getScore());
				//System.out.println(result);
			}
		}
		List<SearchResult> r = new ArrayList<SearchResult>(result);
		Collections.sort(r);
		return r;
		/***********************************************/
	}
	//TODO: this function should be reworked once bug 1 is fixed and then we should have common type of returned value
	public int findDf(String term) {
		int count = 0;
		for(Document doc : this.documents) {
			if(doc.getTokens().contains(term)) {
				count++;
			}
		}
		
		return count;
	}
	
	public double findTf(String term, List<String> x) {
		int freq = Collections.frequency(x, term);
		if(freq != 0) {
			return 1+Math.log10(freq);
		} else {
			return 0;
		}
	}
}
