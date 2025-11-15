package com.randallscharpf.java.jconfigfile;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * Key-value store to hold a program configuration.
 * 
 * Supports create, read, update, and delete operations.
 * <ul>
 * <li>
 * A key-value pair can be created by first checking if the associated key exists
 * against the {@code Set} returned by {@code getKeys} and then, if it is not found,
 * adding the key-value pair with {@code setKey}.
 * </li>
 * <li>
 * A key-value pair can be read with {@code getKeyOrDefault}. If a user needs to
 * determine whether a particular key exists and cannot determine a default value
 * which will never be held by that key, they must check against the set returned
 * by {@code getKeys}.
 * </li>
 * <li>
 * A key-value pair can be updated by first checking if the associated key exists
 * against the {@code Set} returned by {@code getKeys} and then, if it is found,
 * updating the key-value pair with {@code setKey}.
 * </li>
 * <li>
 * A key-value pair can be removed with {@code removeKey}.
 * </li>
 * </ul>
 * 
 * Implementations may back the interface with data storage mechanisms having various
 * levels of persistence. For example, some implementations use a file that maintains
 * configuration settings through system resets, while others use program RAM and
 * have their configuration reset on each execution. The {@code save} method ensures
 * that cached changes are pushed through to the strongest level of persistence, at
 * the expense of a potential processing or I/O delays. The {@code close} method
 * (inherited from {@code Closeable}) is expected to both ensure cached changes are
 * saved to persistent memory as in the {@code save} method and to release any resources
 * used to control the memory for the key-value store.
 * 
 * The configuration only allows strings to be used as keys to be used as keys and
 * values. Application-specific conversion of key-value pairs to and from objects
 * must be handled externally.
 */
public interface Config extends Closeable {

    /**
     * Creates or updates a key-value pair.
     * 
     * If the key does not yet exist in the configuration, this method creates a
     * new key-value pair. If the key already exists, this method updates the value
     * associated with the specified key.
     * 
     * @param key new or existing key for the configuration
     * @param value value to associate with the key
     */
    public void setKey(String key, String value);
    
    /**
     * Returns the value associated with the key in the configuration, if it exists,
     * or a fallback if it does not exist.
     * 
     * @param key the key to look up in the configuration
     * @param fallback the string to return if the key is not found
     * @return the value associated with the key, if it is found, or the fallback otherwise
     */
    public String getKeyOrDefault(String key, String fallback);

    /**
     * Returns a {@link Set} view of the keys held by the configuration.
     * 
     * The semantics of the returned set are the same as the semantics of
     * {@link java.util.Map#keySet}.
     * 
     * @return a set view of the keys held by the configuration
     */
    public Set<String> getKeys();

    /**
     * Removes the key-value pair associated with the specified key.
     * 
     * If the key is not present in the configuration, this method does nothing.
     * 
     * @param key the key to lookup and remove
     */
    public void removeKey(String key);

    /**
     * Ensure that cached changes are pushed through to the strongest level of persistence.
     * 
     * This process may require significant computation or I/O. Implementations must
     * ensure that the process can be called from an auxiliary thread. Users should
     * not call this method from a thread where a temporary blocking delay is
     * unacceptable, especially for configurations with a large number of key-value
     * pairs.
     * 
     * @throws IOException if an I/O error occurs while pushing changes to persistent memory
     */
    public void save() throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException;
    
}
