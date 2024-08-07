package de.enflexit.awb.sa.core;

import java.util.ArrayList;

/**
 * The Class NamespaceList.
 */
public class NamespaceList extends ArrayList<de.enflexit.awb.sa.core.NamespaceList.NamespaceDescription> {
	
	private static final long serialVersionUID = -5419876949202012928L;

	/**
	 * Instantiates a new namespace list.
	 * the following default namespaces are added automatically: rdf, rdfs, owl, xsd, xml
	 */
	public NamespaceList() {
		
		// --- Add default namespaces and prefixes --------
		this.add(new NamespaceDescription("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		this.add(new NamespaceDescription("rdfs", "http://www.w3.org/2000/01/rdf-schema#"));
		this.add(new NamespaceDescription("owl", "http://www.w3.org/2002/07/owl#"));
		this.add(new NamespaceDescription("xsd", "http://www.w3.org/2001/XMLSchema#"));		
	}
	
	public void addNameSpace(String namespacePrefix, String namespaceIRI ) {
		this.addNameSpace(new NamespaceDescription(namespacePrefix, namespaceIRI));
	}
	
	public void addNameSpace(NamespaceDescription nameSpaceDescription) {
		this.add(nameSpaceDescription);
	}
	
	/**
	 * The Class NamespaceDescription.
	 *
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	public class NamespaceDescription {
		
		private String namespacePrefix;
		private String namespaceIRI;
		
		public NamespaceDescription(String namespacePrefix, String namespaceIRI) {
			this.setNamespacePrefix(namespacePrefix);
			this.setNamespaceIRI(namespaceIRI);
		}
		
		public String getNamespacePrefix() {
			return namespacePrefix;
		}
		public void setNamespacePrefix(String namespacePrefix) {
			this.namespacePrefix = namespacePrefix;
		}
		public String getNamespaceIRI() {
			return namespaceIRI;
		}
		public void setNamespaceIRI(String namespaceIRI) {
			this.namespaceIRI = namespaceIRI;
		}
	}
	
}
