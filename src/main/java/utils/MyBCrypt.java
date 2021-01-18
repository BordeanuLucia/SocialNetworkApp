package utils;

import org.mindrot.jbcrypt.BCrypt;

public class MyBCrypt {

    public static String hash(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static Boolean verifyHash(String password, String hash){
        return BCrypt.checkpw(password, hash);
    }
}
