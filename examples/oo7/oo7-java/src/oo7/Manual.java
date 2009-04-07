package oo7;

import java.util.Arrays;

/**
 * OO7 Manual class.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class Manual {

	private String title;
	private Long id;
	private String text;
	private Long textLength;
	
	// For use by frameworks which require a default constructor
	protected Manual() {
	}
	
	public Manual(int manualLength) {
		title = "Manual";
		char[] textChars = new char[manualLength];
		Arrays.fill(textChars, 'm');
		text = new String(textChars);
		textLength = Long.valueOf(manualLength);
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Long getTextLength() {
		return textLength;
	}
	
	public void setTextLength(Long textLength) {
		this.textLength = textLength;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}	
}
