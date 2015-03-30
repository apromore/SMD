package org.prom6.plugins.guidetreeminer.swingx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

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
public class ErrorDialog extends JDialog implements ActionListener{
	private JButton okButton;
	
	private ErrorDialog(Frame owner, String message, Throwable e){
		super(owner, "Error", true);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
         
        ConsolePane consolePane = new ConsolePane();
        
        MutableAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setForeground(a, new Color(58, 106, 122));
        PrintWriter out = consolePane.createPrintWriter(a);
        
        if (message != null){
        	out.println("Message:  "+message);
        }
        if (e != null) {
            out.println("Exception: " + e.getMessage());
            e.printStackTrace(out);
        }
        out.close();
        
        getContentPane().add(consolePane, BorderLayout.CENTER);
        
        
        JPanel okButtonpanel = new JPanel();
        okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButtonpanel.add(okButton);
        
        getContentPane().add(okButtonpanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
//        setSize(320, 240);
        
	}
	public static void showErrorDialog(Component owner, String message,
			Throwable e) {
		ErrorDialog errorDialog = new ErrorDialog(getFrameForComponent(owner), message, e);
		errorDialog.setVisible(true);
	}
	 
	 public static void showErrorDialog(Component owner, String message) {
		ErrorDialog errorDialog = new ErrorDialog(getFrameForComponent(owner),
				message, null);
		errorDialog.setVisible(true);
	}
	 
	 private static Frame getFrameForComponent(Component c) {
		if (c == null)
			return null;
		if (c instanceof Frame)
			return (Frame) c;
		return getFrameForComponent(c.getParent());
	}
	 
	 public void actionPerformed(ActionEvent e) {
	        if (e.getSource() == okButton) 
	            dispose();
	    }
	
}
