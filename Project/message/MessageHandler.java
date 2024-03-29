package project.message;

import java.io.*;
import java.net.Socket;
import project.Node;

public abstract class MessageHandler extends Thread{
    protected ObjectInputStream inputStream;
    protected ObjectOutputStream outputStream;
    protected Socket socket;
    protected Node parentNode;
    protected String ip;
    protected int port;

    /**
     * For every node based device this is the constructor that shall be used.
     * @param parentNode
     * @param newConnection
     */
    public MessageHandler(Node parentNode, Socket newConnection){
        this.parentNode = parentNode;
        this.socket = newConnection;
        this.ip = parentNode.getIp();
        this.port = parentNode.getPort();
        this.initializeStreams(newConnection);
    }

    /**
     * Overloading constructor so clients (which aren't based on nodes) can also be based on message handler.
     * @param newConnection
     * @param ipAddress
     * @param port
     */
    public MessageHandler(Socket newConnection, String ipAddress, int port){
        this.parentNode = null;
        this.socket = newConnection;
        this.ip = ipAddress;
        this.port = port;
        this.initializeStreams(newConnection);
    }

    /**
     * Reads message from input stream.
     * Kills the connection in every exception case because it is always corrupted.
     * @return message object or null
     */
    protected Message readMessage() {
        try {
            Message received = (Message) this.inputStream.readObject();
            return received;
        } catch (EOFException e) {
            System.err.println(e.toString());
            this.closeSocket();
            return null;
        } catch (Exception e){
            System.err.println(e.toString());
            this.closeSocket();
            return null;
        }
    }

    /**
     * waits for response after sending. Don't execute when receiveMessageRoutine is called.
     * Since both methods read, there is one which kills the stream and ends the connection.
     * @param message
     * @return
     */
    public Message sendMessageGetResponse(Message message){
        try {
            this.outputStream.writeObject(message);
            Message received = (Message) this.inputStream.readObject();
            return received;
        } catch (EOFException e) {
            System.err.println(e.toString());
            this.closeSocket();
            return null;
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    /*
     * Sends message. Kills socket in EOFException case (connection is stopped in this case)
     */
    public void sendMessage(Message message){
        try {
            this.outputStream.writeObject(message);
        } catch (EOFException e) {
            this.closeSocket();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Reads new messages, calls handling function for each message case.
     * For efficient usage, call this method in loop to always be able to receive messages.
     */
    protected void receiveMessagesRoutine(){
        try {
            Message message = this.readMessage();
            System.out.println(this.ip + " received a " + message.getType().toString() + " message: " + message.getPayload());
            switch (message.getType()) {
                case INITIALIZE:
                    this.handleInitializeMessage(message);
                    break;
                case HEARTBEAT:
                    this.handleHeartbeatMessage(message);
                    break;
                case SYNC_NODE_LIST:
                    this.handleSyncNodeListMessage(message);
                    break;   
                case NAVIGATION:
                    this.handleNavigationMessage(message);
                    break;
                case SUCCESS:
                    this.handleSuccessMessage(message);
                    break;
                case ERROR:
                    this.handleErrorMessage(message);
                    break;
                case ACK:
                    this.handleAckMessage(message);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    // Every implementation of MessageHandler must implement these message type functionalities, even if it's an error case.
    // MessageHandlers have to be able to answer each wrong message. 
    protected abstract void handleInitializeMessage(Message message);
    protected abstract void handleHeartbeatMessage(Message message);
    protected abstract void handleSyncNodeListMessage(Message message);
    protected abstract void handleNavigationMessage(Message message);

    //the following three codes should not be sent without context (proactively), so they should only received by a routine
    //that is waiting for an answer (when using sendMessageGetResponse), not by the receivingMessages Routine.
    //They might be overrode if functionality is wanted.

    protected void handleSuccessMessage(Message message){
        System.out.println("Please do not send answer codes as request.");
    }

    protected void handleErrorMessage(Message message){
        System.out.println("Please do not send answer codes as request.");
    }

    protected void handleAckMessage(Message message){
        System.out.println("Please do not send answer codes as request.");
    }

    /**
     * Initialize the output and input streams.
     * Both are object streams because every sent package must be a message.
     * Called for every message handler that is created (super constructor calls this)
     * @param newConnection new socket which shall have streams initialized.
     */
    protected void initializeStreams(Socket newConnection){
        try{
            OutputStream outputStream = newConnection.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            this.outputStream = new ObjectOutputStream(dataOutputStream);

            InputStream inputStream = newConnection.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            this.inputStream = new ObjectInputStream(dataInputStream);
        }
        catch(IOException e) {
            System.out.println("Node read initialize failed");
        }        
    }
    
    protected void closeSocket(){
        try {
            System.out.println(this.ip + " lost connection to opponent, closing own socket");
            this.socket.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    public Socket getSocket(){return this.socket;}
    public Node getParentNode(){return this.parentNode;}
}
