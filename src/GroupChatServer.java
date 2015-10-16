import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class  GroupChatServer extends JFrame implements ActionListener{
	
  private static int PORT = 2000;
  private ServerSocket serverSocket;
  private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
  private ArrayList<String> users = new ArrayList<String>();

  private ImagePanel mainPanel;
  private JScrollPane chatboxPane;
  private JTextArea textArea = new JTextArea();
  private JButton exitButton = new JButton("Close Connection");
  private static final Font fontStyle = new Font("Arial", Font.PLAIN, 13);
 
  /**
   * It builds the server's graphical user interface. 
   */
  public GroupChatServer(){
	  setTitle("Server");
	  setSize(350,600);
	  
	  textArea.setRows(12);
	  textArea.setColumns(10);
	  textArea.setEditable(false);
	  textArea.setFont(fontStyle);
	  chatboxPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	  chatboxPane.setBounds(25, 110, 290, 360);
	  exitButton.setBounds(165, 490, 150, 30);
	  exitButton.addActionListener(this);
	  
	  mainPanel = new ImagePanel(new ImageIcon(this.getClass().getResource("images/bg2.jpg")).getImage());
	  mainPanel.add(chatboxPane);
	  mainPanel.add(exitButton);
      add(mainPanel);
      
      //close the connection properly when the user clicks the exit button
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	closeConnection();
		        }
		});
      
	  setResizable(false);
	  setLocationRelativeTo(null);
      setIconImage(new ImageIcon(this.getClass().getResource("images/icon.png")).getImage());
      setVisible(true);
  }
  
  /**
   * This method is called when the exitButton was clicked. It exits the program, closing the connection.  
   * 
   * @param e the actionEvent that the user has performed (since, the only possible source of the event
   * is the ExitButton, checking is unnecessary)
   * 
   */
  public void actionPerformed(ActionEvent e) {
	 closeConnection();
  }
    
  /**
   * This method instantiates the server socket which accepts connection from the client.
   * It instantiates a ClientHandler object, one per client, then, add it 
   * to the list of existing ClientHandler objects. It also displays a message to the 
   * server's GUI whenever a new client has connected.
   * 
   */
  public void startServer()  {
	  try{
	      serverSocket = new ServerSocket(PORT);
	      textArea.append("Server Started... \n");
	      textArea.append("Running port: " + PORT+"\n");
	     
	      while(true) {
	 		 Socket client = serverSocket.accept();
	 		 ClientHandler handler = new ClientHandler(client);
	 		 textArea.append("Made a connection with " + handler.getUserName()+". \n");
	  		 clients.add(handler);
	      }
      }
	  catch(Exception e){
		  System.out.println(e.getMessage());
	  }
  }
  
 /**
  * This method informs all the previously connected clients that a new user has also made a connection 
  * to the server by sending "[new user's name] has entered the chat room." to each of them.
  * 
  * @param username the name of the new user
  */
  public void announceNewUser(String username){
	  for (ClientHandler c : clients)
		  //This checking is done so that the server will not send the message to the new user.
		  if (!c.getUserName().equals(username))
			  c.output.println("[* " + username + " has entered the chatroom. *]");
  }	
  
  /**
   * This method welcomes the new client and sends him all the names of the users who are previously 
   * connected to the server for him to know who to chat with. 
   * It will send the message "There's no one to chat with" if the client is the only one 
   * who are currently connected to the server.
   * 
   * @param username the name of the new user
   * @param client the ClientHandler of the new user
   */
  public void announceCurrentUsers(String username, ClientHandler client){
	  String users = "";
	  //checks if the client has other users to chat with
	  if (clients.size() > 0){
		  //gets the name of all the users, except the new user
		  for (ClientHandler c: clients)
			  if (!c.getUserName().equals(username))	  
				  users+= " " +c.getUserName() + ",";  
		  
		  client.output.println("[* Welcome, "+ username + ". Chat with" + users + " *]");
	  }
	  else 
		  client.output.println("[* Welcome, "+ username + ". There's no one to chat with. *]");
  }
  
  /**
   *  This method broadcasts the message sent by a client to all the users.
   *  It replaces the user's name with "Me" if the message was to be broadcasted to the same client who
   *  has sent it.
   *  
   *  @param username the name of the user who sent the message
   *  @param message the message sent by the user
   * 
   */
  public void broadcastMessage(String username, String message){
	   for (ClientHandler c : clients)
	      if (!c.getUserName().equals(username))
	    	  c.output.println(username + ": " + message);
	       else
	    	   c.output.println("Me: " + message);
  }
    
  /**
   * This method sends the name of the user who disconnected to the server to all the remaining clients to 
   * inform them that the said user already left the chatroom. 
   * 
   * @param username the name of the client who has disconnected from the server
   */
  public void announceHasLeft(String username){  
	  for (ClientHandler c : clients)
		  c.output.println("[* " +username + " left. *]");
  }
  
  /**
   * This method closes the connection and sends "[* Bye! *] to all the clients 
   * connected, properly closing the connection.
   * 
   */
  public void closeConnection(){
	  for (ClientHandler c : clients)
		  c.output.println("[* Bye! *]");
	  try {
		serverSocket.close();
	  } catch (IOException e1) {
	  }
	  System.exit(0);
  }
  
  public static void main(String[] args) throws Exception{
      GroupChatServer chatServer = new GroupChatServer();
      chatServer.startServer();
  }
  
  /**
   *  This class handles the connection of a client, hence the name.
   * 
   */
  private class ClientHandler extends Thread {
	  
	  private Socket client;
	  private BufferedReader input;
	  private PrintWriter output;
	  private String name = "";  
	  
	  /**
	   *  The constructor gets the username entered by the clients and then adds it 
	   *  into the list of existing usernames. After this, the thread will be started.
	   * 
	   *  @param socket the socket of the client that has connected to the server
	   * 
	   */
	  public ClientHandler(Socket socket){
		  client = socket;
		  
	  	  try{
	  		  input = new BufferedReader(new InputStreamReader(client.getInputStream())) ;
	  		  output = new PrintWriter (client.getOutputStream(),true);
	  		  name  = input.readLine();
	  		  users.add(name);
	  		  start();
	  	  }
	  	  catch(IOException e) {
			e.printStackTrace();
	  	  }
      }
	  
	  /**
	   *  This method returns the username of the client.
	   *  @return name
	   *
	   */
      public String getUserName(){  
            return name; 
      }
      
      /**
       * It handles the whole group chat process. This also checks if the user inputs "Bye" to disconnect 
       * from the server. If so, it removes this client from the list of ClientHandler objects 
       * and its username from the list of usernames.
       *
       **/
      public void run(){
    	  announceNewUser(name);
  		  announceCurrentUsers(name, this);
  		  String line;
    	  
    	  try{
    		  while(true){
    			 line = input.readLine();
    			 if (line.equals("Bye")){
    				 clients.remove(this);
    				 users.remove(name);
    				 this.output.println("[* Bye! *]");
    				 announceHasLeft(name);
    				 textArea.append(name + " closed the connection. \n");
    				 break;
    			 }
    			 broadcastMessage(name,line); 
    		  } 
	     } 
	     catch(Exception e) {
	    	 System.out.println(e.getMessage());
	     }
     }    
  
  }
  
} 