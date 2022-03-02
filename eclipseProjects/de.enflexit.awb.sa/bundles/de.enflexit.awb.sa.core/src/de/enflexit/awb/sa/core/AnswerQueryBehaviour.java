package de.enflexit.awb.sa.core;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class AnswerQueryBehaviour extends OneShotBehaviour{

	private static final long serialVersionUID = 1111112 ;
	private static Logger logger = Logger.getRootLogger();
	
	private KnowledgeBase knowledgeBase;
	private String queryString;
	private String msgRef;
	private AID receiverAID;
	private String[][] solutionArray;
	private String contentString = "";
	String ontologyName; 
	
	
	public AnswerQueryBehaviour(KnowledgeBase knowledgeBase, String queryString, String msgRef, AID receiverAID, String ontologyName) {
		this.knowledgeBase = knowledgeBase;
		this.queryString = queryString;
		this.msgRef = msgRef;
		this.receiverAID = receiverAID;
		this.ontologyName = ontologyName; 
	}
		
	
	@Override
	public void action() {
		
		// --- If Jena is updated, the queryType is different (probably a string) and these lines need to be updated accordingly ----
		// 111=Select, 222=Construct, 333=Describe, 444=Ask, 555=Json, -123=Unknown
		int queryTypeInteger = UtilityMethods.createQueryFromString(this.queryString).getQueryType();
		logger.debug("Agent " + this.myAgent.getLocalName() + " has received query of type= " + queryTypeInteger);
		
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
	
	// --- Send ACL INFORM_REF message
	private void sendACLMessage() {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM_REF);
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(receiverAID);
		msg.setContent(contentString);
		msg.setLanguage("TURTLE");
		msg.setOntology(ontologyName);
		msg.setProtocol("FIPA_QUERY"); // könnte auch übergeben werden
		msg.setConversationId(msgRef);
		
		logger.debug("AnswerQueryBehaviour of agent " + this.myAgent.getLocalName() + ": INFORM_REF message sent to agent " + this.receiverAID.getLocalName());
		
		this.myAgent.send(msg);
	}

}
