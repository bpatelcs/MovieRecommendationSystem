
package MovieRecommendation;

import java.util.Comparator;




public class User {
public Double similarity;
public int name;

public User(double similarity,int name){
    this.similarity=similarity;
    this.name=name;
}

public static Comparator<User> BySimilarity=new BySimilarity();
        
 private static class BySimilarity implements Comparator<User>{

        public int compare(User o1, User o2) {
            return o1.similarity.compareTo(o2.similarity);
            
        }
    
}       
    
    
    
    
}
