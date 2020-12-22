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
  private String oldMsg = "";
  private Thread read;
  private String serverName= "localhost";
  private int PORT = 12345;

  final JFrame frame = new JFrame("Chat");
  final JTextPane textPaneMessageBoard = new JTextPane(); // Message field
  final JTextPane textPaneUserList = new JTextPane(); // User list field
  final JTextField textFieldInputChat = new JTextField(); // Input message field
  final JButton buttonLogOut = new JButton("Log out");
  final JButton buttonSend = new JButton("Send");


  // Log in screen
  final JLabel labelUsername = new JLabel("Username:");
  final JLabel labelPassword = new JLabel("Password:");
  final JTextField textFieldUsername = new JTextField();
  final JPasswordField textFieldPassword = new JPasswordField();
  final JButton buttonLogIn = new JButton("Log In");
  final JButton buttonRegister = new JButton("Register");
  final JButton buttonDisconnect = new JButton("Disconnect");

  // Register screen
  // reuse label and text field from log in screen
  final JLabel labelConfirmPassword = new JLabel("Confirm Password:");
  final JPasswordField textFieldConfirmPassword = new JPasswordField();
  final JButton buttonRegisterRequest = new JButton("Register");
  final JButton buttonRegisterCancel = new JButton("Cancel");

  // Connection screen
  //final JTextField jtfName = new JTextField(this.name);
  final JLabel labelServerIP = new JLabel("Server IP:");
  final JLabel labelServerPort = new JLabel("Port:");
  final JTextField textFieldServerPort = new JTextField(Integer.toString(this.PORT));
  final JTextField textFieldServerIP = new JTextField(this.serverName);
  final JButton buttonConnect = new JButton("Connect");

  BufferedReader input;
  PrintWriter output;
  Socket server;

  public ClientGui() {
    String fontFamily = "Arial, sans-serif";
    Font font = new Font(fontFamily, Font.PLAIN, 15);


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
    textFieldInputChat.setBounds(25, 350, 400, 50);
    textFieldInputChat.setFont(font);
    textFieldInputChat.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane scrollPaneInputChat = new JScrollPane(textFieldInputChat);
    scrollPaneInputChat.setBounds(25, 350, 650, 50);

    // button send
    buttonSend.setFont(font);
    buttonSend.setBounds(465,350,100,40);

    // button log out

    buttonLogOut.setFont(font);
    buttonLogOut.setBounds(25,410,130,35);



    // Set log in components positions and fonts
    labelUsername.setBounds(25, 340,135,40);
    labelUsername.setFont(font);
    labelPassword.setBounds(170,340,135,40);
    labelPassword.setFont(font);
    textFieldUsername.setBounds(25, 370,135,40);
    textFieldPassword.setBounds(170,370,135,40);
    buttonLogIn.setBounds(465,370,100,40);
    buttonLogIn.setFont(font);
    buttonRegister.setBounds(575,370,100,40);
    buttonRegister.setFont(font);
    buttonDisconnect.setBounds(500, 415, 130, 40);
    buttonDisconnect.setFont(font);



    // Set register components positions and fonts
    labelConfirmPassword.setBounds(315,340,135,40);
    labelConfirmPassword.setFont(font);
    textFieldConfirmPassword.setBounds(315,370,135,40);
    buttonRegisterRequest.setBounds(465,370,100,40);
    buttonRegisterRequest.setFont(font);
    buttonRegisterCancel.setBounds(575, 370, 100, 40);
    buttonRegisterCancel.setFont(font);





    // check if those field are not empty
    //jtfName.getDocument().addDocumentListener(new TextListener(jtfName, textFieldServerPort, textFieldServerIP, buttonConnect));
    //textFieldServerPort.getDocument().addDocumentListener(new TextListener(jtfName, textFieldServerPort, textFieldServerIP, buttonConnect));
    //textFieldServerIP.getDocument().addDocumentListener(new TextListener(jtfName, textFieldServerPort, textFieldServerIP, buttonConnect));

    // Set connection components positions and fonts
    buttonConnect.setFont(font);
    labelServerIP.setBounds(25, 340,135,40);
    labelServerIP.setFont(font);
    labelServerPort.setBounds(170,340,135,40);
    labelServerPort.setFont(font);
    textFieldServerIP.setBounds(25, 370, 135, 40);
    //jtfName.setBounds(375, 370, 135, 40);
    textFieldServerPort.setBounds(170, 370, 135, 40);
    buttonConnect.setBounds(465, 370, 100, 40);


    // Add components
    frame.add(scrollPaneMessageBoard);
    frame.add(scrollPaneUserList);
    frame.add(labelServerIP);
    frame.add(labelServerPort);
    frame.add(textFieldServerPort);
    frame.add(textFieldServerIP);
    frame.add(buttonConnect);



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

    // On log in button
    buttonLogIn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String username = textFieldUsername.getText().trim();
        String password = new String(textFieldPassword.getPassword()).trim();
        if(username.equals("") || password.equals(""))
        {
          JOptionPane.showMessageDialog(frame, "Username and Password must not be empty");
        }
        else {
          output.println("LOG_IN|"+username+"|"+password);
        }

      }
    });

    // On log out
    buttonLogOut.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // add connect component
        frame.add(labelServerIP);
        frame.add(labelServerPort);
        frame.add(textFieldServerPort);
        frame.add(textFieldServerIP);
        frame.add(buttonConnect);

        // remove chatting component
        frame.remove(textFieldInputChat);
        frame.remove(buttonLogOut);
        frame.remove(buttonSend);

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

    // On log in -> register button
    buttonRegister.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // add register component
        frame.add(labelConfirmPassword);
        frame.add(textFieldConfirmPassword);
        frame.add(buttonRegisterRequest);
        frame.add(buttonRegisterCancel);

        // remove login component
        frame.remove(buttonLogIn);
        frame.remove(buttonRegister);
        frame.remove(buttonDisconnect);

        frame.revalidate();
        frame.repaint();
        textPaneUserList.setText(null);
        textPaneMessageBoard.setBackground(Color.LIGHT_GRAY);
        textPaneUserList.setBackground(Color.LIGHT_GRAY);
      }
    });

    // On register -> register button
    buttonRegisterRequest.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String username = textFieldUsername.getText().trim();
        String password = new String(textFieldPassword.getPassword()).trim();
        String confirmPassword = new String(textFieldConfirmPassword.getPassword()).trim();
        if(username.equals("") || password.equals(""))
        {
          JOptionPane.showMessageDialog(frame, "Username and Password must not be empty");
        }
        else if(!password.equals(confirmPassword)){
          JOptionPane.showMessageDialog(frame, "Password confirmation does not match");
        }
        else {
          output.println("REGISTER|"+username+"|"+password);
        }
      }
    });

    // On return to login from register
    buttonRegisterCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // add login component

        frame.add(buttonLogIn);
        frame.add(buttonRegister);
        frame.add(buttonDisconnect);

        // remove login component
        frame.remove(labelConfirmPassword);
        frame.remove(textFieldConfirmPassword);
        frame.remove(buttonRegisterRequest);
        frame.remove(buttonRegisterCancel);

        frame.revalidate();
        frame.repaint();
      }
    });

    // On connect
    buttonConnect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          //name = jtfName.getText();
          String port = textFieldServerPort.getText().trim();
          serverName = textFieldServerIP.getText().trim();
          if (!(serverName.equals("")||port.equals(""))) {
            PORT = Integer.parseInt(port);

            appendToPane(textPaneMessageBoard, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
            server = new Socket(serverName, PORT);

            appendToPane(textPaneMessageBoard, "<span>Connected to " +
                    server.getRemoteSocketAddress() + "</span>");

            // Get input and output stream to server
            input = new BufferedReader(new InputStreamReader(server.getInputStream()));
            output = new PrintWriter(server.getOutputStream(), true);

            // create new Read Thread
            read = new Read();
            read.start();

            // remove connect component
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
          }
          else{
            JOptionPane.showMessageDialog(frame,"Server IP and Port must not be empty");
          }

        } catch (Exception ex) {
          appendToPane(textPaneMessageBoard, "<span>Could not connect to server</span>");
          JOptionPane.showMessageDialog(frame, "Can not connect to server");
        }
      }

    });

    // on disconnect
    buttonDisconnect.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent ae) {

        // add connect component
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

        textPaneMessageBoard.setBackground(Color.LIGHT_GRAY);
        textPaneUserList.setBackground(Color.LIGHT_GRAY);

        // stop read thread
        read.interrupt();

        appendToPane(textPaneMessageBoard, "<span>Connection closed.</span>");
        output.close();
      }
    });

  }

  public static void main(String[] args) throws Exception {
    ClientGui client = new ClientGui();
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
            }
            // if Server sent a log in authorization
            else if(message.equals("LOG_IN_SUCCESS")){
              // render Chatting Screen
              frame.add(textFieldInputChat);
              frame.add(buttonSend);
              frame.add(buttonLogOut);

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

              textPaneMessageBoard.setBackground(Color.WHITE);
              textPaneUserList.setBackground(Color.WHITE);

            }
            // if Server sent a log in fail
            else if(message.equals("LOG_IN_FAIL")){
              JOptionPane.showMessageDialog(frame,"Wrong Username or Password");
            }
            // if Server sent a register authorization
            else if(message.equals("REGISTER_SUCCESS")){
              // render Chatting Screen
              frame.add(textFieldInputChat);
              frame.add(buttonSend);
              frame.add(buttonLogOut);

              // remove register component
              frame.remove(labelConfirmPassword);
              frame.remove(labelUsername);
              frame.remove(labelPassword);
              frame.remove(textFieldUsername);
              frame.remove(textFieldPassword);
              frame.remove(textFieldConfirmPassword);
              frame.remove(buttonRegisterRequest);
              frame.remove(buttonRegisterCancel);

              frame.revalidate();
              frame.repaint();

              textPaneMessageBoard.setBackground(Color.WHITE);
              textPaneUserList.setBackground(Color.WHITE);
            }
            // if Server sent a register fail
            else if(message.equals("REGISTER_FAIL")){
              JOptionPane.showMessageDialog(frame,"Username already taken");
            }
            else{
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
