package MovieRecommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;

public class CosineRecommendationSystem {

    private User[] users;
    private int activeUserName;
    public int totalUsers = 200;
    private int totalMovies = 1000;
    private int k = 10;
    private double[] similarity;
    private double[][] ratings;
    private int[] topK;
    private Hashtable<Integer, Integer> needToBeWritten;

    public CosineRecommendationSystem() throws FileNotFoundException {
        similarity = new double[totalUsers];
        ratings = new double[totalUsers][totalMovies];
        topK = new int[k];
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File("train.txt"))));

        for (int i = 0; i < totalUsers; i++) {
            for (int j = 0; j < totalMovies; j++) {
                ratings[i][j] = s.nextInt();
            }
        }
    }

    public int loadNextUser(int[] activeUser, Scanner s) {

        int name = activeUserName;

        while (name == activeUserName) {

            try {
                activeUser[s.nextInt() - 1] = s.nextInt();
                if (s.hasNext()) {
                    name = s.nextInt() - 1;

                }
            } catch (Exception e) {
                break;
            }

        }
        return name;
    }

    public void predictBasedOnCosineSimilarity(String input, String output) throws FileNotFoundException, IOException {
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File(input))));
        PrintStream fout = new PrintStream(new File(output));
        //fout.write(System.getProperty( "line.separator" ));
        activeUserName = s.nextInt() - 1;                           //storing username as one less than the original to pace with array indexes.
        while (s.hasNext()) {
            needToBeWritten = new Hashtable<Integer, Integer>();
            int[] activeUser = new int[totalMovies];
            Arrays.fill(activeUser, -1);
            int nextUser = loadNextUser(activeUser, s);
            findCosineSimilarity(activeUser);

            for (int movie = 0; movie < activeUser.length; movie++) {

                double average = findAverage(activeUser);
                if (activeUser[movie] == 0) {
                    needToBeWritten.put(movie, 1);
                    findTopKNearestFor(movie);
                    activeUser[movie] = (int) Math.round(predict(movie));
                      
                    if (activeUser[movie] == 0) {                       //NaN will not appear as we cast double into int.
                        activeUser[movie] = (int) Math.round(average);
                    }
                    findCosineSimilarity(activeUser);                       // finding cosine similarity with predicted rating.
                   
                }
            }
            write(activeUserName, activeUser, fout);
            activeUserName = nextUser;
        }
        fout.flush();
        fout.close();
    }

    private double findAverage(int[] activeUser) {
        double sum = 0, count = 0;
        for (int i = 0; i < activeUser.length; i++) {
            if (activeUser[i] >= 1) {
                sum += activeUser[i];
                count++;
            }
        }
        return sum / count;

    }

    private void write(int activeUserName, int[] activeUser, PrintStream fout) throws IOException {
        for (int movie = 0; movie < activeUser.length; movie++) {
            if (activeUser[movie] != -1 && needToBeWritten.get(movie) != null) {

                fout.println((activeUserName + 1) + " " + (movie + 1) + " " + (activeUser[movie]));   //need to add 1
            }
        }
    }

    private double predict(int movie) {                 
        int j = 0;
        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < k && topK[i] != -1; i++) {
            int user = topK[i];

            numerator += similarity[user] * ratings[user][movie];
            denominator += similarity[user];
        }

        return numerator / denominator;
    }

    private void sortUsers() {
        users = new User[totalUsers];

        for (int i = 0; i < totalUsers; i++) {
            users[i] = new User(similarity[i], i);
        }
        Arrays.sort(users, User.BySimilarity);


    }

    private void findTopKNearestFor(int movie) {
        int j = 0;

        for (int i = totalUsers - 1; j < k && i >= 0; i--) {
            int user = users[i].name;

            if (ratings[user][movie] != 0) {
                topK[j] = users[i].name;
                j++;
            }
        }
        for (; j < k; j++) {
            topK[j] = -1;
        }
    }

    private void findCosineSimilarity(int[] activeUser) {

        for (int user = 0; user < totalUsers; user++) {
            int innerProduct = 0;
            int lengthOfActiveUser = 1;
            int lengthofUser = 1;

            for (int movie = 0; movie < totalMovies; movie++) {
                if (activeUser[movie] != 0 && ratings[user][movie] != 0 && activeUser[movie] != -1) {
                    innerProduct += activeUser[movie] * ratings[user][movie];
                    lengthOfActiveUser += Math.pow(activeUser[movie], 2);
                    lengthofUser += Math.pow(ratings[user][movie], 2);
                }
            }
                
           similarity[user] = innerProduct / (Math.sqrt(lengthOfActiveUser) * Math.sqrt(lengthofUser));                                                    
        }
        sortUsers();

    }

    public void printZeroColumns() {

        for (int column = 0; column < totalMovies; column++) {
            for (int row = 0; row < totalUsers; row++) {



                if (ratings[row][column] != 0) {
                    break;
                }
                if (row == totalUsers - 1) {
                    System.out.println(column + 1);
                }
            }
        }
    }

    public void printZeroRows() {

        for (int row = 0; row < totalUsers; row++) {
            for (int column = 0; column < totalMovies; column++) {



                if (ratings[row][column] != 0) {
                    break;
                }
                if (column == totalMovies - 1) {
                    System.out.println(row + 1);
                }
            }
        }
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {
        CosineRecommendationSystem r = new CosineRecommendationSystem();
        r.predictBasedOnCosineSimilarity("test10.txt", "result10.txt");



    }
}
