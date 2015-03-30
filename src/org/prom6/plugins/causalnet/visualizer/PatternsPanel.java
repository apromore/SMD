package org.prom6.plugins.causalnet.visualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.prom6.plugins.causalnet.temp.Counter;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class PatternsPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6615860311124501461L;

	private String opID;

	private JLabel title, none;
	private JComboBox perspective;
	private JComboBox metric;
	private JPanel annotationsPanel;
	private JScrollPane annotationsScroll;
	private JTable patternsTable, connectionsTable;

	// -------------------------------------

	public PatternsPanel(HashMap<String,Counter> patterns, Set<String> elements, 
			Set<String> dependencies, String opID) {

		SlickerFactory factory = SlickerFactory.instance();
		SlickerDecorator decorator = SlickerDecorator.instance();
		
		this.opID = opID;

		this.setLayout(null);

		this.title = factory.createLabel(opID);
		this.title.setForeground(Color.darkGray);
		this.title.setFont(new java.awt.Font("Dialog", java.awt.Font.ITALIC, 18));

		this.none = factory.createLabel("None");
		this.none.setForeground(Color.darkGray);
		this.none.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
		this.none.setVisible(false);

		this.perspective = factory.createComboBox(new String[] { "Connections",
				"Patterns" });
		this.perspective.setSelectedItem("Patterns");
		this.perspective.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (perspective.getSelectedIndex() == 1)
					annotationsScroll.setViewportView(patternsTable);
				else
					annotationsScroll.setViewportView(connectionsTable);
			}

		});

		this.metric = factory.createComboBox(new String[] { "Frequency" });
		this.metric.setSelectedItem("Frequency");

		this.annotationsScroll = new JScrollPane();
		this.annotationsScroll.setBorder(javax.swing.BorderFactory
				.createEmptyBorder());
		decorator.decorate(this.annotationsScroll, Color.WHITE, Color.GRAY,
				Color.DARK_GRAY);
		this.annotationsScroll.getViewport().setBackground(Color.WHITE);

		this.annotationsPanel = factory.createRoundedPanel(15, Color.WHITE);
		this.annotationsPanel.setLayout(null);
		this.annotationsPanel.add(this.none);
		this.annotationsPanel.add(this.metric);
		this.annotationsPanel.add(this.annotationsScroll);
		
		this.add(this.title);
		this.add(this.perspective);
		this.add(this.annotationsPanel);

		this.setBackground(Color.LIGHT_GRAY);
		
		if(elements != null){
			
			if(elements.isEmpty()) this.none.setVisible(true);
			else this.initTables(patterns, elements, dependencies);
		}
		else{
			
			this.perspective.setVisible(false);
			this.perspective.setEnabled(false);
			this.metric.setVisible(false);
			this.metric.setEnabled(false);
			this.annotationsScroll.setVisible(false);
			this.annotationsScroll.setEnabled(false);
			this.annotationsPanel.setVisible(false);
		}
	}

	public void setSize(int width, int height) {

		super.setSize(width, height);

		this.title.setBounds(0, 0, width, 20);
		this.none.setBounds(20, 40, width - 60, 20);
		this.perspective.setBounds(0, 30, width, 20);
		this.annotationsPanel.setBounds(0, 60, width, height - 60);

		this.annotationsScroll.setBounds(5, 40, width - 10, height - 105);
		this.metric.setBounds(width - 105, 10, 100, 20);
		
		this.setPreferredSize(new Dimension(width, height));
	}

	private void initTables(HashMap<String, Counter> patterns, Set<String> elements, Set<String> dependencies) {

		String[] elementNames = new String[elements.size()];
		int index = 0;
		for(String name : elements){
			
			elementNames[index] = name;
			index++;
		}
		String[] dependencyNames;
		if(dependencies != null){
			
			dependencyNames = new String[dependencies.size()];
			index = 0;
			for(String name : dependencies){
				
				dependencyNames[index] = name;
				index++;
			}
		}
		else dependencyNames = new String[0];
		
		int columns = elementNames.length + dependencyNames.length;
		
		// ------------------

		this.patternsTable = new JTable();

		this.patternsTable.setGridColor(Color.GRAY);
		this.patternsTable.setBackground(Color.WHITE);
		this.patternsTable.setSelectionBackground(Color.LIGHT_GRAY);
		this.patternsTable.setSelectionForeground(Color.DARK_GRAY);
		this.patternsTable.setShowVerticalLines(false);

		final Class<?>[] classesTypes = new Class<?>[columns + 2];
		final boolean[] classesEdit = new boolean[columns + 2];
		for (int i = 0; i < columns; i++) {

			classesEdit[i] = false;
			classesTypes[i] = Boolean.class;
		}
		classesEdit[columns] = false;
		classesTypes[columns] = String.class;
		classesEdit[columns + 1] = false;
		classesTypes[columns + 1] = String.class;

		javax.swing.table.DefaultTableModel newTableModelP = new javax.swing.table.DefaultTableModel(
				new Object[][] {}, new String[] {}) {
			private static final long serialVersionUID = -3760751300047576804L;
			Class<?>[] types = classesTypes;
			boolean[] canEdit = classesEdit;

			public Class<?> getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		};

		int sum = 0;
		ArrayList<Integer> stackC = new ArrayList<Integer>(columns);
		for (int i = 0; i < columns; i++)
			stackC.add(new Integer(0));

		if (patterns != null) {

			ArrayList<String> stackP = new ArrayList<String>(patterns.size());
			ArrayList<Integer> stackV = new ArrayList<Integer>(patterns.size());
			for (java.util.Map.Entry<String, Counter> entry : patterns.entrySet()) {

				int occurrences = entry.getValue().getValue();

				boolean isInserted = false;
				for (int i = 0; i < stackP.size(); i++) {

					if (occurrences > stackV.get(i)) {

						stackP.add(i, entry.getKey());
						stackV.add(i, occurrences);
						isInserted = true;
						break;
					}
				}
				if (!isInserted) {

					stackP.add(entry.getKey());
					stackV.add(occurrences);
				}

				sum += occurrences;
			}

			Boolean[][] p = new Boolean[columns][patterns.size()];
			String[][] m = new String[2][patterns.size()];

			for (int i = 0; i < stackP.size(); i++) {

				String code = stackP.get(i);
				int occurrences = stackV.get(i);

				float percentage = Math.round((float) occurrences / (float) sum
						* 10000) / 100f;

				index = 0;
				for(int j = 0; j < elements.size(); j++) {

					if (code.charAt(j) == '1') {

						p[index][i] = true;

						Integer temp = stackC.remove(index);
						temp += occurrences;
						stackC.add(index, temp);
					} 
					else p[index][i] = false;
					
					index++;
				}
				for(int j = 0; j < dependencyNames.length; j++){
					
					p[index][i] = true;

					Integer temp = stackC.remove(index);
					temp += occurrences;
					stackC.add(index, temp);
					
					index++;
				}
				m[0][i] = " " + String.valueOf(occurrences);
				m[1][i] = percentage + "%";
			}

			for (int i = 0; i < columns; i++)
				newTableModelP.addColumn("", p[i]);

			newTableModelP.addColumn("", m[0]);
			newTableModelP.addColumn("", m[1]);
		}

		final TableCellRenderer headerRenderer = new VerticalTableHeaderCellRenderer();

		this.patternsTable.setModel(newTableModelP);

		if (elements.size() > 0) {
			
			index = 0;
			for(String elementName : elementNames){
				
				TableColumn column = this.patternsTable.getColumnModel().getColumn(index);
				
				column.setMinWidth(20);
				column.setMaxWidth(20);
				
				if(elementName.length() > 20) column.setHeaderValue(" "+elementName.substring(0, 17) + "...");
				else column.setHeaderValue(" "+elementName);
				
				column.setHeaderRenderer(headerRenderer);
				index++;
			}
			for(String dependencyName : dependencyNames){
				
				TableColumn column = this.patternsTable.getColumnModel().getColumn(index);
				
				column.setMinWidth(20);
				column.setMaxWidth(20);
				
				if(dependencyName.length() > 20) column.setHeaderValue(" "+dependencyName.substring(0, 17) + "...");
				else column.setHeaderValue(" "+dependencyName);
				
				column.setHeaderRenderer(headerRenderer);
				index++;
			}
			for(int i = 0; i < 2; i++){
				
				TableColumn column = this.patternsTable.getColumnModel().getColumn(index);
				column.setHeaderRenderer(headerRenderer);
				index++;
			}
			
			TableColumn column1 = this.patternsTable.getColumnModel().getColumn(
					columns);
			TableColumn column2 = this.patternsTable.getColumnModel().getColumn(
					columns + 1);

			column1.setMinWidth(60);
			column1.setMaxWidth(60);
			column2.setMinWidth(50);
			column2.setMaxWidth(50);

			this.patternsTable.getTableHeader().setBackground(Color.WHITE);
		}

		// ------------------

		this.connectionsTable = new JTable();

		this.connectionsTable.setGridColor(Color.GRAY);
		this.connectionsTable.setBackground(Color.WHITE);
		this.connectionsTable.setSelectionBackground(Color.LIGHT_GRAY);
		this.connectionsTable.setSelectionForeground(Color.DARK_GRAY);
		this.connectionsTable.setShowVerticalLines(false);

		javax.swing.table.DefaultTableModel newTableModelC = new javax.swing.table.DefaultTableModel(
				new Object[][] {}, new String[] {}) {
			private static final long serialVersionUID = -9201456440598163559L;
			Class<?>[] types = new Class<?>[] { String.class, String.class,
					String.class };
			boolean[] canEdit = new boolean[] { false, false, false };

			public Class<?> getColumnClass(int columnIndex) {
				return types[columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return canEdit[columnIndex];
			}
		};

		if (patterns != null) {

			ArrayList<Integer> stackI = new ArrayList<Integer>(elements.size());
			for (int i = 0; i < stackC.size(); i++) {

				int value = stackC.get(i);

				boolean isInserted = false;
				for (int j = 0; j < stackI.size(); j++) {

					int temp = stackC.get(stackI.get(j));

					if (value > temp) {

						stackI.add(j, new Integer(i));
						isInserted = true;
						break;
					}
				}
				if (!isInserted) {

					stackI.add(new Integer(i));
				}
			}

			String[][] m = new String[3][columns];

			for (int i = 0; i < stackC.size(); i++) {
			
				String elementID = "";
				if(i < elementNames.length) elementID = elementNames[stackI.get(i)];
				else elementID = dependencyNames[stackI.get(i - elementNames.length)];
				
				int occurrences = stackC.get(stackI.get(i));

				float percentage = Math.round((float) occurrences / (float) sum * 10000) / 100f;

				m[0][i] = elementID;
				m[1][i] = String.valueOf(occurrences);
				m[2][i] = percentage + "%";
			}

			for (int i = 0; i < 3; i++)
				newTableModelC.addColumn("", m[i]);
		}

		this.connectionsTable.setModel(newTableModelC);

		if (patterns != null) {

			TableColumn column1 = this.connectionsTable.getColumnModel()
					.getColumn(1);
			TableColumn column2 = this.connectionsTable.getColumnModel()
					.getColumn(2);

			column1.setMinWidth(60);
			column1.setMaxWidth(60);
			column2.setMinWidth(50);
			column2.setMaxWidth(50);
		}

		// ------------------

		if (this.perspective.getSelectedIndex() == 1)
			this.annotationsScroll.setViewportView(this.patternsTable);
		else
			this.annotationsScroll.setViewportView(this.connectionsTable);
	}

	public void update(String opID, HashMap<String,Counter> patterns, Set<String> elements, Set<String> dependencies) {

		if (!this.opID.equals(opID)) {

			this.opID = opID;

			this.title.setText(opID);
			this.title.setToolTipText(opID);

			if(elements == null){
				
				this.none.setVisible(false);
				this.perspective.setVisible(false);
				this.perspective.setEnabled(false);
				this.metric.setVisible(false);
				this.metric.setEnabled(false);
				this.annotationsScroll.setVisible(false);
				this.annotationsScroll.setEnabled(false);
				this.annotationsPanel.setVisible(false);
			}
			else{
				
				boolean status = !elements.isEmpty();
				
				this.none.setVisible(!status);
				this.perspective.setVisible(true);
				this.perspective.setEnabled(status);
				this.metric.setVisible(true);
				this.metric.setEnabled(status);
				this.annotationsScroll.setVisible(status);
				this.annotationsScroll.setEnabled(status);
				this.annotationsPanel.setVisible(true);
				
				if(status) this.initTables(patterns, elements, dependencies);
				
			}
		}
	}

}

class VerticalTableHeaderCellRenderer extends javax.swing.table.DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5792846837172592507L;

	public VerticalTableHeaderCellRenderer() {

		setOpaque(false);

		setHorizontalAlignment(LEFT);
		setHorizontalTextPosition(CENTER);
		setVerticalAlignment(CENTER);
		setVerticalTextPosition(TOP);
		setUI(new VerticalLabelUI(false));
	}

	protected javax.swing.Icon getIcon(JTable table, int column) {

		javax.swing.RowSorter.SortKey sortKey = getSortKey(table, column);
		if (sortKey != null && sortKey.getColumn() == column) {
			javax.swing.SortOrder sortOrder = sortKey.getSortOrder();
			switch (sortOrder) {
				case ASCENDING:
					return VerticalSortIcon.ASCENDING;
				case DESCENDING:
					return VerticalSortIcon.DESCENDING;
			}
		}
		return null;
	}

	private enum VerticalSortIcon implements javax.swing.Icon {

		ASCENDING, DESCENDING;
		private javax.swing.Icon icon = javax.swing.UIManager
		.getIcon("Table.ascendingSortIcon");

		/**
		 * Paints an icon suitable for the header of a sorted table column,
		 * rotated by 90° clockwise. This rotation is applied to compensate the
		 * rotation already applied to the passed in Graphics reference by the
		 * VerticalLabelUI.
		 * <P>
		 * The icon is retrieved from the UIManager to obtain an icon
		 * appropriate to the L&F.
		 * 
		 * @param c
		 *            the component to which the icon is to be rendered
		 * @param g
		 *            the graphics context
		 * @param x
		 *            the X coordinate of the icon's top-left corner
		 * @param y
		 *            the Y coordinate of the icon's top-left corner
		 */
		public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
			switch (this) {
				case ASCENDING:
					icon = javax.swing.UIManager.getIcon("Table.ascendingSortIcon");
					break;
				case DESCENDING:
					icon = javax.swing.UIManager
					.getIcon("Table.descendingSortIcon");
					break;
			}
			int maxSide = Math.max(getIconWidth(), getIconHeight());
			Graphics2D g2 = (Graphics2D) g.create(x, y, maxSide, maxSide);
			g2.rotate((Math.PI / 2));
			g2.translate(0, -maxSide);
			icon.paintIcon(c, g2, 0, 0);
			g2.dispose();
		}

		/**
		 * Returns the width of the rotated icon.
		 * 
		 * @return the <B>height</B> of the contained icon
		 */
		public int getIconWidth() {
			return icon.getIconHeight();
		}

		/**
		 * Returns the height of the rotated icon.
		 * 
		 * @return the <B>width</B> of the contained icon
		 */
		public int getIconHeight() {
			return icon.getIconWidth();
		}
	}

	protected javax.swing.RowSorter.SortKey getSortKey(JTable table, int column) {
		javax.swing.RowSorter<?> rowSorter = table.getRowSorter();
		if (rowSorter == null) {
			return null;
		}

		List<?> sortedColumns = rowSorter.getSortKeys();
		if (sortedColumns.size() > 0) {
			return (javax.swing.RowSorter.SortKey) sortedColumns.get(0);
		}
		return null;
	}

	@Override
	public java.awt.Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		setIcon(getIcon(table, column));
		setBorder(null);
		return this;
	}
}
