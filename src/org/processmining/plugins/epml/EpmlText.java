package org.processmining.plugins.epml;

public class EpmlText extends EpmlElement {

	private String text;

	public EpmlText(String tag) {
		super(tag);
		text = "";
	}

	protected void importText(String text, Epml epml) {
		this.text = (this.text + text).trim();
	}

	protected String exportElements() {
		return text;
	}

	public String get() {
		return text;
	}
}
