package de.enflexit.awb.sa.core;

import org.apache.log4j.Logger;

import jade.core.behaviours.OneShotBehaviour;

/**
 * The Class ProcessQueryAnswerBehaviour.
 * 
 * @author Sebastian Toersleff, Helmut Schmidt University
 */
public class ProcessQueryAnswerBehaviour extends OneShotBehaviour {
	
	// --- Variables -----------------
	private static final long serialVersionUID = 1111119 ;
	private static Logger logger = Logger.getRootLogger();

	private String queryResult;
	private KnowledgeBase knowledgeBase;
	
	/**
	 * Instantiates a new ProcessQueryAnswerBehaviour.
	 *
	 * @param knowledgeBase the knowledge base
	 * @param queryResult the query result
	 */
	public ProcessQueryAnswerBehaviour(KnowledgeBase knowledgeBase, String queryResult) {
		this.knowledgeBase = knowledgeBase;
		this.queryResult = queryResult;
	}
	
	@Override
	public void action() {
		
		if(this.statementsAreValid()) {
			this.addStatementsToModel();
			
		} else {
			logger.info("Consistency check not passed.\n The statements will not be added to the model.");
		}
	}
	
	/**
	 * Method to perform a consistency check on a copy of the jena model of the internal data model of an agent. 
	 * The copy contains the new statements. 
	 */
	private boolean statementsAreValid() {
		logger.info("Perform consistency check now");
		return UtilityMethods.checkRdfStatementConsistency(queryResult, knowledgeBase);
	}
	
	/**
	 * Method to add new statements to the original model in the internal data model and 
	 * run "OWL" reasoning afterwards to possibly generate new ABox instances.
	 */
	private void addStatementsToModel() {
		
		logger.info("Consistency check passed.\nAdd statements to model now.");
		UtilityMethods.executeSparqlUpdate(this.knowledgeBase, queryResult);
	}
}
