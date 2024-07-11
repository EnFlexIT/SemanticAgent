package optiFlexIteration2;

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
import de.enflexit.awb.sa.core.SendInformBehaviour;
import de.enflexit.awb.sa.core.SendQueryBehaviour;
import de.enflexit.awb.sa.core.UtilityMethods;
import jade.core.AID;
import jade.core.Agent;


/**
 * @author Sebastian T�rsleff, Helmut Schmidt University;
 */
public class ProcessAgent extends Agent {


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

		// -------------------------------------------------------------------
		// --- obligatory setup for a semantic agent -------------------------
		// -------------------------------------------------------------------

		// --- for now only one ontology is supported; this variable is used ------------
		// --- for the ontology field of ACL message objects and message filtering; -----
		this.ontologyName = "OptiFlex_20220318";

		// --- specify ontology folder path and file name --------------------------
		String ontologyDirectory = Application.getProjectFocused().getProjectFolderFullPath()
				+ "knowledgeBases" + File.separator 
				+ Application.getProjectFocused().getSimulationSetupCurrent() + File.separator 
				+ this.getAID().getLocalName();
		String ontologyFileName = "OptiFlex_20220318.owl";

		// --- specify Base URI ------------------------
		String baseUri = "http://www.hsu-ifa.de/ontologies/OptiFlex#"; 

		// --- instantiate knowledge base with previously defined parameters -----------------
		OntModelSpec ontModelSpec = OntModelSpec.OWL_DL_MEM_RULE_INF; 
		this.knowledgeBase = new KnowledgeBase(this, ontologyDirectory, ontologyFileName, baseUri, ontModelSpec);

		// --- add individual namespaces --------------
		knowledgeBase.getNamespaceList().addNameSpace("", baseUri, false);

		// --- Determine communication partner ----------------
		this.communicationPartner = new AID("OptimizationAgent", AID.ISLOCALNAME);


		// --- Generate set of trusted agents used by OMRB for handling of incoming messages
		Set<AID> trustedAgents = Set.of(this.communicationPartner); 


		// --- add OWL message receive behavior ----------------------
//		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase, trustedAgents);
		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase);
		this.addBehaviour(this.owlMsgReceiveBehaviour);

		// --- Logger configuration --------------------------------------
		rootLogger.removeAllAppenders(); 
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.DEBUG);	

		// Timeblocker 2s for setting up JADE sniffer
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

		this.provideFlexibilityUpdate();
//		this.listEnumIndividuals(); 
//		this.showOntologyVersionIRI(); 
//		this.owlInferenceTest(); 
	}

	private void owlInferenceTest() {
		if(this.getLocalName().equals("ProcessAgent")) {
			String query = "SELECT DISTINCT ?fl WHERE {\n"
					+ "	?p rdf:type :Process .\n"
					+ "	?fl :isFlexibleLoadOf ?p .\n"
					+ "}";
			
					

			String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
			String[] queryResults = UtilityMethods.executeSingleColumnSelectQuery(queryPSS, knowledgeBase.getModel());
//			String[][] queryResults = UtilityMethods.executeMultiColumnSelectQuery(queryPSS, this.knowledgeBase.getModel());
//			this.knowledgeBase.printAllModelStatements();
			
			rootLogger.debug(queryResults);
		}
		
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


		rootLogger.removeAllAppenders(); 		

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

	private void provideFlexibilityUpdate() {
		
		String processName; 
		if(this.getLocalName().equals("ProcessAgent")) {
			processName = "process1";
		}
		else {
			processName = "process2";
		}

		String queryResults = null;

		String nodeStateQuery = "CONSTRUCT {?s ?p ?o}\n"
				+ "WHERE {\n"
				+ "    ?s ?p ?o {\n"
				+ "        SELECT ?s ?o\n"
				+ "        WHERE {\n"
				+ "            {\n"
				+ "                ?process :hasFlexibleLoad ?s.\n"
				+ "                FILTER (?process = :"+ processName +" )\n"
				+ "            }\n"
				+ "            UNION\n"
				+ "            {\n"
				+ "                ?process :hasFlexibleLoad ?o.\n"
				+ "                FILTER (?process = :"+ processName +")\n"
				+ "            }\n"
				+ "            UNION \n"
				+ "            {\n"
				+ "                ?s :hasTriggerFlexibleLoad ?fl.\n"
				+ "                ?process :hasFlexibleLoad ?fl\n"
				+ "                FILTER (?process = :"+ processName +")\n"
				+ "            }\n"
				+ "        }        \n"
				+ "    }\n"
				+ "}";


		String nodeStateQueryPSS = UtilityMethods.addPrefixesToSparqlQuery(nodeStateQuery, knowledgeBase);
		queryResults = UtilityMethods.executeConstructQuery(nodeStateQueryPSS, this.knowledgeBase.getModel());

		rootLogger.debug(queryResults);

		SendInformBehaviour sendInformBehaviour = new SendInformBehaviour(this.communicationPartner, queryResults, this.generateArbitraryConversationId(), ontologyName);
		this.addBehaviour(sendInformBehaviour);

	}

}
