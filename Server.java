import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class Server {

  private int port;
  private List<User> clients;
  private ServerSocket server;
  final private String userDataFileDirectory = "user.dat";
  public static JTextPane messageBoard;


  public static void main(String[] args) throws IOException {
    new Server(12345).run();
  }

  public Server(int port) {
    this.port = port;
    this.clients = new ArrayList<User>();
  }

  public void run() throws IOException {
    String fontFamily = "Arial, sans-serif";
    Font font = new Font(fontFamily, Font.PLAIN, 15);

    JFrame frame = new JFrame("Chat Server");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(700,500);
    frame.setResizable(false);

    JLabel title = new JLabel("Server logger");
    JPanel titlePanel = new JPanel();
    titlePanel.add(title,BorderLayout.CENTER);

    messageBoard = new JTextPane();
    messageBoard.setFont(font);
    messageBoard.setMargin(new Insets(6, 6, 6, 6));
    messageBoard.setEditable(false);
    JScrollPane messageBoardSP = new JScrollPane(messageBoard);

    frame.add(titlePanel,BorderLayout.PAGE_START);
    frame.add(messageBoardSP,BorderLayout.CENTER);

    frame.setVisible(true);

    server = new ServerSocket(port) {
      protected void finalize() throws IOException {
        this.close();
      }
    };
    appendToPane(messageBoard,"Port 12345 is now open.\n");

    while (true) {
      // accepts a new client
      Socket client = server.accept();
      appendToPane(messageBoard, "New client at: " + client.getRemoteSocketAddress() + "\n");
      while (true){
        // Get log in or register request
        String request = (new Scanner(client.getInputStream())).nextLine();
        ArrayList<String> listRequestDetail = new ArrayList<>(Arrays.asList(request.split("\\|")));
        PrintWriter output = new PrintWriter(client.getOutputStream(), true);
        if(listRequestDetail.get(0).equals("LOG_IN")) {
          // Log in success
          if(validateLogIn(listRequestDetail.get(1),listRequestDetail.get(2))){
            // create new User
            User newUser = new User(client,listRequestDetail.get(1));
            // Log log in event
            appendToPane(messageBoard, newUser.getNickname()+" has logged in\n");
            // add newUser message to list
            this.clients.add(newUser);

            // Send log in authorization
            output.println("LOG_IN_SUCCESS");

            // Welcome msg
            newUser.getOutStream().println("<b>Welcome</b> " + newUser.toString());

            // create a new thread for newUser incoming messages handling
            new Thread(new UserHandler(this, newUser)).start();
            break;
          }
          // Log in fail
          else{
            output.println("LOG_IN_FAIL");
            continue;
          }
        }
        else if (listRequestDetail.get(0).equals("REGISTER")) {
          if (validateRegister(listRequestDetail.get(1), listRequestDetail.get(2))) {
            // create new User
            User newUser = new User(client, listRequestDetail.get(1));
            // log register event
            appendToPane(messageBoard, newUser.getNickname()+" has registered\n");

            // add newUser message to list
            this.clients.add(newUser);

            // Send log in authorization
            output.println("REGISTER_SUCCESS");

            // Welcome msg
            newUser.getOutStream().println("<b>Welcome</b> " + newUser.toString());

            // create a new thread for newUser incoming messages handling
            new Thread(new UserHandler(this, newUser)).start();
            break;
          } else {
            output.println("REGISTER_FAIL");
          }
        }
      }
    }
  }

  // delete a user from the list
  public void removeUser(User user){
    Server.appendToPane(Server.messageBoard, user.getNickname()+" has log out\n");
    this.clients.remove(user);
  }

  // send incoming msg to all Users
  public void broadcastMessages(String msg, User userSender) {
    for (User client : this.clients) {
      client.getOutStream().println(
          userSender.toString() + "<span>: " + msg+"</span>");
    }
  }

  // send list of clients to all Users
  public void broadcastAllUsers(){
    for (User client : this.clients) {
      client.getOutStream().println(this.clients);
    }
  }

  // send message to a User (String)
  public void sendMessageToUser(String msg, User userSender, String user){
    boolean find = false;
    for (User client : this.clients) {
      if (client.getNickname().equals(user) && client != userSender) {
        find = true;
        userSender.getOutStream().println(userSender.toString() + " -> " + client.toString() +": " + msg);
        client.getOutStream().println(
            "(<b>Private</b>)" + userSender.toString() + "<span>: " + msg+"</span>");
      }
    }
    if (!find) {
      userSender.getOutStream().println(userSender.toString() + " -> (<b>no one!</b>): " + msg);
    }
  }

  // send html to pane
  public static void appendToPane(JTextPane tp, String msg){
    Document doc = tp.getStyledDocument();
    AttributeSet attributeSet = tp.getCharacterAttributes();
    try {
      doc.insertString(doc.getLength(), msg, attributeSet);
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  // Validate log in username and password
  public boolean validateLogIn(String username, String password){
    try{
      BufferedReader reader = new BufferedReader(new FileReader(userDataFileDirectory));
      String line;
      while((line=reader.readLine())!=null){
        String[] combo = line.split(" ");
        if(combo[0].equals(username)  && combo[1].equals(password))
          return true;
      }
      reader.close();
    }
    catch(IOException e){
      e.printStackTrace();
      return false;
    }
    return false;
  }

  // Validate register username and password
  public boolean validateRegister(String username, String password){
    try
    {
      if(checkUserNameAvailability(username))
      {
        BufferedWriter writer = new BufferedWriter(new FileWriter(userDataFileDirectory,true));
        String line = username+" "+password+"\n";
        writer.write(line);
        writer.close();
        return true;

      }
    }catch (IOException e)
    {
      e.printStackTrace();
    }
    return false;
  }

  // Check if user name is available to register
  public boolean checkUserNameAvailability(String username) throws IOException {
    File file = new File(userDataFileDirectory);
    if(!file.exists()) {
      file.createNewFile();
    }
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    while((line=reader.readLine())!=null){
      String[] combo = line.split(" ");
      if(combo[0].equals(username))
        return false;
    }
    reader.close();
    return true;
  }

  public List<User> getClients(){
    return clients;
  }

}

class UserHandler implements Runnable {

  private Server server;
  private User user;

  public UserHandler(Server server, User user) {
    this.server = server;
    this.user = user;
    this.server.broadcastAllUsers();
  }

  public void run() {
    String message;

    // when there is a new message, broadcast to all
    Scanner sc = new Scanner(this.user.getInputStream());
    while (sc.hasNextLine()) {
      message = sc.nextLine();

      // smiley
      message = message.replace(":)", "<img src='http://4.bp.blogspot.com/-ZgtYQpXq0Yo/UZEDl_PJLhI/AAAAAAAADnk/2pgkDG-nlGs/s1600/facebook-smiley-face-for-comments.png'>");
      message = message.replace(":D", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
      message = message.replace(":d", "<img src='http://2.bp.blogspot.com/-OsnLCK0vg6Y/UZD8pZha0NI/AAAAAAAADnY/sViYKsYof-w/s1600/big-smile-emoticon-for-facebook.png'>");
      message = message.replace(":(", "<img src='http://2.bp.blogspot.com/-rnfZUujszZI/UZEFYJ269-I/AAAAAAAADnw/BbB-v_QWo1w/s1600/facebook-frown-emoticon.png'>");
      message = message.replace("-_-", "<img src='http://3.bp.blogspot.com/-wn2wPLAukW8/U1vy7Ol5aEI/AAAAAAAAGq0/f7C6-otIDY0/s1600/squinting-emoticon.png'>");
      message = message.replace(";)", "<img src='http://1.bp.blogspot.com/-lX5leyrnSb4/Tv5TjIVEKfI/AAAAAAAAAi0/GR6QxObL5kM/s400/wink%2Bemoticon.png'>");
      message = message.replace(":P", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
      message = message.replace(":p", "<img src='http://4.bp.blogspot.com/-bTF2qiAqvi0/UZCuIO7xbOI/AAAAAAAADnI/GVx0hhhmM40/s1600/facebook-tongue-out-emoticon.png'>");
      message = message.replace(":o", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");
      message = message.replace(":O", "<img src='http://1.bp.blogspot.com/-MB8OSM9zcmM/TvitChHcRRI/AAAAAAAAAiE/kdA6RbnbzFU/s400/surprised%2Bemoticon.png'>");

      // Forward message chat
      if (message.charAt(0) == '@'){
        if(message.contains(" ")){
          Server.appendToPane(Server.messageBoard,"private msg : " + message+"\n");
          int firstSpace = message.indexOf(" ");
          String receiverUsername= message.substring(1, firstSpace); // Get receiver username from message
          server.sendMessageToUser(
              message.substring(
                firstSpace+1, message.length()
                ), user, receiverUsername
              );
        }

      }// Forward color change
      else if (message.charAt(0) == '#'){
        user.changeColor(message);
        // notify color change for all other users
        this.server.broadcastAllUsers();
      }
      else if(message.equals("LOG_OUT")){
        server.removeUser(user);
        server.broadcastAllUsers();
      }
      else{
        // update user list (new user enter chat room)
        server.broadcastMessages(message, user);
      }
    }
    // end of Thread
    server.removeUser(user); // user left chat room
    this.server.broadcastAllUsers(); // update user list
    Server.appendToPane(Server.messageBoard, "Connect close at: " + user.client.getRemoteSocketAddress() + "\n");
    sc.close();
  }
}

class User {
  private static int nbUser = 0; // Total number of client
  private int userId; // Client id
  private PrintStream streamOut; // Output stream to client
  private InputStream streamIn; // Input stream from client
  private String nickname; // Displayed name of client
  final public Socket client; // Client socket
  private String color; // Client color

  // constructor
  public User(Socket client, String name) throws IOException {
    this.streamOut = new PrintStream(client.getOutputStream());
    this.streamIn = client.getInputStream();
    this.client = client;
    this.nickname = name;
    this.userId = nbUser;
    this.color = ColorInt.getColor(this.userId);
    nbUser += 1;
  }

  // change color user
  public void changeColor(String hexColor){
    // check if it's a valid hexColor
    Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
    Matcher m = colorPattern.matcher(hexColor);
    if (m.matches()){
      Color c = Color.decode(hexColor);
      // if the Color is too Bright don't change
      double luma = 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue(); // per ITU-R BT.709
      if (luma > 160) {
        this.getOutStream().println("<b>Color Too Bright</b>");
        return;
      }
      this.color = hexColor;
      this.getOutStream().println("<b>Color changed successfully</b> " + this.toString());
      return;
    }
    this.getOutStream().println("<b>Failed to change color</b>");
  }

  // getter
  public PrintStream getOutStream(){
    return this.streamOut;
  }

  public InputStream getInputStream(){
    return this.streamIn;
  }

  public String getNickname(){
    return this.nickname;
  }

  // print user with his color
  public String toString(){

    return "<u><span style='color:"+ this.color
      +"'>" + this.getNickname() + "</span></u>";

  }
}

class ColorInt {
    public static String[] mColors = {
            "#3079ab", // dark blue
            "#e15258", // red
            "#f9845b", // orange
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#f092b0", // pink
            "#e8d174", // yellow
            "#e39e54", // orange
            "#d64d4d", // red
            "#4d7358", // green
    };

    public static String getColor(int i) {
        return mColors[i % mColors.length];
    }
}
