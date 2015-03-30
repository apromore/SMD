/*
 * Created on 16-02-2005
 *
 */
package org.prom5.framework.models.bpel4ws.type;

import org.prom5.framework.models.ModelGraph;

/**
 * @author Kristian Bisgaard Lassen
 */
public class BPEL4WS extends ModelGraph {

    /**
     *
     */
    public final String name;

    /**
     *
     */
    public final BPEL4WSProcess process;

    /**
     * @param name
     * @param process
     */
    public BPEL4WS (String name, BPEL4WSProcess process) {
		super(name);
        this.name = name;
        this.process = process;
    }
}
