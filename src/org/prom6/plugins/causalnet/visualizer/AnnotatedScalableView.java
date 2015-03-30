package org.prom6.plugins.causalnet.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.BasicMarqueeHandler;
import org.processmining.framework.util.Cleanable;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableComponent.UpdateListener;
import org.processmining.framework.util.ui.widgets.Inspector;
import org.processmining.framework.util.ui.widgets.InspectorPanel;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.ContextMenuCreator;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.listeners.SelectionListener;
import org.processmining.models.jgraph.views.JGraphShapeView;
import org.prom6.models.causalnet.CausalNetAnnotations;
import org.prom6.plugins.causalnet.miner.EntryDG;
import org.prom6.plugins.causalnet.miner.EntrySJ;
import org.prom6.plugins.causalnet.temp.Counter;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class AnnotatedScalableView extends InspectorPanel implements Cleanable, ChangeListener, MouseMotionListener, UpdateListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8725938709936959781L;

	/**
	 * The maximal zoom factor for the primary view on the transition system.
	 */
	public static final int MAX_ZOOM = 1200;

	/**
	 * The access to scalable methods of primary view
	 */
	protected final ScalableComponent scalable;

	/**
	 * The primary view
	 */
	private JComponent component;

	/**
	 * The scroll pane containing the primary view on the transition system.
	 */
	protected JScrollPane scroll;
	
	protected CausalNetAnnotations annotations;
	
	//---------------
	
	protected Inspector inspector;
	protected HashMap<String, JPanel> inspectorTabs;
//	protected HashSet<ViewInteractionPanel> interactionPanels;
	
	protected SlickerFactory factory;
	protected SlickerDecorator decorator;
	
	private List<SelectionListener<?, ?>> selectionListeners = new ArrayList<SelectionListener<?, ?>>(0);
	private ContextMenuCreator creator = null;
	
	public AnnotatedScalableView(final ScalableComponent scalableComponent, final CausalNetAnnotations annotations){
		
		inspector = this.getInspector();
		inspectorTabs = new HashMap<String, JPanel>();
//		interactionPanels = new HashSet<ViewInteractionPanel>();
		
		this.annotations = annotations;
		
		/*
		 * Register the given view as the primary view, and get the transition
		 * system from the model.
		 */
		this.scalable = scalableComponent;
		component = scalableComponent.getComponent();
		/*
		 * Get some Slickerbox stuff, required by the Look+Feel of some objects.
		 */
		factory = SlickerFactory.instance();
		decorator = SlickerDecorator.instance();
		
		/*
		 * Create the scroll panel containing the primary view, and register the
		 * created adjustment and mouse listener.
		 */
		scroll = new JScrollPane(getComponent());
		/*
		 * Adjust Look+Feel of scrollbar to Slicker.
		 */
		decorator.decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);
		
		this.scroll.addComponentListener(new ComponentListener() {

			public void componentShown(ComponentEvent e) { }

			public void componentResized(ComponentEvent e) {
				scroll.removeComponentListener(this);
				scalable.setScale(1);
				double rx = (scroll.getWidth() - scroll.getVerticalScrollBar().getWidth())
						/ scalable.getComponent().getPreferredSize().getWidth();
				double ry = (scroll.getHeight() - scroll.getHorizontalScrollBar().getHeight())
						/ scalable.getComponent().getPreferredSize().getHeight();
				scalable.setScale(Math.min(rx, ry));
			}

			public void componentMoved(ComponentEvent e) { }
			public void componentHidden(ComponentEvent e) { }
		});
		
		this.setLayout(new BorderLayout());
		this.add(scroll);
		
		this.addMouseMotionListener(this);
		getComponent().addMouseMotionListener(this);
		scalable.addUpdateListener(this);
		
		this.redraw(new AnnotatedVisualizationSettings());
		
		this.validate();
		this.repaint();
	}
	
	public JScrollBar getHorizontalScrollBar() {
		return scroll.getHorizontalScrollBar();
	}

	public JScrollBar getVerticalScrollBar() {
		return scroll.getVerticalScrollBar();
	}

	/**
	 * Returns the zoom factor of the primary view.
	 * 
	 * @return The zoom factor of the primary view.
	 */
	public double getScale() {
		return scalable.getScale();
	}

	/**
	 * Sets the zoom factor of the primary view to the given factor.
	 * 
	 * @param d
	 *            The given factor.
	 */
	public void setScale(double d) {
		double b = Math.max(d, 0.01);
		b = Math.min(b, MAX_ZOOM / 100.);
		scalable.setScale(b);
	}

	/**
	 * Clean up.
	 */
	public void cleanUp() {
		/*
		 * Clean up both views.
		 */
		if (getComponent() instanceof Cleanable) {
			((Cleanable) getComponent()).cleanUp();
		}
		scalable.removeUpdateListener(this);
		getComponent().removeMouseMotionListener(this);
	}

	/**
	 * Deals with change events.
	 */
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (source instanceof JSlider) {
			/*
			 * Slider has been changed. Determine and set new zoom factor.
			 */
			scalable.setScale(((JSlider) source).getValue() / 100.0);
			getComponent().repaint();
			/*
			 * Update secondary view accordingly.
			 */
		}
	}

	/**
	 * Deals with garbage collection.
	 */
	@Override
	public void finalize() throws Throwable {
		try {
			/*
			 * We can now clean up.
			 */
			cleanUp();
		} finally {
			super.finalize();
		}
	}

	public void updated() {
		
		JComponent newComponent = scalable.getComponent();
		if (newComponent != getComponent()) {
			scroll.setViewportView(newComponent);
			if (getComponent() instanceof Cleanable) {
				((Cleanable) getComponent()).cleanUp();
			}
			getComponent().removeMouseMotionListener(this);

			component = newComponent;
			getComponent().addMouseMotionListener(this);
			invalidate();
		}
//		for (ViewInteractionPanel panel : this.interactionPanels) {
//			// HV: Do not call setScalableComponent now, as it changes the originalAttributeMap of the scalable.
////			panel.setScalableComponent(scalable);
//			panel.updated();
//		}
	}

	public JViewport getViewport() {
		return scroll.getViewport();
	}

	public void scaleToFit() {
		scalable.setScale(1);
		double rx = scroll.getViewport().getExtentSize().getWidth()
				/ scalable.getComponent().getPreferredSize().getWidth();
		double ry = scroll.getViewport().getExtentSize().getHeight()
				/ scalable.getComponent().getPreferredSize().getHeight();
		scalable.setScale(Math.min(rx, ry));
	}

	public JComponent getComponent() { return component; }	
	public ScalableComponent getScalable(){ return this.scalable; }

	public void mouseDragged(MouseEvent e) {
		// ignore
	}

	public synchronized void mouseMoved(MouseEvent e) {
		// ignore
	}
	
	public synchronized void addViewPanel(JComponent panel, String title, String tab, boolean open) {
		
		if(tab.equals("Info")){
			
			this.addInfo(title, panel);
		}
		else{
		
			JPanel tabPanel;
			if(this.inspectorTabs.containsKey(tab)) tabPanel = this.inspectorTabs.get(tab);
			else{
				
				tabPanel = this.inspector.addTab(tab);
				this.inspectorTabs.put(tab, tabPanel);
			}
			
			this.inspector.addGroup(tabPanel, title, panel, open);
		}
	}
	
//	public synchronized void addViewInteractionPanel(JComponent panel, String title, String tab, boolean open) {
//	
////		panel.setScalableComponent(scalable);
////		panel.setParent(this);
//		
//		this.addViewPanel(panel.getComponent(), panel.getPanelName(), tab, open);
//				
//		panel.updated();
//		
//		this.interactionPanels.add(panel);
//	}
	
	// -----------------------------
	
	public void redraw(AnnotatedVisualizationSettings settings){
		
		try{
			ProMJGraph graph = (ProMJGraph) this.scalable;
			
			this.renderGraph(graph, this.annotations, settings);
			graph.refresh();
			
//			for (ViewInteractionPanel panel : this.interactionPanels) {
//				// HV: Do not call setScalableComponent now, as it changes the originalAttributeMap of the scalable.
////				panel.setScalableComponent(scalable);
//				
//				panel.updated();
//				panel.willChangeVisibility(true);
//			}
		}
		catch(ClassCastException e){ e.printStackTrace(); }
	}
	
	private void renderGraph(ProMJGraph graph, CausalNetAnnotations annotations, AnnotatedVisualizationSettings settings){

//		AnnotatedVisualizationSettings settings = this.setup.getSettings();

		Set<? extends DirectedGraphNode> nodes = graph.getModel().getGraph().getNodes();
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
		if(settings.isColorScalingEvents()){

			for(DirectedGraphNode node : nodes){

				Object measure = annotations.getNodeInfo(node, settings.getMeasureEvents());

				if(measure == null) continue;

				try{
					float value = ((Number) measure).floatValue();

					if(value > max) max = value;
					if(value < min) min = value;
				}
				catch(ClassCastException e){ }
			}
		}

		for(DirectedGraphNode node : nodes){

			@SuppressWarnings("unchecked")
			List<Pair<String, String>> nodeID = (List<Pair<String, String>>) annotations.getNodeInfo(node, CausalNetAnnotations.id);

			Object measure = annotations.getNodeInfo(node, settings.getMeasureEvents());

			int grayLevel = 0;
			if(settings.isColorScalingEvents() && (measure != null)){

				if(min < max){

					try{
						float value = ((Number) measure).floatValue();
						grayLevel = 190 - Math.round(((value - min) / (max - min)) * 190);
					}
					catch(ClassCastException e){ }
				}
			}
			Color nodeColor = new Color(grayLevel, grayLevel, grayLevel);

			StringBuffer label = new StringBuffer();
			label.append("<html><body style='text-align:center;font-size:9px;color:rgb("+grayLevel+","+grayLevel+","+grayLevel+")'>");
			boolean isFirstValue = true;
			for(Pair<String,String> pair : nodeID){

				if(isFirstValue){

					label.append(pair.getSecond());
					isFirstValue = false;
				}
				else label.append("<p>"+pair.getSecond());
			}
			if(isFirstValue) label.append("ALL");
			if(measure != null) label.append("<p><i/>"+measure.toString());
			label.append("</body></html>");

			node.getAttributeMap().put(AttributeMap.LABEL, label.toString());
			node.getAttributeMap().put(AttributeMap.STROKECOLOR, nodeColor);
			node.getAttributeMap().put(AttributeMap.SHAPE, JGraphShapeView.RECTANGLE);
			node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.WHITE);
			node.getAttributeMap().put(AttributeMap.LABELVERTICALALIGNMENT, SwingConstants.CENTER);
			node.getAttributeMap().put(AttributeMap.INSET, 0);
		}

		//--------------------------------------------------

		Set<? extends DirectedGraphEdge<?,?>> edges = graph.getModel().getGraph().getEdges();
		min = Float.POSITIVE_INFINITY; 
		max = Float.NEGATIVE_INFINITY;
		if(settings.isColorScalingTransitions()){

			for(DirectedGraphEdge<?,?> edge : edges){

				Object measure = annotations.getEdgeInfo(edge, settings.getMeasureTransitions());

				if(measure == null) continue;

				try{
					float value = ((Number) measure).floatValue();

					if(value > max) max = value;
					if(value < min) min = value;
				}
				catch(ClassCastException e){ }
			}
		}

		for(DirectedGraphEdge<?,?> edge : edges){

			Object measure = annotations.getEdgeInfo(edge, settings.getMeasureTransitions());

			if(measure != null){

				int grayLevel = 0;
				if(settings.isColorScalingTransitions() && (measure != null)){

					if(min < max){

						try{
							float value = ((Number) measure).floatValue();
							grayLevel = 190 - Math.round(((value - min) / (max - min)) * 190);
						}
						catch(ClassCastException e){ }
					}
				}
				Color edgeColor = new Color(grayLevel, grayLevel, grayLevel);


				edge.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
				edge.getAttributeMap().put(AttributeMap.LABEL, measure.toString());
				edge.getAttributeMap().put(AttributeMap.EDGECOLOR, edgeColor);
				edge.getAttributeMap().put(AttributeMap.LABELCOLOR, edgeColor);
				edge.getAttributeMap().put(AttributeMap.LABELALONGEDGE, true);
				edge.getAttributeMap().put(AttributeMap.LINEWIDTH, 1f);
			}
			else{

				edge.getAttributeMap().put(AttributeMap.SHOWLABEL, false);
				edge.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.BLACK);
				edge.getAttributeMap().put(AttributeMap.LABELCOLOR, Color.BLACK);
				edge.getAttributeMap().put(AttributeMap.LABEL, "no value");
			}
		}
	}
	
	public void addPatternsViewports(final PatternsPanel joins, final PatternsPanel splits) {

		final ProMJGraph graph = (ProMJGraph) this.scalable;
		
		graph.addGraphSelectionListener(new GraphSelectionListener() {

			@SuppressWarnings("unchecked")
			public void valueChanged(GraphSelectionEvent e) {

				DirectedGraphNode selectedCell = null;

				Object[] cells = e.getCells();
				Collection<ProMGraphCell> nodesAdded = new ArrayList<ProMGraphCell>();
				Collection<ProMGraphEdge> edgesAdded = new ArrayList<ProMGraphEdge>();
				Collection<ProMGraphCell> nodesRemoved = new ArrayList<ProMGraphCell>();
				Collection<ProMGraphEdge> edgesRemoved = new ArrayList<ProMGraphEdge>();
				Collection<?> nodes = graph.getProMGraph().getNodes();
				Collection<?> edges = graph.getProMGraph().getEdges();
				for (int i = 0; i < cells.length; i++) {
					
					Collection nodeList;
					Collection edgeList;

					boolean isCell = cells[i] instanceof ProMGraphCell;
					boolean isEdge = cells[i] instanceof ProMGraphEdge;

					if (e.isAddedCell(i)) {
						nodeList = nodesAdded;
						edgeList = edgesAdded;

						if (isCell && (selectedCell == null))
							selectedCell = ((ProMGraphCell) cells[i]).getNode();

					} else {
						nodeList = nodesRemoved;
						edgeList = edgesRemoved;
					}
					if (isCell) {
						DirectedGraphNode node = ((ProMGraphCell) cells[i])
								.getNode();
						if (nodes.contains(node)) {
							nodeList.add(node);
						}
					} else if (isEdge) {
						DirectedGraphEdge<?, ?> edge = ((ProMGraphEdge) cells[i])
								.getEdge();
						if (edges.contains(edge)) {
							edgeList.add(((ProMGraphEdge) cells[i]).getEdge());
						}
					}
				}
				SelectionListener.SelectionChangeEvent event = new SelectionListener.SelectionChangeEvent(
						nodesAdded, edgesAdded, nodesRemoved, edgesRemoved);
				for (SelectionListener listener : selectionListeners) {
					listener.SelectionChanged(event);
				}
				
				if(selectedCell != null){
					
					if (nodesAdded.size() == 1) {
					
						EntrySJ<Counter> patterns = (EntrySJ<Counter>) annotations.getNodeInfo(selectedCell, CausalNetAnnotations.splitJoinPatterns);
						EntryDG relations = (EntryDG) annotations.getNodeInfo(selectedCell, CausalNetAnnotations.relations);
						EntryDG dependencies = (EntryDG) annotations.getNodeInfo(selectedCell, CausalNetAnnotations.longDistanceRelations);
						
//						System.out.println(patterns.toString());
//						System.out.println(relations.toString());
//						System.out.println(dependencies.toString());
						
						if(patterns != null){
							
							String id = "";
							for(Pair<String,String> value : patterns.getID()){
								
								if(id.isEmpty()) id = value.getSecond();
								else id += ":" + value.getSecond();
							}
							
							Set<String> inputDependencies = null, outputDependencies = null;
							if(dependencies != null){
								
								inputDependencies = dependencies.getInputs();
								outputDependencies = dependencies.getOutputs();
							}
							
							joins.update("Inputs of "+id, patterns.getJoins(), relations.getInputs(), inputDependencies);
							splits.update("Outputs of "+id, patterns.getSplits(), relations.getOutputs(), outputDependencies);
						}
					}
					else{
						
						joins.update("Multiple events selected", null, null, null);
						splits.update("Multiple events selected", null, null, null);
					}
				}
				else{
					
					joins.update("No event selected", null, null, null);
					splits.update("No event selected", null, null, null);
				}
				joins.repaint();
				splits.repaint();
			}

		});
		graph.setTolerance(4);

		graph.setMarqueeHandler(new BasicMarqueeHandler() {
			private boolean test(MouseEvent e) {
				return SwingUtilities.isRightMouseButton(e)
						&& (e.getModifiers() & InputEvent.ALT_MASK) == 0;

			}

			public boolean isForceMarqueeEvent(MouseEvent event) {
				if (test(event)) {
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (test(e)) {
					e.consume();
				} else {
					super.mouseReleased(e);
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (test(e)) {
					synchronized (graph.getProMGraph()) {
						// Check for selection.
						// If the cell that is being clicked is part of the
						// selection,
						// we use the current selection.
						// otherwise, we use a new selection
						Object cell = graph.getFirstCellForLocation(e.getX(), e
								.getY());

						Collection<DirectedGraphElement> sel;
						if (cell == null) {
							// Nothing selected
							graph.clearSelection();
							sel = new ArrayList<DirectedGraphElement>(0);
						} else if (graph.getSelectionModel().isCellSelected(
								cell)) {
							// the current selection contains cell
							// use that selection
							sel = getSelectedElements(graph);
						} else {
							// the current selection does not contain cell.
							// reset the selection to [cell]
							sel = new ArrayList<DirectedGraphElement>(1);
							sel.add(getElementForLocation(graph, e.getX(), e.getY()));
							graph.setSelectionCell(cell);
						}
						if (creator != null) {
							JPopupMenu menu = creator.createMenuFor(graph
									.getProMGraph(), sel);
							if (menu != null) {
								menu.show(graph, e.getX(), e.getY());
							}
						}
					}
				} else {
					super.mousePressed(e);
				}
			}
		});
	}
	
	public void addInteractionViewports(final PIPPanel pip, final ZoomPanel zoom){
		
		this.scroll.addComponentListener(new ComponentListener(){

			public void componentHidden(ComponentEvent arg0) { }
			public void componentMoved(ComponentEvent arg0) { }

			public void componentResized(ComponentEvent arg0) {
				
				if(arg0.getComponent().isValid()){
					
					Dimension size = arg0.getComponent().getSize();
					
					int width = 250, height = 250;
					
					if(size.getWidth() > size.getHeight()) height *= size.getHeight() / size.getWidth();
					else width *= size.getWidth() / size.getHeight();
						
					pip.setPreferredSize(new Dimension(width, height));
					pip.initializeImage();
					
					zoom.computeFitScale();
				}
			}

			public void componentShown(ComponentEvent arg0) { }
			
		});
	}
	
	public DirectedGraphElement getElementForLocation(final ProMJGraph graph, double x, double y) {
		Object cell = graph.getFirstCellForLocation(x, y);
		if (cell instanceof ProMGraphCell) {
			return ((ProMGraphCell) cell).getNode();
		}
		if (cell instanceof ProMGraphEdge) {
			return ((ProMGraphEdge) cell).getEdge();
		}
		return null;
	}
	
	public Collection<DirectedGraphNode> getSelectedNodes(final ProMJGraph graph) {
		List<DirectedGraphNode> nodes = new ArrayList<DirectedGraphNode>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphCell) {
				nodes.add(((ProMGraphCell) o).getNode());
			}
		}
		return nodes;
	}
	
	public Collection<DirectedGraphEdge<?, ?>> getSelectedEdges(final ProMJGraph graph) {
		List<DirectedGraphEdge<?, ?>> edges = new ArrayList<DirectedGraphEdge<?, ?>>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphEdge) {
				edges.add(((ProMGraphEdge) o).getEdge());
			}
		}
		return edges;
	}

	public Collection<DirectedGraphElement> getSelectedElements(final ProMJGraph graph) {
		List<DirectedGraphElement> elements = new ArrayList<DirectedGraphElement>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphCell) {
				elements.add(((ProMGraphCell) o).getNode());
			} else if (o instanceof ProMGraphEdge) {
				elements.add(((ProMGraphEdge) o).getEdge());
			}
		}
		return elements;
	}
	
	//------------------------
	
	
}
