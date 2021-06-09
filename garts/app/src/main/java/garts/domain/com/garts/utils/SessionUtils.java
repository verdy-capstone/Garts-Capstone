package garts.domain.com.garts.utils;

import com.parse.ParseUser;

public class SessionUtils {

    public static boolean isUserLoggedIn() {
        return ParseUser.getCurrentUser().getUsername() != null;
    }
}
