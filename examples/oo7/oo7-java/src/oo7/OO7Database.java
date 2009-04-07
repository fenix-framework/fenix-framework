package oo7;

import java.util.Random;

/**
 * Abstract class which defines many of the parameters
 * of the OO7 data store.
 * Also provides some useful static methods for choosing
 * various properties of created objects.
 * 
 * @author Ali Ibrahim <aibrahim@cs.utexas.edu>
 */
public abstract class OO7Database {
	
	private static final Random rand = new Random();
	
	public static int TINY = 0;
	public static int SMALL = 1;
	public static int MEDIUM = 2;
	public static int LARGE = 3;
	
	/** 0: Tiny, 1: Small, 2: Medium, 3: Large **/
	protected int scale = 0;
	
	// Define the parameters of the OO7 database relative
	// to the scale chosen.
	public static final int[] DOCUMENT_SIZE = {2000, 2000, 20000, 20000};
	public static final int[] MANUAL_SIZE = {10000, 100000, 1000000, 1000000};
	public static final int[] NUM_COMP_PER_MODULE = {50, 500, 500, 500};
	public static final int[] NUM_COMP_PER_ASSM = {2, 3, 3, 3};
	public static final int[] NUM_ATOMIC_PER_COMP = {4, 20, 200, 200};
	public static final int[] NUM_CONN_PER_ATOMIC = {3, 3, 6, 9};
	public static final int[] NUM_MODULES = {1, 1, 1, 10};
	public static final int[] NUM_ASSM_PER_ASSM = {3, 3, 3, 3};
	public static final int[] NUM_ASSM_LEVELS = {4, 7, 7, 7};
	public static final int MIN_ASSM_DATE = 1000;
	public static final int MAX_ASSM_DATE = 1999;
	public static final int MIN_YOUNG_COMP_DATE = 2000;
	public static final int MAX_YOUNG_COMP_DATE = 2999;
	public static final int MIN_OLD_COMP_DATE = 0;
	public static final int MAX_OLD_COMP_DATE = 999;
	public static final int MIN_ATOMIC_DATE = 1000;
	public static final int MAX_ATOMIC_DATE = 1999;
	public static final double YOUNG_COMP_FRAC = .1;

	public OO7Database(int scale) {
		this.scale = scale;
	}
	
	public int documentSize() {
		return DOCUMENT_SIZE[scale];
	}
	
	public int manualSize() {
		return MANUAL_SIZE[scale];
	}
	
	public int numComponentsPerModule() {
		return NUM_COMP_PER_MODULE[scale];
	}
	
	public int numComponentsPerAssembly() {
		return NUM_COMP_PER_ASSM[scale];
	}
	
	public int numAtomicPartsPerComponent() {
		return NUM_ATOMIC_PER_COMP[scale];
	}
	
	public int numAssembliesPerAssembly() {
		return NUM_ASSM_PER_ASSM[scale];
	}
	
	public int numConnectionsPerAtomicPart() {
		return NUM_CONN_PER_ATOMIC[scale];
	}
	
	public int numModules() {
		return NUM_MODULES[scale];
	}
	
	public int numAssemblyLevels() {
		return NUM_ASSM_LEVELS[scale];
	}
	
	public static int chooseAtomicDate() {
		return chooseRandomBetween(MIN_ATOMIC_DATE, MAX_ATOMIC_DATE);
	}

	public void setScale(int newScale) {
		scale = newScale;
	}

	public static int chooseAssmDate() {
		return chooseRandomBetween(MIN_ASSM_DATE, MAX_ASSM_DATE);
	}

	public static int chooseRandomYoungCompDate() {
		return chooseRandomBetween(MIN_YOUNG_COMP_DATE, MAX_YOUNG_COMP_DATE);
	}

	public static int chooseRandomOldCompDate() {
		return chooseRandomBetween(MIN_OLD_COMP_DATE, MAX_OLD_COMP_DATE);
	}

	public static int chooseRandomBetween(int low, int high) {
		return rand.nextInt(high - low + 1) + low;
	}

	public abstract void createOO7Database();
}
