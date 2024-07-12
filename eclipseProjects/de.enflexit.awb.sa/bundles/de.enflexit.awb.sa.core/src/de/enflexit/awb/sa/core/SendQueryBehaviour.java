package de.enflexit.awb.sa.core;

import org.apache.log4j.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


/**
 * A behaviour to send QUERY_REF ACL messages containing SPARQL construct queries to other agents
 * 
 * @author Sebastian Toersleff, Helmut Schmidt University; Nils-Hendric Martens, University of Hamburg
 *
 */
public class SendQueryBehaviour extends OneShotBehaviour{
	
	private static final long serialVersionUID = 2259573879927368562L;

	private static Logger logger = Logger.getRootLogger();
	
	private AID receiverAID;
	private String queryString;
	private String protocol; 
	private String ontologyName; 
	String conversationId; 
	
	
	/**
	 * Instantiates a new send query behaviour.
	 *
	 * @param sendingAgent the sending agent
	 * @param queryString the query string (needs to be SPARQL construct query)
	 * @param receiverAID the receiver AID
	 * @param conversationId the conversation id
	 * @param ontologyName the ontology name 
	 */
	public SendQueryBehaviour(Agent sendingAgent, String queryString, AID receiverAID, String conversationId, String protocol, String ontologyName) {
		super(sendingAgent);
		this.queryString = queryString;
		this.receiverAID = receiverAID;
		this.conversationId = conversationId; 
		this.protocol = protocol;
		this.ontologyName = ontologyName; 
	}
	
	@Override
	public void action() {
		
		// --- Create QUERY_REF message ----------------
		ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(this.receiverAID); 
		msg.setContent(this.queryString);
		msg.setLanguage("SPARQL");
		msg.setOntology(this.ontologyName); 
		msg.setProtocol(protocol);
		msg.setConversationId(this.conversationId);
//		logger.debug(msg.toString());
		
		// --- Send message ------------------------------------
		this.myAgent.send(msg);	
		
		logger.info("Agent " + myAgent.getAID().getLocalName() + ": message (performative QUERY_REF) sent to agent " + this.receiverAID.getLocalName() + ".");


	}
}
