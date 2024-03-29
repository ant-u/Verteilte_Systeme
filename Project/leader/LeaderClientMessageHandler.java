package project.leader;

import java.net.InetSocketAddress;
import java.net.Socket;

import project.Node;
import project.helpers.Coordinate;
import project.message.Message;
import project.message.MessageHandler;
import project.message.MessageType;

/**
 * For every leader-client connection, a leader has a LeaderClientMessageHandler.
 * Receives and sends messages from and to clients.
 */
public class LeaderClientMessageHandler extends MessageHandler {
    private Leader parentLeader;
    private String clientIp;
    private int clientPort;

    public LeaderClientMessageHandler(Node parentNode, Socket newConnection, Leader parentLeader){
        super(parentNode, newConnection);
        this.parentLeader = parentLeader;
    }

    public void run(){
        while(!this.socket.isClosed()){
            this.receiveMessagesRoutine();
        }
    }

    /**
     * Initializing is implemented in registerConnection.
     * Every other initialize message is an error case.
     */
    @Override
    protected void handleInitializeMessage(Message message) {
        System.out.println("answer not implemented");
    }

    /**
     * No heartbeat messages can be send by clients.
     * This is an error case.
     */
    @Override
    protected void handleHeartbeatMessage(Message message) {
        System.out.println("answer not implemented");
    }

    /**
     * Same as Heartbeat, not a client functionality.
     * This is an error case.
     */
    @Override
    protected void handleSyncNodeListMessage(Message message) {
        System.out.println("answer not implemented");
    }

    /**
     * Client can ask for navigation.
     * Returns the next step for client. Message has to contain therefore Coordinate Array with:
     * 0: position
     * 1: destination
     * When client reached its goal, the client is deleted from map.
     * --> client is no more on street, parking somewhere.
     * This is the same function as in LeaderFollowerMessageHandler.
     */
    @Override
    protected void handleNavigationMessage(Message message){
        try {
            Coordinate[] payload = (Coordinate[]) message.getPayload();
            try {
                if(payload.length == 2){
                    if(this.parentNode.getArea().getPosition(message.getSender()) == null){
                        this.parentNode.getArea().place(message.getSender(), payload[0]);
                    }
                    if(!this.parentNode.getArea().getPosition(message.getSender()).compare(payload[0])){
                        this.parentNode.getArea().remove(message.getSender(), this.parentNode.getArea().getPosition(message.getSender()));
                        this.parentNode.getArea().place(message.getSender(), payload[0]);
                    }
                    
                    Coordinate nextStep = this.parentNode.getLogic().move(message.getSender(), payload[1]);
                    if(!nextStep.compare(payload[0])){
                        Message answer = new Message(this.parentNode.getIp(), message.getSender(), nextStep, MessageType.SUCCESS); 
                        this.sendMessage(answer);
                        if(nextStep.compare(payload[1])){
                            this.parentNode.getArea().remove(message.getSender(), nextStep);
                        }
                    }
                    else{
                        Message answer = new Message(this.parentNode.getIp(), message.getSender(), "Can't make move to next field", MessageType.ERROR); 
                        this.sendMessage(answer);
                    }
                }
                else{
                    System.out.println("Payload not containing all information");
                    Message answer = new Message(this.parentNode.getIp(), message.getSender(), "Please send navigation message with Array of 0: your position and 1: your destination", MessageType.ERROR); 
                    this.sendMessage(answer);
                }
            } catch (Exception e) {
                System.err.println("Move not possible: " + e.toString());
                Message answer = new Message(this.parentNode.getIp(), message.getSender(), "Move is not possible", MessageType.ERROR); 
                this.sendMessage(answer);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Inits client connections.
     * @return true when successful, false if not. Only call run() / start() method when initialized correct.
     */
    public Boolean registerConnection(){
        Message message = this.readMessage();
        System.out.println(this.parentNode.getIp() + " received a "+ message.getType() + " message: " + message.getPayload());
        if(message.getType() == MessageType.INITIALIZE){
            try {
                InetSocketAddress clientAddress = (InetSocketAddress) message.getPayload();
                this.clientIp = clientAddress.getHostName();
                this.clientPort = clientAddress.getPort();

                if(this.clientIp.contains("127.0.1.")){
                    System.out.println(this.parentLeader.getParentNode().getIp() + ": Leader registered " + this.clientIp);

                    String payload = "Registered " + this.clientIp + " as Client";
                    Message answer = new Message(this.parentNode.getIp(), message.getSender(), payload, MessageType.SUCCESS); 
                    this.sendMessage(answer);
                    return true;
                }
                else {
                    System.out.println(this.parentLeader.getParentNode().getIp() + ": Leader rejected " + this.clientIp);
                    String payload = "Please connect to " + this.parentLeader.getParentNode().getIp() + ":";
                    payload += this.parentLeader.getParentNode().getPort() + " for network functionality";
                    Message answer = new Message(this.parentNode.getIp(), message.getSender(), payload, MessageType.ERROR); 
                    this.sendMessage(answer);
                    return false;
                }

            } catch (Exception e) {
                System.out.println("Init message failed");
                String payload = "Insert INetSocketAddress of own IP and Port in payload.";
                Message answer = new Message(this.parentNode.getIp(), message.getSender(), payload, MessageType.ERROR); 
                this.sendMessage(answer);
                return false;
            }
        }
        else{
            Message answer = new Message(this.parentNode.getIp(), message.getSender(), "Please send init Message", MessageType.ERROR);
            this.sendMessage(answer);
            return false;
        }
    }

    public Leader getParentLeader() {return this.parentLeader;}
    public void setParentLeader(Leader parentLeader) {this.parentLeader = parentLeader;}
    public String getClientIp() {return this.clientIp;}
    public void setClientIp(String clientIp) {this.clientIp = clientIp;}
    public int getClientPort() {return this.clientPort;}
    public void setClientPort(int clientPort) {this.clientPort = clientPort;}
}
