/**
 * 
 */
package indexreader;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.util.Version;

/**
 * @author ashwani
 *
 */
public final class SimpleAnalyzer extends Analyzer {

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.nalyzer#tokenStream(java.lang.String, java.io.Reader)
		 */
 	private Version versionNumber;
 	public SimpleAnalyzer(Version versionNumber) {
 		this.versionNumber  = versionNumber;
 	}
	
		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#createComponents(java.lang.String, java.io.Reader)
		 */
		@Override
		protected TokenStreamComponents createComponents(String fieldname,
				Reader reader) {
			// TODO Auto-generated method stub
			Tokenizer tokenizer = new LowerCaseTokenizer(this.versionNumber , reader);
			TokenStream tokenstream = new StopFilter(this.versionNumber , 
					new PorterStemFilter(tokenizer) , 
					StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			
			return new Analyzer.TokenStreamComponents(tokenizer ,tokenstream);
		}
 	
 }