package server;

import java.io.*;
import java.net.*;
import org.json.*;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;


class SockBaseServer{

    static ServerSocket server = null;
    static Socket clientSocket = null;
    static Game gameInstance = null;
    static LeaderBoard leaderBoard = null;
    // static InputStream in = null;
    // static OutputStream out = null;

    // public static void createClientSocketAndStreams(){
    //     clientSocket = server.accept();
    //     in = clientSocket.getInputStream();
    //     out = clientSocket.getOutputStream();  
    // }


    public static void main(String[] args) throws IOException{
        if (args.length != 2) {
            System.out.println("Expected arguments: <port(int)> <delay(int)>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);
            server = new ServerSocket(port);
        }
        catch (NumberFormatException nfe) {
            System.out.println("[Port|sleepDelay] must be an integer");
            System.exit(2);
        }
        catch(IOException ioe){
            System.out.println("Error opening socket. Program will terminate now.");
            System.exit(2);
        }

        FileInputStream jsonStream = new FileInputStream("leaderboard.json");
        BufferedReader jsonBuf = new BufferedReader(new InputStreamReader(jsonStream));
        String jsonStr = jsonBuf.readLine();
        JSONObject leaderboardJson = null;
        if(jsonStr == null){
            leaderboardJson = new JSONObject();
        }
        else{
            leaderboardJson = new JSONObject(jsonStr);
        }

        gameInstance = new Game();
        leaderBoard = new LeaderBoard(leaderboardJson);
    

        while(true){

                


                try{
                    clientSocket = server.accept();
                    if(Thread.activeCount() == 1){
                        gameInstance.setWon();
                    }
                }
                catch(IOException ioe){
                    System.out.println("Error opening client socket. Program will terminate now.");
                    System.exit(2);
                }
                finally{
                    ResponseHandler responseHandler = new ResponseHandler(clientSocket, gameInstance, leaderBoard);
                    new Thread(responseHandler).start();
                }



        }
    }
}