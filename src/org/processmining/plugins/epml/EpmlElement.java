package org.processmining.plugins.epml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

public class EpmlElement {

	/**
	 * The PNML tag for this element.
	 */
	private final String tag;

	private int lineNumber;

	/**
	 * Creates a fresh EPML element.
	 * 
	 * @param tag
	 */
	public EpmlElement(String tag) {
		this.tag = tag;
	}

	/**
	 * Imports the given element.
	 * 
	 * @param xpp
	 * @param epml
	 * @throws Exception
	 */
	public void importElement(XmlPullParser xpp, Epml epml) {
		lineNumber = xpp.getLineNumber();
		/*
		 * Import all attributes of this element.
		 */
		importAttributes(xpp, epml);
		/*
		 * Create afresh stack to keep track of start tags to match.
		 */
		ArrayList<String> stack = new ArrayList<String>();
		/*
		 * Add the current tag to this stack, as we still have to find the
		 * matching end tag.
		 */
		stack.add(tag);
		/*
		 * As long as the stack is not empty, we're still working on this
		 * object.
		 */
		while (!stack.isEmpty()) {
			try {
				/*
				 * Get next event.
				 */
				int eventType = xpp.next();
				if (eventType == XmlPullParser.END_DOCUMENT) {
					/*
					 * End of document. Should not happen.
					 */
					epml.log(tag, xpp.getLineNumber(), "Found end of document");
					return;
				} else if (eventType == XmlPullParser.START_TAG) {
					//System.out.println(xpp.getLineNumber() + " <" + xpp.getName() + ">");
					/*
					 * Start tag. Push it on the stack.
					 */
					stack.add(xpp.getName());
					/*
					 * If this tag is the second on the stack, then it is a
					 * direct child.
					 */
					if (stack.size() == 2) {
						/*
						 * For a direct child, check whether the tag is known.
						 * If so, take proper action. Note that this needs not
						 * to be done for other offspring.
						 */
						if (importElements(xpp, epml)) {
							/*
							 * Known start tag. The end tag has been matched and
							 * can be popped from the stack.
							 */
							stack.remove(stack.size() - 1);
						}
					}
				} else if ((eventType == XmlPullParser.END_TAG)) {
					//System.out.println(xpp.getLineNumber() + " </" + xpp.getName() + ">");
					/*
					 * End tag. Should be identical to top of the stack.
					 */
					if (xpp.getName().equals(stack.get(stack.size() - 1))) {
						/*
						 * Yes it is. Pop the stack.
						 */
						stack.remove(stack.size() - 1);
					} else {
						/*
						 * No it is not. XML violation.
						 */
						epml.log(tag, xpp.getLineNumber(), "Found " + xpp.getName() + ", expected "
								+ stack.get(stack.size() - 1));
					}
				} else if (eventType == XmlPullParser.TEXT) {
					/*
					 * Plain text. Import it.
					 */
					importText(xpp.getText(), epml);
				}
			} catch (Exception ex) {
				epml.log(tag, xpp.getLineNumber(), ex.getMessage());
				return;
			}
		}
		/*
		 * The element has been imported. Now is a good time to check its
		 * validity.
		 */
		checkValidity(epml);
	}

	/**
	 * Exports the element.
	 * 
	 * @return
	 */
	public String exportElement() {
		/*
		 * Export all attributes of this element.
		 */
		String s = "<" + tag;
		s += exportAttributes();
		/*
		 * Export all child elements.
		 */
		String t = exportElements();
		if (t.equals("")) {
			/*
			 * No child elements, use combined start-end tag.
			 */
			s += "/>\n";
		} else {
			/*
			 * Child elements, use separated start and end tags.
			 */
			s += ">\n" + t + "</" + tag + ">\n";
		}
		return s;
	}

	/**
	 * Imports all standard attributes: None. If some subclass has attributes,
	 * this method needs to be overruled by it.
	 * 
	 * @param xpp
	 * @param epml
	 */
	protected void importAttributes(XmlPullParser xpp, Epml epml) {
	}

	/**
	 * Exports all standard attributes: None. If some subclass has attributes,
	 * this method needs to be overruled by it.
	 * 
	 * @return
	 */
	protected String exportAttributes() {
		return "";
	}

	/**
	 * Imports all standard child elements: None. If some subclass has child
	 * elements, this method needs to be overruled by it.
	 * 
	 * @param xpp
	 * @param epml
	 * @return
	 */
	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		return false;
	}

	/**
	 * Exports all standard elements: None. If some subclass has child elements,
	 * this method needs to be overruled by it.
	 * 
	 * @return
	 */
	protected String exportElements() {
		return "";
	}

	/**
	 * Imports standard text: No action. If some subclass needs to import text,
	 * this method needs to be overruled by it.
	 * 
	 * @param text
	 * @param epml
	 */
	protected void importText(String text, Epml epml) {
	}

	/**
	 * Default way to export some attribute.
	 * 
	 * @param tag
	 *            The attribute tag.
	 * @param value
	 *            The attribute value.
	 * @return
	 */
	protected String exportAttribute(String tag, String value) {
		return " " + tag + "=\"" + value + "\"";
	}

	/**
	 * Default check for validity: No action. If some subclass needs to check
	 * validity, this method needs to be overruled by it.
	 * 
	 * @param epml
	 */
	protected void checkValidity(Epml epml) {
	}

	protected void checkPositiveInteger(Epml epml, String label, String value, boolean isRequired) {
		if (isRequired) {
			checkRequired(epml, label, value);
		}
		if (value != null) {
			try {
				Integer i = Integer.valueOf(value);
				if (i <= 0) {
					/*
					 * id should be positive integer.
					 */
					epml.log(tag, lineNumber, "Expected positive integer for " + label);
				}
			} catch (Exception ex) {
				epml.log(tag, lineNumber, ex.getMessage());
			}
		}
	}

	protected void checkPositiveIntegerOrK(Epml epml, String label, String value, boolean isRequired) {
		if (isRequired) {
			checkRequired(epml, label, value);
		}
		if ((value != null) && !value.equals("k")) {
			try {
				Integer i = Integer.valueOf(value);
				if (i <= 0) {
					/*
					 * id should be positive integer.
					 */
					epml.log(tag, lineNumber, "Expected positive integer or k for " + label);
				}
			} catch (Exception ex) {
				epml.log(tag, lineNumber, ex.getMessage());
			}
		}
	}

	protected void checkNonNegativeIntegerOrK(Epml epml, String label, String value, boolean isRequired) {
		if (isRequired) {
			checkRequired(epml, label, value);
		}
		if ((value != null) && !value.equals("k")) {
			try {
				Integer i = Integer.valueOf(value);
				if (i < 0) {
					/*
					 * id should be positive integer.
					 */
					epml.log(tag, lineNumber, "Expected non-negative integer or k for " + label);
				}
			} catch (Exception ex) {
				epml.log(tag, lineNumber, ex.getMessage());
			}
		}
	}

	protected void checkPositiveIntegers(Epml epml, String label, String value) {
		if (value != null) {
			String values[] = value.split(" ");
			for (int n = 0; n < values.length; n++) {
				try {
					Integer i = Integer.valueOf(n);
					if (i <= 0) {
						epml.log(tag, lineNumber, "Expected positive integers for " + label);
					}
				} catch (Exception ex) {
					epml.log(tag, lineNumber, ex.getMessage());
				}
			}
		}
	}

	protected void checkRestriction(Epml epml, String label, String value, List<String> restriction, boolean isRequired) {
		if (isRequired) {
			checkRequired(epml, label, value);
		}
		if ((value != null) && !restriction.contains(value)) {
			epml.log(tag, lineNumber, "Expected a value from " + restriction.toString() + " for " + label);
		}
	}

	protected void checkBoolean(Epml epml, String label, String value, boolean isRequired) {
		checkRestriction(epml, label, value, Arrays.asList("true", "false", "1", "0"), isRequired);
	}

	protected void checkDecimal(Epml epml, String label, String value, boolean isRequired) {
		if (isRequired) {
			checkRequired(epml, label, value);
		}
		if (value != null) {
			try {
				Double.valueOf(value);
			} catch (Exception ex) {
				epml.log(tag, lineNumber, ex.getMessage());
			}
		}
	}

	protected void checkURI(Epml epml, String label, String value, boolean isRequired) {
		if (isRequired) {
			checkRequired(epml, label, value);
		}
		if (value != null) {
			try {
				new URI(value);
			} catch (Exception ex) {
				epml.log(tag, lineNumber, ex.getMessage());
			}
		}
	}

	protected void checkRequired(Epml epml, String label, String value) {
		if (value == null) {
			epml.log(tag, lineNumber, "Expected " + label);
		}
	}

}
