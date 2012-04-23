/**
 * 
 */
package mahout.reader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
/**
 * @author ashwani
 * Reads sequence file
 * apache mahout vector is common maths Vector
 */
public final class SequenceFileReader {
	
	SequenceFile.Reader sqreader = null;
	
	public SequenceFileReader(Configuration conf, String filename) {
		try {
			this.sqreader = new SequenceFile.Reader(FileSystem.get(conf),
					new Path(filename),	conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public   Vector getNextVector() throws IOException  {
		
		LongWritable key = new LongWritable();
		VectorWritable  vec = new VectorWritable();
		this.sqreader.next(key, vec);
		
		return vec != null ? vec.get(): null;
	}
	
	public static List<Vector> getAllVector(Configuration conf, 
			String filename) {
		SequenceFile.Reader sqreader = null;
		ArrayList<Vector> vecs = new ArrayList<Vector>();
		try {
			sqreader = new SequenceFile.Reader(FileSystem.get(conf),
					new Path(filename),	conf);
			
			LongWritable key = new LongWritable();
			VectorWritable  vec = new VectorWritable();
			while (sqreader.next(key,vec)) {
				vecs.add(vec.get());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return vecs;
	}
}
