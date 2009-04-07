package oo7;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * OO7 Atomic Part class.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class AtomicPart extends DesignObj {

	private Integer x;
	private Integer y;
	private Long docId;
	private Set<Conn> connections;
	private CompositePart compositePart;
	
	private static final Random rand = new Random();
	
	// For use by frameworks which require a default constructor
	protected AtomicPart() {
	}
	
	public AtomicPart(long buildDate) {
		super(buildDate);
		x = Integer.valueOf(rand.nextInt(10));
		y = Integer.valueOf(rand.nextInt(10));
		docId = Long.valueOf(rand.nextInt(100));
	}
	
	public CompositePart getCompositePart() {
		return compositePart;
	}

	public void setCompositePart(CompositePart compositePart) {
		this.compositePart = compositePart;
	}

	public void addConnection(AtomicPart part) {
		if (connections == null) {
			connections = new HashSet<Conn>();
		}
		Conn conn = new Conn(this, part);
		connections.add(conn);
	}
	
	public Set<Conn> getConnections() {
		return connections;
	}

	public void setConnections(Set<Conn> connections) {
		this.connections = connections;
	}

	public Long getDocId() {
		return docId;
	}
	
	public void setDocId(Long docId) {
		this.docId = docId;
	}
	
	public Integer getX() {
		return x;
	}
	
	public void setX(Integer x) {
		this.x = x;
	}
	
	public Integer getY() {
		return y;
	}
	
	public void setY(Integer y) {
		this.y = y;
	}
}
