package org.apromore.mining.utils;

import org.apromore.graph.JBPT.CPF;

public class CPFUtil {
	
	public static CPF clone(CPF cpf) {
		CPF clone = (CPF) cpf.clone();
		return clone;
	}

}
