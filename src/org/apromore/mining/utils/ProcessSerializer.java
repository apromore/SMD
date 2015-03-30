package org.apromore.mining.utils;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apromore.dao.ProcessDao;
import org.apromore.dao.model.Process;
import org.apromore.graph.JBPT.CPF;
import org.apromore.service.RepositoryService;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ProcessSerializer {

	private static final Logger logger = LoggerFactory.getLogger(ProcessSerializer.class);
	
	@Autowired
	private RepositoryService rsrv;
	
	@Autowired @Qualifier("ProcessDao")
	private ProcessDao pdao;
	
	private FormattableEPCSerializer formattableEPCSerializer = new FormattableEPCSerializer();
	
	public void serializeAllProcesses(String outPath) {
	
		try {
			File outFolder = new File(outPath);
			FileUtils.cleanDirectory(outFolder);
//			if (outFolder.exists()) {
//				FileUtils.cleanDirectory(outFolder);
//			} else {
//				outFolder.mkdir();
//			}
			
			int serializedProcessesCount = 0;
			List<Process> ps = pdao.getProcessesJDBC();
			logger.debug("Serializing {} processes from the repository.", ps.size());
			for (Process p : ps) {
				
				try {
					CPF model = rsrv.getCurrentProcessModel(p.getName(), false);
					File pfile = new File(outPath, p.getName() + ".epml");
					try {
						formattableEPCSerializer.serialize(model, pfile.getAbsolutePath());
						serializedProcessesCount++;
					} catch (Exception ne) {
						logger.error("Error while adding the process model: " + pfile.getName());
					}
				} catch (Exception e) {
					logger.error("Failed to serialize process {} due to {}. Continueing with serializing other processes...", p.getName(), e.getMessage());
				}
					
			}
			logger.debug("Serialized {} processes out of {}.", serializedProcessesCount, ps.size());
		} catch (Exception e) {
			logger.error("Failed to serialize processes: {}.", e.getMessage());
			logger.error("Error: ", e);
		}
		
	}

}
