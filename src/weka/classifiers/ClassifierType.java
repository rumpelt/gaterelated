/**
 * 
 */
package weka.classifiers;

/**
 * @author ashwani
 *
 */
public enum ClassifierType {
j48,
simplelogistic,
logistic,
adaboost,
naivebayesmultinomial,
mahoutOnlineLogisticRegression,
tournamentmodel,
wekaclassifiers;

    public static boolean isWekaClassifer(String str) {
    	ClassifierType clt = ClassifierType.valueOf(str);
    	if (clt.equals(j48) || clt.equals(simplelogistic) || clt.equals(adaboost)
	    || clt.equals(logistic) || clt.equals(naivebayesmultinomial))
    		return true;
        return false;
    }
}
