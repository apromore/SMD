package org.apromore.service.impl;

import org.apache.commons.io.IOUtils;
import org.apromore.TestData;
import org.apromore.common.Constants;
import org.apromore.dao.AnnotationDao;
import org.apromore.dao.NativeDao;
import org.apromore.dao.ProcessModelVersionDao;
import org.apromore.dao.model.NativeType;
import org.apromore.dao.model.ProcessBranch;
import org.apromore.dao.model.ProcessModelVersion;
import org.apromore.dao.model.User;
import org.apromore.exception.ExceptionDao;
import org.apromore.exception.ExportFormatException;
import org.apromore.exception.ImportException;
import org.apromore.exception.LockFailedException;
import org.apromore.graph.JBPT.CPF;
import org.apromore.mining.utils.CPFTransformer;
import org.apromore.mining.utils.CPFtoMultiDirectedGraphConverter;
import org.apromore.mining.utils.CycleRemover;
import org.apromore.mining.utils.SingleTerminalCycleFormer;
import org.apromore.model.ProcessSummariesType;
import org.apromore.model.ProcessSummaryType;
import org.apromore.service.CanoniserService;
import org.apromore.service.FormatService;
import org.apromore.service.ProcessService;
import org.apromore.service.RepositoryService;
import org.apromore.service.UserService;
import org.apromore.service.helper.UIHelper;
import org.apromore.service.model.CanonisedProcess;
import org.apromore.service.search.SearchExpressionBuilder;
import org.apromore.service.utils.EPCDeserializer;
import org.apromore.service.utils.EPCSerializer;
import org.apromore.service.utils.FormattableEPCSerializer;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.graph.algo.DirectedGraphAlgorithms;
import org.jbpt.hypergraph.abs.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Implementation of the UserService Contract.
 *
 * @author <a href="mailto:cam.james@gmail.com">Cameron James</a>
 */
@Service("ProcessService")
@Transactional(propagation = Propagation.REQUIRED)
public class ProcessServiceImpl implements ProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessServiceImpl.class);

    @Autowired @Qualifier("AnnotationDao")
    private AnnotationDao annDao;
    @Autowired @Qualifier("NativeDao")
    private NativeDao natDao;
    @Autowired @Qualifier("ProcessModelVersionDao")
    private ProcessModelVersionDao pmvDao;

    @Autowired @Qualifier("CanoniserService")
    private CanoniserService canSrv;
    @Autowired @Qualifier("UserService")
    private UserService usrSrv;
    @Autowired @Qualifier("FormatService")
    private FormatService fmtSrv;
    @Autowired @Qualifier("RepositoryService")
    private RepositoryService rSrv;
    @Autowired @Qualifier("UIHelper")
    private UIHelper uiSrv;
    
    @Override
	public String getRootFragmentId(String processName) {
    	ProcessModelVersion pmv = pmvDao.getCurrentProcessModelVersion(processName, Constants.TRUNK_NAME);
    	return pmv.getRootFragmentVersionId();
	}
    
    @Override
	public String getRootFragmentId(int processId) {
		return pmvDao.getCurrentRootFragmentId(processId, "1.0");
	}

	/**
     * @see org.apromore.service.ProcessService#readProcessSummaries(String)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProcessSummariesType readProcessSummaries(String searchExpression) {
        ProcessSummariesType processSummaries = null;

        try {
            // Firstly, do we need to use the searchExpression
            SearchExpressionBuilder seb = new SearchExpressionBuilder();
            String conditions = seb.buildSearchConditions(searchExpression);
            LOGGER.debug("Search Expression Builder output: " + conditions);

            // Now... Build the Object tree from this list of processes.
            processSummaries = uiSrv.buildProcessSummaryList(conditions, null);
        } catch (UnsupportedEncodingException usee) {
            LOGGER.error("Failed to get Process Summaries: " + usee.toString());
        }

        return processSummaries;
    }
    
    /**
     * Exports the process as an EPML string. This method should be removed once the proper export method is working.
     * 
     * @param processName
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public String exportProcessAsEPML(String processName) {
    	String epml = null;
    	try {
			CPF cpf = rSrv.getCurrentProcessModel(processName, "1.0", false);
//			epml = new FormattableEPCSerializer().serializeToString(cpf);
			epml = new EPCSerializer().serializeToString(cpf);
		} catch (Exception e) {
			LOGGER.error("Failed to export process as EPML.", e);
			e.printStackTrace();
		}
    	return epml;
    }


    /**
     * @see org.apromore.service.ProcessService#exportFormat(String, Integer, String, String, String, boolean)
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public DataSource exportFormat(final String name, final Integer processId, final String version, final String format,
            final String annName, boolean withAnn) throws ExportFormatException {
        DataSource ds;
        try {
            CPF cpf = rSrv.getCurrentProcessModel(name, version, false);

            // Get the Canonical Model
            if (withAnn && annName.equals(Constants.INITIAL_ANNOTATION)) {
                ds = new ByteArrayDataSource(natDao.getNative(processId, version, format).getContent(), "text/xml");
            } else if (format.startsWith(Constants.ANNOTATIONS)) {
                String type = format.substring(Constants.ANNOTATIONS.length() + 3, format.length());
                ds = new ByteArrayDataSource(annDao.getAnnotation(processId, version, type).getContent(), "text/xml");
            } else {
                if (withAnn) {
                    String annotation = annDao.getAnnotation(processId, version, annName).getContent();
                    DataSource anf = new ByteArrayDataSource(annotation, "text/xml");
                    ds = canSrv.deCanonise(processId, version, format, canSrv.serializeCPF(cpf), anf);
                } else {
                    ds = canSrv.deCanonise(processId, version, format, canSrv.serializeCPF(cpf), null);
                }
            }
        } catch (Exception e) {
            throw new ExportFormatException(e.getMessage(), e.getCause());
        }
        return ds;
    }


    /**
     * @see org.apromore.service.ProcessService#importProcess(String, String, String, String, String, DataHandler, String, String, String, String)
     * {@inheritDoc}
     */
    @Override
    public ProcessSummaryType importProcess(String username, String processName, String cpfURI, String version, String natType,
            DataHandler cpf, String domain, String documentation, String created, String lastUpdate) throws ImportException {
        LOGGER.info("Executing operation canoniseProcess");
        ProcessSummaryType pro;

        try {
            CanonisedProcess cp = canSrv.canonise(natType, cpfURI, cpf.getInputStream());

            User user = usrSrv.findUser(username);
            NativeType nativeType = fmtSrv.findNativeType(natType);
            CPF pg = canSrv.deserializeCPF(cp.getCpt());

            ProcessModelVersion pmv = rSrv.addProcessModel(processName, version, user.getUsername(), cpfURI, nativeType.getNatType(),
                    domain, documentation, created, lastUpdate, pg);
            fmtSrv.storeNative(processName, version, pmv, cpf.getInputStream(), created, lastUpdate, user, nativeType, cp);
            pro = uiSrv.createProcessSummary(processName, pmv.getProcessModelVersionId(), version, nativeType.getNatType(), domain,
                    created, lastUpdate, user.getUsername());
        } catch (Exception e) {
            LOGGER.error("Canonisation Process Failed: " + e.toString());
            throw new ImportException(e);
        }

        return pro;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void addProcessModel(String processName, String epmlString) throws ImportException {
    	
    	DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>(); 
    	
    	String username = "chathura";
        String domain = "General";
        String created = "12/12/2011";
        String lastUpdate = "12/12/2011";
    	
    	try {
    		// duplicate inputstreams have to fixed by using a data handler
    		BufferedInputStream fileIn1 = new BufferedInputStream(IOUtils.toInputStream(epmlString));
    		BufferedInputStream fileIn2 = new BufferedInputStream(IOUtils.toInputStream(epmlString));
    		BufferedInputStream fileIn3 = new BufferedInputStream(IOUtils.toInputStream(epmlString));
    	
    		String documentation = "";
    		String cpfURI = newCpfURI();
    		String natType = Constants.EPML_2_0;
    		String version = "1.0";

    		LOGGER.debug("Adding process model: {}", processName);
    		
            CanonisedProcess cp = canSrv.canonise(natType, cpfURI, fileIn1);

            User user = usrSrv.findUser(username);
            NativeType nativeType = fmtSrv.findNativeType(natType);
            
            CPF pg = new EPCDeserializer().deserializeInputStream(fileIn3);
            
            MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(pg);
            if (!algo.isMultiTerminal(mdg)) {
            	System.out.println("Process model is not multiterminal.");
            	throw new ImportException("Process model is not multiterminal.");
            }
            

            ProcessModelVersion pmv = rSrv.addProcessModel(processName, version, user.getUsername(), cpfURI, nativeType.getNatType(),
                    domain, documentation, created, lastUpdate, pg);
            fmtSrv.storeNative(processName, version, pmv, fileIn2, created, lastUpdate, user, nativeType, cp);
//            pro = uiSrv.createProcessSummary(processName, pmv.getProcessModelVersionId(), version, nativeType.getNatType(), domain,
//                    created, lastUpdate, user.getUsername());
            
            // TODO: remove test code
//            Random r = new Random();
//            if (r.nextInt(10) < 4) {
//            	throw new ImportException("This is a random error for test purpose.");
//            }
            
        } catch (Exception e) {
            LOGGER.error("Process Import failed for process " + processName + " : " + e.toString(), e);
            throw new ImportException(e);
        }
    }
    
    @Override
    public List<String> importProcesses(String username, String folderPath, String domain, String documentation, String created, String lastUpdate) throws ImportException {
        LOGGER.info("Executing operation canoniseProcess");
        ProcessSummaryType pro = null;
        List<String> importedProcesses = new ArrayList<String>();
        
        DirectedGraphAlgorithms<DirectedEdge, Vertex> algo = new DirectedGraphAlgorithms<DirectedEdge, Vertex>(); 
        
        File dir = new File(folderPath);
        File[] files = dir.listFiles();
        int totalFiles = files.length;
        int currentFileNumber = 0;
        for (File file : files) {
        	currentFileNumber++;
        	String processName = file.getName().substring(0, file.getName().lastIndexOf("."));
        	try {
        		// duplicate inputstreams have to fixed by using a data handler
        		BufferedInputStream fileIn1 = new BufferedInputStream(new FileInputStream(file));
        		BufferedInputStream fileIn2 = new BufferedInputStream(new FileInputStream(file));
        		BufferedInputStream fileIn3 = new BufferedInputStream(new FileInputStream(file));
        	
        		String cpfURI = newCpfURI();
        		String natType = Constants.EPML_2_0;
        		String version = "1.0";

        		System.out.println("Adding process model: " + processName + ". Process " + currentFileNumber + " of " + totalFiles);
        		
                CanonisedProcess cp = canSrv.canonise(natType, cpfURI, fileIn1);

                User user = usrSrv.findUser(username);
                NativeType nativeType = fmtSrv.findNativeType(natType);
                
                CPF pg = new EPCDeserializer().deserializeInputStream(fileIn3);
                
                // TODO: TEST CODE TO BE COMMENTED OUT
//                CPFTransformer.correct(pg);
//                MultiDirectedGraph mdg2 = CPFtoMultiDirectedGraphConverter.covert(pg);
//                CycleRemover.removeCycles(pg, mdg2, false);
//                FormattableEPCSerializer serializer = new FormattableEPCSerializer();
//       		 	String outpath = "/home/cn/eps/bpm-demo-new1/bpm-demo/Apromore-Core/apromore-service/data/t2/fs/ff1.epml";
//       		 	serializer.serialize(pg, outpath);
//                SingleTerminalCycleFormer.formSingleTerminalCycles(pg);
                // END OF TEST CODE
                
                
//                CPF pg = canSrv.deserializeCPF(cp.getCpt());
                
                MultiDirectedGraph mdg = CPFtoMultiDirectedGraphConverter.covert(pg);
                if (!algo.isMultiTerminal(mdg)) {
                	System.out.println("Process model is not multidirected.");
                	continue;
                }
                

                ProcessModelVersion pmv = rSrv.addProcessModel(processName, version, user.getUsername(), cpfURI, nativeType.getNatType(),
                        domain, documentation, created, lastUpdate, pg);
                fmtSrv.storeNative(processName, version, pmv, fileIn2, created, lastUpdate, user, nativeType, cp);
                pro = uiSrv.createProcessSummary(processName, pmv.getProcessModelVersionId(), version, nativeType.getNatType(), domain,
                        created, lastUpdate, user.getUsername());
                importedProcesses.add(processName);
            } catch (Exception e) {
                LOGGER.error("Process Import failed for process " + processName + " : " + e.toString(), e);
                throw new ImportException(e);
            }
        }
        
        return importedProcesses;
    }
    
    private static String newCpfURI() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmsSSS");
        Date date = new Date();
        return dateFormat.format(date);
    }


    /**
     * @see org.apromore.service.ProcessService#addProcessModelVersion(ProcessBranch, String, int, String, int, int)
     * {@inheritDoc}
     */
    @Override
    public ProcessModelVersion addProcessModelVersion(ProcessBranch branch, String rootFragmentVersionId, int versionNumber,
            String versionName, int numVertices, int numEdges) throws ExceptionDao {
        ProcessModelVersion pmv = new ProcessModelVersion();

        pmv.setProcessBranch(branch);
        pmv.setRootFragmentVersionId(rootFragmentVersionId);
        pmv.setVersionNumber(versionNumber);
        pmv.setVersionName(versionName);
        pmv.setNumVertices(numVertices);
        pmv.setNumEdges(numEdges);

        pmvDao.save(pmv);
        return pmv;
    }








    /**
     * Set the Annotation DAO object for this class. Mainly for spring tests.
     * @param annDAOJpa the Annotation Dao.
     */
    public void setAnnotationDao(AnnotationDao annDAOJpa) {
        annDao = annDAOJpa;
    }

    /**
     * Set the Process Model Version DAO object for this class. Mainly for spring tests.
     * @param pmvDAOJpa the process model version
     */
    public void setProcessModelVersionDao(ProcessModelVersionDao pmvDAOJpa) {
        pmvDao = pmvDAOJpa;
    }

    /**
     * Set the Native DAO object for this class. Mainly for spring tests.
     * @param natDAOJpa the Native Dao.
     */
    public void setNativeDao(NativeDao natDAOJpa) {
        natDao = natDAOJpa;
    }


    /**
     * Set the Canoniser Service for this class. Mainly for spring tests.
     * @param canSrv the service
     */
    public void setCanoniserService(CanoniserService canSrv) {
        this.canSrv = canSrv;
    }

    /**
     * Set the User Service for this class. Mainly for spring tests.
     * @param usrSrv the service
     */
    public void setUserService(UserService usrSrv) {
        this.usrSrv = usrSrv;
    }

    /**
     * Set the Format Service for this class. Mainly for spring tests.
     * @param fmtSrv the service
     */
    public void setFormatService(FormatService fmtSrv) {
        this.fmtSrv = fmtSrv;
    }

    /**
     * Set the Repository Service for this class. Mainly for spring tests.
     * @param rSrv the service
     */
    public void setRepositoryService(RepositoryService rSrv) {
        this.rSrv = rSrv;
    }

    /**
     * Set the Repository Service for this class. Mainly for spring tests.
     * @param newUISrv the service
     */
    public void setUIHelperService(UIHelper newUISrv) {
        this.uiSrv = newUISrv;
    }
}
