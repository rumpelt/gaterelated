/**
 * 
 */
package indexreader;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;



/**
 * @author ashwani
 *
 */
public class ExTerm {
	private final Term term;
	private List<Integer> docnumbers=null;
	private HashMap<String,Integer> collocation = null;
	public ExTerm(Term term) {
		this.term = term;
	}
	
	private long totaltermcount = -1;
	
	public boolean equals(Object o) {
		if (((Term)o).field().equals(this.term.field()) && ((Term)o).text().equals(this.term.text()))
			return true;
		return false;		
	}
	
	public long populateTermCount(IndexReader ir) {
		this.totaltermcount = 0;
		try {
			TermDocs tdocs = ir.termDocs(this.term);
			while(tdocs.next()) {
				this.totaltermcount = this.totaltermcount + tdocs.freq();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.totaltermcount;
	}
	
	public  void populateCollocation(IndexReader ir, int windowsize) {
		HashSet<String> checked = new HashSet<String>();
		if (this.collocation == null)
			this.collocation = new HashMap<String, Integer>();
		for (int count = 0; count < ir.maxDoc();count ++) {
			try {
				TermFreqVector tvf = ir.getTermFreqVector(count, this.term.field());
				if (tvf == null)
					continue;
				for (String  token : tvf.getTerms()) {
					if ( !token.equals(this.term.text()) && (!checked.contains(token) || !this.collocation.containsKey(token))){
						int collocate = ExTerm.returnNumTimesTermCollocate(this.term, new Term(this.term.field(), token), windowsize, ir,  count);
						if (collocate != 0)
							this.collocation.put(token, collocate);
						else
							checked.add(token);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
     * Returns number of times string s2 is collocated in span of the string s2 in
	 * a particular document. Where span is given by the window size.
	 * Terms must belong to same field. 
	 * @param s1
	 * @param s2
	 * @param field
	 * @param windowSize
	 * @param ir
	 * @param docnumber
	 * @return
	 */
	public static int returnNumberOfTimesCollocate(String s1, String s2, String field, int windowSize, IndexReader ir, int docnumber) {
		
		Term t1 = new Term(field, s1);
		Term t2 = new Term(field, s2);
		return returnNumTimesTermCollocate(t1, t2, windowSize, ir, docnumber);
	}
	
	public static long returnTotalCollocation(Term t1, Term t2, int windowsize, IndexReader ir) {
		long totalCollocation = 0;
		for (int count = 0 ; count < ir.maxDoc(); count++) {
			totalCollocation = totalCollocation 
			+ returnNumTimesTermCollocate(t1, t2, windowsize, ir, count);
		}
		return totalCollocation;
	}
	
	
	public static String reconstructField(TermPositionVector tpv) {
		StringBuffer sb = new StringBuffer();
		HashMap<Integer, String> postionToTerm = new HashMap<Integer, String>();
		for (String term : tpv.getTerms()) {
			int[] tp1 = tpv.getTermPositions(tpv.indexOf(term));

			for (int pos : tp1) {
				postionToTerm.put(pos, term);
			}
		}
		int [] positions = new int[postionToTerm.keySet().size()];
		int count = 0;
		for (int key : postionToTerm.keySet()) {
			positions[count] = key;
			count++;
		}
		Arrays.sort(positions);
		for (int pos : positions) {
			sb.append(postionToTerm.get(pos));
			sb.append(" ");
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
	/**
	 * Returns number of times term t2 is collocated in span of the term t1 in
	 * a particular document. Where span is given by the window size.
	 * Terms must belong to same field. 
	 * @param t1
	 * @param t2
	 * @param windowSize
	 * @param ir
	 * @param docnumber
	 * @return
	 */
	public static int returnNumTimesTermCollocate(Term t1, Term t2, int windowSize, IndexReader ir, int docnumber) {
		
		int collocate = 0;
		if (!t1.field().equals(t2.field()))
			return 0;
		
		try {
			TermPositionVector tpv = (TermPositionVector) ir.getTermFreqVector(docnumber, t1.field());
			
			if (tpv.indexOf(t1.text()) == -1 || tpv.indexOf(t2.text()) == -1)
				return 0;
			int[] tp1 = tpv.getTermPositions(tpv.indexOf(t1.text()));
			int[] tp2 = tpv.getTermPositions(tpv.indexOf(t2.text()));
			Arrays.sort(tp1);
			Arrays.sort(tp2);
			int scount = 0;
			for (int fcount=0; fcount < tp1.length; )
			{  
				if (scount >= tp2.length )
					break;
				if(Math.abs(tp1[fcount] - tp2[scount]) <= windowSize) {
					collocate++;
					fcount++;
					scount++;
				}
				else if ((tp1[fcount] - tp2[scount]) < 0) 
					fcount++;
				else
					scount++;
			}
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return collocate;
	}
	
}
