/**
 * 
 */
package weka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.util.Version;

import edu.stanford.nlp.stats.Counter;

import stats.KLDivergence;
import tokenizers.StopWordList;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.CommonClassifierRoutines;
import weka.classifiers.Evaluation;
import weka.classifiers.J48Classifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instances;

import misc.FeedCategories;
import misc.MedicalTrainer;

/**
 * @author ashwani
 *	Main driver class for Weka Related class experiments performed by me
 */
public final class MainDriver {
	private static Options options=  new Options();
	public static void parsearguments(String[] commandargs) throws ParseException {
		CommandLineParser cmdLineGnuParser = new GnuParser();
		CommandLine command = cmdLineGnuParser.parse(options, commandargs);
		if (command.hasOption("mt")) {
			String[] newCommand = new String[commandargs.length -1];
			for (int i=1; i< commandargs.length; i++) {
				newCommand[i-1] = commandargs[i];
			}
			
		}
			
	}
	public static void addoptions() {
		options.addOption("mt", "medicaltrainer", true, "the medical trainer module");
	}
	/**
	 * @param args
	 */
	public static void main(String[] argv) {
		// TODO Auto-generated method stub
		String csvfile = argv[0];
		addoptions();
		parsearguments(commandargs);
	}
	
	
}
