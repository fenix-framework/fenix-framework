package oo7;

import java.util.Random;

/**
 * OO7 Connection class.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class Conn {

	private Long id;
	private Integer length;
	private String type;
	private AtomicPart to;
	private AtomicPart from;
	
	// For use by frameworks which require a default constructor
	protected Conn() {
	}
	
	public Conn(AtomicPart from, AtomicPart to) {
		this.from = from;
		this.to = to;
		length = Integer.valueOf(new Random().nextInt(10));
		type = "Type" + new Random().nextInt(10);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AtomicPart getFrom() {
		return from;
	}

	public void setFrom(AtomicPart from) {
		this.from = from;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public AtomicPart getTo() {
		return to;
	}

	public void setTo(AtomicPart to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
