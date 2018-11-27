package com.kaori.kaori;

import com.kaori.kaori.DBObjects.User;

/**
 * Singleton class that represent a store
 * for the most important and common data.
 */
public class DataHub {

    private static DataHub dataHub;

    private boolean isAuthenticated;
    private User user;

    private DataHub(){
        isAuthenticated = false;
        user = new User();
    }

    public static DataHub getInstance(){
        if(dataHub == null)
            dataHub = new DataHub();
        return dataHub;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
