package semanticContractNet;

import de.enflexit.awb.sa.core.KnowledgeBase;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class SemanticContractNetResponder extends ContractNetResponder {
	
	private KnowledgeBase knowledgeBase; 

	public SemanticContractNetResponder(Agent a, MessageTemplate mt) {
		super(a, mt);
		knowledgeBase = ((ResponderAgent) a).getKnowledgeBase(); 
	}

	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		
		System.out.println("Agent " + this.getAgent().getLocalName() + ": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
		int proposal = this.evaluateAction();
		// We provide a proposal
		System.out.println("Agent " + this.getAgent().getLocalName() + ": Proposing " + proposal);
		ACLMessage propose = cfp.createReply();
		propose.setPerformative(ACLMessage.PROPOSE);
		propose.setContent(String.valueOf(proposal));
		return propose;
	}

	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
		
		System.out.println("Agent " + this.getAgent().getLocalName() + ": Proposal accepted");
		System.out.println("Agent " + this.getAgent().getLocalName() + ": Action successfully performed");
		ACLMessage inform = accept.createReply();
		inform.setPerformative(ACLMessage.INFORM);
		return inform;

	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		System.out.println("Agent " + this.getAgent().getLocalName() + ": Proposal rejected");
	}
	
	private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		return (int) (Math.random() * 10);
	}

}
