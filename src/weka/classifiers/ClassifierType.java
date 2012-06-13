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
randomforest,
naivebayesmultinomial,
mahoutOnlineLogisticRegression,
tournamentmodel,
wekaclassifiers;

    public static boolean isWekaClassifer(String str) {
    	ClassifierType clt = ClassifierType.valueOf(str);
    	if (!clt.equals(mahoutOnlineLogisticRegression))
    		return true;
        return false;
    }
}
