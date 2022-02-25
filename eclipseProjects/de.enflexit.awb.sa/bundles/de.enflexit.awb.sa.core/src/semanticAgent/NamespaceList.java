package semanticAgent;

import java.util.ArrayList;

public class NamespaceList extends ArrayList<semanticAgent.NamespaceList.NameSpaceDescription> {
	
	private static final long serialVersionUID = -5419876949202012928L;

	/**
	 * Instantiates a new name space list.
	 */
	public NamespaceList() {
		
		// --- Add default namespaces --------
		this.add(new NameSpaceDescription("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", true));
		
	}
	
	public void addNameSpace(String namespacePrefix, String namespaceIRI, boolean isGeneralNamespace) {
		this.addNameSpace(new NameSpaceDescription(namespacePrefix, namespaceIRI, isGeneralNamespace));
	}
	public void addNameSpace(NameSpaceDescription nameSpaceDescription) {
		this.add(nameSpaceDescription);
	}
	
	
	
	
	
	/**
	 * The Class NameSpaceDescription.
	 *
	 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
	 */
	public class NameSpaceDescription {
		
		private String namespacePrefix;
		private String namespaceIRI;
		private boolean isGeneralNamespace;
		
		public NameSpaceDescription(String namespacePrefix, String namespaceIRI, boolean isGeneralNamespace) {
			this.setNamespacePrefix(namespacePrefix);
			this.setNamespaceIRI(namespaceIRI);
			this.setGeneralNamespace(isGeneralNamespace);
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
		public boolean isGeneralNamespace() {
			return isGeneralNamespace;
		}
		public void setGeneralNamespace(boolean isGeneralNamespace) {
			this.isGeneralNamespace = isGeneralNamespace;
		}
	}
	
}
