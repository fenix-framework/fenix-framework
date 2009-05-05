package pt.ist.fenixframework.example.oo7;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jvstm.TransactionalCommand;
import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.example.oo7.domain.AtomicPart;
import pt.ist.fenixframework.example.oo7.domain.BaseAssembly;
import pt.ist.fenixframework.example.oo7.domain.CompositePart;
import pt.ist.fenixframework.example.oo7.domain.Document;
import pt.ist.fenixframework.example.oo7.domain.Manual;
import pt.ist.fenixframework.example.oo7.domain.Module;
import pt.ist.fenixframework.example.oo7.domain.OO7Application;
import pt.ist.fenixframework.example.oo7.extra.OO7Benchmark;
import pt.ist.fenixframework.example.oo7.extra.OO7Database;
import pt.ist.fenixframework.pstm.Transaction;

public class OO7FFBenchmark extends OO7Benchmark {

	//private SessionFactory sf = null;


	private static Random rand = new Random();

	public static void main(String args[]) {

		/*if (args.length < 2) {
            System.out.println("This program takes 2 arguments:\n"
                    + "1. The scale of the database "
                    + "(0:Tiny, 1:Small, 2:Medium, 3:Large).\n"
                    + "2. The number of  iterations.\n");
            System.exit(-1);
        }
		 */
		Config config = new Config() {{
			domainModelPath = "/oo7.dml";
			dbAlias = "//localhost:3306/ssilva";
			dbUsername = "ssilva";
			dbPassword = "ssilva";
                        rootClass = OO7Application.class;
		}};
		FenixFramework.initialize(config);

		int scale = 1;//Integer.parseInt(args[0]);
		int iterations = 2;//Integer.parseInt(args[1]);
		OO7FFBenchmark b = new OO7FFBenchmark(scale);
		for (int i = 0; i < iterations; i++) {
			b.run();
		}



	}

	public void run() {
		runQueries();
		runTraversals();

	}

	public OO7FFBenchmark(int scale) {
		super(scale);
	}

	public void query1(final Long[] ids) {

		new FFAction() {

			@Override
			protected Object performAction() {
				List<AtomicPart> parts = OO7Application.getInstance().getAtomicParts();
				int count=0;
				for (int i = 0; i < ids.length; i++)
					for(AtomicPart a : parts) {
						if(a!= null && a.getDocId().equals(ids[i]))
							count++;
					}
				return count;
			}

		}.execute();

	}

	public Long[] chooseAtomicParts(final int numParts) {

		Long ids[] = new Long[numParts];
		for (int i = 0; i < numParts; i++) {
			ids[i] = Long.valueOf(rand.nextInt(100));
		}
		return ids;
	}

	public void query2() {
		new FFAction() {

			@Override
			protected Object performAction() {
				long dateThreshold = getRange(OO7Database.MIN_ATOMIC_DATE,
						OO7Database.MAX_ATOMIC_DATE, 0.99);
				List<AtomicPart> parts = OO7Application.getInstance().getAtomicParts();
				List<AtomicPart> ret = new ArrayList<AtomicPart>();
				for(AtomicPart a : parts) {
					if (a.getBuildDate() > dateThreshold)
						ret.add(a);
				}
				System.out.println("Query2: " + ret.size());
				return ret;
			}

		}.execute();

	}

	public void query3() {
		new FFAction() {

			@Override
			protected Object performAction() {
				long dateThreshold = getRange(OO7Database.MIN_ATOMIC_DATE,
						OO7Database.MAX_ATOMIC_DATE, 0.99);
				List<AtomicPart> parts = OO7Application.getInstance().getAtomicParts();
				List<AtomicPart> ret = new ArrayList<AtomicPart>();
				for(AtomicPart a : parts) {
					if (a.getBuildDate() > dateThreshold)
						ret.add(a);
				}
				System.out.println("Query3: " + ret.size());
				return ret;
			}
		}.execute();

	}

	public void query7() {
		new FFAction() {

			@Override
			protected Object performAction() {
				List<AtomicPart> parts = OO7Application.getInstance().getAtomicParts();
				for(AtomicPart a : parts) {
					a.getDocId();
				}
				return parts;
			}

		}.execute();
	}

	public Long[] getRandomDocIds(final int count) {
		return (Long[]) new FFAction() {

			@Override
			protected Object performAction() {
				ArrayList<Long> docIds = new ArrayList<Long>();
				List<Document> documentIds = OO7Application.getInstance().getDocuments();
				if (documentIds.size() < count) {
					throw new IllegalStateException(
							"Too few documents for query 4.");
				}
				for (int i = 0; i < count; i++) {
					Document doc = documentIds.get(rand.nextInt(documentIds.size()));
					Long id = (Long) doc.getId();
					if (docIds.contains(id)) {
						i--;
					} else {
						docIds.add(id);
					}
				}
				return docIds.toArray(new Long[] {});
			}

		}.execute();

	}

	public void query4(final Long[] docIds) {
		new FFAction() {

			@Override
			protected Object performAction() {
				int count=0;
				List<BaseAssembly> bas = OO7Application.getInstance().getBaseAssemblies();
				for (int i = 0; i < docIds.length; i++) {
					for(BaseAssembly ba : bas)
						for(CompositePart pa : ba.getUnsharedPart())
							if (pa.getDocument().getId().equals(docIds[i]))
								count++;
				}
				System.out.println("Query 4: " + count);
				return count;
			}

		}.execute();


	}

	public void query5() {
		new FFAction() {

			@Override
			protected Object performAction() {
				int numBaseAssemblies = 0;
				List<BaseAssembly> bas = OO7Application.getInstance().getBaseAssemblies();
				for(BaseAssembly ba : bas) {
					List<CompositePart> cps = ba.getUnsharedPart();
					for(CompositePart cp : cps) {
						if(ba.getBuildDate() < cp.getBuildDate())
							numBaseAssemblies++;
					}
				}
				System.out.println("Query 5: " + numBaseAssemblies);
				return numBaseAssemblies;
			}

		}.execute();


	}

	public void query8() {
		new FFAction() {

			@Override
			protected Object performAction() {
				List<Document> ds = OO7Application.getInstance().getDocuments();
				List<AtomicPart> aps = OO7Application.getInstance().getAtomicParts();
				int numPairs = 0;
				for(Document d : ds) {
					for(AtomicPart ap : aps) {
						if(ap.getDocId() == d.getId())
							numPairs++;
					}
				}
				System.out.println("Query 8: " + numPairs);
				return numPairs;
			}
		}.execute();


	}



	private long getRange(long min, long max, double percentage) {
		return (long) ((max - min) * percentage) + min;
	}



	@Override
	public List<Manual> getManuals() {
		return OO7Application.getInstance().getManuals();
	}

	@Override
	public List<Module> getModules() {

		return OO7Application.getInstance().getModules();
	}

	public abstract class FFAction {
		protected abstract Object performAction();
		Object ret = null;
		public Object execute() {
			Transaction.withTransaction(true,new TransactionalCommand() {

				public void doIt() {
					ret = performAction();
				}

			});
			return ret;
		}
	}

	@Override
	public void endTransaction() {
		Transaction.commit();
	}

	@Override
	public void startTransaction() {
		Transaction.begin();
	}

}
