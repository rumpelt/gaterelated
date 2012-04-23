/**
 * 
 */
package mahout;

import java.io.IOException;
import java.util.List;

import mahout.classifiers.LogisticRegression;
import mahout.reader.SequenceFileReader;

import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.math.Vector;


/**
 * @author ashwani
 *	Main driver for mahout related libraries
 */
public class MainDriver {
	private static Options options;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		final Configuration conf = new Configuration();
	    List<Vector> vecs = SequenceFileReader.getAllVector(conf, 
				"/home/ashwani/xyz/arff/biasedsetunigram.arff.mvc");
		LogisticRegression.doLeaveOneOutCrossValidation(vecs);
	}
	
}	
