package pt.ist.fenixframework.example.oo7.domain;

public class Module extends Module_Base {

    public  Module() {
        super();
    }

    public void changeManual(Manual manual) {
    	setManual(manual);
	}

    public void changeDesignRoot(ComplexAssembly designRoot) {
		designRoot.setModule(this);
		setDesignRoot(designRoot);
	}
}
