package dataserver.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author  mario
 */
public class ConnectionPooler {

    static private ConnectionPooler cpInstance = null;
    static private Logger log = Logger.getLogger("ConnectionPoolDbcp");    //il data source
    private BasicDataSource ds;
    static private HashMap params = null;

    static public synchronized ConnectionPooler getInstance() throws SQLException {
        if (null == cpInstance) {
            log.debug("Creating a connection pooler instance...");
            cpInstance = new ConnectionPooler();
        }
        //log.debug("returning [" + cpInstance + "]");
        return cpInstance;
    }

    public static void initConnectionPooler() {
        ResourceBundle dbb = null;
        try {
            dbb = ResourceBundle.getBundle("db");
        } catch(MissingResourceException e) {
            log.error(e,e);
            return;
        }
        
        Enumeration keys = dbb.getKeys();
        params = new HashMap();
        while (keys.hasMoreElements()) {
            String k = (String) keys.nextElement();
            String v = dbb.getString(k);
            params.put(k, v);
        }
    }

    private ConnectionPooler() throws SQLException {
        if (params == null) {
            throw new RuntimeException("Initialize connection pooler please!");
        }
        log.debug("Creating and setting data source...");
        ds = new BasicDataSource();
        ds.setDriverClassName((String) params.get("DRIVER"));
        ds.setUsername((String) params.get("USERNAME"));
        ds.setPassword((String) params.get("PASSWORD"));
        ds.setUrl((String) params.get("URL"));

        log.debug("Driver =  [" + ds.getDriverClassName() + "]");
        log.debug("Connecting to [" + ds.getUrl() + "]");
        log.debug("Username is [" + ds.getUsername() + "]");
        log.debug("Password is [" + ds.getPassword() + "]");

        ds.setInitialSize(new Integer((String) params.get("INITIALSIZE")).intValue());
        ds.setMaxActive(new Integer((String) params.get("NUMMAXACTIVECONNECTIONS")).intValue());
        ds.setDefaultAutoCommit(false);
        log.debug("Data source created!");
    }

    public Connection getConnection() throws Exception {
    //    try {
            Connection conn = ds.getConnection();
            return conn;
      /*  } catch (Exception e) {
            log.error(e, e);
            e.printStackTrace(System.out);
            throw e;
        }
       **/
    }

    public void shutDownDataSource() throws SQLException {
        log.debug("Shutting down data source...");
        ds.close();
        log.debug("Data source close!");
    }

    public int getNumActiveConnections() {
        return ds.getNumActive();
    }

    public int getNumIdleConnections() {
        return ds.getNumIdle();
    }
}
