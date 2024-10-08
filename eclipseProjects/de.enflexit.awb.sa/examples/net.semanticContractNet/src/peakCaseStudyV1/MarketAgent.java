package peakCaseStudyV1;

import java.io.File;
import java.util.Date;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.vocabulary.RDFS;
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


/**
 * @author Sebastian Törsleff, Helmut Schmidt University
 */
public class MarketAgent extends Agent {


	// --- static variables -----------------------------
	private static final long serialVersionUID = 3164333512327730902L;
	private static Logger rootLogger = Logger.getRootLogger();


	// --- obligatory variables for a semantic agent
	private KnowledgeBase knowledgeBase;
	private OwlMessageReceiveBehaviour owlMsgReceiveBehaviour;
	private String ontologyName; 

	// --- variables required for evaluation
	private AID communicationPartner;
	private AID communicationPartnerTwo;
	
	// --- conversation ID variables
	private String cidBase;
	protected static int cidCnt = 0;



	@Override
	protected void setup() {
		
		// --- Logger configuration --------------------------------------
		rootLogger.removeAllAppenders(); 
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		rootLogger.addAppender(consoleAppender);
		rootLogger.setLevel(Level.DEBUG);		

		// Timeblocker 2s for setting up JADE sniffer
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		

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
		String ontologyFileName = "PEAKv2_taskfit_forMarketAgent.xml";

		// --- specify Base URI ------------------------
		String baseUri = "https://www.hsu-ifa.de/ontologies/PeakEvaluation02taskfit#"; 

		// --- instantiate knowledge base with previously defined parameters -----------------
		OntModelSpec ontModelSpec = OntModelSpec.OWL_MEM_MICRO_RULE_INF;
		this.knowledgeBase = new KnowledgeBase(this, ontologyDirectory, ontologyFileName, baseUri, ontModelSpec);

		// --- add individual namespaces --------------
		knowledgeBase.getNamespaceList().addNameSpace("", baseUri);
		knowledgeBase.getNamespaceList().addNameSpace("s4ener", "https://saref.etsi.org/saref4ener/");
		knowledgeBase.getNamespaceList().addNameSpace("ao", "https://www.hsu-ifa.de/ontologies/PeakEvaluation01AlignmentOntology#");
		knowledgeBase.getNamespaceList().addNameSpace("s4enerext", "https://www.hsu-ifa.de/ontologies/SAREF4ENERExtension#");
		knowledgeBase.getNamespaceList().addNameSpace("topo", "https://www.hsu-ifa.de/ontologies/LVGridTopology#");
		knowledgeBase.getNamespaceList().addNameSpace("obv", "http://www.hsu-ifa.de/ontologies/OrderBookValues#");
		

		// --- Determine communication partner ----------------
//		this.communicationPartner = new AID("ProcessAgent", AID.ISLOCALNAME);
//		this.communicationPartnerTwo = new AID("ProcessAgent2", AID.ISLOCALNAME);

		// --- Generate set of trusted agents used by OMRB for handling of incoming messages
//		Set<AID> trustedAgents = Set.of(this.communicationPartner, this.communicationPartnerTwo); 

		// --- add OWL message receive behavior ----------------------
		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase);
		this.addBehaviour(this.owlMsgReceiveBehaviour);
		
		
		
	    
	    this.testSemanticContractNet(); 

	}
	
	private void testSemanticContractNet() {
		// -------------------------------------------------------------------
		// --- get latest flexibility request -------------------------
		// -------------------------------------------------------------------
		
		String query = "CONSTRUCT {\n"
				+ "  ?ind ?p ?o .\n"
				+ "  ?s ?p ?ind .\n"
				+ "  ?interval ?dp ?dpv. \n"
				+ "}\n"
				+ "WHERE {\n"
				+ "    {\n"
				+ "        select ?ind ?creationTime where { \n"
				+ "            ?ind s4ener:hasCreationTime ?creationTime .\n"
				+ "        } \n"
				+ "    order by desc(?creationTime)\n"
				+ "    limit 1 \n"
				+ "    }\n"
				+ "    ?ind ?p ?o .\n"
				+ "    OPTIONAL { ?s ?p ?ind . }\n"
				+ "    OPTIONAL {\n"
				+ "        ?ind s4ener:hasEffectivePeriod ?interval .\n"
				+ "        ?interval ?dp ?dpv .\n"
				+ "    }\n"
				+ "}";
		
		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
		String queryResults = UtilityMethods.executeConstructQuery(queryPSS, knowledgeBase.getModel());
		
		
		
		// -------------------------------------------------------------------
		// --- contract net setup -------------------------
		// -------------------------------------------------------------------
		
		// Create the CFP message
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		cfp.addReceiver(new AID("ProsumerAgent", AID.ISLOCALNAME)); // add receivers to cfp
		cfp.setContent(queryResults); // add triples specifying the flexibility request; for now empty
		cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		cfp.setOntology(this.ontologyName);
		// We want to receive a reply in 10 secs
		cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000)); // 10 seconds timeout
		
		// Add the customized ContractNetInitiator behaviour
		addBehaviour(new SemanticContractNetInitiator(this, cfp, 1));
		
	}

	@Override
	protected void takeDown() {

		// --- Save the knowledge base model into a file, as the current implementation ---
		// --- does not use a persistent storage ------------------------------------------
		this.knowledgeBase.saveModel();
			
		// --- Close the knowledge base model ---------------------------------------------
		this.knowledgeBase.closeModel();
		
		// --- give time to the Prosumer agent to conclude its takeDown before shutting down the logger 
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
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

	protected KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	protected void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}
	
	
	
	
}
