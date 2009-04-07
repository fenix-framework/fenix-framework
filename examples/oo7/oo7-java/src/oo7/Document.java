package oo7;

import java.util.Arrays;

/**
 * OO7 Document class.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class Document {

	private String title;
	private Long id;
	private String text;
	
	// For use by frameworks which require a default constructor
	protected Document() {
	}
	
	public Document(int documentLength) {
		title = "Title";
		char[] textChars = new char[documentLength];
		Arrays.fill(textChars, 'd');
		text = new String(textChars);
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
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
}
