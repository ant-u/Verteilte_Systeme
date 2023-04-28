package Project.follower;
import java.io.IOException;
import java.net.Socket;

import Project.Node;
import Project.message.Message;
import Project.message.MessageHandler;

public class FollowerClientMessageHandler extends MessageHandler{
    private Follower parentFollower;

    public FollowerClientMessageHandler(Follower parentFollower, Node parentNode, Socket newConnection){
        super(parentNode, newConnection);
        this.parentFollower = parentFollower;
    }

    public void run(){
        while(!this.socket.isClosed()){
            Message message = this.readMessage();
            System.out.println(this.parentNode.getIp() + " received client message, forwarding to leader.");
            Message answerFromLeader = this.parentFollower.getConnectionToLeader().sendMessageGetResponse(message);
            try {
                this.outputStream.writeObject(answerFromLeader);
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }
    }

    //TODO
    protected void handleReadMessage(Message message){

    }
    protected void handleWriteMessage(Message message){

    }
    protected void handleInitializeMessage(Message message){

    }
    protected void handleHeartbeatMessage(Message message){

    }
    protected void handleUnknownMessage(Message message){

    }
}
