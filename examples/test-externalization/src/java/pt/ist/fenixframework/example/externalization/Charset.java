package pt.ist.fenixframework.example.externalization;

import java.lang.reflect.*;
import java.util.TimeZone;
import java.util.Iterator;
import java.math.BigDecimal;
import java.sql.*;


public class Charset {


    public static void main(String [] args) throws Exception {
	Class.forName("com.mysql.jdbc.Driver");
	Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&clobCharacterEncoding=utf-8&characterSetResults=utf-8", "test", "test");
// 	Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "test", "test");
	Statement st = conn.createStatement();

	ResultSet results = st.executeQuery("SHOW VARIABLES WHERE Variable_name ='language' OR Variable_name = 'net_write_timeout' OR Variable_name = 'interactive_timeout' OR Variable_name = 'wait_timeout' OR Variable_name = 'character_set_client' OR Variable_name = 'character_set_connection' OR Variable_name = 'character_set' OR Variable_name = 'character_set_server' OR Variable_name = 'tx_isolation' OR Variable_name = 'transaction_isolation' OR Variable_name = 'character_set_results' OR Variable_name = 'timezone' OR Variable_name = 'time_zone' OR Variable_name = 'system_time_zone' OR Variable_name = 'lower_case_table_names' OR Variable_name = 'max_allowed_packet' OR Variable_name = 'net_buffer_length' OR Variable_name = 'sql_mode' OR Variable_name = 'query_cache_type' OR Variable_name = 'query_cache_size' OR Variable_name = 'init_connect'");
	while (results.next()) {
	    System.out.println("App: | " + results.getString(1) + " | " + results.getString(2) + " |");
	}
	st.close();
	conn.close();
    }
}
