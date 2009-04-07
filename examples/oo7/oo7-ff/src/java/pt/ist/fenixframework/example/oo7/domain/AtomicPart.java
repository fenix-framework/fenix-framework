package pt.ist.fenixframework.example.oo7.domain;

import java.util.List;
import java.util.Random;

public class AtomicPart extends AtomicPart_Base {
	private static final Random rand = new Random();

    public  AtomicPart() {
        super();
    }

    public AtomicPart(long buildDate) {
    	super();
    	this.init(buildDate);
    }

    public void init(long buildDate) {
    	super.init(buildDate);
    	setX(rand.nextInt(10));
    	setY(rand.nextInt(10));
    	setDocId(Long.valueOf(rand.nextInt(100)));
    }

    public void addConnection(AtomicPart to) {
    	Conn c = new Conn();
    	c.setFrom(this);
    	c.setTo(to);
    }

    public List<Conn> getConnections() {
    	List<Conn> conns = getConnectionsFrom();
    	conns.addAll(getConnectionsTo());
    	return conns;
    }
}
