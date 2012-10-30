package pt.ist.fenixframework.dml;

public class Annotation {

    public static final String INDEX_ANNOTATION = "{\"unique\":true}";
    
    private String name;
    
    public Annotation(String name) {
	this.name = name;
    }
    
    public void setName(String name) {
	this.name = name;
    }
    
    public String getName() {
	return this.name;
    }
    
}
