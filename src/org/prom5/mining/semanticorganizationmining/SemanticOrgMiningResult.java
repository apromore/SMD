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

package org.prom5.mining.semanticorganizationmining;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.prom5.framework.log.LogEvent;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.models.orgmodel.OrgModelConcept;
import org.prom5.framework.models.orgmodel.Task;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.plugin.Provider;
import org.prom5.importing.LogReaderConnection;
import org.prom5.mining.MiningResult;
import org.prom5.mining.semanticorganizationmining.ui.OrgMiningResultPanel;
import org.prom5.mining.snamining.model.SocialNetworkMatrix;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class SemanticOrgMiningResult extends JPanel implements MiningResult, Provider, LogReaderConnection {

	private OrgModelConcept orgModel = new OrgModelConcept();

	private LogReader log;
	private SocialNetworkMatrix snMatrix;
	private OrgMiningResultPanel gPanel;

	/**
	 * If this plugin is used multiple times, each time the simulation model to be
	 * provided will have an incremented number (in order to distinguish them later when they
	 * e.g., need to be joined)
	 */
	private static int simulationModelCounter = 0;

	public SemanticOrgMiningResult(LogReader log, OrgModelConcept orgModel) {
		this.log = log;
		this.orgModel = orgModel;
	}

	public SemanticOrgMiningResult(LogReader log, OrgModelConcept orgModel, SocialNetworkMatrix snMatrix) {
		this.log = log;
		this.orgModel = orgModel;
		this.snMatrix = snMatrix;
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = {
							new ProvidedObject("Organization Model", new Object[] {orgModel}),
						   new ProvidedObject("Whole Log", new Object[] {log}),
	    } ;
	    return objects;
	}

	public JComponent getVisualization() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public LogReader getLogReader() {
		return log;
	}

	private void jbInit() throws Exception {
		gPanel = new OrgMiningResultPanel(orgModel, snMatrix, this);
		this.setLayout(new BorderLayout());
		this.add(gPanel, BorderLayout.CENTER);
	}

	public OrgMiningResultPanel getOrgMiningResultPanel()
	{
		return gPanel;
	}

	public void connectWith(LogReader newLog, HashMap eventsMapping) {
		log = newLog;

		if (eventsMapping != null) {
			Iterator it = orgModel.getTaskList().iterator();
			while (it.hasNext()) {
				Task tempTask = (Task) it.next();
					Object[] mapped = (Object[]) eventsMapping.get(tempTask);
						// if the imported transition does not specify a log event type,
						// it is invisible by nature
						tempTask.setLogEvent((LogEvent) mapped[0]);
			}
		}
	}

	public ArrayList getConnectableObjects() {
		ArrayList result = new ArrayList();
		result.addAll(orgModel.getTaskList());
		return result;
	}

}
