##Movie Recommendation System

This project was developed as a part of assignment given during Web Search Information Retrieval Class.We were supposed to
use different techniques learned during the class to predict the ratings of the movie.

#####`Train.txt` This file contains the ratings of 200 users for 1000 movies.Some users might not have rated some movie.

#####`Test5.txt` This file contains users having predicted 5 movies and based on that 5 ratings and ratings of that 200 users we had to predict the rating for the movies for which there is no ratings in this file.For more understanding look test5.txt.

#####`text10.txt` This file contatins the users having rated 10 movies we had to predict the rating for movie for which rating is missing.

#####`text20.txt` This file contating the users having rated 20 movies we had to predict the rating for movie for which ratings is missing.


#####`user.java` This file represents the object user which is used to store the similarity of user with particular to active user

#####`CosineRecommendationSystem.java` This code predicts the ratings for active user with missing rating using basic cosine similarity.This is not the best technique as it doen't consider the user bias during the calculation of similarity.

#####`PearsonRecommendationSystem.java` This code predicts the ratings for active user with missing rating using pearson Correalation.
#####`AdvancedPearsonRecommendationSystem.java` This code predicts the rating using Pearson Corrleation and Inverse User Frequency and Case Amplification Technique.

#####`ItemBasedRecommendationSystem.java` This code predicts the rating using item-tem similarity.This is not the best method as training matrix is very sparse.




