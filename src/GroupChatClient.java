import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class GroupChatClient extends JFrame implements ActionListener{

    private JLabel usernameL, hostL;
    private ImagePanel mainPanel;
    private JTextField iPAddressTextField;
    private JTextField usernameTextField;
    private JTextField messageTextField;
    private JTextArea chatbox;
    private JScrollPane chatboxPane;
    private JButton start, exitButton;

    private static final int PORT = 2000;
    String username = "", host = "";

    PrintWriter networkOutput;
    BufferedReader networkInput;
    Socket clientSocket;

    /**
     * It creates the GUI to ask for the hostname of the server and username
     * of the client.
     */
    public GroupChatClient() {
        setTitle("Cassiopeia Messenger");
        setSize(350, 600);

        hostL = new JLabel("Host: ");
        hostL.setBounds(70, 190, 200, 30);

        iPAddressTextField = new JTextField();
        iPAddressTextField.setBounds(70, 220, 200, 30);

        usernameL = new JLabel("Username:");
        usernameL.setBounds(70, 250, 200, 30);

        usernameTextField = new JTextField();
        usernameTextField.setBounds(70, 280, 200, 30);

        start = new JButton("Ok");
        start.setBounds(150, 320, 50, 30);
        start.addActionListener(this);

        mainPanel = new ImagePanel(new ImageIcon(this.getClass().getResource("images/bg.jpg")).getImage());
        mainPanel.add(hostL);
        mainPanel.add(iPAddressTextField);
        mainPanel.add(usernameL);
        mainPanel.add(usernameTextField);
        mainPanel.add(start);

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
    public GroupChatClient(String username, String host, Point location) {
        setTitle("Cassiopeia Messenger");
        setSize(350, 600);

        chatbox = new JTextArea();
        chatbox.setRows(12);
        chatbox.setColumns(10);
        chatbox.setEditable(false);
        chatbox.setFont(new Font("Arial", Font.PLAIN, 12));
        chatboxPane = new JScrollPane(
        	chatbox,
        	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        chatboxPane.setBounds(25, 110, 290, 330);

        // closes the connection properly when the user clicks the exit button
        this.addWindowListener(new java.awt.event.WindowAdapter() {
	        @Override
	        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
	            networkOutput.println("Bye");
	            System.exit(0);
	        }
        });

        messageTextField = new JTextField();
        // sends the message in the text field to the server when the user pressed the enter key
        messageTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                networkOutput.println(messageTextField.getText());
                messageTextField.setText("");
            }
         });
        messageTextField.setBounds(25,440,290,30);

        exitButton = new JButton("Leave Conversation");
        exitButton.setBounds(165, 490, 150, 30);
        exitButton.addActionListener(this);

        mainPanel = new ImagePanel(new ImageIcon(this.getClass().getResource("images/bg2.jpg")).getImage());
        mainPanel.add(chatboxPane);
        mainPanel.add(messageTextField);
        mainPanel.add(exitButton);

        try {
            clientSocket  = new Socket(host, PORT);
            networkInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())) ;
            networkOutput = new PrintWriter(clientSocket.getOutputStream(), true);
            networkOutput.println(username);
        } catch (UnknownHostException e) {
            System.out.println("\nHost ID not found!\n");
            System.exit(1);
        } catch (IOException e) {
        	e.printStackTrace();
            System.exit(1);
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
            //sends "Bye" to the server, indicating the client wants to end the connection
            networkOutput.println("Bye");
        } else {
            /*
             * If not the exitButton, then the Ok button was the source of the ActionEvent.
             * Checks if the user provides a username and the IP Address of the server
             */
            if (usernameTextField.getText().isEmpty() || iPAddressTextField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please don't leave any empty field/s.");
            } else {
                startConnection();
            }
        }
    }

    /**
     *  This method starts the connection to the server by creating an instance of this class.
     */
    public void startConnection() {
        username = usernameTextField.getText();
        host = iPAddressTextField.getText();
        new GroupChatClient(username, host, this.getLocation());
        setVisible(false);
    }

    /**
     * Class that creates a thread for the client
     */
    private class ClientThread extends Thread {
        /**
         * This method reads the message from the server and displays the message in the GUI
         */
        public void run() {
            String message;
            try {
                //if null it means the server has already closed the connection
                while((message = networkInput.readLine()) != null)
                    chatbox.append(message + "\n");
           	} catch (IOException e) {
           		e.printStackTrace();
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
           	}
        }
    }

    public static void main(String args[]) {
        new GroupChatClient();
    }

}