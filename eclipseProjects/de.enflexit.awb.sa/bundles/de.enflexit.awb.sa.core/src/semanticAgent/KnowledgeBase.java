package semanticAgent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.log4j.Logger;
import jade.core.Agent;

/**
 * @author Sebastian T�rsleff, Helmut Schmidt University; Nils-Hendric Martens, University of Hamburg
 *
 */

public class KnowledgeBase {
	
	// --- Static Members------------------------------------------------------
	private static Logger logger = Logger.getRootLogger();
	
	private File knowledgeBaseFolder;
	private Model model; 
	private NamespaceList namespaceList;
	
	private Agent myAgent; 
	private String ontologyDirectory; 
	private String ontologyFileName;
	
	// --- Constructor for knowledgeBase --------------------------------------
	public KnowledgeBase(Agent myAgent, String ontologyDirectory, String ontologyFileName) {
		
		this.myAgent = myAgent; 
		this.ontologyDirectory = ontologyDirectory; 
		this.ontologyFileName = ontologyFileName; 
		
		// --- instantiate a model using ModelFactory 
		model = this.instantiateModelInFactory();   
	}
	
	private Model instantiateModelInFactory() {
		
		String filePath = this.getKnowledgeBaseFolderPath() + File.separator + ontologyFileName; 
		Model model = ModelFactory.createDefaultModel();
		model.read(filePath); 		
		return model;
	}

	public void printAllModelStatements () {
		StmtIterator iter = model.listStatements();
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
	
	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
	public void closeModel () {
		this.model.close(); 
	}
	
	/**
	 * A method to save the current in-memory model of the knowledgeBase (Ontology) of a semantic agent into a file on hard disk. 
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
	    try {				// --- Select serialization method here
	        this.model.write( out, "RDF/XML" );
	    }
	    	finally {
	       			try {
	       				out.close();
	       			}
	       			catch (IOException closeException){
	       				closeException.printStackTrace();
	       			}
	  		}
		logger.info("Ontology of "+ this.myAgent.getAID().getLocalName() + " successfully saved to: " + this.getKnowledgeBaseFolderPath()+  File.separator + fileSaveName);
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
			// TODO Find the ontology specific namespace entries in the Jena Model (if possible)
			
		}
		return namespaceList; 
	}

}
