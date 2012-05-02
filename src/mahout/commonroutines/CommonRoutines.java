/**
 * 
 */
package mahout.commonroutines;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.math.Vector;

/**
 * @author ashwani
 *	Some common routines
 */
public final class CommonRoutines {

	public static List<Vector> leaveOneOutTrainingSet(List<Vector> input ,
			int presentIndex) {
		if (presentIndex >= input.size())
			presentIndex =0;
		List<Vector> local = new ArrayList<Vector>(input);
		local.remove(presentIndex);
		presentIndex++;
		return local;
	}
	public static void removeIndices(List<Vector> input , 
			int[] indicesTORemove) {
		for(Vector v : input) {
			for (int ind : indicesTORemove)
			v.set(ind, 0.0);
		}
	}
}
