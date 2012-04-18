/**
 * 
 */
package indexreader;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;


/**
 * @author ashwani
 *
 */
public class CollocationRoutines {
	
	
	private int wc = -1;
	/**
	 * returns the total number of terms/words in a particular field over all
	 * documents
	 * @param field
	 * @param ir
	 * @return
	 */
	public long totalTermCount(String field, IndexReader ir) {
		long termcount = 0;
		for (int count = 0; count < ir.maxDoc(); count++) {
			try {
				TermFreqVector tf = ir.getTermFreqVector(count, field);
				for (int tc : tf.getTermFrequencies() ) {
					termcount = termcount + tc;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return termcount;
	}
	
	
	
	
	
	
	
	
	
	public long returnTotalOccurence(IndexReader ir, Term term) {
		long termcount = 0;
		try {
			for(int count = 0; count < ir.maxDoc();count++) {
				TermFreqVector tvf = ir.getTermFreqVector(count, term.field());
				if (tvf != null && tvf.indexOf(term.text()) != -1)
					termcount = termcount + tvf.getTermFrequencies()[tvf.indexOf(term.text())];
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return termcount;
	}
	
	public void initializeTerms(IndexReader ir) {
		TermEnum allterms;
		List<ExTerm> allExtendedTerms = new Vector<ExTerm>();
		try {
			allterms = ir.terms();
			while(allterms.next()) {
				ExTerm exterm = new ExTerm(allterms.term());
				exterm.populateTermCount(ir);
				exterm.populateCollocation(ir, 1);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public HashMap<String, Double> calculateMutualInformation(IndexReader ir, 
			int docnumber, int windowsize, String field) {
		long totalwc = this.totalTermCount(field, ir);
		HashMap<String , Double > result = new HashMap<String, Double>();
		try {
			TermFreqVector tfv = ir.getTermFreqVector(docnumber, field);
			String[] terms = tfv.getTerms();
			for (String x : terms) {
				double mi = 0.0;
				double totalXOccurence = this.returnTotalOccurence(ir,
						new Term(field, x));
				double probX = totalXOccurence/ totalwc;
				for (String y : tfv.getTerms()) {
					
					double totalYOccurence = this.returnTotalOccurence(ir, new Term(field, y));
					double probY = totalYOccurence/ totalwc;
					
					double collc = 0.0;
					if (!x.equals(y)) {
				//		collc =this.returnTotalCollocation(new Term(field, x),
								//	new Term(field,y), windowsize, ir);
						if (collc != 0.0) {
							mi = mi + Math.log(collc / totalXOccurence);
						}
					}
				}
			//	result.put(term, mi);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
