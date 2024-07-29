package semanticAgent;

import java.io.File;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import agentgui.core.application.Application;
import de.enflexit.awb.sa.core.KnowledgeBase;
import de.enflexit.awb.sa.core.OwlMessageReceiveBehaviour;
import de.enflexit.awb.sa.core.SendInformBehaviour;
import de.enflexit.awb.sa.core.SendQueryBehaviour;
import de.enflexit.awb.sa.core.UtilityMethods;
import jade.core.AID;
import jade.core.Agent;


/**
 * This class is intended to serve as a template for the development of semantic agents. 
 * It includes all required setup steps and configurations to make use of the
 * semantic agent core classes and the underlying Jena framework integration. 
 * 
 * @author Sebastian Toersleff, Helmut Schmidt University
 */
public class SemanticAgentTemplate extends Agent {

	private static final long serialVersionUID = -2493803948645554649L;
	
	// --- Roots logger instatiation to support logging and debugging
	private static Logger rootLogger = Logger.getRootLogger();
	
	
	// --- obligatory variables for a semantic agent
	private KnowledgeBase knowledgeBase; // the KnowledgeBase is the container of the ontology and facilitates its management
	private OwlMessageReceiveBehaviour owlMsgReceiveBehaviour;
	private Set<AID> trustedAgents; // this set is used by the OWL message receive behavior to filter incoming messages
	private String ontologyName; //  this variable is used for the ontology field of ACL message objects and message filtering by the OwlMessageReceiveBehaviour
	
	
	private String cidBase;
	protected static int cidCnt = 0;
	
	@Override
	protected void setup() {
		

		// -------------------------------------------------------------------
		// --- setup for a semantic agent -------------------------
		// -------------------------------------------------------------------
		
		// --- specify the name of the ontology for use in ACL messages and message filtering ------
		this.ontologyName = "SemanticAgentTestOntology"; 
		
		// --- specify ontology folder path and file name (the example uses the folder of the AWB project) -----------
		String ontologyDirectory = Application.getProjectFocused().getProjectFolderFullPath()
				+ "knowledgeBases" + File.separator 
				+ Application.getProjectFocused().getSimulationSetupCurrent() + File.separator 
				+ this.getAID().getLocalName();
		String ontologyFileName = "SemanticAgentTestOntology.owl";
		
		// --- specify Base URI -------------------------------------
		String baseUri = "http://www.hsu-ifa.de/ontologies/SemanticAgentTestOntology#"; 
		
		// --- instantiate KnowledgeBase with previously specified parameters -----------------
		OntModelSpec ontModelSpec = OntModelSpec.OWL_DL_MEM_RULE_INF; 
		this.knowledgeBase = new KnowledgeBase(this, ontologyDirectory, ontologyFileName, baseUri, ontModelSpec);
		
		// --- add non-generic namespaces --------------
		knowledgeBase.getNamespaceList().addNameSpace("", baseUri);

		// --- Populate set of trusted agents ----------------------------
		// trustedAgents.add(new AID("NameOfAgent1", AID.ISLOCALNAME)); 
		// trustedAgents.add(new AID("NameOfAgent2", AID.ISLOCALNAME)); 

		// --- instantiate and add OWL message receive behavior ----------------------
		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase, this.trustedAgents);
		this.addBehaviour(this.owlMsgReceiveBehaviour);
		
		// --- Logger configuration --------------------------------------
		rootLogger.removeAllAppenders(); 
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.DEBUG);			
		
		// --- Timeblocker 2s for setting up JADE sniffer --- 
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
			
		// --- Evaluation method ----------------------------
//		this.knowledgeBase.printAllModelStatements();
		this.testSparqlQuery();

	
	}

	@Override
	protected void takeDown() {

		// --- Save the knowledge base model into a file, as the current implementation ---
		// --- does not use a persistent storage ------------------------------------------
		this.knowledgeBase.saveModel();
			
		// --- Close the knowledge base model ---------------------------------------------
		this.knowledgeBase.closeModel();
		
		rootLogger.removeAllAppenders(); 
				
		super.takeDown();
	}
	
	/**
	 * Method to generate a unique conversation ID for ACL messages
	 * @return String with a unique conversation ID
	 */
	public String generateArbitraryConversationId() {
		
		if (cidBase==null) {
			cidBase = this.getLocalName() + hashCode() + System.currentTimeMillis()%10000 + "_";		
		}
		return cidBase + (cidCnt++);
	}
	
	private void testSparqlQuery() {
						
		String query = "SELECT ?instance ?class WHERE {\n"
				+ "	?instance rdfs:subClassOf ?class. \n"
				+ "}";

		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
		String[][] queryResults = UtilityMethods.executeMultiColumnSelectQuery(queryPSS, this.knowledgeBase.getModel());
		
		rootLogger.debug(queryResults);
	}
	
}
