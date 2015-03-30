package org.processmining.plugins.epml;

/**
 * @author hverbeek
 * 
 */
public class EpmlExpression extends EpmlElement {

	public final static String TAG = "expression";

	/*
	 * Raw contents
	 */
	private String text;

	public EpmlExpression() {
		super(TAG);

		text = "";
	}

	protected void importText(String text, Epml epml) {
		this.text = (this.text + text).trim();
	}

	protected String exportElements() {
		return text;
	}

}
