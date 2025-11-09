package com.yousif.attemp2;

/**
 * UserBaseController - Base interface for all user portal controllers
 * Allows sharing common functionality and passing user data to controllers
 */
public interface UserBaseController {
    /**
     * Sets the user data for the controller
     * @param userData The user data to set
     */
    void setUserData(LoginController.UserData userData);
} 