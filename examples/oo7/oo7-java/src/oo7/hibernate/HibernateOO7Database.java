package oo7.hibernate;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import oo7.Assembly;
import oo7.AtomicPart;
import oo7.BaseAssembly;
import oo7.ComplexAssembly;
import oo7.CompositePart;
import oo7.Manual;
import oo7.Module;
import oo7.OO7Database;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
/**
 * This class uses Hibernate to create the OO7 database.
 * This class is not thread safe.
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public class HibernateOO7Database extends OO7Database {

	private static Random rand = new Random();
	
	private Configuration cfg = null;
	private SessionFactory sf = null;
	
	private PrintStream infoStream;
	
	public static void main(String args[]) {
		int dbSize;
		if (args.length < 1) {
			throw new IllegalArgumentException("Not enough arguments spceified");
		}
		dbSize = Integer.parseInt(args[0]);
		
		new HibernateOO7Database(dbSize).createOO7Database();
	}
	
	public HibernateOO7Database(int scale) {
		this(scale, System.out);
	}
	
	public HibernateOO7Database(int scale, PrintStream pw) {
		super(scale);
		infoStream = pw;
		cfg = new Configuration().configure();
		sf = cfg.buildSessionFactory();
	}
	
	/**
	 * Create entire OO7 database.
	 */
	public void createOO7Database() {
		
		infoStream.println("Creating database schema ...");
		
		new SchemaExport(cfg).drop(true, true);
		new SchemaExport(cfg).create(true, true);
		
		infoStream.println("Finished creating database schema.");
		
		for (int i = 0; i < numModules(); i++) {
			infoStream.println("Creating module ...");
			createModule();
			infoStream.println("Finished creating module.");
		}
		
		infoStream.println("Done.");
	}
	
	/**
	 * Create a single OO7 module
	 */
	public void createModule() {
		
		Session sess = null;
		Transaction tx = null;
		
		// We will split the work across multiple sessions to avoid creating the 
		// entire module in memory.
		// First we create the assembly hierarchy and the composite parts.
		// Second we connect choose composite parts for the base assemblies.
		
		// Create assembly hierarchy and composite parts
		Set<BaseAssembly> baseAssemblies = new HashSet<BaseAssembly>();
		CompositePart[] compositeParts;
		
		try {
			Set<Assembly> allAssemblies = new HashSet<Assembly>();
			Module module = new Module();
			module.changeManual(new Manual(manualSize()));
			
			sess = getSession();
			tx = sess.beginTransaction();
			sess.save(module);
	
			infoStream.println("Creating assemblies..");			
			ComplexAssembly rootAssembly = new ComplexAssembly();
			allAssemblies.add(rootAssembly);
			module.changeDesignRoot(rootAssembly);
			createSubAssemblies(rootAssembly, 1, baseAssemblies, allAssemblies);
			module.setAssemblies(allAssemblies);
			
			infoStream.println("Creating composite parts...");
			
			compositeParts = createCompositeParts(sess);
			
			tx.commit();
			
			infoStream.println("Number of assemblies created: " + allAssemblies.size());
			infoStream.println("Number of base assemblies created: " + baseAssemblies.size());
			infoStream.println("Number of composite parts created: " + compositeParts.length);
		} finally {
			if (tx != null && tx.isActive()) { // We didn't commit transaction
				tx.rollback();
			}
			if (sess != null) {
				sess.close();
			}
		}
		
		// Create parts
		infoStream.println("Associating composite parts with assemblies.");
		Iterator<BaseAssembly> baseAssembliesIter = baseAssemblies.iterator();
		while (baseAssembliesIter.hasNext()) {
			try {			
				sess = getSession();
				tx = sess.beginTransaction();
				BaseAssembly bAssm = baseAssembliesIter.next();
				BaseAssembly constructed = (BaseAssembly)sess.load(BaseAssembly.class, bAssm.getId());
				createAssemblyParts(constructed, compositeParts, sess);
				tx.commit();
			} catch (HibernateException he) {
				if (tx != null) {
					tx.rollback();
				}
				throw new RuntimeException("Could not create module", he);
			} finally {
				baseAssembliesIter.remove();
				sess.close();
			}
		}
	}
	
    private CompositePart[] createCompositeParts(Session sess) {
    	CompositePart[] compositeParts = new CompositePart[numComponentsPerModule()];
    	for (int i = 0; i < compositeParts.length; i++) {
			CompositePart compositePart = new CompositePart(documentSize());
			sess.save(compositePart);
			compositePart.setRootPart(createAtomicParts(compositePart, sess));
			if (rand.nextDouble() < YOUNG_COMP_FRAC) { // young composite part
				compositePart.setBuildDate(Long.valueOf(chooseRandomYoungCompDate()));
			} else {
				compositePart.setBuildDate(Long.valueOf(chooseRandomOldCompDate()));
			}
			compositeParts[i] = compositePart;
			
			// We don't want to store all the parts in memory
			if (i % 10 == 0) {
				sess.flush();
				sess.clear();
				infoStream.println("Created composite part #" + i);
			}
    	}
    	return compositeParts;
    }
	
	private void createAssemblyParts(BaseAssembly assm, CompositePart[] compositeParts, Session sess) {
		for (int i = 0; i < numComponentsPerAssembly(); i++) {
			Long partId = compositeParts[rand.nextInt(compositeParts.length)].getId();
			CompositePart compositePart = (CompositePart) sess.load(CompositePart.class, partId);
			assm.addUnsharedPart(compositePart);
		}
	}

	private AtomicPart createAtomicParts(CompositePart compositePart, Session sess) {
		
		// First create atomic parts and put them into a ring
		AtomicPart[] atomicParts = new AtomicPart[numAtomicPartsPerComponent()];
		for (int i = 0; i < atomicParts.length; i++) {
			atomicParts[i] = new AtomicPart(chooseAtomicDate());
			compositePart.addAtomicPart(atomicParts[i]);
			sess.save(atomicParts[i]);
			if (i > 0) { // make connection to previous part
				atomicParts[i - 1].addConnection(atomicParts[i]);
			}		
		}
		// Form ring by connecting last atomic part to first atomic part
		if (atomicParts.length > 1) {
			atomicParts[atomicParts.length - 1].addConnection(atomicParts[0]);
		}
		// Put rest of connections into atomic parts
		for (int i = 0; i < atomicParts.length; i++) {
			for (int j = 0; j < numConnectionsPerAtomicPart() - 1; j++) {
				// Choose random part to connect to that isn't ourself
				int randomPartIndex;
				do {
					randomPartIndex = rand.nextInt(atomicParts.length - 1);
				} while (randomPartIndex == i);
				
				atomicParts[i].addConnection(atomicParts[randomPartIndex]);
			}
		}
		
		return atomicParts[0];
	}
	
	private void createSubAssemblies(ComplexAssembly cAssm, int level,
            Set<BaseAssembly> baseAssemblies, Set<Assembly> allAssemblies) {
		if (level < numAssemblyLevels() - 1) {
			for (int i = 0; i < numAssembliesPerAssembly(); i++) {
				infoStream.println("Creating interior assembly node at level: " + level);
				ComplexAssembly subAssm = new ComplexAssembly();
				subAssm.setModule(cAssm.getModule());
				subAssm.setBuildDate(Long.valueOf(chooseAssmDate()));
				cAssm.addSubAssembly(subAssm);
				allAssemblies.add(subAssm);
				createSubAssemblies(subAssm, level + 1, baseAssemblies, allAssemblies);;
			}
		} else {
			for (int i =0; i < numAssembliesPerAssembly(); i++) {
				infoStream.println("Creating leaf assembly node.");
				BaseAssembly subAssm = new BaseAssembly();
				subAssm.setModule(cAssm.getModule());
				subAssm.setBuildDate(Long.valueOf(chooseAssmDate()));
				cAssm.addSubAssembly(subAssm);
				baseAssemblies.add(subAssm);
				allAssemblies.add(subAssm);
			}
		}
	}
	
	private Session getSession() {
		return sf.openSession();
	}
}
