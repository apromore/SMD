package org.prom6.plugins.guidetreeminer.swingx;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

@SuppressWarnings("serial")
public class ConsolePane extends JScrollPane {
	private JTextComponent outputArea;
	
	 public ConsolePane() {
	        super();
	        outputArea = new JTextPane();
	        outputArea.setEditable(false);
	        JViewport viewPort = getViewport();
	        viewPort.add(outputArea);
	 }

	 /**
	 * Create a PrintStream that will display in the console using the given
	 * attributes.
	 */
	public PrintStream createPrintStream(AttributeSet a) {
		Document doc = outputArea.getDocument();
		OutputStream out = new DocumentOutputStream(doc,a);
		PrintStream pOut = new PrintStream(out);

		return pOut;
	}

	/**
	 * Create a PrintWriter that will display in the console using the given
	 * attributes.
	 */
	public PrintWriter createPrintWriter(AttributeSet a) {
		Document doc = outputArea.getDocument();
		Writer out = new DocumentWriter(doc, a);
		PrintWriter pOut = new PrintWriter(out);

		return pOut;
	}
	
	/**
     * Clear the document.
     */
    public void clear() throws Exception { 
        Document doc = outputArea.getDocument();

        doc.remove(0, doc.getLength());
    }

    /**
     * Fetch the component used for the output.  This
     * allows further parsing of the output if desired,
     * and allows things like mouse listeners to be 
     * attached.  This can be useful for things like 
     * compiler output where clicking on an error 
     * warps another view to the location of the error.
     */
    public JTextComponent getOutputArea() {
        return outputArea;
    }
}
