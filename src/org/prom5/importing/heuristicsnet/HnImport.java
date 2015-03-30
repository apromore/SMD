/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.prom5.importing.heuristicsnet;

import java.io.IOException;
import java.io.InputStream;

import org.prom5.exporting.heuristicsNet.HnExport;
import org.prom5.framework.models.heuristics.HeuristicsNet;
import org.prom5.framework.ui.filters.GenericFileFilter;
import org.prom5.importing.LogReaderConnectionImportPlugin;
import org.prom5.mining.MiningResult;
import org.prom5.mining.geneticmining.util.MethodsOverIndividuals;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class HnImport implements LogReaderConnectionImportPlugin {

	public HnImport() {}

	public String getName() {
		return HnExport.FILE_TYPE;
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter(HnExport.FILE_TERMINATION);
	}

	public MiningResult importFile(InputStream input) throws IOException {
		HeuristicsNetFromFile fromFile = new HeuristicsNetFromFile(input);

		HeuristicsNet net = MethodsOverIndividuals.removeDanglingElementReferences(fromFile.getNet());

		return new HeuristicsNetResultWithLogReader(net, null);
	}

	public String getHtmlDescription() {
		return "<p> This plug-in import a <i>heuristics net</i> from a file.";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
