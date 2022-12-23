package client;

import java.net.*;
import java.io.*;

import org.json.*;

import buffers.RequestProtos.Request;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;

import java.util.*;
import java.util.stream.Collectors;

class SockBaseClient {

    static Socket serverSock = null;
    static OutputStream out = null;
    static InputStream in = null;


    public static void createSocketAndStreams(String args0, String args1) throws IOException{
        String host = args0;
        int port = Integer.parseInt(args1);
        serverSock = new Socket(host, port);
        out = serverSock.getOutputStream();
        in = serverSock.getInputStream();  
    }

    public static Request createRequestFromAnswer(){
        Request.Builder requestBuilder = Request.newBuilder();
        Request request = null;
        try{
            String answer = readFromConsole();
            System.out.println("YOUR ANSWER: " + answer);
            request = requestBuilder.setOperationType(Request.OperationType.ANSWER)
                                            .setAnswer(answer)                      
                                            .build();
        }
        catch(IOException ioe){
            System.out.println("Error reading from socket. Program will terminate now.");
            System.exit(2);
        }
        return request;
    }


    public static Request createRequestWithName(){
        Request.Builder requestBuilder = Request.newBuilder();
        Request request = null;
        try{
            System.out.println("Please provide your name for the server. ( ͡❛ ͜ʖ ͡❛)");
            String name = readFromConsole();
            request = requestBuilder.setName(name).build();
        }
        catch(IOException ioe){
            System.out.println("Error reading from socket. Program will terminate now.");
            System.exit(2); 
        }
        return request;
    }

    public static Request createRequestFromChoice(){
        Request.Builder requestBuilder = Request.newBuilder();
        Request request = null;
        int playerChoice = 0;

        while(true){
            try{
                playerChoice = Integer.parseInt(readFromConsole());
                // System.out.println("Your choice: " + playerChoice);
                switch(playerChoice){
                    case 1:
                        request = requestBuilder.setOperationType(Request.OperationType.LEADER)
                                                .build();
                        break;
                    case 2:
                        request = requestBuilder.setOperationType(Request.OperationType.NEW)
                                                .build();
                        break;
                    case 3:
                        request = requestBuilder.setOperationType(Request.OperationType.QUIT)
                                                .build();
                        break;
                    default:
                        System.out.println("\nPlease choose a number between 1 and 3.\n");
                }
                break;
            }
            catch(NumberFormatException nfe){
                System.out.println("\nIncorrect input. Kindly choose a number between 1-3.");
                continue;
            }
            catch(IOException ioe){
                System.out.println("Error reading from socket. Program will terminate now.");
                System.exit(2);
            }
        }
        return request;
    }


    public static String readFromConsole() throws IOException{
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        return stdIn.readLine();
    }
    

    public static void writeRequest(Request request) throws IOException{
        request.writeDelimitedTo(out);
    }

    public static Request showResponseAndCreateRequest() throws IOException{
        Request request = null;
        Response response = Response.parseDelimitedFrom(in);
        // System.out.println("Received response from the server");
        Response.ResponseType responseType = response.getResponseType();
        int responseTypeVal = responseType.getNumber();
        // System.out.println("Received " + responseTypeVal + " from the server");
        switch(responseTypeVal){
            case 0:
                showGreetings(response);
                break;
            case 1:
                showLeaderBoard(response);
                break;
            case 2:
                showTask(response);
                request = createRequestFromAnswer();
                break;
            case 3:
                showWon(response);
                break;
            case 4:
                showError(response);
                request = createRequestFromAnswer();
                break;
            case 5:
                closeSocket();
        }
        return request;
    }

    public static void showGreetings(Response response){
        System.out.println(response.getMessage() + "\n");
    }

    public static void showLeaderBoard(Response response){
        System.out.println(response.getMessage());
        List<Entry> entries = response.getLeaderList();
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("[");

        for(Entry entry: entries){
            strBuilder.append("Name: " + entry.getName());
            strBuilder.append(", ");
            strBuilder.append("Wins: " + entry.getWins());
            strBuilder.append(", ");
            strBuilder.append("Logins: " + entry.getLogins());
            strBuilder.append("\n");
        }
        strBuilder.deleteCharAt(strBuilder.length()-1);
        strBuilder.append("]\n");
        System.out.println(strBuilder.toString());
    }

    public static void showTask(Response response){
        System.out.println(response.getMessage());
        if(response.hasImage()){
            System.out.println(response.getImage());
            System.out.println("\n");
        }
        System.out.println("##### YOUR TASK #####");
        System.out.println(response.getTask());
        System.out.println("\n");
    }

    public static void showWon(Response response){
        System.out.println("##### CURRENT IMAGE #####");
        System.out.println(response.getImage());
        System.out.println("\n\n");
        System.out.println(response.getMessage());
        System.out.println("\n\n");
    }

    public static void showError(Response response){
        System.out.println(response.getMessage());
        System.out.println("\n");
    }

    public static void printGameOptions(){
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Please choose one of the following options and enter the number (1-3) corresponding to that option\n");
        strBuilder.append("1. See the leader board\n2. Play the game\n3. Quit\n");
        System.out.println(strBuilder.toString());
    }


    public static void closeSocket() throws IOException{
        in.close();
        out.close();
        serverSock.close();
        System.exit(0);      
    }

    public static void main (String args[]) throws IOException {

        // Make sure two arguments are given
        if (args.length != 2) {
            System.out.println("Expected arguments: <host(String)> <port(int)>");
            System.exit(1);
        }

        try {
            createSocketAndStreams(args[0], args[1]);
            Request request = createRequestWithName();
            writeRequest(request);
            showResponseAndCreateRequest();
    
            while(true){
                printGameOptions();
                request = createRequestFromChoice();
                if(request != null){
                    while(true){
                        writeRequest(request);
                        request = showResponseAndCreateRequest();
                        if(request == null){
                            break;
                        }
                    }                    
                }

            }
        }
        catch (NumberFormatException nfe) {
            System.out.println("[Port] must be integer");
            System.exit(2);
        }
        catch (IOException ioe) {
            System.out.println("Error opening socket or writing to the socket. Program will terminate now.");
            System.exit(0);
        }
    }
}


