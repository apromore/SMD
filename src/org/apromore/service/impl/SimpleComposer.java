package org.apromore.service.impl;

import org.apromore.graph.JBPT.CPF;
import org.apromore.service.GraphService;
import org.apromore.service.IComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("SimpleComposer")
@Transactional(propagation = Propagation.REQUIRED)
public class SimpleComposer implements IComposer {
	
	@Autowired @Qualifier("GraphService")
    private GraphService gSrv;
	
	private static final Logger log = LoggerFactory.getLogger(SimpleComposer.class);
	
	public CPF compose(String fragmentId) {
		log.debug("Composing the content of fragment " + fragmentId);
		CPF g = new CPF();
		gSrv.fillNodesByFragmentId(g, fragmentId);
		gSrv.fillEdgesByFragmentId(g, fragmentId);
		return g;
	}
}
