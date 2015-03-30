package org.prom6.plugins.guidetreeminer.encoding;

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
public class EncodingNotFoundException extends Exception {
	String str;
	
	EncodingNotFoundException(String a){
		str = a;
	}
	
	public String toString(){
		return "Encoding Not Found For "+str;
	}

}
