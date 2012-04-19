/**
 * 
 */
package weka;



import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cchs.MedicalTrainer;



/**
 * @author ashwani
 *	Main driver class for Weka Related class experiments performed by me
 */
public final class MainDriver {
	private static Options options=  new Options();
	public static void parsearguments(String[] commandargs) throws Exception {
		CommandLineParser cmdLineGnuParser = new GnuParser();
		CommandLine command = cmdLineGnuParser.parse(options, commandargs, true);
		if (command.hasOption("mt")) {
			String[] newCommand = new String[commandargs.length -1];
			for (int i=1; i< commandargs.length; i++) {
				newCommand[i-1] = commandargs[i];
			}
			MedicalTrainer.parserArgument(newCommand).launchPad();
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
	
		addoptions();
		try {
			parsearguments(argv);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
