package peakCaseStudyV1;

import de.enflexit.awb.sa.core.KnowledgeBase;
import de.enflexit.awb.sa.core.UtilityMethods;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class SemanticContractNetResponder extends ContractNetResponder {
	
	private static final long serialVersionUID = 4896059995183694333L;

	private KnowledgeBase knowledgeBase; 
	
	private String latestFlexRequest = ":fr02"; 

	public SemanticContractNetResponder(Agent a, MessageTemplate mt) {
		super(a, mt);
		knowledgeBase = ((ProsumerAgent) a).getKnowledgeBase(); 
	}

	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		
		System.out.println("Agent " + this.getAgent().getLocalName() + ": CFP received from "+cfp.getSender().getName()+". Content is "+ "\n" +cfp.getContent());
		int proposal = this.evaluateAction();
		// We provide a proposal
		System.out.println("Agent " + this.getAgent().getLocalName() + ": Proposing " + proposal);
		ACLMessage propose = cfp.createReply();
		propose.setPerformative(ACLMessage.PROPOSE);
		String flexOffer = this.createFlexOffer(); 
		propose.setContent(flexOffer);
		return propose;
	}

	private String createFlexOffer() {
		
		String query = "CONSTRUCT {\n"
				+ "  ?fo ?p ?o .\n"
				+ "  ?s ?p ?fo .\n"
				+ "  ?interval ?dp ?dpv. \n"
				+ "}\n"
				+ "WHERE {\n"
				+ "    {\n"
				+ "        select ?fo where { \n"
				+ "			?fo s4ener:relatesToRequest " + this.latestFlexRequest + " .\n"
				+ "		} \n"
				+ "    }\n"
				+ "    ?fo ?p ?o .\n"
				+ "  	OPTIONAL { ?s ?p ?fo . }\n"
				+ "    OPTIONAL {\n"
				+ "    	?fo s4ener:hasEffectivePeriod ?interval .\n"
				+ "    	?interval ?dp ?dpv .\n"
				+ "  	}\n"
				+ "}";
		
//		String query = "CONSTRUCT {\n"
//				+ "  ?fo ?p ?o .\n"
//				+ "}\n"
//				+ "WHERE {\n"
//				+ "    {\n"
//				+ "        select ?fo where { \n"
//				+ "			?fo s4ener:relatesToRequest :fr02 .\n"
//				+ "		} \n"
//				+ "    }\n"
//				+ "    ?fo ?p ?o .\n"
//				+ "}";
		


		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
		String queryResults = UtilityMethods.executeConstructQuery(queryPSS, knowledgeBase.getModel());
		
		return queryResults; 

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
