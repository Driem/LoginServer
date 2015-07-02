import java.sql.*;
import java.net.*;
import java.io.*;

//Tutorial de como usar sqlite http://www.tutorialspoint.com/sqlite/sqlite_java.htm

public class ServerDB {
	private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DataInputStream streamIn =  null;
    public static void main( String args[] )
    {
    	String nameDB = "DataBase";
    	createDB(nameDB);
    	createTable(nameDB);
		ServerDB server = new ServerDB(nameDB, 4343);
	}
	public ServerDB(String nameDB, int port){
		System.out.println("ServerDB...");
		int id = 0;
		String name = "";
		String pass = "";
		try{
			System.out.println("Socket abierto");
			server = new ServerSocket(port);
			System.out.println("Esperando al cliente");
			socket = server.accept();
			open();
			int n = 0;
			boolean done = false;
	    	boolean login = false;
	    	boolean register = false;
	    	boolean session = false;
			while(!done){
				try{
					System.out.println("Esperando el mensaje de el cliente");
					String line = streamIn.readUTF();
					for (String retval: line.split("-")){
						if (login){
							if(n == 0){
								name = retval;
							}
							if(n == 1){
								pass = retval;
								n++;
							}
							if(n == 2){
								session = login(nameDB, name, pass);
								if(session){
									System.out.println("Inicio de sesion correcto :D");
								}else{
									System.out.println("Inicio de sesion incorrecto D:");
								}
							}
							n++;
					    }
						if(retval.equals("login")){
					    	   login = true;
					    	   n = 0;
					    	   }
						if (register){
							if(n == 0){
								id = Integer.parseInt(retval);
							}
							if(n == 1){
								name = retval;
								n++;
							}
							if(n == 2){
								pass = retval;
								n++;
							}
							if(n == 3){
								session = registerUser(nameDB, id, name, pass);
								if(session){
									System.out.println("Registro correcto :D");
								}else{
									System.out.println("Registro incorrecto D:");
								}
							}
							n++;
					    }
						if(retval.equals("register")){
					    	   register = true;
					    	   n = 0;
					    	   }
					}
					done = line.equals("bye");
				}catch(IOException ioe){
					done = true;
				}
				close();
			}
		}catch(IOException ioe){
			System.out.println(ioe);
		}
	}
	public void open() throws IOException{
		streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }
	public void close() throws IOException{
		if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
    }
	public static void viewAllUsers(String nameDB){
		System.out.println("viewAllUsers");
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:"+ nameDB +".db");
	      c.setAutoCommit(false);
	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM USER;" );
	      while ( rs.next() ) {
	         int id = rs.getInt("id");
	         String  name = rs.getString("name");
	         System.out.println("Id: " + id);
	         System.out.println("Nombre: " + name);
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	}
	public static boolean registerUser(String nameDB, int id,String nombre, String contra){
		System.out.println("registerUser...");
		System.out.println("Buscando usuario");
		if(login(nameDB, nombre, contra)){
			System.out.println("Usuario ya existe");
			return false;
		}else{
			System.out.println("Usuario no existe");
			Connection c = null;
		    Statement stmt = null;
		    try {
		      Class.forName("org.sqlite.JDBC");
		      c = DriverManager.getConnection("jdbc:sqlite:" + nameDB + ".db");
		      c.setAutoCommit(false);
		      System.out.println("database.db abierta");
		      
		      System.out.println("Registrando usuario");
		      stmt = c.createStatement();
		      String sql = "INSERT INTO USER (ID,NAME,PASS) " +
		                   "VALUES (" + id + ", '"+ nombre +"', '" + contra+ "');";
		      stmt.executeUpdate(sql);
		      
		      stmt.close();
		      c.commit();
		      c.close();
		    } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		    }
		    System.out.println("Registro con exito");
		    return true;
		}
	}
	public static boolean login(String nameDB, String nombre, String contra ){
		System.out.println("Login...");
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + nameDB + ".db");
	      c.setAutoCommit(false);
	      System.out.println("database.db abierta");

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM USER;" );
	      System.out.println("Buscando Usuario y contraseña");
	      while ( rs.next() ) {
	         int id = rs.getInt("id");
	         String  name = rs.getString("name");
	         String pass  = rs.getString("pass");
	         if (name.equals(nombre)){
	        	 if(pass.equals(contra)){
	        		 System.out.println("Login correcto");
	        		 return true;
	        	 }
	         }
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Login incorrecto");
		return false;
	}
	public static void createDB(String nameDB){
		System.out.println("createDB...");
		Connection c = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + nameDB + ".db");
	      System.out.println("Base de datos database.db creda");
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	}
	public static void createTable(String nameDB){
		System.out.println("createTable...");
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + nameDB + ".db");
	      System.out.println("database.db abierta");
	      
	      stmt = c.createStatement();
	      String sql = "CREATE TABLE USER " +
	                   "(ID INT PRIMARY KEY     NOT NULL," +
	                   " NAME           TEXT    NOT NULL, " +
	                   " PASS           TEXT    NOT NULL)";
	      stmt.executeUpdate(sql);
	      stmt.close();
	      c.close();
	      System.out.println("Tabla creada");
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	}
}