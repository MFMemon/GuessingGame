package server;

import org.json.*;
import java.util.Set;

class LeaderBoard{

    JSONObject leaderBoard;

    public LeaderBoard(JSONObject leaderBoard){
        this.leaderBoard = leaderBoard;
    }

    public synchronized boolean has(String key){
        return leaderBoard.has(key);
    }

    public synchronized JSONObject getObject(String key){
        return leaderBoard.getJSONObject(key);
    }

    public synchronized void put(String key, JSONObject value){
        leaderBoard.put(key, value);
    }

    public synchronized Set<String> keySet(){
        return leaderBoard.keySet();
    }

    public synchronized JSONObject getLeaderBoard(){
        return leaderBoard;
    }



}