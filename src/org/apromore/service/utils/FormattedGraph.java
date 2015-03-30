package org.apromore.service.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apromore.graph.JBPT.CPF;
import org.jbpt.pm.ControlFlow;
import org.jbpt.pm.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.hpi.bpt.graph.DirectedEdge;
import de.hpi.bpt.hypergraph.abs.Vertex;

/**
 * @author Chathura Ekanayake
 *
 */
public class FormattedGraph {

	private static Logger log = LoggerFactory.getLogger(FormattedGraph.class);
	
	public static final int ACTIVITY_HEIGHT = 60;
	public static final int EVENT_HEIGHT = 60;
	public static final int CONNECTOR_SIZE = 30;
	public static final int MAX_CHARACTORS_PER_LINE = 20;
	
	public static int X_SPACE = 100;
	public static int X_START = 10;
	public static int Y_SPACE = 100;
	public static int Y_START = 10;
	public static int DEFAULT_ACTIVITY_WIDTH = 400;
	
	private static int MAX_RUNS = 4000;

	private CPF g;
	private Map<FlowNode, Point> coordinates;

	public FormattedGraph(CPF g, int activityWidth) {
		this.g = g;
		coordinates = new HashMap<FlowNode, Point>();
		X_SPACE = activityWidth + 20;
		format();
	}
	
	public FormattedGraph(CPF g, boolean format) {
		this.g = g;
		coordinates = new HashMap<FlowNode, Point>();
		
		Random r = new Random();
		Collection<FlowNode> vs = g.getFlowNodes();
		for (FlowNode v : vs) {
			coordinates.put(v, new Point(r.nextInt(200), r.nextInt(200)));
		}
	}

	public void format() {
		Map<FlowNode, Point> visitedJoins = new HashMap<FlowNode, Point>();
		Queue<FlowNode> q = new ConcurrentLinkedQueue<FlowNode>();
		List<FlowNode> ss = g.getSourceVertices();
		int xLevel = X_START;
		for (FlowNode s: ss) {
			format(s, xLevel, Y_START, visitedJoins, q);
			xLevel += X_SPACE;
		}
		
		int runs = 0;
		while (!q.isEmpty()) {
			runs++;
			if (runs > MAX_RUNS) {
				System.out.println(MAX_RUNS + " exceeded. Breaking the loop...");
				break;
			}
			
			FlowNode j = q.poll();
			Point p = visitedJoins.get(j);
			coordinates.put(j, p);
			Collection<FlowNode> svs = g.getDirectSuccessors(j);
			for (FlowNode sv : svs) {
				format(sv, p.x, p.y + Y_SPACE, visitedJoins, q);
			}
			visitedJoins.remove(j);
		}
	}

	private int format(FlowNode v, int xLevel, int yLevel, Map<FlowNode, Point> visitedJoins, Queue<FlowNode> q) {

		int preset = g.getDirectPredecessors(v).size();
//		int preset = 0;
		if (preset > 1) {
			// v is a join. coordinates of successive nodes cannot be decided, until all preceding paths have been processed.
			Point currentPoint = visitedJoins.get(v);
			if (currentPoint == null) {
				// this is the first visit to the join.
				currentPoint = new Point(xLevel, yLevel);
				currentPoint.visits = 1;
				q.add(v);
				visitedJoins.put(v, currentPoint);
				return xLevel;
			} else if (currentPoint.visits < preset - 1) {
				// this is a repeating visit to the join. but not the last visit.
				currentPoint.visits++;
				if (currentPoint.y < yLevel) {
					currentPoint.x = xLevel;
					currentPoint.y = yLevel;
				}
				return currentPoint.x;
			}

			q.remove(v);
			visitedJoins.remove(v);
			if (currentPoint.y > yLevel) {
				yLevel = currentPoint.y;
				xLevel = currentPoint.x;
			}
		} else {
			// non-join node may be visited more than once when processing a loop. in such cases, we should simply return without initiating continues loop
			if (coordinates.get(v) != null)
				return xLevel;	
		}

		coordinates.put(v, new Point(xLevel, yLevel));
		Collection<FlowNode> svs = g.getDirectSuccessors(v);
		yLevel += Y_SPACE;
		for (FlowNode sv : svs) {
			xLevel = format(sv, xLevel, yLevel, visitedJoins, q);
			xLevel += X_SPACE;
		}
		
		return xLevel - X_SPACE;
	}

	public Collection<FlowNode> getVertices() {
		return g.getVertices();
	}

	public Collection<ControlFlow<FlowNode>> getEdges() {
		return g.getEdges();
	}

	public Point getCoordinates(FlowNode v) {
		Point p = coordinates.get(v);
		if (p == null) {
			String msg = "Null coordinates - Node: " + v + " " + v.getName() + "\n";
//					"Successor: " + g.getFirstSuccessor(v).getId() + " " + g.getFirstSuccessor(v).getName();
			log.info(msg);
			Random r = new Random();
			p = new Point(r.nextInt(200), r.nextInt(200));
		}
		return p;
	}

	public String getVertexProperty(String vertexId, String propertyName) {
		return g.getVertexProperty(vertexId, propertyName);
	}
}
