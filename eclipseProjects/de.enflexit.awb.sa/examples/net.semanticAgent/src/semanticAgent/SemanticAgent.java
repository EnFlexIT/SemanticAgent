package semanticAgent;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;
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
import de.enflexit.awb.sa.core.UtilityStrings;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;


/**
 * Represents a semantic energy agent. 
 * This agent can be used to send SPARQL queries to other semantic agents, which will execute the queries automatically. 
 * The result is sent back to the asking agent automatically as well. 
 * In addition the semantic agent can query his own ontology or add knowledge in terms of statements (turtle) into it.
 * 
 * @author Sebastian Törsleff, Helmut Schmidt University; Nils-Hendric Martens, University of Hamburg
 */
public class SemanticAgent extends Agent {

	// --- static variables -----------------------------
	private static final long serialVersionUID = -2493803948645554649L;
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
		// --- this is not the file name of the ontology -------------------
		this.ontologyName = UtilityStrings.lvGridFlexOntologyName;
		
		// --- specify ontology folder path and file name --------------------------
		String ontologyDirectory = Application.getProjectFocused().getProjectFolderFullPath()
				+ "knowledgeBases" + File.separator 
				+ Application.getProjectFocused().getSimulationSetupCurrent() + File.separator 
				+ this.getAID().getLocalName();
		String ontologyFileName = "LVGridFlexOntology.owl";
		
		// --- instantiate knowledge base -------------------------------
		this.knowledgeBase = new KnowledgeBase(this, ontologyDirectory, ontologyFileName);
		
		// --- Determine communication partner ----------------
		if (this.getAID().getLocalName().equals("A1")) {
			this.communicationPartner = new AID("A2", AID.ISLOCALNAME);
		} else {
			this.communicationPartner = new AID("A1", AID.ISLOCALNAME);
		}

		// --- Generate set of trusted agents used by OMRB for handling of incoming messages
		Set<AID> trustedAgents = Set.of(this.communicationPartner); 

		// --- add OWL message receive behavior ----------------------
		this.owlMsgReceiveBehaviour = new OwlMessageReceiveBehaviour(this.ontologyName, this, this.knowledgeBase, trustedAgents);
		this.addBehaviour(this.owlMsgReceiveBehaviour);
		
		// --- Run ontology reasoning at agent setup; possibly infers additional triples ----------------------
		UtilityMethods.runInferenceEngineOnModel(this.knowledgeBase.getModel(), null);
		
		// -------------------------------------------------------------------
		// --- setup required for evaluation ---------------------------------
		// -------------------------------------------------------------------
		
		// --- Logger configuration --------------------------------------
		if (this.getAID().getLocalName().equals("A1")) {
			rootLogger.removeAllAppenders(); //unschöner workaround. sollte besser an zentraler Stelle erfolgen und nicht bei jedem Agenten individuell
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			rootLogger.addAppender(consoleAppender);
			rootLogger.setLevel(Level.DEBUG);			
		}
		
		// Timeblocker 4s for setting up JADE sniffer
		try {Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
			
		// --- Evaluation methods ----------------------------
//		this.testAnswerQueryBehaviour(); 
//		this.testSendInformBehaviour();
		this.testSparqlQuery();
//		this.determineAllDerasThatControlBatteryStoragesAtSpecificBusbar2Col();
//		this.determineAllDerasThatControlBatteryStoragesAtSpecificBusbar();
//		this.determineLineSegmentsExceedingMaxCurrent();
//		this.determineMostRecentVoltageAtSpecificNode();
//		this.determineBusbarsWithLineSegmentsExceedingMaxCurrent();
//		this.updateFlexibilityPotential();
//		this.gsaAsksDeraForFlexibility();
	
	}


	@Override
	protected void takeDown() {

		// --- Save the knowledge base model into a file, as the current implementation ---
		// --- does not use a persistent storage ------------------------------------------
		this.knowledgeBase.saveModel();
			
		// --- Close the knowledge base model ---------------------------------------------
		this.knowledgeBase.closeModel();
		
		if (this.getAID().getLocalName().equals("A1")) {
			rootLogger.removeAllAppenders(); //unschöner workaround. sollte besser an zentraler Stelle erfolgen und nicht bei jedem Agenten individuell		
		}
				
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

	/**
	 * The Sensor Agent (A2) queries his ontology for the newest measurements of two components (parameters) and sends these to the Grid State Agent.
	 * The GSA then performs a consistency check and adds the measurements to his ontology in case this information does not cause any inconsistency in the ontology of the GSA.
	 * A1 = GridStateAgent, A2 = SensorAgent
	 * Test erfordert : "LVGridFlexOntology.owl" bei beiden Agenten 
	 */
	private void testSendInformBehaviour() {
		if (this.getAID().getLocalName().equals("A2")) {
			String node = "node01";
			String lineSegment = "ls01";
			
			String queryResults = null;
			
			String nodeStateQuery = "CONSTRUCT {?nodestate ?p ?o}" +
				    "	 WHERE {\n" + 
					"	 ?nodestate ?p ?o {\n" + 
					"		SELECT ?nodestate\n" + 
					"		WHERE {\n" + 
					"		?nodestate rdf:type :NodeState .\n" + 
					"		?nodestate :hasTimeStamp ?timestamp .\n" + 
					"		?nodestate :componentId ?cid .\n" + 
					"		FILTER (regex(str(?cid), \""+node+"\")) \n" + 
					"		}\n" + 
					"		ORDER BY DESC (?timestamp) \n" + 
					"		LIMIT 1 }\n" + 
					"	}";
			
			
			String nodeStateQueryPSS = UtilityMethods.addPrefixesToSparqlQuery(nodeStateQuery);
			queryResults = UtilityMethods.executeConstructQuery(nodeStateQueryPSS, this.knowledgeBase.getModel());
			
			String lineSegmentStateQuery = "CONSTRUCT {?linesegmentstate ?p ?o}\n" + 
					"    WHERE {\n" +
					"	 ?linesegmentstate ?p ?o {\n" + 
					"		SELECT ?linesegmentstate\n" + 
					"		WHERE {\n" + 
					"		?linesegmentstate rdf:type :LineSegmentState .\n" + 
					"		?linesegmentstate :hasTimeStamp ?timestamp .\n" + 
					"		?linesegmentstate :componentId ?cid .\n" + 
					"		FILTER (regex(str(?cid), \""+lineSegment+"\")) \n" + 
					"		}\n" + 
					"		ORDER BY DESC (?timestamp) \n" + 
					"		LIMIT 1 }\n" + 
					"	}";
			
			String lineSegmentStateQueryPSS = UtilityMethods.addPrefixesToSparqlQuery(lineSegmentStateQuery);
			
			queryResults+=UtilityMethods.executeConstructQuery(lineSegmentStateQueryPSS, this.knowledgeBase.getModel());
			
			rootLogger.debug(queryResults);
			
			SendInformBehaviour sendInformBehaviour = new SendInformBehaviour(this.communicationPartner, queryResults, this.generateArbitraryConversationId());
			this.addBehaviour(sendInformBehaviour);
		}
	}
	
	private void testSparqlQuery() {
		if (this.getAID().getLocalName().equals("A2")) {
						
			String query = "SELECT ?sc ?c WHERE {\n"
					+ "	?sc rdfs:subClassOf ?c. \n"
					+ "}";
					
					
					
			
			String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query);
			String[][] queryResults = UtilityMethods.executeMultiColumnSelectQuery(queryPSS, this.knowledgeBase.getModel());
//			this.knowledgeBase.printAllModelStatements();
			
			rootLogger.debug(queryResults);
		}
	}
	
	
	

	/**
	 * GSA (A1) queries his ontology to determine which DERAs control battery storages that are connectedTo a specific busbar
	 * The busbar is the parameter of the SPARQL query.
	 * @param busbar_ Busbar
	 */
	private void determineAllDerasThatControlBatteryStoragesAtSpecificBusbar() {
		if (this.getAID().getLocalName().equals("A1")) {
			
			String busbarId = "bb01"; 
			
			String selectQuery = "SELECT DISTINCT ?dera \n" + 
					"	WHERE {\n" + 
					"	?dera :controls ?bs .\n" + 
//					"	?bs rdf:type :BatteryStorage .\n" +
//					"	?bs :connectedTo ?node .\n" + 
//					"	?node rdf:type :Node .\n" + 
//					"	?node :isComponentOf ?bb .\n" + 
//					"	?bb rdf:type :Busbar. \n" + 
//					"	?bb :hasId ?id .\n" + 
//					"	FILTER (str(?id)=\"" + busbarId + "\") \n" + 
					"	}";
			
			String prefixedSelectQuery = UtilityMethods.addPrefixesToSparqlQuery(selectQuery);
			String[] solutionArray = UtilityMethods.executeSingleColumnSelectQuery(prefixedSelectQuery, this.knowledgeBase.getModel());

			
			String solutionString = UtilityMethods.stringArrayToString(solutionArray);
			
			rootLogger.debug(solutionString);

		}
	}
	
	/**
	 * GSA (A1) queries his ontology to determine which DERAs control battery storages that are connectedTo a specific busbar 
	 * including the resource IDs of the battery storages
	 * The busbar is the parameter of the SPARQL query.
	 * @param busbar_ Busbar
	 */
	private void determineAllDerasThatControlBatteryStoragesAtSpecificBusbar2Col() {
		if (this.getAID().getLocalName().equals("A1")) {
			
			String busbarId = "bb01"; 
			
			String selectQuery = "SELECT DISTINCT ?dera ?resourceId\n" + 
					"	WHERE {\n" + 
					"	?dera :controls ?bs .\n" + 
					"	?bs rdf:type :BatteryStorage .\n" +
					"	?bs :hasId ?resourceId .\n" +
					"	?bs :connectedTo ?node .\n" + 
					"	?node rdf:type :Node .\n" + 
					"	?node :isComponentOf ?bb .\n" + 
					"	?bb rdf:type :Busbar. \n" + 
					"	?bb :hasId ?id .\n" + 
					"	FILTER (str(?id)=\"" + busbarId + "\") \n" + 
					"	}";
			
			String prefixedSelectQuery = UtilityMethods.addPrefixesToSparqlQuery(selectQuery);
			String[][] solutionArrayMultiCol = UtilityMethods.executeMultiColumnSelectQuery(prefixedSelectQuery, this.knowledgeBase.getModel());
			
			rootLogger.debug(solutionArrayMultiCol);

		}
	}
	
	/**
	 * A2 queries his ontology to determine line segments that exceed the maximum current allowed.
	 */
	private void determineLineSegmentsExceedingMaxCurrent() {
		if (this.getAID().getLocalName().equals("A2")) {
			
			String query = "SELECT DISTINCT ?ls\n" + 
					"	WHERE {\n" + 
					"	?ls rdf:type :LineSegment .\n" + 
					"	?ls :hasMaximumCurrent ?maxcurrent .\n" + 
					"	?ls :hasLineSegmentState ?lss .\n" + 
					"	?lss :hasCurrent ?current .\n" + 
					"	FILTER (?current > ?maxcurrent)\n" + 
					"	}";
		
			String sparqlString = UtilityMethods.addPrefixesToSparqlQuery(query);
			String[] queryResult = UtilityMethods.executeSingleColumnSelectQuery(sparqlString, this.knowledgeBase.getModel());
			
			String contentString = UtilityMethods.stringArrayToString(queryResult);
			
			rootLogger.debug(queryResult);	
		}
	}
	
	/**
	 * A2 queries his ontology to determine the most recent voltage at a specific node.
	 */
	private void determineMostRecentVoltageAtSpecificNode() {
		if (this.getAID().getLocalName().equals("A2")) {
			
			String nodeId = "node01";
			
			String query= "SELECT ?v\n"
					+ "WHERE { \n" + 
					"		?ns rdf:type :NodeState. \n" + 
					"		?ns :componentId ?cid. \n" + 
					"       ?ns :hasVoltage ?v. \n" +
					"       ?ns :hasTimeStamp ?ts. \n" +
					"		FILTER (str(?cid)=\"" + nodeId + "\") \n" + 
					"		}\n" + 
					"ORDER BY DESC (?ts) \n" + 
					"LIMIT 1 \n";
		
			String sparqlString = UtilityMethods.addPrefixesToSparqlQuery(query);
			String queryResult = UtilityMethods.executeSingleCellSelectQuery(sparqlString, this.knowledgeBase.getModel());
			
			double voltage = Double.parseDouble(queryResult); 
			
			rootLogger.debug(queryResult);	
		}
	}
	
	
	
	
	/**
	 * A2 queries his ontology to determine which busbars comprise line segments that exceed the maximum current allowed.
	 */
	private void determineBusbarsWithLineSegmentsExceedingMaxCurrent() {
		if (this.getAID().getLocalName().equals("A2")) {
			
			String commandText = "SELECT DISTINCT ?bb\n" + 
					"	WHERE {\n" + 
					"	?bb rdf:type :Busbar .\n" + 
					"	?bb :hasComponent ?ls .\n" +   //hier steht nur node01 zur Verfügung - in Protege testen
					"	?ls rdf:type :LineSegment .\n" + 
					"	?ls :hasMaximumCurrent ?maxcurrent .\n" + 
					"	?ls :hasLineSegmentState ?lss .\n" + 
					"	?lss :hasCurrent ?current .\n" + 
					"	FILTER (?current > ?maxcurrent)\n" + 
					"	}";
			
			String sparqlString = UtilityMethods.addPrefixesToSparqlQuery(commandText);
			String[] solution = UtilityMethods.executeSingleColumnSelectQuery(sparqlString, this.knowledgeBase.getModel());
			String contentString = UtilityMethods.stringArrayToString(solution);
			rootLogger.debug(contentString);
			
		}
	}
	/**
	 * Method used for updating the flexibility potential of a DERA (A2) by using sparql update functionality
	 */
	private void updateFlexibilityPotential() {
		
		// --- list of parameters that would be passed to this method -------
		double totalEnergy = 9.55;
		double maxPower = 30.0; 
		double minPower = 5.0; 
		String resourceId = "bs01"; 
		String timeStamp = "2022-02-04T20:27:00";
		
		if (this.getAID().getLocalName().equals("A2")) {
			
//			String totalEnergy = "\"9.55\"^^xsd:double";
			
			// --- Update string für neues Flexibilitätspotential
			
//					+"<http://www.hsu-ifa.de/ontologies/LVGridFlex#fp02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.hsu-ifa.de/ontologies/LVGridFlex#FlexibilityPotential>.\n"  
//					+"<http://www.hsu-ifa.de/ontologies/LVGridFlex#fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasMaximumPower> "+maxPower+".\n"
//					+"<http://www.hsu-ifa.de/ontologies/LVGridFlex#fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasMinimumPower> " +minPower+".\n"
//					+"<http://www.hsu-ifa.de/ontologies/LVGridFlex#fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasTotalEnergy> " + totalEnergy +".\n"
//					+"<http://www.hsu-ifa.de/ontologies/LVGridFlex#fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#resourceId> " + resourceId + ".\n" 
//					+"<http://www.hsu-ifa.de/ontologies/LVGridFlex#fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasTimeStamp> " + timeStamp + ".";			
			
//			String updateString = "<fp02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#NamedIndividual>.\n" 
//					+"<fp02> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.hsu-ifa.de/ontologies/LVGridFlex#FlexibilityPotential>.\n"  
//					+"<fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasMaximumPower> "+maxPower+".\n"
//					+"<fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasMinimumPower> " +minPower+".\n"
//					+"<fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasTotalEnergy> " + totalEnergy +".\n"
//					+"<fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#resourceId> " + resourceId + ".\n" 
//					+"<fp02> <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasTimeStamp> " + timeStamp + ".";	
			
//			String updateString = ":fp02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#NamedIndividual>.\n" 
//					+":fp02 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.hsu-ifa.de/ontologies/LVGridFlex#FlexibilityPotential>.\n"  
//					+":fp02 <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasMaximumPower> "+maxPower+".\n"
//					+":fp02 <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasMinimumPower> " +minPower+".\n"
//					+":fp02 <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasTotalEnergy> " + totalEnergy +".\n"
//					+":fp02 <http://www.hsu-ifa.de/ontologies/LVGridFlex#resourceId> " + resourceId + ".\n" 
//					+":fp02 <http://www.hsu-ifa.de/ontologies/LVGridFlex#hasTimeStamp> " + timeStamp + ".";		
			
			String sparqlUpdateTriples = ":fp02 rdf:type owl:NamedIndividual.\n" 
					+":fp02 rdf:type :FlexibilityPotential.\n"  
					+":fp02 :hasMaximumPower "+ UtilityMethods.doubleToXsdDouble(maxPower)+".\n"
					+":fp02 :hasMinimumPower " + UtilityMethods.doubleToXsdDouble(minPower)+".\n"
					+":fp02 :hasTotalEnergy " + UtilityMethods.doubleToXsdDouble(totalEnergy) +".\n"
					+":fp02 :resourceId " + UtilityMethods.stringtoRdfsLiteral(resourceId) + ".\n" 
					+":fp02 :hasTimeStamp " + UtilityMethods.stringToXsdDateTime(timeStamp) + ".";	
			
			
			// --- RDF Statements model via SPARQL Update hinzufügen
			String sparqlUpdateStatement = UtilityMethods.addPrefixesToSparqlUpdate(sparqlUpdateTriples);
			
			rootLogger.debug(this.getAID().getLocalName() + "/updateFlexibilityPotential() will execute this SPARQL update: \n" + sparqlUpdateStatement);
			
			UtilityMethods.executeSparqlUpdate(this.knowledgeBase.getModel(),sparqlUpdateStatement);
		}
	}
	
	/**
	 * The Grid State Agent (A1) asks a Distributed Energy Resource Agent (A2) for his flexibility potential of a specific Distributed Energy Resource (battery storage bs01). 
	 * After the DERA receives the SPARQL query he executes the query and send back the result.
	 * The GSA then adds the flexibility potential to his ontology after it passed the consistency check.
	 * Zum Testen sicherstellen, dass in der Ontologie von A1 keine flexpo enthalten ist, das in der Onto von A2 enthalten ist 
	 */
	private void gsaAsksDeraForFlexibility() {
		
		if (this.getAID().getLocalName().equals("A1")) {	
			
			String batteryStorageId = "bs01"; 
			
			String commandText= "CONSTRUCT {?flexpo ?p ?o}\n"
					+ "WHERE { \n" + 
					"	?flexpo ?p ?o { \n" + 
					"		SELECT ?flexpo \n" + 
					"		WHERE {\n" + 
					"		?flexpo rdf:type :FlexibilityPotential. \n" + 
					"		?flexpo :resourceId ?rid. \n" + 
					"       ?flexpo :hasTimeStamp ?timestamp. \n" +
					"		FILTER (regex(str(?rid), \""+batteryStorageId+"\")) \n" + 
					"		}\n" + 
					"	ORDER BY DESC (?timestamp) \n" + 
					"		LIMIT 1}\n" + 
					"}";
			String queryString = UtilityMethods.addPrefixesToSparqlQuery(commandText);
			SendQueryBehaviour sqb = new SendQueryBehaviour(this, queryString, this.communicationPartner, this.generateArbitraryConversationId(), this.ontologyName);
			this.addBehaviour(sqb);
		}
	}
}
