package oo7;

import java.util.Random;

/**
 * OO7 base clase.
 *
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class DesignObj {

	private Long id;
	private String type;
	private Long buildDate;

	private static final Random rand = new Random();


	public DesignObj() {
	}

	public DesignObj(long buildDate) {
		this();
		type = "Type" + rand.nextInt(10);
		this.buildDate = Long.valueOf(buildDate);
	}

	public Long getBuildDate() {
		return buildDate;
	}

	public void setBuildDate(Long buildDate) {
		this.buildDate = buildDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
