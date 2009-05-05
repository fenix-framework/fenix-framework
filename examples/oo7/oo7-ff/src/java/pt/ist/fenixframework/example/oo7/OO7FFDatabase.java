package pt.ist.fenixframework.example.oo7;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import pt.ist.fenixframework.example.oo7.domain.Assembly;
import pt.ist.fenixframework.example.oo7.domain.AtomicPart;
import pt.ist.fenixframework.example.oo7.domain.BaseAssembly;
import pt.ist.fenixframework.example.oo7.domain.ComplexAssembly;
import pt.ist.fenixframework.example.oo7.domain.CompositePart;
import pt.ist.fenixframework.example.oo7.domain.Document;
import pt.ist.fenixframework.example.oo7.domain.Manual;
import pt.ist.fenixframework.example.oo7.domain.Module;
import pt.ist.fenixframework.example.oo7.domain.OO7Application;
import pt.ist.fenixframework.example.oo7.extra.OO7Database;
import pt.ist.fenixframework.pstm.Transaction;

public class OO7FFDatabase extends OO7Database {

	private PrintStream infoStream;
	private static Random rand = new Random();

	public OO7FFDatabase(int scale) {
		this(scale,System.out);
	}

	public OO7FFDatabase(int scale,PrintStream pw) {
		super(scale);
		infoStream = pw;
	}

	@Override
	public void createOO7Database() {
		for (int i = 0; i < numModules(); i++) {
			infoStream.println("Creating module ...");
			createModule();
			infoStream.println("Finished creating module.");
		}
		infoStream.println("Done.");
	}

	public void createModule() {


		// We will split the work across multiple sessions to avoid creating the
		// entire module in memory.
		// First we create the assembly hierarchy and the composite parts.
		// Second we connect choose composite parts for the base assemblies.

		// Create assembly hierarchy and composite parts
		Set<BaseAssembly> baseAssemblies = new HashSet<BaseAssembly>();
		CompositePart[] compositeParts;

		try {
			Transaction.begin();
			Set<Assembly> allAssemblies = new HashSet<Assembly>();
			Module module = new Module();
			OO7Application.getInstance().addModules(module);
			Manual manual = new Manual(manualSize());
			module.changeManual(manual);
			OO7Application.getInstance().addManuals(manual);
			infoStream.println("Creating assemblies..");
			ComplexAssembly rootAssembly = new ComplexAssembly();
			allAssemblies.add(rootAssembly);
			module.changeDesignRoot(rootAssembly);
			createSubAssemblies(rootAssembly, 1, baseAssemblies, allAssemblies);
			//module.setAssemblies(allAssemblies);
			//TODO : efficiency? other way of doing this?
			for(Assembly a : allAssemblies)
				module.addAssemblies(a);

			infoStream.println("Creating composite parts...");

			Transaction.commit();
			compositeParts = createCompositeParts();

			infoStream.println("Number of assemblies created: " + allAssemblies.size());
			infoStream.println("Number of base assemblies created: " + baseAssemblies.size());
			infoStream.println("Number of composite parts created: " + compositeParts.length);
		} finally {
		}
		// Create parts
		infoStream.println("Associating composite parts with assemblies.");
		Iterator<BaseAssembly> baseAssembliesIter = baseAssemblies.iterator();
		while (baseAssembliesIter.hasNext()) {
			try {
				Transaction.begin();
				BaseAssembly bAssm = baseAssembliesIter.next();
				//BaseAssembly constructed = (BaseAssembly)Transaction.getDomainObject(BaseAssembly.class.getName(), bAssm.getId().intValue());
				createAssemblyParts(/*constructed*/bAssm, compositeParts);
				Transaction.commit();
			} finally {
				baseAssembliesIter.remove();
			}
		}
	}

	private CompositePart[] createCompositeParts() {
    	CompositePart[] compositeParts = new CompositePart[numComponentsPerModule()];
    	boolean doIt = true;
    	for (int i = 0; i < compositeParts.length; i++) {
    		if (doIt) {
    			System.out.println("Begin Transaction");
    			Transaction.begin();
    			doIt = false;
    		}
    		Document doc = new Document(documentSize());
    		OO7Application.getInstance().addDocuments(doc);
			CompositePart compositePart = new CompositePart(doc);
			//sess.save(compositePart);
			compositePart.setRootPart(createAtomicParts(compositePart/*, sess*/));
			if (rand.nextDouble() < YOUNG_COMP_FRAC) { // young composite part
				compositePart.setBuildDate(Long.valueOf(chooseRandomYoungCompDate()));
			} else {
				compositePart.setBuildDate(Long.valueOf(chooseRandomOldCompDate()));
			}
			compositeParts[i] = compositePart;

			// We don't want to store all the parts in memory
			if (i % 10 == 0 && !doIt) {
				System.out.println("Commit Transaction");
				Transaction.commit();
				doIt = true;
				infoStream.println("Created composite part #" + i);
			}
    	}
    	//if (!doIt)
    		Transaction.commit();
    	return compositeParts;
    }

	private void createAssemblyParts(BaseAssembly assm, CompositePart[] compositeParts/*, Session sess*/) {
		for (int i = 0; i < numComponentsPerAssembly(); i++) {
			/*Long partId = compositeParts[rand.nextInt(compositeParts.length)].getId();
		    CompositePart compositePart = (CompositePart) sess.load(CompositePart.class, partId);
			CompositePart compositePart = (CompositePart) Transaction.getDomainObject(CompositePart.class.getName(), partId.intValue());*/
			CompositePart compositePart = compositeParts[rand.nextInt(compositeParts.length)];
			assm.addUnsharedPart(compositePart);
		}
	}

	private AtomicPart createAtomicParts(CompositePart compositePart/*, Session sess*/) {

		// First create atomic parts and put them into a ring
		AtomicPart[] atomicParts = new AtomicPart[numAtomicPartsPerComponent()];
		for (int i = 0; i < atomicParts.length; i++) {
			atomicParts[i] = new AtomicPart(chooseAtomicDate());
			compositePart.addAtomicPart(atomicParts[i]);
			OO7Application.getInstance().addAtomicParts(atomicParts[i]);
			//sess.save(atomicParts[i]);
			if (i > 0) { // make connection to previous part
				//TODO : changed .. new method at atomicPart Class to addConnection
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
				//cAssm.addSubAssembly(subAssm);
				cAssm.addSubAssemblies(subAssm);
				allAssemblies.add(subAssm);
				createSubAssemblies(subAssm, level + 1, baseAssemblies, allAssemblies);;
			}
		} else {
			for (int i =0; i < numAssembliesPerAssembly(); i++) {
				infoStream.println("Creating leaf assembly node.");
				BaseAssembly subAssm = new BaseAssembly();
				OO7Application.getInstance().addBaseAssemblies(subAssm);
				subAssm.setModule(cAssm.getModule());
				subAssm.setBuildDate(Long.valueOf(chooseAssmDate()));
				//cAssm.addSubAssembly(subAssm);
				cAssm.addSubAssemblies(subAssm);
				baseAssemblies.add(subAssm);
				allAssemblies.add(subAssm);
			}
		}
	}


}
