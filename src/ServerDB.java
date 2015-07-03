import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

//Tutorial de como usar sqlite http://www.tutorialspoint.com/sqlite/sqlite_java.htm

public class ServerDB {
	static ArrayList<Socket> client_sockets;

    public static void main( String args[] ) throws Exception
    {

    		DBManager DB = new DBManager();

    	//
    		//DB.createDB();
    		//DB.createTable();
    		//DB.viewAllUsers();
    		//DB.registerUser("Jalil","12345");
    		DB.viewAllUsers();

    	    client_sockets = new ArrayList<Socket>();

           ServerSocket server_socket = new ServerSocket(4343);

           Responder responder = new Responder();
           while (true)
           {
               System.out.println("Esperando que se conecte un cliente.");
               System.out.flush();
               Socket client_socket = server_socket.accept();
               System.out.println("Se ha conectado un cliente.");
               System.out.flush();
               client_sockets.add(client_socket);
               Thread t = new Thread(new MyServer(responder, client_socket));
               t.start();

           }
       }
}


class DBManager{

	static String nameDB;
	DBManager(){
		 this.nameDB = "DataBase";
	}



	public static void viewAllUsers(){
		System.out.println("viewAllUsers");
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:"+ nameDB +".db");
	      c.setAutoCommit(false);
	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM TestUser;" );
	      while ( rs.next() ) {
	         int id = rs.getInt("id");
	         String  name = rs.getString("name");
	         String pass = rs.getString("pass");
	         System.out.println("Id: " + id);
	         System.out.println("Nombre: " + name);
	         System.out.println("Password: " + pass);
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	}
	public static boolean registerUser(String nombre, String contra){
		System.out.println("registerUser...");
		System.out.println("Buscando usuario");
		if(login(nombre, contra)){
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
		      String sql = "INSERT INTO TestUser (ID,NAME,PASS) " +
		                   "VALUES (" + null + ", '"+ nombre +"', '" + contra+ "');";
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
	public static boolean login(String nombre, String contra ){
		System.out.println("Login...");
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + nameDB + ".db");
	      c.setAutoCommit(false);
	      System.out.println("database.db abierta");

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM TestUser;" );
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
	public static void createDB(){
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
	public static void createTable(){
		System.out.println("createTable...");
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + nameDB + ".db");
	      System.out.println("database.db abierta");

	      stmt = c.createStatement();
	      String sql = "CREATE TABLE TestUser " +
	                   "(ID INTEGER PRIMARY KEY     autoincrement," +
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

class MyServer implements Runnable {

    Responder responder;
    Socket client_socket;

    public MyServer(Responder responder, Socket client_socket) {
        this.responder = responder;
        this.client_socket = client_socket;
    }

    @Override
    public void run()
    {
        BufferedReader inFromClient =
                null;
        try {
            inFromClient = new BufferedReader(
                    new InputStreamReader(
                            client_socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (responder.responderMethod(inFromClient)) {
            try {
                //System.out.println("Durmiendo 2s.");
                //System.out.flush();
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        try {
            client_socket.close();
        } catch (IOException ex) {
        }
    }

}

class Responder {
	DBManager BD = new DBManager();
    synchronized public boolean responderMethod(BufferedReader inFromClient) {
        try {

            if(!inFromClient.ready())
            {
                return true;
            }


            String clientSentence = inFromClient.readLine();

            if (clientSentence == null) {
                return false;
            }

            System.out.println("Recibido: " + clientSentence);
            //System.out.flush();

            Comprobar(clientSentence);


            for(int i=0; i<ServerDB.client_sockets.size();i++)
            {
                //System.out.println("Enviando a cliente #" + i);
                DataOutputStream data_output_stream =
                        new DataOutputStream(
                                ServerDB.client_sockets.get(i).getOutputStream());
                data_output_stream.writeBytes(clientSentence +"\n");
                data_output_stream.flush();
            }
            return true;

        } catch (SocketException e) {
            System.out.println("Disconnected");
            System.out.flush();System.out.flush();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void Comprobar(String clientSentence){
    	 int n = 0;
			boolean done = false;
	    	boolean login = false;
	    	boolean register = false;
	    	boolean session = false;
	    	int id = 0;
			String name = "";
			String pass = "";
			try{

         for (String retval: clientSentence.split("-")){
						if (login){
							if(n == 0){
								name = retval;
							}
							if(n == 1){
								pass = retval;
								n++;
							}
							if(n == 2){
								session = BD.login(name, pass);
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
								session = BD.registerUser(name, pass);
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
					done = clientSentence.equals("bye");
				}catch(Exception e){
					done = true;
				}

    }

}
