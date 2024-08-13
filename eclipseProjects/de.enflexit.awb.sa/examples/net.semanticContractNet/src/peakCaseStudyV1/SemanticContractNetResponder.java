package peakCaseStudyV1;

import org.apache.log4j.Logger;

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
	
	private static Logger logger = Logger.getRootLogger();

	public SemanticContractNetResponder(Agent a, MessageTemplate mt) {
		super(a, mt);
		knowledgeBase = ((ProsumerAgent) a).getKnowledgeBase(); 
	}

	@Override
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		
		logger.info("Agent " + this.getAgent().getLocalName() + ": CFP received from " + cfp.getSender().getLocalName());
		ACLMessage propose = cfp.createReply();
		propose.setPerformative(ACLMessage.PROPOSE);
		
		// create proposal 
		String flexOffer = this.createFlexOffer(); 
		propose.setContent(flexOffer);
		logger.info("Agent " + this.getAgent().getLocalName() + ": sending proposal to " + cfp.getSender().getLocalName());
		
		return propose;
	}

	private String createFlexOffer() {
		
		String relatedFlexRequest = ":fr02"; 
		
		String query = "CONSTRUCT {\r\n"
				+ "  ?fo ?p ?o .\r\n"
				+ "  ?s ?p ?fo .\r\n"
				+ "  ?interval ?dp ?dpv. \r\n"
				+ "}\r\n"
				+ "WHERE {\r\n"
				+ "    BIND(" + relatedFlexRequest + " AS ?flexRequest)\r\n"
				+ "    {\r\n"
				+ "        select ?fo where { \r\n"
				+ "            ?fo s4ener:relatesToRequest ?flexRequest .\r\n"
				+ "        } \r\n"
				+ "    }\r\n"
				+ "    ?fo ?p ?o .\r\n"
				+ "    OPTIONAL { ?s ?p ?fo . }\r\n"
				+ "    OPTIONAL {\r\n"
				+ "        ?fo s4ener:hasEffectivePeriod ?interval .\r\n"
				+ "        ?interval ?dp ?dpv .\r\n"
				+ "    }\r\n"
				+ "}";
		


		String queryPSS = UtilityMethods.addPrefixesToSparqlQuery(query, knowledgeBase);
		String queryResults = UtilityMethods.executeConstructQuery(queryPSS, knowledgeBase.getModel());
		
		return queryResults; 

	}

	@Override
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
		
		logger.info("Agent " + this.getAgent().getLocalName() + ": received proposal acceptance from " + cfp.getSender().getLocalName());
		ACLMessage inform = accept.createReply();
		inform.setPerformative(ACLMessage.INFORM);
		return inform;

	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		logger.info("Agent " + this.getAgent().getLocalName() + ": received proposal rejection from " + cfp.getSender().getLocalName());
	}
	
	private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		return (int) (Math.random() * 10);
	}

}
