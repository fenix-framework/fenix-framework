package pt.ist.fenixframework.example.oo7.domain;

import java.util.Arrays;

public class Document extends Document_Base {

    public  Document() {
        super();
    }

    public Document(int documentLength) {
    	super();
		this.setTitle("Title");
		char[] textChars = new char[documentLength];
		Arrays.fill(textChars, 'd');
		this.setText(new String(textChars));
	}

    @Override
    public Long getId() {
    	return super.get$idInternal().longValue();
    }

}
