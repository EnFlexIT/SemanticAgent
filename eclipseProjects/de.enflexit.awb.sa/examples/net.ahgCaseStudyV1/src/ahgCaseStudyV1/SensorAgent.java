package ahgCaseStudyV1;

import java.io.File;
import java.util.Date;

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
import de.enflexit.awb.sa.core.UtilityMethods;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

public class SensorAgent extends Agent {
	

	// --- static variables -----------------------------
	private static final long serialVersionUID = 3164333512327730902L;
	private static Logger logger = Logger.getRootLogger();


	// --- obligatory variables for a semantic agent
	private KnowledgeBase knowledgeBase;
	private OwlMessageReceiveBehaviour owlMsgReceiveBehaviour;
	private String ontologyName; 
	
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
		logger.info("Agent "+ this.getAID().getLocalName() + ": performing setup routine"); 
		
		// --- for now only one ontology is supported; this variable is used ------------
		// --- for the ontology field of ACL message objects and message filtering; -----
		this.ontologyName = "AHGv1";

		// --- specify ontology folder path and file name --------------------------
		String ontologyDirectory = Application.getProjectFocused().getProjectFolderFullPath()
				+ "knowledgeBases" + File.separator 
				+ Application.getProjectFocused().getSimulationSetupCurrent() + File.separator 
				+ this.getAID().getLocalName();
		String ontologyFileName = "AHGv1_taskfit.xml";

		// --- specify Base URI ------------------------
		String baseUri = "https://www.hsu-ifa.de/ontologies/casestudies/AHGv1/taskfitverification#"; 

		// --- instantiate knowledge base with previously defined parameters -----------------
		OntModelSpec ontModelSpec = OntModelSpec.OWL_MEM_MICRO_RULE_INF;
		this.knowledgeBase = new KnowledgeBase(this, ontologyDirectory, ontologyFileName, baseUri, ontModelSpec);

		// --- add individual namespaces --------------
		knowledgeBase.getNamespaceList().addNameSpace("", baseUri);
		knowledgeBase.getNamespaceList().addNameSpace("s4ener", "https://saref.etsi.org/saref4ener/");
		knowledgeBase.getNamespaceList().addNameSpace("ao", "https://www.hsu-ifa.de/ontologies/AHG01AlignmentOntology#");
		knowledgeBase.getNamespaceList().addNameSpace("s4enerext", "https://www.hsu-ifa.de/ontologies/SAREF4ENERbs#");
		knowledgeBase.getNamespaceList().addNameSpace("topo", "http://www.hsu-ifa.de/ontologies/LVGridTopology#");
		knowledgeBase.getNamespaceList().addNameSpace("sosa", "http://www.w3.org/ns/sosa/");
		knowledgeBase.getNamespaceList().addNameSpace("sosaext", "https://www.hsu-ifa.de/ontologies/SOSAext#");
		knowledgeBase.getNamespaceList().addNameSpace("saref", "https://saref.etsi.org/core/");
		knowledgeBase.getNamespaceList().addNameSpace("qudt", "http://qudt.org/schema/qudt/");
		knowledgeBase.getNamespaceList().addNameSpace("unit", "http://qudt.org/vocab/unit/");


		// --- Generate set of trusted agents used by OMRB for handling of incoming messages
//		Set<AID> trustedAgents = Set.of(this.communicationPartner, this.communicationPartnerTwo); 

		// --- add OWL message receive behavior ----------------------
		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase);
		this.addBehaviour(this.owlMsgReceiveBehaviour);
		
		
//		 perform consistency check of the ontology 
//		UtilityMethods.checkRdfStatementConsistency("", this.knowledgeBase);
		
	    
	    this.sendLatestSensorMeausurementsToGda(); 

	}
	
	private void sendLatestSensorMeausurementsToGda() {
		
		// 3s sleep for letting GDA perform initial grid state evaluation
		logger.info("Agent "+ this.getAID().getLocalName() + ": waiting 3s before sending latest sensor measurementto GDA");
		try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
		
		
		String node = ":node01";
		String queryResults = null;
		
		String sensorMeaurementQuery = "CONSTRUCT {\r\n"
				+ "    ?voltObs ?p1 ?o1 .\r\n"
				+ "    ?voltProp ?p2 ?o2 .\r\n"
				+ "    ?currObs ?p3 ?o3 .\r\n"
				+ "    ?currProp ?p4 ?o4 .\r\n"
				+ "}\r\n"
				+ "WHERE { \r\n"
				+ "    BIND(:node01 AS ?node)\r\n"
				+ "    {\r\n"
				+ "        SELECT ?voltObs ?voltProp WHERE { \r\n"
				+ "            ?voltObs sosa:observedProperty ?voltProp .\r\n"
				+ "            ?voltProp rdf:type sosaext:Voltage .\r\n"
				+ "            ?voltProp saref:isPropertyOf ?node .\r\n"
				+ "            ?voltObs sosa:resultTime ?resultTime .\r\n"
				+ "        }\r\n"
				+ "        ORDER BY DESC(?resultTime)\r\n"
				+ "        LIMIT 1\r\n"
				+ "    }\r\n"
				+ "    ?voltObs ?p1 ?o1 .\r\n"
				+ "    OPTIONAL { ?voltProp ?p2 ?o2 . }\r\n"
				+ "\r\n"
				+ "    {\r\n"
				+ "        SELECT ?currObs ?currProp WHERE { \r\n"
				+ "            ?currObs sosa:observedProperty ?currProp .\r\n"
				+ "            ?currProp rdf:type sosaext:Current .\r\n"
				+ "            ?currProp saref:isPropertyOf ?node .\r\n"
				+ "            ?currObs sosa:resultTime ?resultTime .\r\n"
				+ "        }\r\n"
				+ "        ORDER BY DESC(?resultTime)\r\n"
				+ "        LIMIT 1\r\n"
				+ "    }\r\n"
				+ "    ?currObs ?p3 ?o3 .\r\n"
				+ "    OPTIONAL { ?currProp ?p4 ?o4 . }\r\n"
				+ "}";
		
		String sensorMeaurementQueryPSS = UtilityMethods.addPrefixesToSparqlQuery(sensorMeaurementQuery, knowledgeBase);
		queryResults=UtilityMethods.executeConstructQuery(sensorMeaurementQueryPSS, this.knowledgeBase.getModel());
		
		
		SendInformBehaviour sendInformBehaviour = new SendInformBehaviour(new AID("GDA", AID.ISLOCALNAME), queryResults, this.generateArbitraryConversationId(), SemanticAgentProtocols.OWL_INFORM, ontologyName);
		this.addBehaviour(sendInformBehaviour);
		
	}

	@Override
	protected void takeDown() {

		// --- Save the knowledge base model into a file, as the current implementation ---
		// --- does not use a persistent storage ------------------------------------------
		this.knowledgeBase.saveModel();
			
		// --- Close the knowledge base model ---------------------------------------------
		this.knowledgeBase.closeModel();	
		
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
