package peakCaseStudyV1;

import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import de.enflexit.awb.sa.core.KnowledgeBase;
import de.enflexit.awb.sa.core.ReceiveInformBehaviour;

class SemanticContractNetInitiator extends ContractNetInitiator {
	

	private static final long serialVersionUID = 7402712978730955161L;
	
	private int nParticipants; 
	private KnowledgeBase knowledgeBase; 

	public SemanticContractNetInitiator(Agent a, ACLMessage cfp, int nParticipants) {
		super(a, cfp);
		this.nParticipants = nParticipants; 
		this.knowledgeBase =  ((MarketAgent) a).getKnowledgeBase(); 
	}

	protected void handleRefuse(ACLMessage refuse) {
		System.out.println("Agent "+refuse.getSender().getName()+" refused");
	}

	protected void handlePropose(ACLMessage propose, Vector v) {
		System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
	}
	
	protected void handleAllResponses(Vector responses, Vector acceptances) { // "acceptances" ist verwirrend, weil hier auch die Rejections reingetan werden 
		
		if (responses.size() < nParticipants) {
			// Some participants didn't reply within the specified timeout
			System.out.println("Timeout expired: missing " + (nParticipants - responses.size()) + " responses");
		}
				
		int nProposals = 0;
		
		AID bestProposer = null;
		ACLMessage accept = null;
		
		for (int i = 0; i < responses.size(); ++i) { 
			ACLMessage rsp = (ACLMessage) responses.get(i); 
			if (rsp.getPerformative() == ACLMessage.PROPOSE) { 
				nProposals++; 
				System.out.println("Adding proposal from " + rsp.getSender().getName()+ " to knowledge base");
				
				// save content of proposal to knowledge base 
				String flexOfferTriples = rsp.getContent();
				if (!flexOfferTriples.isEmpty()) {
					this.myAgent.addBehaviour(new ReceiveInformBehaviour(knowledgeBase, flexOfferTriples));
				}
				ACLMessage reply = rsp.createReply();
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				acceptances.addElement(reply); 	// es werden erstmal alle Nachrichten mit REJECT in den Antwortvektor geschoben
												// weiter unten kann das Performative der Gewinnerproposals in ACCEPT geändert werden
				
			} 
			
		}	
		
		if (nProposals > 0) {
			System.out.println("All proposals added to knowledgebase, now evaluating winning offer"); 
			// perform optimization and select winning proposal(s)
			
			// if solution is found
			// acceptances.add(... // add winning proposal(s) to acceptances
		} else {
			System.out.println("No proposals received."); 
		}
		
	}

	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// FAILURE notification from the JADE runtime: the receiver
			// does not exist
			System.out.println("Responder does not exist");
		}
		else {
			System.out.println("Agent "+failure.getSender().getName()+" failed");
		}
		// Immediate failure --> we will not receive a response from this agent
	}
	
	
	
	

}
