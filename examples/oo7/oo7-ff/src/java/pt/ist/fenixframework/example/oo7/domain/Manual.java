package pt.ist.fenixframework.example.oo7.domain;

import java.util.Arrays;

public class Manual extends Manual_Base {
    
    public  Manual() {
        super();
    }
    
    public Manual(int manualLength) {
    	super();
    	this.setTitle("Manual");
		char[] textChars = new char[manualLength];
		Arrays.fill(textChars, 'm');
		this.setText(new String(textChars));
		this.setTextLength(Long.valueOf(manualLength));
	}
}
