import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import java.util.ArrayList;
import java.util.Arrays;


public class ClientGui extends Thread{

  final JTextPane textPaneMessageBoard = new JTextPane(); // Message field
  final JTextPane textPaneUserList = new JTextPane(); // User list field
  final JTextField textFieldInputChat = new JTextField(); // Input message field
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name;
  BufferedReader input;
  PrintWriter output;
  Socket server;

  public ClientGui() {
    this.serverName = "localhost";
    this.PORT = 12345;
    this.name = "nickname";

    String fontFamily = "Arial, sans-serif";
    Font font = new Font(fontFamily, Font.PLAIN, 15);

    final JFrame frame = new JFrame("Chat");
    frame.getContentPane().setLayout(null);
    frame.setSize(700, 500);
    frame.setResizable(false);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    // Chatting screen
    // Message field
    textPaneMessageBoard.setBounds(25, 25, 490, 320);
    textPaneMessageBoard.setFont(font);
    textPaneMessageBoard.setMargin(new Insets(6, 6, 6, 6));
    textPaneMessageBoard.setEditable(false);
    JScrollPane scrollPaneMessageBoard = new JScrollPane(textPaneMessageBoard);
    scrollPaneMessageBoard.setBounds(25, 25, 490, 320);

    textPaneMessageBoard.setContentType("text/html");
    textPaneMessageBoard.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Users list field
    textPaneUserList.setBounds(520, 25, 156, 320);
    textPaneUserList.setEditable(true);
    textPaneUserList.setFont(font);
    textPaneUserList.setMargin(new Insets(6, 6, 6, 6));
    textPaneUserList.setEditable(false);
    JScrollPane scrollPaneUserList = new JScrollPane(textPaneUserList);
    scrollPaneUserList.setBounds(520, 25, 156, 320);

    textPaneUserList.setContentType("text/html");
    textPaneUserList.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Set background color for text pane
    textPaneMessageBoard.setBackground(Color.LIGHT_GRAY);
    textPaneUserList.setBackground(Color.LIGHT_GRAY);

    // Input message Field
    textFieldInputChat.setBounds(0, 350, 400, 50);
    textFieldInputChat.setFont(font);
    textFieldInputChat.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane scrollPaneInputChat = new JScrollPane(textFieldInputChat);
    scrollPaneInputChat.setBounds(25, 350, 650, 50);

    // button send
    final JButton buttonSend = new JButton("Send");
    buttonSend.setFont(font);
    buttonSend.setBounds(575, 410, 100, 35);


    // Log in screen
    final JLabel labelUsername = new JLabel("Username:");
    final JLabel labelPassword = new JLabel("Password:");
    final JTextField textFieldUsername = new JTextField();
    final JPasswordField textFieldPassword = new JPasswordField();
    final JButton buttonLogIn = new JButton("Log In");
    final JButton buttonRegister = new JButton("Register");
    final JButton buttonDisconnect = new JButton("Disconnect");

    // Set log in components position
    labelUsername.setBounds(25, 340,135,40);
    labelUsername.setFont(font);
    labelPassword.setBounds(200,340,135,40);
    labelPassword.setFont(font);
    textFieldUsername.setBounds(25, 370,135,40);
    textFieldPassword.setBounds(200,370,135,40);
    buttonLogIn.setBounds(450,370,100,40);
    buttonLogIn.setFont(font);
    buttonRegister.setBounds(575,370,100,40);
    buttonRegister.setFont(font);
    buttonDisconnect.setBounds(500, 415, 130, 40);
    buttonDisconnect.setFont(font);


    // Connection screen
    //final JTextField jtfName = new JTextField(this.name);
    final JLabel labelServerIP = new JLabel("Server IP:");
    final JLabel labelServerPort = new JLabel("Port:");
    final JTextField textFieldServerPort = new JTextField(Integer.toString(this.PORT));
    final JTextField textFieldServerIP = new JTextField(this.serverName);
    final JButton buttonConnect = new JButton("Connect");

    // check if those field are not empty
    //jtfName.getDocument().addDocumentListener(new TextListener(jtfName, textFieldServerPort, textFieldServerIP, buttonConnect));
    //textFieldServerPort.getDocument().addDocumentListener(new TextListener(jtfName, textFieldServerPort, textFieldServerIP, buttonConnect));
    //textFieldServerIP.getDocument().addDocumentListener(new TextListener(jtfName, textFieldServerPort, textFieldServerIP, buttonConnect));

    // Set connection components position
    buttonConnect.setFont(font);
    labelServerIP.setBounds(25, 340,135,40);
    labelServerIP.setFont(font);
    labelServerPort.setBounds(200,340,135,40);
    labelServerPort.setFont(font);
    textFieldServerIP.setBounds(25, 370, 135, 40);
    //jtfName.setBounds(375, 370, 135, 40);
    textFieldServerPort.setBounds(200, 370, 135, 40);
    buttonConnect.setBounds(450, 370, 100, 40);



    // Add components
    frame.add(scrollPaneMessageBoard);
    frame.add(scrollPaneUserList);
//    frame.add(jtfName);
    frame.add(labelServerIP);
    frame.add(labelServerPort);
    frame.add(textFieldServerPort);
    frame.add(textFieldServerIP);
    frame.add(buttonConnect);
//    frame.add(labelUsername);
//    frame.add(labelPassword);
//    frame.add(textFieldUsername);
//    frame.add(textFieldPassword);
//    frame.add(buttonLogIn);
//    frame.add(buttonRegister);
//    frame.add(buttonDisconnect);


    frame.setVisible(true);


    // Chat instruction
    appendToPane(textPaneMessageBoard, "<h4>Possible commands:</h4>"
        +"<ul>"
        +"<li><b>@nickname</b> - Send private message to user 'nickname'</li>"
        +"<li><b>#d3961b</b> - Change your username color </li>"
        +"<li><b>;)</b> - Smiley emoji </li>"
        +"<li><b>Up arrow</b> - Get last typed message</li>"
        +"</ul><br/>");

    // On input chat key pressed
    textFieldInputChat.addKeyListener(new KeyAdapter() {
      // send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = textFieldInputChat.getText().trim();
          textFieldInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = textFieldInputChat.getText().trim();
          textFieldInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });

    // On send button pressed
    buttonSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    // On log in
    buttonLogIn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {


      }
    });

    // On register
    buttonRegister.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

      }
    });

    // On connect
    buttonConnect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          //name = jtfName.getText();
          String port = textFieldServerPort.getText();
          serverName = textFieldServerIP.getText();
          PORT = Integer.parseInt(port);

          appendToPane(textPaneMessageBoard, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
          server = new Socket(serverName, PORT);

          appendToPane(textPaneMessageBoard, "<span>Connected to " +
              server.getRemoteSocketAddress()+"</span>");

          // Get input and output stream to server
          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
          output = new PrintWriter(server.getOutputStream(), true);

          // create new Read Thread
          read = new Read();
          read.start();

          // remove connect component
          //frame.remove(jtfName);
          frame.remove(labelServerIP);
          frame.remove(labelServerPort);
          frame.remove(textFieldServerPort);
          frame.remove(textFieldServerIP);
          frame.remove(buttonConnect);

          // add login component
          frame.add(labelUsername);
          frame.add(labelPassword);
          frame.add(textFieldUsername);
          frame.add(textFieldPassword);
          frame.add(buttonLogIn);
          frame.add(buttonRegister);
          frame.add(buttonDisconnect);

          frame.revalidate();
          frame.repaint();
          textPaneMessageBoard.setBackground(Color.WHITE);
          textPaneUserList.setBackground(Color.WHITE);
        } catch (Exception ex) {
          appendToPane(textPaneMessageBoard, "<span>Could not connect to server</span>");
          JOptionPane.showMessageDialog(frame, "Connection refused");
        }
      }

    });

    // on disconnect
    buttonDisconnect.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent ae) {

        // add connect component
        //frame.add(jtfName);
        frame.add(labelServerIP);
        frame.add(labelServerPort);
        frame.add(textFieldServerPort);
        frame.add(textFieldServerIP);
        frame.add(buttonConnect);

        // remove login component
        frame.remove(labelUsername);
        frame.remove(labelPassword);
        frame.remove(textFieldUsername);
        frame.remove(textFieldPassword);
        frame.remove(buttonLogIn);
        frame.remove(buttonRegister);
        frame.remove(buttonDisconnect);

        frame.revalidate();
        frame.repaint();
        textPaneUserList.setText(null);
        textPaneMessageBoard.setBackground(Color.LIGHT_GRAY);
        textPaneUserList.setBackground(Color.LIGHT_GRAY);

        // stop read thread
        read.interrupt();

        appendToPane(textPaneMessageBoard, "<span>Connection closed.</span>");
        output.close();
      }
    });

  }

  // check if if all field are not empty
  public class TextListener implements DocumentListener{
    JTextField jtf1;
    JTextField jtf2;
    JTextField jtf3;
    JButton jcbtn;

    public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn){
      this.jtf1 = jtf1;
      this.jtf2 = jtf2;
      this.jtf3 = jtf3;
      this.jcbtn = jcbtn;
    }

    public void changedUpdate(DocumentEvent e) {}

    public void removeUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }
    public void insertUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }

  }

  // Send messages
  public void sendMessage() {
    try {
      String message = textFieldInputChat.getText().trim();
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
      output.println(message);
      textFieldInputChat.requestFocus();
      textFieldInputChat.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    ClientGui client = new ClientGui();
  }

  // read new incoming messages
  class Read extends Thread {
    public void run() {
      String message;
      while(!Thread.currentThread().isInterrupted()){
        try {
          message = input.readLine();
          if(message != null){
            // if Server sent a list of user
            if (message.charAt(0) == '[') {
              message = message.substring(1, message.length()-1);
              ArrayList<String> ListUser = new ArrayList<String>(
                  Arrays.asList(message.split(", "))
                  );
              textPaneUserList.setText(null);
              for (String user : ListUser) {
                appendToPane(textPaneUserList, "@" + user);
              }
            }else{
              appendToPane(textPaneMessageBoard, message);
            }
          }
        }
        catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }

  // send html to pane
  private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch(Exception e){
      e.printStackTrace();
    }
  }
}
