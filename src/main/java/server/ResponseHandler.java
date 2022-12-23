package server;

import java.io.*;
import java.net.*;
import org.json.*;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import buffers.RequestProtos.Request;
import buffers.RequestProtos.Message;
import buffers.RequestProtos.Logs;
import buffers.ResponseProtos.Response;
import buffers.ResponseProtos.Entry;


class ResponseHandler implements Runnable{

    InputStream in;
    OutputStream out;
    LeaderBoard leaderBoard;
    Response.Builder responseBuilder;
    Game gameInstance;
    String playerName;
    Tasks tasks;
    String logFilename;

    public ResponseHandler(Socket clientSocket, Game gameInstance, LeaderBoard leaderBoard) throws IOException{
        this.leaderBoard = leaderBoard;
        this.in = clientSocket.getInputStream();
        this.out = clientSocket.getOutputStream();
        // this.numberOfTasks = numberOfTasks;
        this.gameInstance = gameInstance;
        this.playerName = null;
        this.tasks = new Tasks();
        this.logFilename = "logs.txt";
    }

    public Request receieveRequest() throws IOException{
        Request request= Request.parseDelimitedFrom(in);
        return request;
    }

    public Response makeGreetings(String name){
        Response.Builder responseBuilder = Response.newBuilder();
        this.playerName = name;
        JSONObject winsAndLogins = null;
        if(leaderBoard.has(name)){
            winsAndLogins = leaderBoard.getObject(name);
            int logins = winsAndLogins.getInt("logins");
            winsAndLogins.put("logins", logins + 1);
        }
        else{
            winsAndLogins = new JSONObject();
            winsAndLogins.put("wins", 0); 
            winsAndLogins.put("logins", 1); 
        }
        leaderBoard.put(name, winsAndLogins);
        Response response = responseBuilder.setResponseType(Response.ResponseType.GREETING)
                                            .setMessage("Welcome to the game " + name + "\n"+ "This game is all maths so give all your answers in numbers, please.\n")
                                            .build();
        writeLeaderBoardToFile();
        writeToLog(playerName, Message.CONNECT);
        return response;
    }

    public void writeToLog(String name, Message message){
        try {
            Logs.Builder logs = readLogFile();
            Date date = java.util.Calendar.getInstance().getTime();
            logs.addLog(date.toString() + ": " +  name + " - " + message);
            FileOutputStream output = new FileOutputStream(logFilename);
            Logs logsObj = logs.build();

            for (String log: logsObj.getLogList()){
                System.out.println(log);
            }
            logsObj.writeTo(output);
        }
        catch(Exception e){
            System.out.println("Issue while trying to save");
        }
    }

    public Logs.Builder readLogFile() throws Exception{
        Logs.Builder logs = Logs.newBuilder();

        try {
            return logs.mergeFrom(new FileInputStream(logFilename));
        } catch (FileNotFoundException e) {
            System.out.println(logFilename + ": File not found.  Creating a new file.");
            return logs;
        }
    }

    public Response retrieveLeaderBoard(){
        Response.Builder responseBuilder = Response.newBuilder();
        ArrayList<Entry> entries = new ArrayList<Entry>();
        Set<String> keys = leaderBoard.keySet();
        for(String name: keys){
            JSONObject winsAndLogins = leaderBoard.getObject(name);
            String nameEntry = name;
            int winsEntry = winsAndLogins.getInt("wins");
            int loginsEntry = winsAndLogins.getInt("logins");
            Entry entry = Entry.newBuilder()
                                .setName(nameEntry)
                                .setWins(winsEntry)
                                .setLogins(loginsEntry)
                                .build();
            entries.add(entry);
        }
        Response response = responseBuilder.setResponseType(Response.ResponseType.LEADER)
                                            .addAllLeader(entries)
                                            .setMessage("##### LEADER BOARD #####")
                                            .build();
        return response;
    }


    public Response showTask(){
        Response.Builder responseBuilder = Response.newBuilder();
        gameInstance.newGame();
        int playerWins = leaderBoard.getObject(playerName).getInt("wins");
        gameInstance.setImageRevealFactor(8 - playerWins);
        Response response = responseBuilder.setResponseType(Response.ResponseType.TASK)
                                            .setTask(tasks.questions.get(tasks.getIndex()))
                                            .setImage(gameInstance.getImage())
                                            .setMessage("##### CURRENT IMAGE #####")
                                            .build();
        System.out.println("Question: " + tasks.questions.get(tasks.getIndex()));
        System.out.println("Answer: " + tasks.answers.get(tasks.getIndex()) + "\n");
        
        return response;
    }


    public Response evaluateAnswer(String answer){
        if(answer.equals("exit")){
            return sayBye();
        }

        Response.Builder responseBuilder = Response.newBuilder();
        Response response = null;
        
        try{
            int givenAnswer = Integer.parseInt(answer);
            int correctAnswer = tasks.answers.get(tasks.getIndex());
    
            if(givenAnswer != correctAnswer){
                response = responseBuilder.setResponseType(Response.ResponseType.TASK)
                .setTask(tasks.questions.get(tasks.getIndex()))
                .setMessage("Wrong answer! Please try again.\n")
                .build();
                return response;
            }
    
            tasks.addIndex();
    
            for (int i = 0; i < gameInstance.getImageRevealFactor(); i++){
                if (gameInstance.getIdx()< gameInstance.getIdxMax())
                    gameInstance.replaceOneCharacter();
            }
    
            responseBuilder.setEval(true).setImage(gameInstance.getImage()); 
        }
        catch(NumberFormatException nfe){
            return showError();
        }

        if(gameInstance.getIdx() == gameInstance.getIdxMax()){
            responseBuilder.setResponseType(Response.ResponseType.WON).setMessage("##### YOU WON #####");
            JSONObject winsAndLogins = leaderBoard.getObject(playerName);
            int currentWins = winsAndLogins.getInt("wins");
            winsAndLogins.put("wins", currentWins + 1);
            leaderBoard.put(playerName, winsAndLogins);
            gameInstance.setWon();
            tasks.resetIndex();
            writeLeaderBoardToFile();
        }
        else{
            responseBuilder.setResponseType(Response.ResponseType.TASK)
                            .setTask(tasks.questions.get(tasks.getIndex()))
                            .setMessage("Correct answer!\n\n##### CURRENT IMAGE #####");
            System.out.println("Question: " + tasks.questions.get(tasks.getIndex()));
            System.out.println("Answer: " + tasks.answers.get(tasks.getIndex()) + "\n");
        }

        response = responseBuilder.build();
        return response;
    }


    public Response sayBye(){
        Response.Builder responseBuilder = Response.newBuilder();
        Response response = responseBuilder.setResponseType(Response.ResponseType.BYE).build();
        return response;
    }


    public Response showError(){
        Response.Builder responseBuilder = Response.newBuilder();
        Response response = responseBuilder.setResponseType(Response.ResponseType.ERROR)
                                            .setMessage("Please answer in number format.")                                    
                                            .build();
        return response;
    }


    public Response createResponse(Request request){
        Response response = null;
        Request.OperationType requestType = request.getOperationType();
        int requestTypeVal = requestType.getNumber();
        // System.out.println("Received " + requestTypeVal + " from the client");
        switch(requestTypeVal){
            case 0:
                response = makeGreetings(request.getName());
                break;
            case 1:
                response = retrieveLeaderBoard();
                break;
            case 2:
                response = showTask();
                break;
            case 3:
                response = evaluateAnswer(request.getAnswer());
                break;
            case 4:
                response = sayBye();
                break;
            default:
                // response = showError();
        }
        return response;
    }

    public void writeResponse(Response response) throws IOException{
        response.writeDelimitedTo(out);
    }

    public void writeLeaderBoardToFile(){
        try{
            File file = new File("leaderboard.json");
            FileOutputStream fileOut = new FileOutputStream(file);
            byte[] leaderBoardBytes = leaderBoard.getLeaderBoard().toString().getBytes();
            fileOut.write(leaderBoardBytes);
        }
        catch(IOException ioe){
            System.out.println("Error writing leaderboard to the file. Program will terminate now!");
            System.exit(2);
        }

    }

    public void run(){

        while(true){
            try{
                Response.Builder responseBuilder = Response.newBuilder();
                Request request = receieveRequest();
                if(request == null){
                    System.out.println("Client " + playerName + " exits the game.");
                    return;
                }
                Response response = createResponse(request);
                writeResponse(response);
            }
            catch(IOException ioe){
                System.out.println("Error writing to or reading from the socket. Program will terminate now.");
                System.exit(2);
            }
        }



        // if(opType == Request.OperationType.NAME){
        //     Entry.Builder ent = Entry.newBuilder();
        //     ent.setName(req.getName());
        //     System.out.println(req.getName() + " has connected.");
        //     resBld.setResponseType(Response.ResponseType.GREETING);
        //     resBld.setMessage("Welcome to the game " + req.getName() + "\n");
        // }
        // if(opType == Request.OperationType.NEW){
        //     FileReader fr = new FileReader(new File("src/main/resources/cat.txt"));
        //     StringBuilder strBuilder = new StringBuilder();
        //     int i = 0;
        //     while((i=fr.read())!=-1){
        //         strBuilder.append((char)i);
        //     }
        //     String imageStr = strBuilder.toString();
        //     String imageStrCopy = new String(imageStr);
        //     StringBuilder newImageBuilder = new StringBuilder();
        //     for(int j=0; j<(int)(imageStr.length()/3); j++){
        //         newImageBuilder.append("X");
        //     }
        //     newImageBuilder.append(imageStr.substring((int)(imageStr.length()/3), imageStr.length()));

        //     System.out.println(newImageBuilder.toString());
    }

}