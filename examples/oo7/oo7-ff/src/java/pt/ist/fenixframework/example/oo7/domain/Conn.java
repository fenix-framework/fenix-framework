package pt.ist.fenixframework.example.oo7.domain;

import java.util.Random;

public class Conn extends Conn_Base {

    private static Random rand = new Random();

	public  Conn() {
        super();
    }

	public Conn(AtomicPart from, AtomicPart to) {
		super();
		this.setFrom(from);
		this.setTo(to);
		this.setLength(rand.nextInt(10));
		this.setType("Type" + rand.nextInt(10));
	}

}
