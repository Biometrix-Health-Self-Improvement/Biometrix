package com.rocket.biometrix.Database;

/**
 * Created by Troy Riblett, troy.riblett@oit.edu
 * DatabaseConnectionTypes
 * This class is a container class to choose which type of operation should be performed by the
 * database
 */
public final class DatabaseConnectionTypes
{
    //References to different operations for the database
    public static final String LOGIN_CHECK = "Test User Login";
    public static final String LOGIN_CREATE = "Create User Login";
    public static final String LOGIN_DELETE = "Delete User Login";
    public static final String LOGIN_RESET = "Reset User Password";
    public static final String GOOGLE_TOKEN = "Verify Google Token";
    public static final String INSERT_TABLE_VALUES = "Insert Table Values";
    public static final String UPDATE_TABLE_VALUES = "Update Table Values";
    public static final String DELETE_TABLE_VALUES = "Delete Table Values";
    public static final String CONNECTION_FAIL = "Unable to connect to database";

    public static final String EXERCISE_TABLE = "Exercise";
    public static final String SLEEP_TABLE = "Sleep";
    public static final String MOOD_TABLE = "Mood";
    public static final String DIET_TABLE = "Diet";
    public static final String MEDICATION_TABLE = "Medication";

    private DatabaseConnectionTypes() {}
}
