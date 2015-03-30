package org.prom5.framework.models.logabstraction;

import java.util.List;

import org.prom5.analysis.log.scale.ProcessInstanceScale;
import org.prom5.framework.log.LogReader;
import org.prom5.framework.log.ProcessInstance;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface LogAbstractionFactory {

	public List<LogAbstraction> getAbstractions(LogReader log, ProcessInstanceScale scale);

	public LogAbstraction getAbstraction(LogReader log, ProcessInstance pi, ProcessInstanceScale scale);

}
