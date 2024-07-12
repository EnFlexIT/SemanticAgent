package optiFlex;

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
	private String cidBase;
	protected static int cidCnt = 0;
	
	@Override
	protected void setup() {
		

		// -------------------------------------------------------------------
		// --- obligatory setup for a semantic agent -------------------------
		// -------------------------------------------------------------------
		
		// --- for now only one ontology is supported; this variable is used ------------
		// --- for the ontology field of ACL message objects and message filtering; -----
		this.ontologyName = "OptiFlexOntology";
		
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
		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase, trustedAgents);
		this.addBehaviour(this.owlMsgReceiveBehaviour);
		
		
		// -------------------------------------------------------------------
		// --- setup required for evaluation ---------------------------------
		// -------------------------------------------------------------------
		
		// --- Logger configuration --------------------------------------
		rootLogger.removeAllAppenders(); //unsch�ner workaround. sollte besser an zentraler Stelle erfolgen und nicht bei jedem Agenten individuell
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.INFO);			
		
		// Timeblocker 4s for setting up JADE sniffer
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
			
		// --- Evaluation methods ----------------------------
//		this.testSendInformBehaviour();
//		this.testSparqlQuery();
//		this.testSparqlConstruct();
		this.sendFlexLoadInfoToOptiAgent(); 
//		this.determineAllDerasThatControlBatteryStoragesAtSpecificBusbar2Col();
//		this.determineDerasControllingDersAtBusbar();
//		this.determineAllDerasThatControlBatteryStoragesAtSpecificBusbar();
//		this.determineLineSegmentsExceedingMaxCurrent();
//		this.determineMostRecentVoltageAtSpecificNode();
//		this.determineBusbarsWithLineSegmentsExceedingMaxCurrent();
//		this.updateFlexibilityPotential();
		
	}




	@Override
	protected void takeDown() {

		// --- Save the knowledge base model into a file, as the current implementation ---
		// --- does not use a persistent storage ------------------------------------------
		this.knowledgeBase.saveModel();
			
		// --- Close the knowledge base model ---------------------------------------------
		this.knowledgeBase.closeModel();
		
		rootLogger.removeAllAppenders(); //unsch�ner workaround. sollte besser an zentraler Stelle erfolgen und nicht bei jedem Agenten individuell		
				
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
	
	// -------------------------------------------------------------------
	// -------------------------------------------------------------------
	// ---------------- tested evaluation methods ------------------------
	// -------------------------------------------------------------------
	// -------------------------------------------------------------------
	
	
	private void testSparqlQuery() {
		
		String query = "SELECT ?x ?y WHERE {\n"
				+ "	?x :hasFlexibleLoad ?y .\n"
				+ "}";
				
		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
		String[][] queryResults = UtilityMethods.executeMultiColumnSelectQuery(queryPSS, this.knowledgeBase.getModel());
		
		rootLogger.debug(queryResults);
	}

	private void testSparqlConstruct() {						
			
		String query = ""
				+ "CONSTRUCT {?s ?p ?o}\n"
				+ "WHERE {\n"
				+ "    ?s ?p ?o {\n"
				+ "        SELECT ?s ?o\n"
				+ "        WHERE {\n"
				+ "            {\n"
				+ "                ?process :hasFlexibleLoad ?s.\n"
				+ "                FILTER (?process = :process1)\n"
				+ "            }\n"
				+ "            UNION\n"
				+ "            {\n"
				+ "                ?process :hasFlexibleLoad ?o.\n"
				+ "                FILTER (?process = :process1)\n"
				+ "            }\n"
				+ "            UNION \n"
				+ "            {\n"
				+ "                ?s :hasTriggerFlexibleLoad ?fl.\n"
				+ "                ?process :hasFlexibleLoad ?fl\n"
				+ "                FILTER (?process = :process1)\n"
				+ "            }\n"
				+ "        }        \n"
				+ "    }\n"
				+ "}";
				

		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
		String queryResults = UtilityMethods.executeConstructQuery(queryPSS, this.knowledgeBase.getModel());
//			this.knowledgeBase.printAllModelStatements();
		
		rootLogger.debug(queryResults);
	}

	private void sendFlexLoadInfoToOptiAgent() {
		String query = ""
				+ "CONSTRUCT {?s ?p ?o}\n"
				+ "WHERE {\n"
				+ "    ?s ?p ?o {\n"
				+ "        SELECT ?s ?o\n"
				+ "        WHERE {\n"
				+ "            {\n"
				+ "                ?process :hasFlexibleLoad ?s.\n"
				+ "                FILTER (?process = :process1)\n"
				+ "            }\n"
				+ "            UNION\n"
				+ "            {\n"
				+ "                ?process :hasFlexibleLoad ?o.\n"
				+ "                FILTER (?process = :process1)\n"
				+ "            }\n"
				+ "            UNION \n"
				+ "            {\n"
				+ "                ?s :hasTriggerFlexibleLoad ?fl.\n"
				+ "                ?process :hasFlexibleLoad ?fl\n"
				+ "                FILTER (?process = :process1)\n"
				+ "            }\n"
				+ "        }        \n"
				+ "    }\n"
				+ "}";

		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
		String queryResults = UtilityMethods.executeConstructQuery(queryPSS, knowledgeBase.getModel());
		
		SendInformBehaviour sendInformBehaviour = new SendInformBehaviour(communicationPartner, queryResults, this.generateArbitraryConversationId(), SemanticAgentProtocols.OWL_INFORM, ontologyName);
		this.addBehaviour(sendInformBehaviour);
		
	}
}

	
