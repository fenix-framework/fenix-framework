package oo7;

/**
 * OO7 Assembly class.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class Assembly extends DesignObj {
	private ComplexAssembly superAssembly;
	private Module module;
	
	public Module getModule() {
		return module;
	}
	
	public void setModule(Module module) {
		this.module = module;
	}
	
	public ComplexAssembly getSuperAssembly() {
		return superAssembly;
	}
	
	public void setSuperAssembly(ComplexAssembly superAssembly) {
		this.superAssembly = superAssembly;
	}
}
