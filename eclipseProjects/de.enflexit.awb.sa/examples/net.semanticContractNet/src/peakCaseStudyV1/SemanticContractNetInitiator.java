package peakCaseStudyV1;

import java.util.Vector;

import org.apache.log4j.Logger;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import de.enflexit.awb.sa.core.KnowledgeBase;
import de.enflexit.awb.sa.core.ReceiveInformBehaviour;
import de.enflexit.awb.sa.core.UtilityMethods;

class SemanticContractNetInitiator extends ContractNetInitiator {
	

	private static final long serialVersionUID = 7402712978730955161L;
	
	private int nParticipants; 
	private KnowledgeBase knowledgeBase; 
	
	private static Logger logger = Logger.getRootLogger();

	public SemanticContractNetInitiator(Agent a, ACLMessage cfp, int nParticipants) {
		super(a, cfp);
		this.nParticipants = nParticipants; 
		this.knowledgeBase =  ((MarketAgent) a).getKnowledgeBase(); 
	}

	protected void handleRefuse(ACLMessage refuse) {
		System.out.println("Agent " + this.getAgent().getLocalName() + ": Agent "+refuse.getSender().getName()+" refused");
	}

	protected void handlePropose(ACLMessage propose, Vector v) {
		logger.info("Agent " + this.getAgent().getLocalName() + ": "+propose.getSender().getLocalName()+" poposes \n"+propose.getContent());
	}
	
	protected void handleAllResponses(Vector responses, Vector acceptances) { // "acceptances" ist verwirrend, weil hier auch die Rejections reingetan werden 
		
		if (responses.size() < nParticipants) {
			// Some participants didn't reply within the specified timeout
			logger.info("Agent " + this.getAgent().getLocalName() + ": timeout expired, missing " + (nParticipants - responses.size()) + " responses");
		}
				
		int nProposals = 0;
		
		AID bestProposer = null;
		ACLMessage accept = null;
		
		for (int i = 0; i < responses.size(); ++i) { 
			ACLMessage rsp = (ACLMessage) responses.get(i); 
			if (rsp.getPerformative() == ACLMessage.PROPOSE) { 
				nProposals++; 
				
				logger.info("Agent " + this.getAgent().getLocalName() + ": trying to add proposal from " + rsp.getSender().getLocalName()+ " to knowledge base");
				// save content of proposal to knowledge base 
				String flexOfferTriples = rsp.getContent();
				if (!flexOfferTriples.isEmpty()) {
					this.addOfferToKnowledgeBase(flexOfferTriples); 
				}
				
				ACLMessage reply = rsp.createReply();
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				acceptances.addElement(reply); 	// es werden erstmal alle Nachrichten mit REJECT in den Antwortvektor geschoben
												// weiter unten kann das Performative der Gewinnerproposals in ACCEPT geändert werden
				
			} 
			
		}	
		
		if (nProposals > 0) {
			logger.info("Agent " + this.getAgent().getLocalName() + ": all proposals were added to knowledgebase, now evaluating winning offer"); 
			// perform optimization and select winning proposal(s)
			
			// if solution is found
			// acceptances.add(... // add winning proposal(s) to acceptances
		} else {
			logger.info("Agent " + this.getAgent().getLocalName() + ": no proposals received."); 
		}
		
	}

	private void addOfferToKnowledgeBase(String flexOfferTriples) {
		
		logger.info("Agent " + myAgent.getAID().getLocalName() + ": performing consistency check for received proposal...");
		boolean triplesAreValid = UtilityMethods.checkRdfStatementConsistency(flexOfferTriples, this.knowledgeBase);
		if (triplesAreValid) {
			logger.info("Agent " + myAgent.getAID().getLocalName() + ": consistency check passed; adding triples to the knowledge base.");
			UtilityMethods.addTriplesToKnowledgeBase(this.knowledgeBase, flexOfferTriples); 
		} else {
			logger.info("Agent " + myAgent.getAID().getLocalName() + ": consistency check not passed; triples will not be added to the knowledge base.");
		}
			
	}

	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			logger.info("Agent " + this.getAgent().getLocalName() + ": responder does not exist");
		}
		else {
			logger.info("Agent " + this.getAgent().getLocalName() + ": "+failure.getSender().getLocalName()+" failed");
		}
		// Immediate failure --> we will not receive a response from this agent
	}
	
	
	
	

}
