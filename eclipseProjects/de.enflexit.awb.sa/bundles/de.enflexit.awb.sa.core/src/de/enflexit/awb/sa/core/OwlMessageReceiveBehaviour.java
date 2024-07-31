package de.enflexit.awb.sa.core;

import java.util.Set;

import org.apache.log4j.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The Class OwlMessageReceiveBehaviour.
 * 
 * This behaviour can be instantiated for agents to process OWL messages. 
 * The methods for handling the different types of messages according to their performative can be overridden.
 * 
 * @author Sebastian Toersleff, Helmut Schmidt University; Nils-Hendric Martens, University of Hamburg
 * 
 */
public class OwlMessageReceiveBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 1987731297499836350L;

	private static Logger logger = Logger.getRootLogger();
	
	String ontologyName; 
	MessageTemplate messageTemplate;
	Agent myAgent; 
	KnowledgeBase knowledgeBase; 
	
	// --- Set of AIDs of trusted agents whose message will be processed by the behaviour 
	Set<AID> trustedAgents; 
	
	/**
	 * Instantiates a new OWL message receive behaviour that enables an agent to process OWL messages.
	 * Depending on the performative, messages are processed by the suitable behaviour (AnswerQueryBehaviour, ProcessQueryBehaviour, ReceiveInformBehaviour). 
	 *
	 * @param ontologyName the ontology name will be used as a filter for incoming messages. Only messages that have the ontology field set to this name will be processed by the behaviour.
	 * @param myAgent the my agent
	 * @param knowledgeBase the knowledge base
	 * @param trustedAgents only messages received by these agents will be processed
	 */
	public OwlMessageReceiveBehaviour(String ontologyName, Agent myAgent, KnowledgeBase knowledgeBase, Set<AID> trustedAgents) {
		
		this.ontologyName = ontologyName; 	
		this.myAgent = myAgent; 
		this.knowledgeBase = knowledgeBase; 
		this.trustedAgents = trustedAgents; 
		
		//folgende Zeile auskommentiert; w�re relevant, wenn ein AbstractEnergyAgent genutzt werden w�rde
//		myAgent.getDefaultMessageReceiveBehaviour().addMessageTemplateToIgnoreList(this.getMessageTemplate());
	}
	
	public OwlMessageReceiveBehaviour(String ontologyName, Agent myAgent, KnowledgeBase knowledgeBase) {
		
		this.ontologyName = ontologyName; 	
		this.myAgent = myAgent; 
		this.knowledgeBase = knowledgeBase; 
		this.trustedAgents = null; 
	}
	
	

	public MessageTemplate getMessageTemplate() {
		if (messageTemplate == null) {
			this.messageTemplate = MessageTemplate.and(
				MessageTemplate.MatchOntology(this.ontologyName),
				MessageTemplate.or(
					MessageTemplate.MatchProtocol(SemanticAgentProtocols.OWL_INFORM), 
					MessageTemplate.MatchProtocol(SemanticAgentProtocols.OWL_QUERY)
				)
			);
		}
		return messageTemplate;
	}

	public void setMessageTemplate(MessageTemplate mt) {
		this.messageTemplate = mt;
	}
	
	@Override
	public void action() {
		
		
		ACLMessage message = this.myAgent.receive(this.getMessageTemplate());
		
		if (message != null) {
			
			AID messageSender = message.getSender();
			
			if (this.trustedAgents == null || this.trustedAgents.contains(messageSender)) {
				
				logger.info("Agent " + myAgent.getAID().getLocalName() + ": " + messageSender.getLocalName() + " is a trusted agent (based on AID comparison). Message will be processed.");	
				
				
				switch (message.getPerformative()) {
				
				case ACLMessage.QUERY_REF:
					logger.info("Agent " + myAgent.getAID().getLocalName() + ": received an OWL message (performative: QUERY_REF) from " + messageSender.getLocalName() + " .");	
					this.processQueryRefMessage(message); 
					break;
					
				case ACLMessage.INFORM: 
					logger.info("Agent " + myAgent.getAID().getLocalName() + ": received an OWL message (performative: INFORM) from " + messageSender.getLocalName() + " .");	

					this.processInformMessage(message); 
					break;
					
				case ACLMessage.INFORM_REF:
					logger.info("Agent " + myAgent.getAID().getLocalName() + ": received an OWL message (performative: INFORM_REF) from " + messageSender.getLocalName() + " .");	
					this.processQueryAnswer(message); 
					break;
					
				case ACLMessage.INFORM_IF: 
					break;
					
				case ACLMessage.AGREE: 
					break;
					
				case ACLMessage.QUERY_IF: 
					break;	
					
				case ACLMessage.REQUEST:
					break;	
				}
			} 
			
			else {
				logger.info("Agent " + myAgent.getAID().getLocalName() + ": agent " + messageSender.getLocalName() + " is not a trusted agent. Message will not be processed.");	
			}
			
		} else {
			this.block();			
		}
	}
	
	protected void processQueryAnswer(ACLMessage message) {

		ProcessQueryAnswerBehaviour pqab_ref = new ProcessQueryAnswerBehaviour(this.knowledgeBase, message.getContent());
		this.myAgent.addBehaviour(pqab_ref);
	}

	protected void processInformMessage(ACLMessage message) {

		ReceiveInformBehaviour rib = new ReceiveInformBehaviour(this.knowledgeBase, message.getContent());
		this.myAgent.addBehaviour(rib);		
	}

	protected void processQueryRefMessage(ACLMessage message) {
		
		AnswerQueryBehaviour aqb = new AnswerQueryBehaviour(this.knowledgeBase, message.getContent(), message.getConversationId(), message.getSender(), message.getProtocol(), ontologyName);
		this.myAgent.addBehaviour(aqb);		

	}

	/**
	 * Gets the trusted agents.
	 *
	 * @return the trusted agents
	 */
	public Set<AID> getTrustedAgents() {
		
		return this.trustedAgents;
	}

	/**
	 * Sets the trusted agents.
	 *
	 * @param trustedAgents the new trusted agents
	 */
	public void setTrustedAgents(Set<AID> trustedAgents) {
		
		this.trustedAgents = trustedAgents;
	}
}
