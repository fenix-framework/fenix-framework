package oo7;

import java.util.HashSet;
import java.util.Set;

/**
 * OO7 Composite Part class.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class CompositePart extends DesignObj{
	
	private Document document;
	private Set<AtomicPart> atomicParts;
	private AtomicPart rootPart;
	private Set<BaseAssembly> baseAssemblies;
	
	// For use by frameworks which require a default constructor
	protected CompositePart() {
	}
	
	public CompositePart(int documentLength) {
		Document doc = new Document(documentLength);
		changeDocument(doc);
	}
	
	public Set getBaseAssemblies() {
		return baseAssemblies;
	}

	public void setBaseAssemblies(Set<BaseAssembly> baseAssembly) {
		this.baseAssemblies = baseAssembly;
	}
	
	public void addBaseAssembly(BaseAssembly baseAsembly) {
		if (baseAssemblies == null) {
			baseAssemblies = new HashSet<BaseAssembly>();
		}
		baseAssemblies.add(baseAsembly);
	}

	public AtomicPart getRootPart() {
		return rootPart;
	}

	public void setRootPart(AtomicPart rootPart) {
		this.rootPart = rootPart;
	}

	public Set getAtomicParts() {
		return atomicParts;
	}
	
	public void setAtomicParts(Set<AtomicPart> atomicParts) {
		this.atomicParts = atomicParts;
	}
	
	public void addAtomicPart(AtomicPart atomicPart) {
		if (atomicParts == null) {
			atomicParts = new HashSet<AtomicPart>();
		}
		atomicPart.setCompositePart(this);
		atomicParts.add(atomicPart);
	}
	
	public Document getDocument() {
		return document;
	}
	
	private void setDocument(Document document) {
		this.document = document;
	}
	
	public void changeDocument(Document document) {
		setDocument(document);
	}
}
