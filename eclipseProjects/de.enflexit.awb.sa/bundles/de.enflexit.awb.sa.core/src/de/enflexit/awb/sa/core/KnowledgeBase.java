package de.enflexit.awb.sa.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileManagerImpl;
import org.apache.log4j.Logger;

import de.enflexit.awb.sa.core.NamespaceList.NamespaceDescription;
import jade.core.Agent;

/**
 * @author Sebastian Toersleff, Helmut Schmidt University; Nils-Hendric Martens, University of Hamburg
 *
 */

public class KnowledgeBase {
	
	// --- Static Members------------------------------------------------------
	private static Logger logger = Logger.getRootLogger();
	
	private File knowledgeBaseFolder;
	private OntModel ontModel; 
	private NamespaceList namespaceList;
	
	private Agent myAgent; 
	private String ontologyDirectory; 
	private String ontologyFileName;
	
	private String baseUri; 
	
	// --- Constructor for knowledgeBase --------------------------------------
	public KnowledgeBase(Agent myAgent, String ontologyDirectory, String ontologyFileName, String baseUri, OntModelSpec ontModelSpec) {
		
		this.myAgent = myAgent; 
		this.ontologyDirectory = ontologyDirectory; 
		this.ontologyFileName = ontologyFileName; 
		this.setBaseUri(baseUri); 
		
		FileManager.get().resetCache();
		
		// --- instantiate a ontModel using ModelFactory 
		ontModel = this.instantiateModelInFactory(ontModelSpec);
	}
	
	private OntModel instantiateModelInFactory(OntModelSpec ontModelSpec) {
				
		String filePath = this.getKnowledgeBaseFolderPath() + File.separator + ontologyFileName; 
		OntModel ontModel = ModelFactory.createOntologyModel(ontModelSpec); 
		logger.info("Agent " + myAgent.getLocalName() + ": creating ontology model from " + ontologyFileName); 
		
		ontModel.read(filePath);
		
		return ontModel;
	}

	public void printAllModelStatements () {
		StmtIterator iter = this.getModel().listStatements();
	    while (iter.hasNext()) {
	    	Statement stmt      = iter.nextStatement();  // get next statement
	    	Resource  subject   = stmt.getSubject();     // get the subject
	    	Property  predicate = stmt.getPredicate();   // get the predicate
	    	RDFNode   object    = stmt.getObject();      // get the object (RDFNode is a superclass of Literal and Resource)

	    	System.out.print(subject.toString());
	    	System.out.print(" " + predicate.toString() + " ");
	    
	    	if (object instanceof Resource) {
	    		System.out.print(object.toString());	     
	    	} else {
	    		// object is a literal
	    		System.out.print(" \"" + object.toString() + "\"");	     
	    	}
	    	System.out.println(" .");	 	    
	    } 
	}
	
	public OntModel getModel() {
		return ontModel;
	}

	public void setModel(OntModel ontModel) {
		this.ontModel = ontModel;
	}
	
	public void closeModel () {
		this.ontModel.close(); 
	}
	
	/**
	 * A method to save the current in-memory ontModel of the knowledgeBase (Ontology) of a semantic agent into a file on hard disk. 
	 * The file is saved under a new name: "..._postSim".
	 */
	public void saveModel() {
		FileWriter out = null;
		String fileSaveName = this.ontologyFileName.replaceAll(".owl","") + "_postSim.owl";
//		String fileSaveName = this.ontologyFileName + "_postSim" + ontologyFileType;
	    try {
	        out = new FileWriter(this.getKnowledgeBaseFolderPath()+  File.separator + fileSaveName);
	    	} 
	    		catch (IOException e) {
	    			e.printStackTrace();
	    		}
	    try {				// --- Select serialization here
	        this.ontModel.write(out, "TURTLE" );
	    }
	    	finally {
	       			try {
	       				out.close();
	       			}
	       			catch (IOException closeException){
	       				closeException.printStackTrace();
	       			}
	  		}
		logger.info("Agent "+ this.myAgent.getAID().getLocalName() + ": ontology successfully saved to " + this.getKnowledgeBaseFolderPath()+  File.separator + fileSaveName);
	}
	

	private String getKnowledgeBaseFolderPath() {
		
		if (knowledgeBaseFolder == null) {
			knowledgeBaseFolder = new File(this.ontologyDirectory); 
			if (knowledgeBaseFolder.exists() == false) {
				knowledgeBaseFolder.mkdirs(); 
			}			
		} 
		return knowledgeBaseFolder.getAbsolutePath(); 
	}
	
	public NamespaceList getNamespaceList() {
		if (namespaceList == null) {
			namespaceList = new NamespaceList();			
		}
		return namespaceList; 
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

}
