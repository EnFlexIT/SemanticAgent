package de.enflexit.awb.sa.core;

import org.apache.log4j.Logger;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * The Class ReceiveInformBehaviour.
 * This behaviour is used to process incoming INFORM messages that contain RDF triples. 
 * Triples are only added to the knowledge base if they pass the consistency check.
 */
public class ReceiveInformBehaviour extends OneShotBehaviour {
	
	private static Logger logger = Logger.getRootLogger();

	private String rdfTriples;
	private KnowledgeBase knowledgeBase;

	/**
	 * Instantiates a new ReceiveInformBehaviour.
	 *
	 * @param knowledgeBase the knowledge base that the tripels will be added to (after successful consistency check).
	 * @param rdfTriples the rdf triples that are supposed to be injected into the knowledge base 
	 */
	public ReceiveInformBehaviour(KnowledgeBase knowledgeBase, String rdfTriples) {
		this.knowledgeBase = knowledgeBase;
		this.rdfTriples = rdfTriples;
	}
	@Override
	public void action() {
		
		if(this.statementsAreValid()) {
			logger.info("Agent " + myAgent.getAID().getLocalName() + ": consistency check passed; adding triples to ontology.");
			this.addStatementsToModel();
			
		} else {
			logger.info("Agent " + myAgent.getAID().getLocalName() + ": consistency check not passed, the statements will not be added to the model.");
		}
	}
	
	/**
	 * Method to perform a consistency check on a copy of the jena model of the internal data model of an agent. 
	 * The copy contains the new statements. 
	 */
	private boolean statementsAreValid() {
		logger.info("Agent " + myAgent.getAID().getLocalName() + ": performing consistency check for received triples...");
		return UtilityMethods.checkRdfStatementConsistency(rdfTriples, this.knowledgeBase);
	}
	
	/**
	 * Method to add new statements to the original model in the internal data model and 
	 * run "OWL" reasoning afterwards to possibly generate new ABox instances.
	 */
	private void addStatementsToModel() {

		UtilityMethods.addTriplesToKnowledgeBase(this.knowledgeBase, this.rdfTriples);
	}

}
