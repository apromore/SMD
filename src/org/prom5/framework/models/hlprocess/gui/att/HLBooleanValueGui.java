package org.prom5.framework.models.hlprocess.gui.att;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.prom5.framework.models.hlprocess.att.HLBooleanValue;
import org.prom5.framework.models.hlprocess.gui.HLAttributeGui;
import org.prom5.framework.models.hlprocess.gui.HLAttributeValueGui;
import org.prom5.framework.util.GUIPropertyListEnumeration;

/**
 * GUI for boolean value.
 */
public class HLBooleanValueGui extends HLAttributeValueGui {

	protected GUIPropertyListEnumeration value;
	protected JPanel panel;
	
	/**
	 * Default Constructor
	 * @param parent the boolean distribution to be edited through this GUI
	 */
	public HLBooleanValueGui(HLBooleanValue val, HLAttributeGui theParent) {
		super(theParent);
		ArrayList<HLBooleanValue> values = new ArrayList<HLBooleanValue>();
		values.add(new HLBooleanValue(true));
		values.add(new HLBooleanValue(false));
		value = new GUIPropertyListEnumeration("Value",
				"Please select the value you want", 
				values, this, 200);
		if (val != null) {
			value.setValue(val);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 0));
			panel.add(value.getPropertyPanel());
		}
		return panel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui#getValue()
	 */
	public HLBooleanValue getValue() {
		return (HLBooleanValue) value.getValue();
	}
	
}
