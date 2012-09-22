/**
 * 
 */
package tokenizers;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;

/**
 * @author ashwani
 * A wrapper for lucene tokenizer.
 * Does nothing. 
 */
public abstract class LuceneTokenizer extends Tokenizer {

	/**
	 * @param input
	 */
	protected LuceneTokenizer(Reader input) {
		super(input);
		// TODO Auto-generated constructor stub
	}
}
