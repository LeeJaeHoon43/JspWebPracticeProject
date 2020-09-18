package ch14;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;

public class DBConnectionMgr {
	
	private Vector connections = new Vector(10);
	private String driverName = "com.mysql.cj.jdbc.Driver";
	private String url = "jdbc:mysql://localhost:3306/jsp_practice?serverTimezone=Asia/Seoul"; 
	private String uid = "jsp"; 
	private String upw = "jsp"; 
	private boolean traceOn = false;
	private boolean initialized = false;
	private int openConnections = 10;
	private static DBConnectionMgr instance = null;
	
	public DBConnectionMgr() {}
	
	public static DBConnectionMgr getInstance() {
		if (instance == null) {
			synchronized (DBConnectionMgr.class) {
				if (instance == null) {
					instance = new DBConnectionMgr();
				}
			}
		}
		return instance;
	}
	
	public void setOpenConnectionCount(int count) {
		openConnections = count;
	}
	
	public void setEnableTrace(boolean enable) {
		traceOn = enable;
	}
	
	public Vector getConnectionList() {
		return connections;
	}
	
	public synchronized void setInitOpenConnections(int count)throws Exception{
		Connection conn = null;
		ConnectionObject co = null;
		 for (int i = 0; i < count; i++) {
	            conn = createConnection();
	            co = new ConnectionObject(conn, false);

	            connections.addElement(co);
	            trace("ConnectionPoolManager: Adding new DB connection to pool (" + connections.size() + ")");
	        }
	    }
	
	    public int getConnectionCount() {
	        return connections.size();
	    }

	    public synchronized Connection getConnection()
	            throws Exception {
	        if (!initialized) {
	            Class c = Class.forName(driverName);
	            DriverManager.registerDriver((Driver) c.newInstance());

	            initialized = true;
	        }


	        Connection conn = null;
	        ConnectionObject co = null;
	        boolean badConnection = false;


	        for (int i = 0; i < connections.size(); i++) {
	            co = (ConnectionObject) connections.elementAt(i);

	            if (!co.inUse) {
	                try {
	                    badConnection = co.connection.isClosed();
	                    if (!badConnection)
	                        badConnection = (co.connection.getWarnings() != null);
	                } catch (Exception e) {
	                    badConnection = true;
	                    e.printStackTrace();
	                }

	                if (badConnection) {
	                    connections.removeElementAt(i);
	                    trace("ConnectionPoolManager: Remove disconnected DB connection #" + i);
	                    continue;
	                }

	                conn = co.connection;
	                co.inUse = true;

	                trace("ConnectionPoolManager: Using existing DB connection #" + (i + 1));
	                break;
	            }
	        }

	        if (conn == null) {
	            conn = createConnection();
	            co = new ConnectionObject(conn, true);
	            connections.addElement(co);

	            trace("ConnectionPoolManager: Creating new DB connection #" + connections.size());
	        }

	        return conn;
	    }

	    public synchronized void freeConnection(Connection conn) {
	        if (conn == null)
	            return;

	        ConnectionObject co = null;

	        for (int i = 0; i < connections.size(); i++) {
	            co = (ConnectionObject) connections.elementAt(i);
	            if (conn == co.connection) {
	                co.inUse = false;
	                break;
	            }
	        }

	        for (int i = 0; i < connections.size(); i++) {
	            co = (ConnectionObject) connections.elementAt(i);
	            if ((i + 1) > openConnections && !co.inUse)
	                removeConnection(co.connection);
	        }
	    }

	    public void freeConnection(Connection conn, PreparedStatement pstmt, ResultSet rs) {
	        try {
	            if (rs != null) rs.close();
	            if (pstmt != null) pstmt.close();
	            freeConnection(conn);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    public void freeConnection(Connection conn, Statement stmt, ResultSet rs) {
	        try {
	            if (rs != null) rs.close();
	            if (stmt != null) stmt.close();
	            freeConnection(conn);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    public void freeConnection(Connection conn, PreparedStatement pstmt) {
	        try {
	            if (pstmt != null) pstmt.close();
	            freeConnection(conn);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    public void freeConnection(Connection conn, Statement stmt) {
	        try {
	            if (stmt != null) stmt.close();
	            freeConnection(conn);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    public synchronized void removeConnection(Connection conn) {
	        if (conn == null)
	            return;

	        ConnectionObject co = null;
	        for (int i = 0; i < connections.size(); i++) {
	            co = (ConnectionObject) connections.elementAt(i);
	            if (conn == co.connection) {
	                try {
	                    conn.close();
	                    connections.removeElementAt(i);
	                    trace("Removed : " + conn.toString());
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	                break;
	            }
	        }
	    }

	    private Connection createConnection()
	            throws SQLException {
	        Connection con = null;

	        try {
	            if (uid == null) {
	                uid = "";
	            }
	            if (upw == null) {
	                upw = "";
	            }
	            Properties props = new Properties();
	            props.put("user", uid);
	            props.put("password", upw);
	            con = DriverManager.getConnection(url, props);
	        } catch (Throwable t) {
	            throw new SQLException(t.getMessage());
	        }
	        return con;
	    }

	    public void releaseFreeConnections() {
	        trace("ConnectionPoolManager.releaseFreeConnections()");

	        Connection conn = null;
	        ConnectionObject co = null;

	        for (int i = 0; i < connections.size(); i++) {
	            co = (ConnectionObject) connections.elementAt(i);
	            if (!co.inUse)
	                removeConnection(co.connection);
	        }
	    }

	    public void finalize() {
	        trace("ConnectionPoolManager.finalize()");

	        Connection conn = null;
	        ConnectionObject co = null;

	        for (int i = 0; i < connections.size(); i++) {
	            co = (ConnectionObject) connections.elementAt(i);
	            try {
	                co.connection.close();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            co = null;
	        }
	        connections.removeAllElements();
	    }

	    private void trace(String s) {
	        if (traceOn)
	            System.err.println(s);
	    }
	}


	class ConnectionObject {
	    public java.sql.Connection connection = null;
	    public boolean inUse = false;

	    public ConnectionObject(Connection conn, boolean useFlag) {
	        connection = conn;
	        inUse = useFlag;
	    }
	}
