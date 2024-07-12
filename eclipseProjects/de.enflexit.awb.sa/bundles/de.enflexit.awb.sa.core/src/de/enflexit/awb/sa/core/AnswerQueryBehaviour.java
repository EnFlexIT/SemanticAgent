package de.enflexit.awb.sa.core;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class AnswerQueryBehaviour. The result of the query is send to the asking agent using the INFORM_REF performative.
 * 
 * @author Sebastian Toersleff, Helmut Schmidt University; Nils-Hendric Martens, University of Hamburg
 */
public class AnswerQueryBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = 1111112 ;
	private static Logger logger = Logger.getRootLogger();
	
	private KnowledgeBase knowledgeBase;
	private String queryString;
	private String conversationId;
	private AID receiverAID;
	private String contentString = "";
	private String protocol; 
	String ontologyName; 
	
	
	/**
	 * Instantiates a new AnswerQueryBehaviour.
	 *
	 * @param knowledgeBase the knowledge base
	 * @param queryString the query string (only construct queries can be processed)
	 * @param conversationId the conversation ID provided by the agent that sent the query
	 * @param receiverAID the receiver AID
	 * @param ontologyName the ontology name
	 */
	public AnswerQueryBehaviour(KnowledgeBase knowledgeBase, String queryString, String conversationId, AID receiverAID, String protocol, String ontologyName) {
		this.knowledgeBase = knowledgeBase;
		this.queryString = queryString;
		this.conversationId = conversationId;
		this.receiverAID = receiverAID;
		this.protocol = protocol; 
		this.ontologyName = ontologyName; 
	}
		
	
	@Override
	public void action() {
		
		// --- If Jena is updated, the queryType is different (probably a string) and these lines need to be updated accordingly ----
		// 111=Select, 222=Construct, 333=Describe, 444=Ask, 555=Json, -123=Unknown
		int queryTypeInteger = UtilityMethods.createQueryFromString(this.queryString).getQueryType();
		
		switch (queryTypeInteger) {
			
			// --- Select Queries sollen nicht beantwortet werden, daher auskommentiert  -------------------------
//			case 111: 	this.solutionArray = UtilityMethods.executeSelectQuery(this.queryString, this.knowledgeBase.getModel());
//						contentString = UtilityMethods.stringArrayToString(solutionArray);
//						break;
			
			case 222:	this.contentString = UtilityMethods.executeConstructQuery(this.queryString, this.knowledgeBase.getModel());
						break;
		}
		
		this.sendACLMessage();
	}
	
	private void sendACLMessage() {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM_REF);
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(receiverAID);
		msg.setContent(contentString);
		msg.setLanguage("RDF/XML");
		msg.setOntology(ontologyName);
		msg.setProtocol(protocol); 
		msg.setConversationId(conversationId);
		
		
		this.myAgent.send(msg);
		logger.info("Agent " + myAgent.getAID().getLocalName() + ": message (performative INFORM_REF) sent to agent " + this.receiverAID.getLocalName() + ".");
	}

}
