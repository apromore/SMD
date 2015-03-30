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

package org.prom5.framework.ui;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

import org.prom5.converting.ConvertingPlugin;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.plugin.Provider;
import org.prom5.mining.MiningResult;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class ConversionFrame extends JInternalFrame implements Provider {

	private MiningResult result;

	public ConversionFrame(ConvertingPlugin algorithm, MiningResult result) {
		super("Conversion - " + algorithm.getName(), true, true, true, true);

		this.result = result;

		this.getContentPane().setLayout(new BorderLayout());
		if (result != null) {
			this.getContentPane().add(result.getVisualization(), BorderLayout.CENTER);
		}
		pack();
	}

	public ProvidedObject[] getProvidedObjects() {
		return result instanceof Provider
				? ((Provider) result).getProvidedObjects()
				: new ProvidedObject[0];
	}
}
