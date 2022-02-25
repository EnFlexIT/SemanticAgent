package semanticAgent;

import org.apache.log4j.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


/**
 * A behaviour to send queries as ACL Messages to other semantic agents
 * @author Sebastian T�rsleff, Helmut Schmidt University; Nils-Hendric Martens, University of Hamburg
 *
 */
public class SendQueryBehaviour extends OneShotBehaviour{
	
	private static final long serialVersionUID = 2259573879927368562L;

	private static Logger logger = Logger.getRootLogger();
	
	private AID receiverAID;
	private String queryString;
	private String ontologyName; 
	String conversationId; 
	
	
	public SendQueryBehaviour(Agent sendingAgent, String queryString, AID receiverAID, String conversationId, String ontologyName) {
		super(sendingAgent);
		this.queryString = queryString;
		this.receiverAID = receiverAID;
		this.conversationId = conversationId; 
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
		msg.setProtocol("FIPA_QUERY");
		msg.setConversationId(this.conversationId);
//		logger.debug(msg.toString());
		
		// --- Send message ------------------------------------
		this.myAgent.send(msg);	

	}
}
