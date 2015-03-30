package org.prom5.clus;

import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import org.prom5.exporting.petrinet.PnmlExport;
import org.prom5.framework.log.LogFile;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogReaderFactory;
import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.ui.Message;
import org.prom5.framework.ui.OpenLogSettings;
import org.prom5.framework.ui.slicker.logdialog.SlickerOpenLogSettings;
import org.prom5.mining.petrinetmining.AlphaProcessMiner;
import org.prom5.mining.petrinetmining.PetriNetResult;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AlphaMiner {

	private String inputFile = null, outputFile = null;
	private LogReader logReader = null;
	private PetriNet petriNet = null;

	/**
	 * Create an Alpha Miner, read the arguments.
	 *
	 * @param args String[] The arguments, format: -i inputfile [-o output file]
	 */
	public AlphaMiner(String[] args) {
		int argMode = 0;
		for (int i = 0; i < args.length; i++) {
			if (argMode == 0) {
				if (args[i].contentEquals("-i")) {
					argMode = 1;
				} else if (args[i].contentEquals("-o")) {
					argMode = 2;
				}
			} else if (argMode == 1) {
				inputFile = new String(args[i]);
				argMode = 0;
			} else if (argMode == 2) {
				outputFile = new String(args[i]);
				argMode = 0;
			}
		}
	}

	/**
	 * Open the log, given the input file specified.
	 */
	public void OpenLog() {
		if (inputFile != null) {
			// Open the log.
			LogFile logFile = LogFile.getInstance(inputFile);
			final OpenLogSettings settings = new SlickerOpenLogSettings(logFile);
			try {
				logReader = LogReaderFactory.createInstance(settings.getLogFilter(), logFile);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} else {
			System.err.println("No input file found.");
		}
	}

	/**
	 * Mine the log for a Petri net.
	 */
	public void Mine() {
		if (logReader != null) {
			// Mine the log for a Petri net.
			AlphaProcessMiner miningPlugin = new AlphaProcessMiner();
			PetriNetResult result = (PetriNetResult) miningPlugin.mine(logReader);
			petriNet = result.getPetriNet();
		} else {
			System.err.println("No log reader could be constructed.");
		}
	}

	/**
	 * Export the mined Petri net to a PNML file.
	 */
	public void Export() {
		if (petriNet != null) {
			// Export the Petri net as PNML.
			PnmlExport exportPlugin = new PnmlExport();
			Object[] objects = new Object[] {petriNet};
			ProvidedObject object = new ProvidedObject("temp", objects);
			FileOutputStream outputStream = null;
			try {
				if (outputFile != null) {
					outputStream = new FileOutputStream(outputFile);
				}
				// If no output file specified, write to System.out
				// However, some other thing smay get written to System.out as well :-(.
				exportPlugin.export(object, (outputStream != null ? outputStream : System.out));
				System.exit(0);
			} catch (Exception e) {
				System.err.println("Unable to write to file: " + e.toString());
			}
		} else {
			System.err.println("No Petri net could be constructed.");
		}
	}

	/**
	 * Main.
	 * @param args String[]
	 */
	public static void main(String[] args) {
		AlphaMiner alphaMiner = new AlphaMiner(args);
		alphaMiner.OpenLog();
		alphaMiner.Mine();
		alphaMiner.Export();
	}
}
