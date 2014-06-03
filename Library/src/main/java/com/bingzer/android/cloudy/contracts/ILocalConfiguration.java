package com.bingzer.android.cloudy.contracts;

/**
 * Represents local configuration.
 * This table should not be synced to remote
 */
public interface ILocalConfiguration extends ISystemEntity {
    String TABLE_NAME = "LocalConfiguration";

    /**
     * Sets the setting name
     * @param name the name of the setting
     */
    void setName(String name);

    /**
     * Returns the name of the setting
     * @return the name of the setting
     */
    String getName();

    /**
     * Sets the value
     * @param value the value
     */
    void setValue(String value);

    /**
     * Returns the value
     * @return the value
     */
    String getValue();

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets value as long value
     */
    void setValue(long value);

    /**
     * Returns value as Long
     */
    long getValueAsLong();

    /**
     * Sets a boolean value
     */
    void setValue(boolean value);

    /**
     * Returns value as boolean
     */
    boolean getValueAsBoolean();

    /**
     * Sets an integer value
     */
    void setValue(int value);

    /**
     * Returns value as Integer
     */
    int getValueAsInteger();

    /**
     * Sets a double value
     */
    void setValue(double value);

    /**
     * Returns value as double
     */
    double getValueAsDouble();

}
