package de.enflexit.awb.sa.core;

import org.apache.log4j.Logger;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The Class SendInformBehaviour.
 * 
 * @author Sebastian Toersleff, Helmut Schmidt University
 */
public class SendInformBehaviour extends OneShotBehaviour{
	
	// --- Variables -----------------
	private static final long serialVersionUID = 1111116 ;
	private static Logger logger = Logger.getRootLogger();
	
	private AID receiverAID;
	private String rdfString;
	private String conversationId; 
	private String ontologyName; 
	
	
	/**
	 * Instantiates a new send inform behaviour.
	 *
	 * @param receiverAID the receiver AID
	 * @param rdfString the rdf string that will be sent
	 * @param conversationId the conversation id
	 * @param ontologyName the ontology name
	 */
	public SendInformBehaviour(AID receiverAID, String rdfString, String conversationId, String ontologyName) {
		this.receiverAID = receiverAID;
		this.rdfString = rdfString;
		this.conversationId = conversationId; 
		this.ontologyName = ontologyName; 
	}

	@Override
	public void action() {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(this.receiverAID);
		msg.setContent(this.rdfString);
		msg.setLanguage("TURTLE");
		msg.setOntology(ontologyName);
		//Protokoll für diesen Anwendugnsfall auslassenauslassen?
		msg.setConversationId(this.conversationId);
		
		this.myAgent.send(msg);
		
	}

}
