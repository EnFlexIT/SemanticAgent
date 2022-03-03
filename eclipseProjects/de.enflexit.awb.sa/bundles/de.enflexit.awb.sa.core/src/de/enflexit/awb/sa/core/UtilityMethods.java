package de.enflexit.awb.sa.core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.*;
import org.apache.log4j.Logger;
import de.enflexit.awb.sa.core.NamespaceList.NamespaceDescription;

import org.apache.jena.reasoner.*;


/**
 * This class is used to hold some operations regarding SPARQL like 
 * creating and executing SELECT and CONSTRUCT queries and parameterized queries, 
 * performing SPARQL updates and consistency checks on inferred models....
 * @author Sebastian Törsleff, Helmut Schmidt University; Nils-Hendric Martens, University of Hamburg
 *
 */
public class UtilityMethods{
	// --- Variables -----------------
	private static final long serialVersionUID = 391335478;
	private static Logger logger = Logger.getRootLogger();
	
	/**
	 * This static method is used to first create and then execute SPARQL updates on the provided Jena model
	 * @param model Jena model, target of update
	 * @param updateString A complete and valid SPARQL update string in TURTLE syntax with prefixes, no checks are made on syntax. 
	 * This string is used to add to a updateRequest object.
	 */
	public static void executeSparqlUpdate(Model model, String updateString) {	// TODO eventuell einbauen, dass das Update erst ausgefürt wird, wenn chechRdfConsistency erfolgreich war	
		UpdateRequest updateRequest = UpdateFactory.create();
		updateRequest.add(updateString);
		UpdateAction.execute(updateRequest, model);
//		logger.debug("-------------New model-------------");
//		logger.debug(model.write(System.out, "TURTLE"));

	}
	
	/**
	 * This static method will create a SPARQL update string that can be added to a UpdateRequest in a subsequent step. 
	 * The prefixes according to the UTILITYSTRINGS are added automatically.
	 * 
	 * @param updateString Content of update, only consisting of valid triples. NO checks are made on syntax. 
	 * It's recommended to separate every triple with a dot.
	 * @return A string that can be added to a UpdateRequest object
	 */
	public static String addPrefixesToSparqlUpdate(String updateString, KnowledgeBase kb)  { // TODO create variation for graph-specific updates 
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		
		// --- Define command text here, here SPARQL Update/"INSERT DATA" ---
		// --- As long as not defined in another way, this command text ---
		// --- will use the default graph to operate on -------------------
		pss.setCommandText("INSERT DATA\n"
						 + "{\n"
						 +updateString+"\n"
						 + "}");
		
		// --- Define command text here, possibility to select a graph to operate on
//		pss.setCommandText("INSERT DATA\n"
//						+ "{ GRAPH <>\n" //Insert special graph to update on here
//						+ "{\n"
//						+updateString+".\n"
//						+ "}}");
			
		pss.setBaseUri(kb.getBaseUri()); 
		
		NamespaceList namespaceList = kb.getNamespaceList(); 
		for (int i = 0; i < namespaceList.size(); i++) {
			NamespaceDescription nd = namespaceList.get(i);
			pss.setNsPrefix(nd.getNamespacePrefix(), nd.getNamespaceIRI());
		}
			
		return pss.toString();
	}
	
	/**This static method creates a SPARQL query using the provided command text (SELECT or CONSTRUCT query).
	 * The prefixes according to the UTILITYSTRINGS are added automatically.
	 * 
	 * @param commandText Content of the SPARQL query, either SELECT or CONSTRUCT queries are possible 
	 * @return SPARQL query string with prefixes
	 */
	
	public static String addPrefixesToSparqlQuery(String commandText, KnowledgeBase kb) {
		
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		
		pss.setCommandText(commandText);
//		pss.setBaseUri(UtilityStrings.lvGridFlexNamespaceIri); 
//		pss.setNsPrefix(UtilityStrings.owlQname, UtilityStrings.owlNamespaceIri);
//		pss.setNsPrefix(UtilityStrings.rdfQname, UtilityStrings.rdfNamespaceIri);
//		pss.setNsPrefix(UtilityStrings.rdfsQname, UtilityStrings.rdfsNamespaceIri);
//		pss.setNsPrefix(UtilityStrings.xsdQname, UtilityStrings.xsdNamespaceIri);
//		pss.setNsPrefix(UtilityStrings.xmlQname, UtilityStrings.xmlNamespaceIri);
//		pss.setNsPrefix(UtilityStrings.lvGridFlexQname, UtilityStrings.lvGridFlexNamespaceIri);
		
		pss.setBaseUri(kb.getBaseUri()); 
		
		NamespaceList namespaceList = kb.getNamespaceList(); 
		for (int i = 0; i < namespaceList.size(); i++) {
			NamespaceDescription nd = namespaceList.get(i);
			pss.setNsPrefix(nd.getNamespacePrefix(), nd.getNamespaceIRI());
		}
		
		return pss.toString();
	}

	
	/**
	 * Method to create a Jena Query object from a valid SPARQL query string
	 * @param queryString String that contains a SPARQL query including prefixes
	 * @return Query A Jena query object that can be used to create a Jena QueryExecution object
	 */
	public static Query createQueryFromString(String queryString) {
		Query query = null;
		
		try {
			query = QueryFactory.create(queryString);
			}
			catch(QueryException createQueryException){
				logger.debug("Exception thrown creating query: " +createQueryException);
			}
		
		if(query==null) {logger.debug("Query is NULL!");}
		
		return query;
	}

	/**
	 * Method to create and execute SPARQL CONSTRUCT queries received as a String 
	 * on the provided model. Returns a Turtle-serielized String consisting of 
	 * SPO (subject, predicate, object) triples with full IRIs for all resources and literals. 
	 * @param constructQuery String containing a SPARQL CONSTRUCT query
	 * @param model Target of the query
	 * @return Returns a well formed solution string serialized in TURTLE containing 
	 * statements as defined in the CONSTRUCT query. These statements can be added to
	 * a model via a SPARQL update.
	 */
	public static String executeConstructQuery(String constructQuery, Model model) {
		Query query = null;
		String queryResult = "";		
		
		// --- Create a SPARQL query object from a string -------------
		try {query = QueryFactory.create(constructQuery);
		}
			catch(QueryException createQueryException){
				logger.debug("Exception thrown creating query: " +createQueryException);
			}
		if(query==null) {logger.debug("Query is NULL!");}
		
		// --- Create a query execution -----------------
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		if(qexec==null) {logger.debug("QueryExecution is NULL!");}
		
		// --- Execute CONSTRUCT query ------------------
		Model solutionModel = ModelFactory.createDefaultModel();
		solutionModel = qexec.execConstruct(solutionModel);
		
		// --- With this code one can create the result string in RDF/XML serialization ---
//		java.io.StringWriter out = new java.io.StringWriter();
//		solutionModel.write(out, "RDF/XML");
//		logger.debug(out.toString());
		
		// --- Building the solution string containing single statements from the solution model (=query answer)
		StmtIterator stmtIterator = solutionModel.listStatements();
		try {
			while(stmtIterator.hasNext()) {
		
				Statement stmt = stmtIterator.nextStatement();
				RDFNode s = stmt.getSubject();
				RDFNode p = stmt.getPredicate();
				RDFNode o = stmt.getObject();
			
				if(o.isLiteral()) {
					Literal literal = (Literal) o; 
					String value = literal.getLexicalForm(); 
					String datatypeUri = literal.getDatatypeURI();
					queryResult += "<" + s + "> <" + p + "> \"" + value + "\"^^<" + datatypeUri + ">.\n";
				}
				else if (o.isResource()) {
					queryResult += "<" + s + "> <" + p + "> <" + o + ">.\n";
				}

			}
		} finally {
			qexec.close();
		}
		
		return queryResult;
	}
	
	/**
	 * Method to execute a SPARQL SELECT query received as a String on the provided model. 
	 * The fields include IRIs for resources and only values for literals.
	 * Will only return first column in case of a multi-column query result.
	 * 
	 * @param queryString String containing the SPARQL SELECT query
	 * @param model Target of the query
	 * @return Returns a one-dimensional string array containing only the values of the bindings. 
	 * The array size is determined by the number of rows in the query result.
	 */
	public static String[] executeSingleColumnSelectQuery(String queryString, Model model) {
		
		String[][] multiColSolStrArray = UtilityMethods.executeMultiColumnSelectQuery(queryString, model);
		
		String[] singleColSolStrArray = new String[multiColSolStrArray.length]; 
		
		for (int i=0; i<=multiColSolStrArray.length-1; i++) {
			singleColSolStrArray[i] = multiColSolStrArray[i][0];
		}
				
		return singleColSolStrArray;
	}

	/**
	 * Method to execute a SPARQL SELECT query received as a String on the provided model. 
	 * The fields include IRIs for resources and only values for literals.
	 * Column headers (as per the query) are not included in the resulting String[]
	 * 
	 * @param queryString String containing the SPARQL SELECT query
	 * @param model Target of the query
	 * @return Returns a two-dimensional string array containing only the values of the bindings. 
	 * The array size is determined by the number of variables and rows in the query result.
	 */
	public static String[][] executeMultiColumnSelectQuery(String queryString, Model model) {
		Query query = null;
		String[][] solStrArray= null;
		ResultSet results = null;
		QuerySolution qsolution = null;
		
		
		// --- Create query from string ----------------
		try {query = QueryFactory.create(queryString);
		}
		catch(QueryException createQueryException){
			logger.debug("Exception thrown creating query: " +createQueryException);
		}
		if(query==null) {logger.debug("Query is NULL!");}
		
		// --- Create query execution object -----------
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		if(qexec==null) {logger.debug("QueryExecution is NULL!");}
		
		// --- Execute query and prepare results -------		
		results = qexec.execSelect();
		
		List<String> resultVariables = results.getResultVars(); //Die Bezeichner der Ergebnisvariablen entsprechend der übergebenen Query
		
		int nofColumns = resultVariables.size();
		solStrArray = new String[1][nofColumns];
		try {
			int i = 0;
			
			while(results.hasNext()) {
				qsolution = results.next();
				
				for (int j = 0; j < nofColumns; j++) {
					RDFNode rdfNode = qsolution.get(resultVariables.get(j)); 
					
					if (rdfNode != null) {
						if (rdfNode.isResource()) {
							solStrArray[i][j] = ((Resource) rdfNode).getURI();
							logger.debug("Content of cell "+ i + "/" + j + ": " + solStrArray[i][j]);
						}
						else if (rdfNode.isLiteral()) {
							solStrArray[i][j] = ((Literal) rdfNode).asLiteral().getLexicalForm(); //getLexicalForm() -> without unit
							logger.debug("Content of cell "+ i + "/" + j + ": " + solStrArray[i][j]); 
							logger.debug("\t Datatype of this Literal: "+ rdfNode.asLiteral().getDatatypeURI());
						}
					}
				}
				
				// --- Append additional row to solStrArray if there are more results -------
				if(results.hasNext()) {
					String[][] tmp = new String[solStrArray.length+1][resultVariables.size()];
					System.arraycopy(solStrArray, 0, tmp, 0, solStrArray.length);
					solStrArray = tmp;
				}
				i++;
			}
		} finally {
			qexec.close();
		}
	
		return solStrArray;
	}
		
	/**
	 * A method to generate a inferred model from a given Jena model (the provided model is not changed)
	 * @param model Target of the inference process, must be the model from the agents knowledge base
	 * @param reasonerString Identifier for reasoner to use. "OWL" / "RDFS" / "transitive" possible, OWL Reasoner used by default
	 * @return Inferred model with added statements that were implicit before
	 */
	public static Model generateInferredModel(Model model, String reasonerString) {
		Reasoner reasoner=null;
		if(reasonerString=="OWL") {
			reasoner = ReasonerRegistry.getOWLReasoner();
		}
		else if(reasonerString=="RDFS") {
			reasoner = ReasonerRegistry.getRDFSReasoner();
		}
		else if(reasonerString=="transitive") {
			reasoner = ReasonerRegistry.getTransitiveReasoner();
		}
//		else if(reasonerString=="openllet") { // In case the openllet reasoner gets implemented
//			Reasoner reasoner = PelletReasonerFactory.theInstance().create();
//		}
		else {
			reasoner = ReasonerRegistry.getOWLReasoner();
		}
		
		InfModel infModel = ModelFactory.createInfModel(reasoner, model);
		
		ValidityReport validity = infModel.validate();
		if (validity.isValid()) {
		    logger.info("Validity: OK");
		} else {
		    logger.info("Validity: NOT OK\nSee conflicts");
		    for (Iterator i = validity.getReports(); i.hasNext(); ) {
		        ValidityReport.Report report = (ValidityReport.Report)i.next();
		        logger.info(" - " + report);
		    }
		}
		
		return infModel;
		
	}
	
	/**
	 * A method to perform a consistency check on a Jena model to verify if new statements can be added to
	 * that model without harming any restrictions of the ontology. Therefore a copy of the model gets created, 
	 * to which the new statements are added via a SPARQL update. The selected reasoner then validates the model and prints
	 * a validity report. OWL reasoner by default. In case the validity report shows at least one error/conflict, the statements should not be added to the model.
	 * @param stringToAddToModel Well formed string containing triples in TURTLE Syntax.
	 * @param model Jena model to make a deep copy from
	 * @return Boolean: True, if modified model passed consistency check, otherwise false
	 */
	public static boolean checkRdfStatementConsistency(String stringToAddToModel, KnowledgeBase kb) {
		boolean validityResult = false;
		
		Model modelCopy = ModelFactory.createDefaultModel().add(kb.getModel()); 
		
		String updateString = addPrefixesToSparqlUpdate(stringToAddToModel, kb);
		
		// --- Add statement(s) to copy of model for further consistency checks -----
		UtilityMethods.executeSparqlUpdate(modelCopy, updateString);
		
		// --- Select the reasoner -------------------------
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//		Reasoner reasoner = PelletReasonerFactory.theInstance().create(); //going to be added
//		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		
		// --- Create inferred model --------------------------------------
		InfModel inferredModel = ModelFactory.createInfModel(reasoner, modelCopy);
		
		// --- Create validity report -------------------------------------
		ValidityReport validity = inferredModel.validate();
		if (validity.isValid()) {
		    logger.info("Validity = OK");
		    validityResult = true;
		} else {
			logger.info("Validity = NOT OK \nConflicts");
			logger.info("Statement(s) not added to model due to inconsistency");
		    for (Iterator i = validity.getReports(); i.hasNext(); ) {
		        ValidityReport.Report report = (ValidityReport.Report)i.next();
		        logger.info(" - " + report);
		    }
		}
		return validityResult;
	}
	
	/**
	 * Method to turn a one-dimensional string array into a string that is consisting of individual 
	 * lines of text for each field of the array.
	 * 
	 * @param stringArray the one-dimensional string array supposed to be transformed to a simple string
	 * @return the generatedString
	 */
	public static String oneDimensionalStringArrayToString(String[] stringArray) {
		
		String generatedString = new String(); 
		
		int nofRemainingLines = stringArray.length; 
		
		if (stringArray[0]!=null) {
			for (int i=0; i<=stringArray.length-1 ;i++) {
				generatedString += stringArray[i].toString();
				nofRemainingLines--;
				if (nofRemainingLines > 0) {
					generatedString += " \n";
				}
			}
			logger.debug("SemanticUtilities.oneDimensionalStringArrayToString(): generatedString = \n" + generatedString);
		}
		else {
				logger.debug("String array is empty");
				generatedString = null;
		}
		
		return generatedString;
	}
	

	/**
	 * Converts a double to a String that can be used as an object of a triple in a SPARQL Update
	 * statement. It uses the qname "xsd".  
	 *
	 * @param doubleValue 
	 * @return the double as xsd double
	 */
	public static String doubleToXsdDouble(double doubleValue) {
		
		String doubleAsXsdDouble = "\"" + doubleValue + "\"^^xsd:double";
		
		return doubleAsXsdDouble;
	}

	/**
	 * Converts a String to a String that can be used as an object of a triple in a SPARQL Update
	 * statement. It uses the qname "rdfs".  
	 * 
	 * @param stringValue
	 * @return stringAsRdfsLiteral
	 */
	public static String stringtoRdfsLiteral(String stringValue) {
		
		String stringAsRdfsLiteral = "\"" + stringValue + "\"^^rdfs:Literal";
		
		return stringAsRdfsLiteral;
	}

	/**
	 * Converts a String to a String that can be used as an object of a triple in a SPARQL Update
	 * statement. It uses the qname "xsd".  
	 * 
	 * The provided String must be in the following format: YYYY-MM-DDTHH:MM:SS, e.g. 2022-02-04T20:27:00
	 * 
	 * @param timeStamp
	 * @return
	 */
	public static String stringToXsdDateTime(String timeStamp) {
		
		String stringAsXsdDateTime = "\"" + timeStamp + "\"^^xsd:dateTime";
		
		return stringAsXsdDateTime;
	}

	/**
	 * Runs a specified inference engine on the model of a given knowledgeBase. 
	 * The model in the given knowledge base is replaced with the inferred model.
	 * 
	 * @param knowledgeBase
	 * @param infEngine
	 */
	public static void runInferenceEngineOnModel(KnowledgeBase knowledgeBase, String infEngine) {
		
		knowledgeBase.setModel(UtilityMethods.generateInferredModel(knowledgeBase.getModel(), infEngine));	
	}	
	
	/**
	 * Method to execute a SPARQL SELECT query received as a String on the provided model. 
	 * The result includes an IRI for resources and only values for literals, i.e. the values of datatype properties.
	 * Only works properly for select queries that have a single field as a result.
	 * 
	 * @param queryString String containing the SPARQL SELECT query
	 * @param model Target of the query
	 * @return Returns a String containing the value of the cell
	 */
	public static String executeSingleCellSelectQuery(String queryString, Model model) {
		
		String[][] multiColSolStrArray = UtilityMethods.executeMultiColumnSelectQuery(queryString, model);
		
		String singleCellResult = UtilityMethods.executeSingleColumnSelectQuery(queryString, model)[0]; 
				
		return singleCellResult;
	}
	
}
