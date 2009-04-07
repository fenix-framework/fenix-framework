package oo7;

import java.util.HashSet;
import java.util.Set;

/**
 * OO7 Module class.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class Module extends DesignObj {
	
	private Manual manual;
	private Set<Assembly> assemblies;
	private ComplexAssembly designRoot;
	
	public Set<Assembly> getAssemblies() {
		return assemblies;
	}
	
	public void setAssemblies(Set<Assembly> assemblies) {
		this.assemblies = assemblies;
	}
	
	public void addAssembly(Assembly assembly) {
		if (assemblies == null) {
			assemblies = new HashSet<Assembly>();
		}
		assembly.setModule(this);
		assemblies.add(assembly);
	}
	
	public ComplexAssembly getDesignRoot() {
		return designRoot;
	}
	
	private void setDesignRoot(ComplexAssembly designRoot) {
		this.designRoot = designRoot;
	}
	
	public void changeDesignRoot(ComplexAssembly designRoot) {
		designRoot.setModule(this);
		setDesignRoot(designRoot);
	}
	
	public Manual getManual() {
		return manual;
	}
	
	private void setManual(Manual manual) {
		this.manual = manual;
	}
	
	public void changeManual(Manual manual) {
		setManual(manual);
	}
}
