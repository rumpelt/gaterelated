/**
 * 
 */
package tokenizers;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import edu.stanford.nlp.process.Tokenizer;


/**
 * @author ashwani
 * A simple Ngram tokenizer.
 * This class uses tokenizer from stanford nlp.
 * Can consume memory if the the input size if big.
 * TODO: extend LuceneTokenizer so that you can use this to do 
 * lucene indexing of ngrams using the stanford tokenizers.
 */
public class NgramTokenizer extends LuceneTokenizer implements Tokenizer {
	private Tokenizer basetokenizer;
	private int maxngram;
	private int minngram;
	private int curroffset;
	/**
	 * @return the curroffset
	 */
	public int getCurroffset() {
		return curroffset;
	}

	/**
	 * @param curroffset the curroffset to set
	 */
	public void setCurroffset(int curroffset) {
		this.curroffset = curroffset;
	}

	private int numBaseTokens;
	/**
	 * contain unigram tokens used to generate ngrams
	 */
	private List<String> tokens;
	/**
	 * @return the tokens
	 */
	public List<String> getTokens() {
		return tokens;
	}

	/**
	 * @param tokens the tokens to set
	 */
	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	public NgramTokenizer(Tokenizer tokenizer, int ngram) {
		this.maxngram = ngram;
		this.minngram = ngram;
		this.curroffset = -1;
		this.basetokenizer = tokenizer;
		this.tokens = new Vector<String>();
	}
	
	public NgramTokenizer(Tokenizer tokenizer, int maxngram, int minngram) {
		this.maxngram = maxngram;
		this.minngram = minngram;
		this.curroffset = -1;
		this.basetokenizer = tokenizer;
		this.tokens = new Vector<String>();
	}
	public List<String> tokenize() {
		if (this.basetokenizer != null) {
			while (this.basetokenizer.hasNext()) {
				this.tokens.add((String) this.basetokenizer.next());
			}
			if (this.tokens.size() > 0)
				this.curroffset = 0;
		}
		return this.tokens;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.nlp.process.Tokenizer#hasNext()
	 */
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return  ((this.curroffset + this.maxngram) <= this.tokens.size()
				|| (this.curroffset + this.minngram) <= this.tokens.size()) ?
            true : false;
	}

	/* (non-Javadoc)
	 * @see edu.stanford.nlp.process.Tokenizer#next()
	 */
	@Override
	public Object next() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		assert(this.curroffset >= 0);
		for(int count = 0; count < this.maxngram &&
			(this.curroffset + count < this.tokens.size()) ; count++ ) {
		//	if (this.curroffset + count > this.tokens.size())
			//	break;
			sb.append(this.tokens.get(this.curroffset+count));
			sb.append(" ");
		}
		this.curroffset++;
		return sb.toString().trim();
	}

	/* (non-Javadoc)
	 * @see edu.stanford.nlp.process.Tokenizer#peek()
	 * same as this.next() but does not increment the current offset.
	 */
	@Override
	public Object peek() {
		// TODO Auto-generated method stub
		
		StringBuilder sb = new StringBuilder();
		assert(this.curroffset >= 0);
		for(int count = 0; count < this.maxngram; count++ ) {
			sb.append(this.tokens.get(this.curroffset+count));
			sb.append(" ");
		}
		return sb.toString().trim();
	}

	/* (non-Javadoc)
	 * @see edu.stanford.nlp.process.Tokenizer#remove()
	 */
	@Override
	public void remove() {
		// TODO Auto-generated method stub
		new UnsupportedOperationException("Unsupoorted operation on the tokenizer");
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	@Override
	public boolean incrementToken() throws IOException {
		// TODO Auto-generated method stub
		new UnsupportedOperationException("TO Do operation on this tokenizer");
		return false;
	}
	
	public static void main(String[] argv) {
		String st = "breast and bottle fed through milk";
		StringReader sr = new StringReader(st);
		LucenePTBTokenizer toki = new LucenePTBTokenizer(sr);
		NgramTokenizer nt = new NgramTokenizer(toki, 1);
		nt.tokenize();
		System.out.println("unigram results");
		while(nt.hasNext()) {
			System.out.println(nt.next());
		}
		
		sr = new StringReader(st);
		toki = new LucenePTBTokenizer(sr);
		nt = new NgramTokenizer(toki, 2);
		nt.tokenize();
		System.out.println("bigram results ");
		while(nt.hasNext()) {
			System.out.println(nt.next());
		}
		
		sr = new StringReader(st);
		toki = new LucenePTBTokenizer(sr);
		nt = new NgramTokenizer(toki, 3);
		nt.tokenize();
		System.out.println("trigram results");
		while(nt.hasNext()) {
			System.out.println(nt.next());
		}
		
		
		sr = new StringReader(st);
		toki = new LucenePTBTokenizer(sr);
		nt = new NgramTokenizer(toki, 7,1);
		nt.tokenize();
		System.out.println("7gram results");
		while(nt.hasNext()) {
			System.out.println(nt.next());
		}
	}
	
}
