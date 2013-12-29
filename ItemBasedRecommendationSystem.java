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

public class ItemBasedRecommendationSystem {

    private User[] movies;
    private int activeUserName;
    public int totalUsers = 200;
    private int totalMovies = 1000;
    private int k = 30;
    private double[] similarity;
    private double[][] ratings;
    private int[] topK;
    private Hashtable<Integer, Integer> needToBeWritten;
    private Hashtable<Double,Double> negativeValues;
    private double[] averageOfUser;
    private double activeUserAverage;
    private double[]activeUser;

    public ItemBasedRecommendationSystem() throws FileNotFoundException {
        similarity = new double[totalMovies];
        ratings = new double[totalUsers][totalMovies];
        topK = new int[k];
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File("train.txt"))));

        for (int i = 0; i < totalUsers; i++) {

            for (int j = 0; j < totalMovies; j++) {
                ratings[i][j] = s.nextInt();
            }
        }
        findUserAverages();
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

    private void findUserAverages() {

        averageOfUser = new double[totalUsers];
        for (int i = 0; i < totalUsers; i++) {
            double sum = 0, count = 0;
            for (int j = 0; j < totalMovies; j++) {
                if (ratings[i][j] != 0) {
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

  

    public void predictItemBasedSimilarity(String input, String output) throws FileNotFoundException, IOException {
        Scanner s = new Scanner(new BufferedReader(new FileReader(new File(input))));
        PrintStream fout = new PrintStream(new File(output));
        activeUserName = s.nextInt() - 1;                           //storing username as one less than the original to pace with array indexes.
        while (s.hasNext()) {
            needToBeWritten = new Hashtable<Integer, Integer>();
            activeUser = new double[totalMovies];
            Arrays.fill(activeUser, -1);
            int nextUser = loadNextUser(activeUser, s);
            
        for (int movie = 0; movie < activeUser.length; movie++) {
                if (activeUser[movie] == 0) {                                                                                                 
                    activeUserAverage = findAverage(activeUser);                                            
                    findSimilarMoviesTo(movie);
                    findTopKNearestFor();
                    needToBeWritten.put(movie, 1);
                    double prediction=predict();
                    double rounded=Math.round(prediction);
                    double adjusted=adjust(rounded);
                    System.out.println(activeUserName+" "+movie+" "+prediction+" "+rounded+" "+adjusted);
                   
                    activeUser[movie] = adjusted;  //NaN will nit appear here as we cast double into int                                                                               
                }                                                                  //logic removed to insert averageofActiveUser in case prediction is zero.added into findSimilarMoviesTo.
            }
            write(activeUserName, activeUser, fout);
            activeUserName = nextUser;
        }
       
        fout.flush();
        fout.close();
    }

    private int adjust(double d) {
        if (d > 5) {
            return 5;
        } else if (d <= 0) {                        
            
                int temp=(int)(d+5);
                if(temp>5)
                    return 5;
                else if (temp==0)
                    return 1;
                else return temp;
            
            
        } else {
            return (int)d;
        }
    }

    private void findTopKNearestFor() {
        int j = 0;
        

        for (int i = totalMovies - 1; j < k && i >= 0; i--) {
            int movie = movies[i].name;

                if(activeUser[movie]>0){
                topK[j] = movies[i].name;
                j++;
                }
            
        }
        for (; j < k; j++) {
            topK[j] = -1;
        }
    }

    private void sortUsers() {
        
        movies = new User[totalMovies];

        for (int i = 0; i < totalMovies; i++) {
           
            movies[i] = new User(Math.abs(similarity[i]), i);
        }
        Arrays.sort(movies, User.BySimilarity);


    }        
    private void findSimilarMoviesTo(int predictForMovie) {          // Add Logic to check that not all elements are same otherwise Wau =0 
        

           for (int movie = 0; movie < totalMovies; movie++) {
            if (movie == predictForMovie) {
                continue;
            }
            double movieMean=findMeanFor(movie);
            double predictForMovieMean=findMeanFor(predictForMovie);
            double innerProduct = 0;
            double lengthOfActiveUser = 1;
            double lengthofUser = 1;
                
            for (int user = 0; user < totalUsers; user++) {

                if (ratings[user][movie] > 0 && ratings[user][predictForMovie] > 0) {  //(activeUser[movie] - activeUserAverage) add logic to rectify this to be 0


                    innerProduct += (ratings[user][movie] -averageOfUser[user]) * (ratings[user][predictForMovie] - averageOfUser[user]);
                    lengthOfActiveUser += Math.pow((ratings[user][movie] - averageOfUser[user]), 2);
                    lengthofUser += Math.pow((ratings[user][predictForMovie] - averageOfUser[user]), 2);                                                                                    
                }
            }

            similarity[movie] = innerProduct / (Math.sqrt(lengthOfActiveUser) * Math.sqrt(lengthofUser));
                                                                      
        }
        sortUsers();
    }
       

    private void write(int activeUserName, double[] activeUser, PrintStream fout) {
        for (int movie = 0; movie < activeUser.length; movie++) {
            if (activeUser[movie] != -1 && needToBeWritten.get(movie) != null) {

                fout.println((activeUserName + 1) + " " + (movie + 1) + " " + (int)(Math.round(activeUser[movie])));   //need to add 1
            }
        }


    }
     private int findMeanFor(int movie){
        double sum=0,count=0;
        
        for(int user=0;user<totalUsers;user++){
            if(ratings[user][movie]>0){
                sum+=ratings[user][movie];
                count++;
            }
        }
        
        return (int)Math.round(sum/count);
    }

    private double predict() {
        int j = 0;
        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < k && topK[i] != -1; i++) {
            int movie = topK[i];
             
            if(activeUser[movie]>0){     
               
                    numerator += similarity[movie] * (activeUser[movie]);
                
             
             denominator += Math.abs(similarity[movie]);
             }
                                                        
        }               
        return  (numerator / denominator);

    } 
    public static void main(String args[]) throws FileNotFoundException, IOException {
        ItemBasedRecommendationSystem r = new ItemBasedRecommendationSystem();
        r.predictItemBasedSimilarity("test20.txt", "result10.txt");
      
    }
}
