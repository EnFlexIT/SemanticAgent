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
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class ChargingStationAgent extends Agent {
	

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
//		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase);
//		this.addBehaviour(this.owlMsgReceiveBehaviour);
		
//		 perform consistency check of the ontology 
//		UtilityMethods.checkRdfStatementConsistency("", this.knowledgeBase);
		
		this.addAchieveREResponderBehaviour(); 
		
	    

	}
	
	
	private void addAchieveREResponderBehaviour() {
		        
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
				MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF), 
					MessageTemplate.MatchOntology(this.ontologyName)
				)
			); 
		
		addBehaviour(new AchieveREResponder(this, msgTemplate) {
            protected ACLMessage prepareResponse(ACLMessage request) {
            	
				logger.info("Agent " + myAgent.getAID().getLocalName() + ": received query from " + request.getSender().getLocalName());
				
				boolean reply = true; 
				ACLMessage inform = request.createReply();
				
				if (reply) {
					logger.info("Agent " + myAgent.getAID().getLocalName() + ": preparing reply...");
					inform.setPerformative(ACLMessage.INFORM);
					KnowledgeBase kb = ((ChargingStationAgent) myAgent).getKnowledgeBase(); 
					String queryResult = UtilityMethods.executeConstructQuery(request.getContent(),kb.getModel());
					inform.setContent(queryResult);
					logger.info("Agent " + myAgent.getAID().getLocalName() + ": sending reply to " + request.getSender().getLocalName());
					return inform;					
				} else {
					logger.info("Agent " + myAgent.getAID().getLocalName() + ": refusing to reply");
					inform.setPerformative(ACLMessage.REFUSE);
				}
				
				return inform;

            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                return response;
            }
        });
		
		logger.info("Agent " + this.getAID().getLocalName() + ": instantiated AchieveREResponder behaviour");
		
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
