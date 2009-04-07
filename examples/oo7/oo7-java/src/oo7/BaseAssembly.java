package oo7;

import java.util.HashSet;
import java.util.Set;

/**
 * OO7 Base Assembly class.
 *
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class BaseAssembly extends Assembly {

	private Set<CompositePart> sharedParts;
	private Set<CompositePart> unsharedParts;

	public Set getSharedParts() {
		return sharedParts;
	}

	public void setSharedParts(Set<CompositePart> sharedParts) {
		this.sharedParts = sharedParts;
	}

	public Set<CompositePart> getUnsharedParts() {
		return unsharedParts;
	}

	public void setUnsharedParts(Set<CompositePart> unsharedParts) {
		this.unsharedParts = unsharedParts;
	}

	public void addUnsharedPart(CompositePart part) {
		if (unsharedParts == null) {
			unsharedParts = new HashSet<CompositePart>();
		}
		part.addBaseAssembly(this);
		unsharedParts.add(part);
	}
}
