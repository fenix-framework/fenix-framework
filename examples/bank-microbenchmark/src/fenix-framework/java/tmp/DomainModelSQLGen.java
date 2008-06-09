package tmp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.pstm.MetadataManager;

public class DomainModelSQLGen {

    public static void main(String[] args) {
	try {
            final String domainModel = args[0];
            String sqlfile = args[1];

	    Config config = new Config() {{
                domainModelPath = domainModel;
            }};

	    MetadataManager.init(config);
	    generate(sqlfile);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	System.out.println("Generation Complete.");
	
    }

    private static void generate(final String destinationFilename) throws IOException {
	final StringBuilder stringBuilder = new StringBuilder();

	final Map<String, SqlTable> sqlTables = DatabaseDescriptorFactory.getSqlTables();
	for (final SqlTable sqlTable : sqlTables.values()) {
	    sqlTable.appendCreateTableMySql(stringBuilder);
	    stringBuilder.append("\n\n");
	}

	writeFile(destinationFilename, stringBuilder.toString());
    }

    public static void writeFile(final String filename, final String fileContents) throws IOException {
	final File file = new File(filename);
	if (!file.exists()) {
	    file.createNewFile();
	}

	final FileWriter fileWriter = new FileWriter(file, false);

	fileWriter.write(fileContents);
	fileWriter.close();
    }

}
