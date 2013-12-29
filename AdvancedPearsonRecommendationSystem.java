package MovieRecommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;

public class AdvancedPearsonRecommendationSystem {

    private User[] users;
    private int activeUserName;
    public int totalUsers = 200;
    private int totalMovies = 1000;
    private int k = 10;
    private double[] similarity;
    private double[][] ratings;
    private int[] topK;
    private Hashtable<Integer, Integer> needToBeWritten;
    private double[] averageOfUser;
    private double activeUserAverage;
    private double[] IUF;

    public AdvancedPearsonRecommendationSystem() throws FileNotFoundException {

        ratings = new double[totalUsers][totalMovies];
        IUF = new double[totalMovies];
        topK = new int[k];
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File("train.txt"))));
        similarity = new double[totalUsers];
        for (int i = 0; i < totalUsers; i++) {

            for (int j = 0; j < totalMovies; j++) {
                ratings[i][j] = s.nextInt();
                if (ratings[i][j] > 0) {
                    IUF[j]++;
                }
            }
        }

        for (int movie = 0; movie < totalMovies; movie++) {
            if (IUF[movie] > 0) {
                IUF[movie] = Math.log10(totalUsers / IUF[movie]);
            }

        }
    }

    public int loadNextUser(double[] activeUser, Scanner s) {

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

    private void findAveragesForMovie(int movie) {

        averageOfUser = new double[totalUsers];
        for (int i = 0; i < totalUsers; i++) {
            double sum = 0, count = 0;
            for (int j = 0; j < totalMovies; j++) {
                if (ratings[i][j] > 0) {
                    sum += ratings[i][j];
                    count++;
                }
            }
            averageOfUser[i] = sum / count;
        }
    }

    private double findAverage(double[] activeUser) {
        double sum = 0, count = 0;
        for (int i = 0; i < activeUser.length; i++) {
            if (activeUser[i] >= 1) {
                sum += activeUser[i];
                count++;
            }
        }
        return sum / count;
    }

    public void predictBasedOnPearsonSimilarity(String input, String output) throws FileNotFoundException, IOException {
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File(input))));
        PrintStream fout = new PrintStream(new File(output));
        activeUserName = s.nextInt() - 1;                           //storing username as one less than the original to pace with array indexes.
        while (s.hasNext()) {
            needToBeWritten = new Hashtable<Integer, Integer>();
            double[] activeUser = new double[totalMovies];
            Arrays.fill(activeUser, -1);
            int nextUser = loadNextUser(activeUser, s);
            activeUserAverage = findAverage(activeUser);            //put outside fromm for loop
            for (int movie = 0; movie < activeUser.length; movie++) {

                if (activeUser[movie] == 0) {
                    findAveragesForMovie(movie);
                    
                    findCosineSimilarity(activeUser);
                    findTopKNearestFor(movie);
                    needToBeWritten.put(movie, 1);
                    double prediction = predict(movie);
                    activeUser[movie] = adjust((int) Math.round(prediction));  //NaN will nit appear here as we cast double into int                                                                                                                                                                      
                }                                                              //logic removed to insert averageofActiveUser in case prediction is zero.added into findCosineSimilarity.
            }
            write(activeUserName, activeUser, fout);
            activeUserName = nextUser;
        }

        fout.flush();
        fout.close();
    }

    private int adjust(int d) {
        if (d == 0) {
            return adjust((int) Math.round(activeUserAverage));
        } else if (d > 5) {
            return 5;
        } else if (d < 0) {
            return 1;
        } else {

            return d;
        }
    }

    private void findTopKNearestFor(int movie) {
        int j = 0;
        for (int i = totalUsers - 1; j < k && i >= 0; i--) {
            int user = users[i].name;

            if (ratings[user][movie] > 0) {
                topK[j] = users[i].name;
                j++;
            }
        }
        for (; j < k; j++) {
            topK[j] = -1;
        }
    }

    private void sortUsers() {
        users = new User[totalUsers];

        for (int i = 0; i < totalUsers; i++) {
            users[i] = new User(similarity[i], i);
        }
        Arrays.sort(users, User.BySimilarity);
    }

    private void findCosineSimilarity(double[] activeUser) {          // Add Logic to check that not all elements are same otherwise Wau =0 
        for (int user = 0; user < totalUsers; user++) {

            double innerProduct = 0;
            double lengthOfActiveUser = 1;
            double lengthofUser = 1;
            for (int movie = 0; movie < totalMovies; movie++) {
                if (activeUser[movie] > 0 && ratings[user][movie] > 0) {  //(activeUser[movie] - activeUserAverage) add logic to rectify this to be 0
                    innerProduct += ((activeUser[movie]*IUF[movie]) - (activeUserAverage*IUF[movie]) )* ((ratings[user][movie]*IUF[movie]) - (averageOfUser[user]*IUF[movie]));
                    lengthOfActiveUser += Math.pow(((activeUser[movie]*IUF[movie]) - (activeUserAverage*IUF[movie])), 2);
                    lengthofUser += Math.pow(((ratings[user][movie]*IUF[movie]) - (averageOfUser[user]*IUF[movie])), 2);
                    
                   
                    
                    
                }
            }

            similarity[user] = innerProduct / (Math.sqrt(lengthOfActiveUser) * Math.sqrt(lengthofUser));
            if (similarity[user] == 0 && (isElementAverageInActiveUser(activeUser) || isElementAverage(user))) {                                
                similarity[user] = cosineSimilarity(user, activeUser);
                
            }
            similarity[user] = applyCaseAmplification(similarity[user]);

        }
        sortUsers();
    }
    private double cosineSimilarity(int user, double[] activeUser) {
        double innerProduct = 0;
        double lengthOfActiveUser = 1;
        double lengthofUser = 1;

        for (int movie = 0; movie < totalMovies; movie++) {
            if (activeUser[movie] != 0 && ratings[user][movie] != 0 && activeUser[movie] != -1) {
                innerProduct += activeUser[movie] * ratings[user][movie];
                lengthOfActiveUser += Math.pow(activeUser[movie], 2);
                lengthofUser += Math.pow(ratings[user][movie], 2);
            }
        }

        return  innerProduct / (Math.sqrt(lengthOfActiveUser) * Math.sqrt(lengthofUser));

    
    } 

    private double applyCaseAmplification(double rating) {
        double r = 2.7;
        double absRating = Math.abs(rating);
        rating = rating * Math.pow(absRating, r - 1);
        return rating;
    }

    private boolean isElementAverage(int user) {

        for (int movie = 0; movie < totalMovies; movie++) {
            if (ratings[user][movie] == averageOfUser[user]) {
                continue;
            } else if (ratings[user][movie] > 0) {
                return false;
            }
        }

        return true;
    }

    private boolean isElementAverageInActiveUser(double[] activeUser) {

        for (int movie = 0; movie < totalMovies; movie++) {
            if (activeUser[movie] == activeUserAverage) {
                continue;
            } else if (activeUser[movie] > 0) {
                return false;
            }
        }

        return true;
    }

    private void write(int activeUserName, double[] activeUser, PrintStream fout) {
        for (int movie = 0; movie < activeUser.length; movie++) {
            if (activeUser[movie] != -1 && needToBeWritten.get(movie) != null) {

                fout.println((activeUserName + 1) + " " + (movie + 1) + " " + ((int) activeUser[movie]));   //need to add 1
            }
        }
    }

    private double predict(int movie) {
        int j = 0;
        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < k && topK[i] != -1; i++) {
            int user = topK[i];

            numerator += similarity[user] * (ratings[user][movie] - averageOfUser[user]);//ratings can be lower than averages and that can cause value 1
            denominator += Math.abs(similarity[user]);
        }
       
        return activeUserAverage + (numerator / denominator);

    }
 

    private void printColumn(int column) {
        int count = 0;
        for (int user = 0; user < totalUsers; user++) {
            System.out.println(user + " " + ratings[user][column]);
        }

    }

    private void printRow(int user) {
        for (int movie = 0; movie < totalMovies; movie++) {
            System.out.println(movie + " " + ratings[user][movie]);
        }
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {
        AdvancedPearsonRecommendationSystem r = new AdvancedPearsonRecommendationSystem();
        r.predictBasedOnPearsonSimilarity("test20.txt", "result10.txt");
    }
}
