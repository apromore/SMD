package org.prom5.framework.models.bpel4ws.type.activity;

import org.prom5.framework.models.bpel4ws.type.BPEL4WS;
/**
 * @author Kristian Bisgaard Lassen
 *
 */
public abstract class Composed extends Activity {

    /**
     * @param name
     */
    public Composed (String name) {
        super(name);
    }

	public abstract String writeToDot(BPEL4WS model);
}
