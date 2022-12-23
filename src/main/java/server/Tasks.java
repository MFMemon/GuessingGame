package server;

import java.util.LinkedHashMap;
import java.util.ArrayList;

class Tasks{

    ArrayList<String> questions = new ArrayList<String>();
    ArrayList<Integer> answers = new ArrayList<Integer>();
    int index = 0;

    public Tasks(){

        questions.add("Add 24 and 36 in decimal?");
        questions.add("Add two binary numbers 10 and 11 and give your answer in decimal?");
        questions.add("What is the square root of 64?");
        questions.add("What is the base of hex numbers, 2 or 16?");
        questions.add("Multiply 5 by 6?");
        questions.add("Calculate the factorial of 5?");
        questions.add("What is the remainder of the division of an even number by 2?");
        questions.add("How many seconds are there in 11 minutes?");
    
        answers.add(60);
        answers.add(5);
        answers.add(8);
        answers.add(16);
        answers.add(30);
        answers.add(120);
        answers.add(0);
        answers.add(660);

    }


    public void addIndex(){
        this.index += 1;
    }

    public void resetIndex(){
        this.index = 0;
    }

    public int getIndex(){
        return this.index;
    }

}