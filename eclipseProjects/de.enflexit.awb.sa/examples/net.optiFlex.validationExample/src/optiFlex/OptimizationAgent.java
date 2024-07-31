package optiFlex;

import java.io.File;
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


/**
 * @author Sebastian Toersleff, Helmut Schmidt University
 */
public class OptimizationAgent extends Agent {


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
		
		// --- add default namespace --------------
		knowledgeBase.getNamespaceList().addNameSpace("", baseUri);
		
		// --- Determine communication partner ----------------
		this.communicationPartner = new AID("ProcessAgent", AID.ISLOCALNAME);

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
			
		String query = "CONSTRUCT {?s ?p ?o}\n"
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


	
	/**
	 * GSA (A1) queries his ontology to determine which DERAs control DERs that are connectedTo a specific busbar
	 * The busbar is the parameter of the SPARQL query.
	 * Requires reasoning to be executed before querying, otherwise result is null. 
	 * 
	 * @param busbar_ Busbar
	 */
	private void determineDerasControllingDersAtBusbar() {
		if (this.getAID().getLocalName().equals("A1")) {
			
			String busbarId = "bb01"; 
			
			String selectQuery = "SELECT DISTINCT ?dera ?der \n" + 
					"	WHERE {\n" + 
					"	?dera :controls ?der .\n" + 
					"	?der rdf:type :DistributedEnergyResource .\n" +
					"	?der :connectedTo ?node .\n" + 
					"	?node rdf:type :Node .\n" + 
					"	?node :isComponentOf ?bb .\n" + 
					"	?bb rdf:type :Busbar. \n" + 
					"	?bb :hasId ?id .\n" + 
					"	FILTER (str(?id)=\"" + busbarId + "\") \n" + 
					"	}";
			
			String prefixedSelectQuery = UtilityMethods.addPrefixesToSparqlQuery(selectQuery, knowledgeBase);
			String[][] solutionArrayMultiCol = UtilityMethods.executeMultiColumnSelectQuery(prefixedSelectQuery, this.knowledgeBase.getModel());
					
			rootLogger.debug(solutionArrayMultiCol);
	
		}
	}


	/**
	 * GSA (A1) queries his ontology to determine which DERAs control battery storages that are connectedTo a specific busbar
	 * The busbar is the parameter of the SPARQL query.
	 * Requires reasoning to be executed before querying, otherwise result is null. 
	 * 
	 * @param busbar_ Busbar
	 */
	private void determineAllDerasThatControlBatteryStoragesAtSpecificBusbar() {
		if (this.getAID().getLocalName().equals("A1")) {
			
			String busbarId = "bb01"; 
			
			String selectQuery = "SELECT DISTINCT ?dera \n" + 
					"	WHERE {\n" + 
					"	?dera :controls ?bs .\n" + 
					"	?bs rdf:type :BatteryStorage .\n" +
					"	?bs :connectedTo ?node .\n" + 
					"	?node rdf:type :Node .\n" + 
					"	?node :isComponentOf ?bb .\n" + 
					"	?bb rdf:type :Busbar. \n" + 
					"	?bb :hasId ?id .\n" + 
					"	FILTER (str(?id)=\"" + busbarId + "\") \n" + 
					"	}";
			
			String prefixedSelectQuery = UtilityMethods.addPrefixesToSparqlQuery(selectQuery, knowledgeBase);
			String[] solutionArray = UtilityMethods.executeSingleColumnSelectQuery(prefixedSelectQuery, knowledgeBase.getModel());

			
			String solutionString = UtilityMethods.oneDimensionalStringArrayToString(solutionArray);
			
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
			
			String prefixedSelectQuery = UtilityMethods.addPrefixesToSparqlQuery(selectQuery, knowledgeBase);
			String[][] solutionArrayMultiCol = UtilityMethods.executeMultiColumnSelectQuery(prefixedSelectQuery, this.knowledgeBase.getModel());
			
			rootLogger.debug(solutionArrayMultiCol);
			
		}
	}
	
	/**
	 * A2 queries his ontology to determine line segments that exceed the maximum current allowed.
	 */
	private void determineLineSegmentsExceedingMaxCurrent() {
		if (this.getAID().getLocalName().equals("A1")) {
			
			String query = "SELECT DISTINCT ?ls ?maxcurrent ?current \n" + 
					"	WHERE {\n" + 
					"	?ls rdf:type :LineSegment .\n" + 
					"	?ls :hasMaximumCurrent ?maxcurrent .\n" + 
					"	?ls :hasLineSegmentState ?lss .\n" + 
					"	?lss :hasCurrent ?current .\n" + 
					"	FILTER (?current > ?maxcurrent)\n" + 
					"	}";
		
			String sparqlString = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
			String[][] queryResult = UtilityMethods.executeMultiColumnSelectQuery(sparqlString, this.knowledgeBase.getModel());
						
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
					"	?bb :hasComponent ?ls .\n" +   //hier steht nur node01 zur Verf�gung - in Protege testen
					"	?ls rdf:type :LineSegment .\n" + 
					"	?ls :hasMaximumCurrent ?maxcurrent .\n" + 
					"	?ls :hasLineSegmentState ?lss .\n" + 
					"	?lss :hasCurrent ?current .\n" + 
					"	FILTER (?current > ?maxcurrent)\n" + 
					"	}";
			
			String sparqlString = UtilityMethods.addPrefixesToSparqlQuery(commandText, knowledgeBase);
			String[] solution = UtilityMethods.executeSingleColumnSelectQuery(sparqlString, this.knowledgeBase.getModel());
			String contentString = UtilityMethods.oneDimensionalStringArrayToString(solution);
			rootLogger.debug(contentString);
			
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
			String queryString = UtilityMethods.addPrefixesToSparqlQuery(commandText, knowledgeBase);
			SendQueryBehaviour sqb = new SendQueryBehaviour(this, queryString, this.communicationPartner, this.generateArbitraryConversationId(), SemanticAgentProtocols.OWL_QUERY, this.ontologyName);
			this.addBehaviour(sqb);
		}
	}
}
