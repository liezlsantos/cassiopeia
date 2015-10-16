import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class GroupChatClient extends JFrame implements ActionListener{
	
	private static final Font fontStyle = new Font("Arial", Font.PLAIN, 12);
	private JLabel usernameL = new JLabel("Username:"), hostL = new JLabel("Host: ");
	private ImagePanel mainPanel;
	private JTextField IPAddressTextField = new JTextField();
	private JTextField usernameTextField = new JTextField();
	private JTextField messageTextField = new JTextField();
	private JTextArea chatbox = new JTextArea();
	private JScrollPane chatboxPane;
	private JButton start = new JButton("Ok"), exitButton = new JButton("Leave Conversation");
	
	private static final int PORT = 2000;
	String username = "", host= "";

	PrintWriter networkOutput;
	BufferedReader networkInput;
    Socket clientSocket;
    
    /**
     * It creates the GUI to ask for the hostname of the server and username
     * of the client.
     * 
     */
	public GroupChatClient(){	
		setTitle("Cassiopeia Messenger");
		setSize(350, 600);
		
		hostL.setBounds(70,190,200,30);
		IPAddressTextField.setBounds(70,220,200,30);
		usernameL.setBounds(70,250,200,30);
		usernameTextField.setBounds(70,280,200,30);
		start.setBounds(150, 320, 50, 30);
		start.addActionListener(this);
		mainPanel = new ImagePanel(new ImageIcon(this.getClass().getResource("images/bg.jpg")).getImage());
		mainPanel.add(start);
		mainPanel.add(usernameL);
		mainPanel.add(hostL);
		mainPanel.add(IPAddressTextField);
		mainPanel.add(usernameTextField);
		
		setLocationRelativeTo(null);
		add(mainPanel);
		setResizable(false);
		setIconImage(new ImageIcon(this.getClass().getResource("images/icon.png")).getImage());
		setVisible(true);
	}
	
	 /**
     * It creates the GUI for the client, socket for the connection to the server, 
     * stream for the messages to and from the server, and thread for the client.
     * 
     * @param username  name entered by the user
     * @param host the IP Address of the server entered by the user
     */
	public GroupChatClient(String username, String host, Point location){	
		setTitle("Cassiopeia Messenger");
		setSize(350, 600);
	
		chatbox.setRows(12);
		chatbox.setColumns(10);
		chatbox.setEditable(false);
		chatbox.setFont(fontStyle);
		chatboxPane = new JScrollPane(chatbox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		           JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		chatboxPane.setBounds(25, 110, 290, 330);
		
		//close the connection properly when the user clicks the exit button
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	networkOutput.println("Bye");  
		        System.exit(0);
		        }
		});
			
		//send the message in the text field to the server when the user pressed the enter key 
		messageTextField.addActionListener(new ActionListener() {
		       public void actionPerformed(ActionEvent e) {
		    	   networkOutput.println(messageTextField.getText());
		           messageTextField.setText("");
		           }
		       });
		messageTextField.setBounds(25,440,290,30);
		exitButton.setBounds(165, 490, 150, 30);
		exitButton.addActionListener(this);
		mainPanel = new ImagePanel(new ImageIcon(this.getClass().getResource("images/bg2.jpg")).getImage());
		mainPanel.add(chatboxPane);
		mainPanel.add(messageTextField);
		mainPanel.add(exitButton);
			
		try{
			clientSocket  = new Socket(host,PORT);
			networkInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())) ;
			networkOutput = new PrintWriter(clientSocket.getOutputStream(),true);
			networkOutput.println(username);
		}
		catch (UnknownHostException e1) {
			System.out.println("\nHost ID not found!\n");
			System.exit(1);
		} 
		catch (IOException e1) {
		}
			
		ClientThread clientT = new ClientThread();
		clientT.start();
			
		add(mainPanel);
		setResizable(false);
		setLocation(location);
		setIconImage(new ImageIcon(this.getClass().getResource("images/icon.png")).getImage());
		setVisible(true);
	}
	
	/**
	 * This method handles the action events in the GUI.
	 * 
	 * @param e the actionEvent that the user has performed
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == exitButton){
			//sends "Bye" to the server, indicating he wants to end the connection
			networkOutput.println("Bye");  
		}
		//if not the exitButton, then the Ok button was the source of the ActionEvent
		else{
			//checks if the user provides a username and the IP Address of the server
			if(usernameTextField.getText().isEmpty()|| IPAddressTextField.getText().isEmpty())
	
				JOptionPane.showMessageDialog(null, "Please don't leave any empty field/s.");				
			else
				startConnection();
		}
	}

	/**
	 *  This method starts the connection to the server by creating an instance of this class.
	 */
	public void startConnection(){
		username = usernameTextField.getText();
		host = IPAddressTextField.getText();
		new GroupChatClient(username, host, this.getLocation());
		setVisible(false);
	}

	/**
	 * Class that creates a thread for the client
	 */
	private class ClientThread extends Thread 
	{
		/**
		 * This method reads the message from the server and displays the message in the GUI
		 */
	    public void run() {
	        String message;
	        try {
	        	//if null it means the server has already closed the connection
	            while((message = networkInput.readLine())!= null) 
	                chatbox.append(message + "\n");
	       } 
	       catch(IOException ioe) {
	    	   setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	       }
	    }
	}
	
	public static void main(String args[]){
		new GroupChatClient();	
	}
	
}