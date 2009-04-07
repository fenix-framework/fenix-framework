package oo7;

import java.util.HashSet;
import java.util.Set;

/**
 * OO7 Complex Assembly class.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class ComplexAssembly extends Assembly {

	private Set<Assembly> subAssemblies;

	public Set<Assembly> getSubAssemblies() {
		return subAssemblies;
	}

	public void setSubAssemblies(Set <Assembly>subAssemblies) {
		this.subAssemblies = subAssemblies;
	}
	
	public void addSubAssembly(Assembly assembly) {
		if (subAssemblies == null) {
			subAssemblies = new HashSet<Assembly>();
		}
		assembly.setSuperAssembly(this);
		subAssemblies.add(assembly);
	}

}
