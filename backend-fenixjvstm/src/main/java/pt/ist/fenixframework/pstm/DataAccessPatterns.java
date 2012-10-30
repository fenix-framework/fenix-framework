package pt.ist.fenixframework.pstm;

import java.util.Iterator;

import jvstm.TopLevelTransaction;
import pt.ist.dap.implementation.simple.SimpleContextManager;
import pt.ist.dap.structure.PClass;
import pt.ist.dap.structure.PField;
import pt.ist.dap.structure.StructureManager;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.backend.fenixjvstm.FenixJvstmConfig;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

public class DataAccessPatterns {

    private static final String DAP_ROOT_DIR = "dap-" + Util.getHostAddress();

    private static boolean COLLECT_DATA = false;

    public static void init(FenixJvstmConfig config) {
	if (config.getCollectDataAccessPatterns()) {
	    try {
		COLLECT_DATA = true;
		SimpleContextManager.setPath(config.getCollectDataAccessPatternsPath() + DAP_ROOT_DIR);
		SimpleContextManager.init();
		setupDapStructures();
		startWriteThread();
	    } catch (Throwable e) {
		System.err.println("Problem during the initialization of the data-access patterns module: " + e);
		e.printStackTrace();
	    }
	}
    }

    public static void noteGetAccess(Object receiver, String slotName) {
	if (COLLECT_DATA) {
	    try {
		String ctxt = ((TopLevelTransaction) Transaction.current()).getContext();
		if ((ctxt != null) && (!ctxt.equals(""))) {
		    SimpleContextManager.updateReadStatisticsFenix(receiver.getClass().getName(), slotName, 1, ctxt);
		}
	    } catch (Throwable t) {
		// ignore all throwables so that no exception during
		// the data-access patterns collection breaks the
		// application
		System.err.println("DAP: noted exception during data collection " + t);
	    }
	}
    }

    private static void setupDapStructures() {
	DomainModel model = FenixFramework.getDomainModel();
	StructureManager mng = new StructureManager();

	for (Iterator iterator = model.getClasses(); iterator.hasNext();) {
	    DomainClass domClass = (DomainClass) iterator.next();
	    String className = domClass.getFullName();

	    PClass pClass = new PClass(className);

	    while (domClass != null) {
		Iterator<Slot> slots = domClass.getSlots();

		while (slots.hasNext()) {
		    Slot slot = slots.next();
		    String slotName = slot.getName();
		    String slotType = slot.getSlotType().getFullname();

		    pClass.addPField(slotName, new PField(slotName, slotType, className, false, false, false, 1));
		}

		for (Role role : domClass.getRoleSlotsList()) {
		    String roleName = role.getName();
		    if (roleName != null) {
			String roleType = role.getType().getFullName();

			pClass.addPField(roleName, new PField(roleName, roleType, className, true, true, false, 1));
		    }
		}
		domClass = (DomainClass) domClass.getSuperclass();
	    }
	    mng.addPClass(pClass);
	}
	SimpleContextManager.setStructureManager(mng);
    }

    private static void startWriteThread() {
	new DapThread().start();
    }

    static class DapThread extends Thread {
	private static final long SECONDS_BETWEEN_REPORTS = 1 * 60;

	DapThread() {
	    setDaemon(true);
	}

	@Override
	public void run() {
	    try {
		while (true) {
		    try {
			sleep(SECONDS_BETWEEN_REPORTS * 1000);
		    } catch (InterruptedException ie) {
			// ignore exception
		    }
		    SimpleContextManager.storeStats();
		    SimpleContextManager.accumulatePrevData();
		    SimpleContextManager.writeStatisticsToCSV(DAP_ROOT_DIR + "/dap-stats.csv");
		}
	    } catch (Throwable e) {
		System.err.println("Found a problem in the thread that writes the data-access patterns stats.  Terminating it.");
	    }
	}
    }

}
