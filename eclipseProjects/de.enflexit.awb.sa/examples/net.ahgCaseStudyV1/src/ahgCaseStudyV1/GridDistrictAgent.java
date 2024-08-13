package ahgCaseStudyV1;

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
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;


/**
 * @author Sebastian Törsleff, Helmut Schmidt University
 */
public class GridDistrictAgent extends Agent {


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
		
		// --- Logger configuration --------------------------------------
		logger.removeAllAppenders(); 
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.INFO);		

		// Timeblocker 2s for setting up JADE sniffer
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}
		

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
		String ontologyFileName = "AHGv1_taskfit_forGridDistricAgent.xml";

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
		
		
	    
//	    this.instantiateGridStateEvaluationBehaviour();
		
		this.addAchieveREInitiatorBehaviour(); 
	    
	}
	
	private void addAchieveREInitiatorBehaviour() {
		
		
		
		String chargingStationId = ":cs01";  
		
        String constructQuery= "CONSTRUCT {\n"
        		+ "    ?capms ?p1 ?o1 .\n"
        		+ "    ?capprop ?p2 ?o2 .\n"
        		+ "    ?s3 ?p3 ?capprop .\n"
        		+ "    ?socms ?p4 ?o4 .\n"
        		+ "    ?socprop ?p5 ?o5 .\n"
        		+ "    ?s6 ?p6 ?socprop .\n"
        		+ "} \n"
        		+ "where { \n"
        		+ "    BIND("+ chargingStationId +" AS ?chargingStation)\n"
        		+ "    {\n"
        		+ "        select ?capms ?capprop where { \n"
        		+ "            ?capms rdf:type saref:Measurement .\n"
        		+ "            ?capms saref:hasTimestamp ?timestamp .\n"
        		+ "            ?capms saref:relatesToProperty ?capprop .\n"
        		+ "            ?capprop rdf:type s4enerext:Capacity .\n"
        		+ "            ?chargingStation saref:hasProperty ?capprop .\n"
        		+ "        } \n"
        		+ "        order by desc(?timestamp)\n"
        		+ "        limit 1 \n"
        		+ "    }\n"
        		+ "    ?capms ?p1 ?o1\n"
        		+ "    OPTIONAL {\n"
        		+ "        ?capprop ?p2 ?o2 .\n"
        		+ "        ?s3 ?p3 ?capprop .\n"
        		+ "        FILTER(?p3 != saref:relatesToProperty)\n"
        		+ "    }\n"
        		+ "    {\n"
        		+ "        select ?socms ?socprop where { \n"
        		+ "            ?socms rdf:type saref:Measurement .\n"
        		+ "            ?socms saref:hasTimestamp ?timestamp .\n"
        		+ "            ?socms saref:relatesToProperty ?socprop .\n"
        		+ "            ?socprop rdf:type s4enerext:StateOfCharge .\n"
        		+ "            ?chargingStation saref:hasProperty ?socprop .\n"
        		+ "        } \n"
        		+ "        order by desc(?timestamp)\n"
        		+ "        limit 1 \n"
        		+ "    }\n"
        		+ "    ?socms ?p4 ?o4\n"
        		+ "    OPTIONAL {\n"
        		+ "        ?socprop ?p5 ?o5 .\n"
        		+ "        ?s6 ?p6 ?socprop .\n"
        		+ "        FILTER(?p6 != saref:relatesToProperty)\n"
        		+ "    }\n"
        		+ "} ";
        
        String prefixedQueryString = UtilityMethods.addPrefixesToSparqlQuery(constructQuery, knowledgeBase);
        
		try {Thread.sleep(2000);} catch (InterruptedException e) {e.printStackTrace();}

        
        ACLMessage query = new ACLMessage(ACLMessage.QUERY_REF);
        query.addReceiver(new AID("CSA", AID.ISLOCALNAME));
		query.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
        query.setContent(prefixedQueryString);
        query.setOntology(this.ontologyName);
		
		this.addBehaviour(new AchieveREInitiator(this, query) {
            
			protected void handleInform(ACLMessage inform) {
            	logger.info("Agent " + this.getAgent().getLocalName() + ": received inform from " + inform.getSender().getLocalName());
				String informTriples = inform.getContent();
				if (!informTriples.isEmpty()) {
					logger.info("Agent " + myAgent.getAID().getLocalName() + ": performing consistency check for received triples...");
					KnowledgeBase kb = ((GridDistrictAgent) myAgent).getKnowledgeBase(); 
					boolean triplesAreValid = UtilityMethods.checkRdfStatementConsistency(informTriples, kb);
					if (triplesAreValid) {
						logger.info("Agent " + myAgent.getAID().getLocalName() + ": consistency check passed; adding triples to the knowledge base.");
						UtilityMethods.addTriplesToKnowledgeBase(kb, informTriples); 
					} else {
						logger.info("Agent " + myAgent.getAID().getLocalName() + ": consistency check not passed; triples will not be added to the knowledge base.");
					}
				}
            }

            protected void handleRefuse(ACLMessage refuse) {
        		System.out.println("Agent " + this.getAgent().getLocalName() + ": Agent " + refuse.getSender().getLocalName() + " refused");
            }

            protected void handleFailure(ACLMessage failure) {
                System.out.println("Query failed");
            }
        });
		
		logger.info("Agent " + this.getAID().getLocalName() + ": instantiated AchieveREInitiator behaviour");
	}

	private void instantiateGridStateEvaluationBehaviour() {
		
		this.addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {

        		double upperVoltageThreshold = 320.0; 
        		double lowerVoltageThreshold = 220.0; 
        		double currentThreshold = 40.0; 
        		
        		String query = "ASK { \n"
        				+ "    BIND(" + upperVoltageThreshold + " AS ?upperVoltageThreshold)\n"
        				+ "    BIND(" + lowerVoltageThreshold + " AS ?lowerVoltageThreshold)\n"
        				+ "    BIND(" + currentThreshold + " AS ?currentThreshold)\n"
        				+ "    BIND(:gd01 AS ?gridDistrict)\n"
        				+ "    {\n"
        				+ "        select ?voltValue ?currValue where { \n"
        				+ "            ?gridDistrict topo:hasComponent ?node .\n"
        				+ "            \n"
        				+ "            # Voltage section\n"
        				+ "            ?voltProp saref:isPropertyOf ?node .\n"
        				+ "            ?voltProp rdf:type sosaext:Voltage .\n"
        				+ "            ?voltObs sosa:observedProperty ?voltProp .\n"
        				+ "            ?voltObs sosa:hasSimpleResult ?voltValue .\n"
        				+ "            ?voltObs sosa:resultTime ?voltResultTime .\n"
        				+ "            \n"
        				+ "            # Current section\n"
        				+ "            ?currProp saref:isPropertyOf ?node .\n"
        				+ "            ?currProp rdf:type sosaext:Current .\n"
        				+ "            ?currObs sosa:observedProperty ?currProp .\n"
        				+ "            ?currObs sosa:hasSimpleResult ?currValue .\n"
        				+ "            ?currObs sosa:resultTime ?currResultTime\n"
        				+ "        }\n"
        				+ "        order by desc(?voltResultTime) desc(?currResultTime)\n"
        				+ "        limit 1\n"
        				+ "    }\n"
        				+ "    FILTER((?voltValue > ?upperVoltageThreshold || ?voltValue < ?lowerVoltageThreshold) || (?currValue > ?currentThreshold))\n"
        				+ "}";
        		
        		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
        		boolean thresholdViolation = UtilityMethods.executeAskQuery(queryPSS, knowledgeBase.getModel());
        		if (thresholdViolation) {
        			logger.info("Agent "+ this.myAgent.getAID().getLocalName() + ": there is a threshold violation in the grid"); 
        		} else {
        			logger.info("Agent "+ this.myAgent.getAID().getLocalName() + ": there is no threshold violation in the grid"); 
        		}
            }
        });
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
		logger.removeAllAppenders(); 		
		
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
