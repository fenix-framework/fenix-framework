package pt.ist.fenixframework.example.oo7.domain;

import java.util.Random;

public class DesignObj extends DesignObj_Base {

    private static Random rand = new Random();

    public  DesignObj() {
        super();
    }

    public DesignObj(long buildDate) {
		super();
		init(buildDate);
	}

    public void init(long buildDate) {
    	this.setType("Type" + rand.nextInt(10));
		this.setBuildDate(Long.valueOf(buildDate));
    }

    @Override
    public Long getId() {
    	return super.get$idInternal().longValue();
    }

}
