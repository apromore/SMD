package org.prom5.mining.explicitmodel;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.prom5.framework.log.AuditTrailEntry;
import org.prom5.framework.log.AuditTrailEntryList;
import org.prom5.framework.log.LogEvent;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.LogSummary;
import org.prom5.framework.log.ProcessInstance;
import org.prom5.framework.models.petrinet.PNEdge;
import org.prom5.framework.models.petrinet.PetriNet;
import org.prom5.framework.models.petrinet.Place;
import org.prom5.framework.models.petrinet.Transition;
import org.prom5.framework.plugin.ProvidedObject;
import org.prom5.framework.plugin.Provider;
import org.prom5.mining.MiningPlugin;
import org.prom5.mining.MiningResult;

/**
 * Simple mining plugin that constructs a so-called "Explicit sequence model" from the given event log.
 * Can be useful as a reference model, and as a helper model if no real model is available but 
 * a model is needed for some analysis technique (e.g., the Decision Miner).
 * 
 * @author Anne Rozinat
 */
public class ExplicitMiningPlugin implements MiningPlugin {

	public JPanel getOptionsPanel(LogSummary summary) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.processmining.mining.MiningPlugin#mine(org.processmining.framework.log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		// build base model
		PetriNet result = new PetriNet();
		Place start = new Place("Start", result);
		Place end = new Place("End", result);
		Place middle1 = new Place("Middle1", result);
		Place middle2 = new Place("Middle2", result);
		result.addPlace(start);
		result.addPlace(end);
		result.addPlace(middle1);
		result.addPlace(middle2);
		Transition first = new Transition("first", result);
		Transition last = new Transition("last", result);
		result.addTransition(first);
		result.addTransition(last);
		PNEdge startToFirst = new PNEdge(start, first);
		PNEdge firstToMiddle = new PNEdge(first, middle1);
		PNEdge middleToLast = new PNEdge(middle2, last);
		PNEdge lastToEnd = new PNEdge(last, end);
		result.addEdge(startToFirst);
		result.addEdge(firstToMiddle);
		result.addEdge(middleToLast);
		result.addEdge(lastToEnd);
		
		Place previous;
		int counter = 0;
		for (ProcessInstance inst : log.getInstances()) {
			AuditTrailEntryList eList = inst.getAuditTrailEntryList();
			Iterator ateIterator = eList.iterator();
			previous = middle1;
			while (ateIterator.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ateIterator.next();
				LogEvent le = new LogEvent(ate.getElement(), ate.getType());
				Transition trans = new Transition(le, result);
				result.addTransition(trans);
				PNEdge from = new PNEdge(previous, trans);
				result.addEdge(from);
				PNEdge to;
				if (ateIterator.hasNext()) {
					Place interim = new Place("Place" + counter, result);
					result.addPlace(interim);
					to = new PNEdge(trans, interim);
					previous = interim;
					counter++;
				} else {
					to = new PNEdge(trans, middle2);
				}
				result.addEdge(to);
			}
		}
		return new ExplicitResult(result, log);
	}

	/*
	 * (non-Javadoc)
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "Builds a so-called Explicit sequence model for the given event log. This model is the most precise model one can think of as " + 
		"it only allows for the sequences that have been observed in the log. This can be useful as a reference model. Assumes that the log has been grouped before.";
	}

	/*
	 * (non-Javadoc)
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Explicit Model Miner";
	}
	

	public class ExplicitResult implements MiningResult, Provider {
		
		protected LogReader logReader;
		protected PetriNet model;
		
		/**
		 * Creates a Flower model mining result based on the given model and log.
		 * @param net the Petri net that represents the Flower model
		 * @param log the log that was used to construct the Flower model
		 */
		public ExplicitResult(PetriNet net, LogReader log) {
			logReader = log;
			model = net;
		}

		/*
		 * (non-Javadoc)
		 * @see org.processmining.mining.MiningResult#getLogReader()
		 */
		public LogReader getLogReader() {
			return logReader;
		}
		
		/**
		 * Returns the mined flower Petri net. 
		 * @return the net
		 */
		public PetriNet getPetriNet() {
			return model;
		}

		/*
		 * (non-Javadoc)
		 * @see org.processmining.mining.MiningResult#getVisualization()
		 */
		public JComponent getVisualization() {
			JComponent result = new JPanel(new BorderLayout());
			result.add(model.getGrappaVisualization(), BorderLayout.CENTER);
			return result;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
		 */
		public ProvidedObject[] getProvidedObjects() {	
			return new ProvidedObject[] {
					new ProvidedObject("Explicit Model", new Object[] {model, logReader})
			};
		}
	}
	
}
