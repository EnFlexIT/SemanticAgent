package peakCaseStudyV1;

import java.io.File;
import java.util.Set;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import agentgui.core.application.Application;
import de.enflexit.awb.sa.core.KnowledgeBase;
import de.enflexit.awb.sa.core.OwlMessageReceiveBehaviour;
import de.enflexit.awb.sa.core.SemanticAgentProtocols;
import de.enflexit.awb.sa.core.SendInformBehaviour;
import de.enflexit.awb.sa.core.SendQueryBehaviour;
import de.enflexit.awb.sa.core.UtilityMethods;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


/**
 * @author Sebastian T�rsleff, Helmut Schmidt University;
 */
public class ProsumerAgent extends Agent {


	// --- static variables -----------------------------
	private static final long serialVersionUID = 3164333512327730902L;
	private static Logger rootLogger = Logger.getRootLogger();

	// --- obligatory variables for a semantic agent
	private KnowledgeBase knowledgeBase;
	private OwlMessageReceiveBehaviour owlMsgReceiveBehaviour;
	private String ontologyName; 

	// --- variables required for evaluation
	private AID communicationPartner;

	// --- conversation ID variables
	private String cidBase;
	protected static int cidCnt = 0;


	@Override
	protected void setup() {
		
		// Timeblocker 1s for setting up logger
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

		// -------------------------------------------------------------------
		// --- obligatory setup for a semantic agent -------------------------
		// -------------------------------------------------------------------
		
		rootLogger.info("Agent "+ this.getAID().getLocalName() + ": performing setup routine"); 

		// --- for now only one ontology is supported; this variable is used ------------
		// --- for the ontology field of ACL message objects and message filtering; -----
		this.ontologyName = "PEAKv1";

		// --- specify ontology folder path and file name --------------------------
		String ontologyDirectory = Application.getProjectFocused().getProjectFolderFullPath()
				+ "knowledgeBases" + File.separator 
				+ Application.getProjectFocused().getSimulationSetupCurrent() + File.separator 
				+ this.getAID().getLocalName();
		String ontologyFileName = "peakv1taskfit_forProsumerAgent.owl";

		// --- specify Base URI ------------------------
		String baseUri = "https://www.hsu-ifa.de/ontologies/peakv1taskfit#"; 

		// --- instantiate knowledge base with previously defined parameters -----------------
		OntModelSpec ontModelSpec = OntModelSpec.OWL_MEM_MICRO_RULE_INF;
		this.knowledgeBase = new KnowledgeBase(this, ontologyDirectory, ontologyFileName, baseUri, ontModelSpec);
		

		// --- add individual namespaces --------------
		knowledgeBase.getNamespaceList().addNameSpace("", baseUri);
		knowledgeBase.getNamespaceList().addNameSpace("s4ener", "https://saref.etsi.org/saref4ener/");
		knowledgeBase.getNamespaceList().addNameSpace("ao", "https://www.hsu-ifa.de/ontologies/PeakEvaluation01AlignmentOntology#");
		knowledgeBase.getNamespaceList().addNameSpace("s4enerext", "https://www.hsu-ifa.de/ontologies/SAREF4ENERExtension#");
		knowledgeBase.getNamespaceList().addNameSpace("topo", "https://www.hsu-ifa.de/ontologies/LVGridTopology#");
		

		// --- Determine communication partner ----------------
//		this.communicationPartner = new AID("OptimizationAgent", AID.ISLOCALNAME);


		// --- Generate set of trusted agents used by OMRB for handling of incoming messages
//		Set<AID> trustedAgents = Set.of(this.communicationPartner); 


		// --- add OWL message receive behavior ----------------------
//		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase, trustedAgents);
//		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase);
//		this.addBehaviour(this.owlMsgReceiveBehaviour);

		// --- Logger configuration --------------------------------------
//		rootLogger.removeAllAppenders(); 
//		SimpleLayout layout = new SimpleLayout();
//		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
//		rootLogger.addAppender(consoleAppender);
//		rootLogger.setLevel(Level.INFO);	

		
		MessageTemplate msgTemplate = MessageTemplate.and(
			MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
			MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.CFP), 
				MessageTemplate.MatchOntology(this.ontologyName)
			)
		); 
				
		this.addBehaviour(new SemanticContractNetResponder(this, msgTemplate));
		
//		this.testSelectQuery(); 

	}

	private void testSelectQuery() {
		
		String query = "        select ?fo where { \n"
				+ "			?fo s4ener:relatesToRequest :fr02 .\n"
				+ "		}";
		


		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
		String[] queryResults = UtilityMethods.executeSingleColumnSelectQuery(queryPSS, knowledgeBase.getModel());
		
		rootLogger.debug(queryResults);
		
	}

	private void showOntologyVersionIRI() {
		
		if(this.getLocalName().equals("ProcessAgent")) {
			String query = "SELECT DISTINCT ?ip WHERE {\n"
					+ "	?ip rdf:type owl:IrreflexiveProperty .\n"
					+ "}";
			
					

			String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
			String[] queryResults = UtilityMethods.executeSingleColumnSelectQuery(queryPSS, knowledgeBase.getModel());
//			String[][] queryResults = UtilityMethods.executeMultiColumnSelectQuery(queryPSS, this.knowledgeBase.getModel());
//			this.knowledgeBase.printAllModelStatements();
			
			rootLogger.debug(queryResults);
		}
		
	}

	private void listEnumIndividuals() {
		
		if(this.getLocalName().equals("ProcessAgent")) {
			String query = "SELECT DISTINCT ?tt WHERE {\n"
					+ "	?tt rdf:type :TemporalType .\n"
					+ "}";
			
					

			String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
			String[] queryResults = UtilityMethods.executeSingleColumnSelectQuery(queryPSS, knowledgeBase.getModel());
//			String[][] queryResults = UtilityMethods.executeMultiColumnSelectQuery(queryPSS, this.knowledgeBase.getModel());
//			this.knowledgeBase.printAllModelStatements();
			
			rootLogger.debug(queryResults);
		}
	}

	@Override
	protected void takeDown() {

		// --- Save the knowledge base model into a file, as the current implementation ---
		// --- does not use a persistent storage ------------------------------------------
		this.knowledgeBase.saveModel();

		// --- Close the knowledge base model ---------------------------------------------
		this.knowledgeBase.closeModel();


//		rootLogger.removeAllAppenders(); 		

		super.takeDown();
	}

	/**
	 * Method to generate a unique conversation ID for ACL messages
	 * @return String with the conversation ID
	 */
	public String generateArbitraryConversationId() {
		if (cidBase==null) {
			cidBase = this.getLocalName() + hashCode() +System.currentTimeMillis()%10000 + "_";		
		}
		return cidBase + (cidCnt++);
	}
	
	protected KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	protected void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}
}
