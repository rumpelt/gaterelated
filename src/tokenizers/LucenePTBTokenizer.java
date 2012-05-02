/**
 * 
 */
package tokenizers;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;


/**
 * @author ashwani
 * Look at the default PTB options.
 * Also look at you what can be set for the PTB options.
 * A Stanford PTB tokenizer which can be used by Lucene.
 * Also A generic unigram tokenizer.
 * Requires Stannford NLP jar and Lucene 3.5 jar
 */

public class LucenePTBTokenizer extends Tokenizer implements StanfordTokenizer{
	
	private String ptbOptions="ptb3Escaping=false,normalizeAmpersandEntity=true,ptb3Ellipsis=true," +
	"ptb3Dashes=true,untokenizable=noneDelete";
	
	private final PTBTokenizer ptbtokenizer;
	private String input;
	
	public Iterator<String> iterator() {
		if (this.ptbtokenizer != null) {
			List<String> iterator = new Vector<String>();
			while(this.ptbtokenizer.hasNext()) {
				CoreLabel label = (CoreLabel) this.ptbtokenizer.next();
				iterator.add(label.word());
			}
			return iterator.iterator();
		}
		else
			return null;			
	}
	public boolean hasNext() {
		if (this.ptbtokenizer != null)
			return this.ptbtokenizer.hasNext();
		return false;
	}
	
	public String next() {
		if (this.ptbtokenizer != null) {
			
			return ((CoreLabel) this.ptbtokenizer.next()).word();
		}
		return null;
	}
	
	public String peek() {
		if (this.ptbtokenizer != null) 
			return ((CoreLabel) this.ptbtokenizer.peek()).word();
		return null;
	}
	
	public void remove() {
		if (this.ptbtokenizer != null)
			this.ptbtokenizer.remove();
	}
	
	public List tokenize() {
		if (this.ptbtokenizer != null) {
			List<String> result = new Vector<String>();
			while(this.ptbtokenizer.hasNext()) {
				CoreLabel label = (CoreLabel) this.ptbtokenizer.next();
				result.add(label.word());
			}
			return result;
		}
		else
			return null;
	}
	/**
	 * @return the ptbtokenizer
	 */
	public PTBTokenizer getPtbtokenizer() {
		return ptbtokenizer;
	}
	
	
	
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	//int offset = -1;
	public LucenePTBTokenizer(Reader input) {
		super(input);
		this.ptbtokenizer = new PTBTokenizer(input, new CoreLabelTokenFactory(), this.ptbOptions);
	}
	
	public LucenePTBTokenizer(Reader input, String ptbOptions) {
		super(input);
		this.ptbOptions = ptbOptions;
		this.ptbtokenizer = new PTBTokenizer(input, new CoreLabelTokenFactory(), this.ptbOptions);
	}
	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#incrementToken()
	 */
	@Override
	public boolean incrementToken() throws IOException {
		// TODO Auto-generated method stub
		clearAttributes();
		if (this.ptbtokenizer.hasNext()) {
			CoreLabel label = (CoreLabel) this.ptbtokenizer.next();
			termAtt.setEmpty().append(label.word());
		//	System.out.println(label.word());
		//	String s = label.word();
			offsetAtt.setOffset(label.beginPosition(),
					label.endPosition());
			return true;
		}		
		return false;
	}
	
	@Override
	 public void reset(Reader input) throws IOException {
	   super.reset(input);
	//   offset = -1;
	 }

}