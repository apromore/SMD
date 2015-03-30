package org.prom5.importing.yawl;

import java.io.IOException;
import java.io.InputStream;

import org.prom5.analysis.edithlprocess.EditHighLevelProcessGui;
import org.prom5.framework.models.hlprocess.hlmodel.HLYAWL;
import org.prom5.framework.models.yawl.algorithms.NewYAWLReader;
import org.prom5.framework.ui.filters.GenericFileFilter;
import org.prom5.importing.ImportPlugin;
import org.prom5.mining.MiningResult;

public class NewYAWLImport implements ImportPlugin {

	/**
	 * Create the import plug-in
	 */
	public NewYAWLImport() {
	}

	/**
	 * Ge tthe name of the plug-in
	 * @return "YAWL file"
	 */
	public String getName() {
		return "newYAWL file";
	}

	/**
	 * YAWL engine files have xml extensions
	 * @return File filter for xml files
	 */
	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	/**
	 * Import the YAWL file which is waiting in hte given stream
	 * @param input The given stream
	 * @return The YAWL model as a MiningResult
	 * @throws IOException If reading fails
	 */
	public MiningResult importFile(InputStream input) throws IOException {
		try {
			HLYAWL model = NewYAWLReader.read(input);

			if (model == null) {
				return null;
			}
			return new EditHighLevelProcessGui(model);

		} catch (Throwable x) {
			x.printStackTrace();
			throw new IOException(x.getMessage());
		}
	}

	/**
	 * Provide help
	 * @return Help text
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/newyawlimport";
	}
}
