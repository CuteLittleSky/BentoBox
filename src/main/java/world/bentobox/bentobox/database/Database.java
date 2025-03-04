package world.bentobox.bentobox.database;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;

/**
 * Handy class to store and load Java POJOs in the Database
 * @author tastybento
 *
 * @param <T>
 */
public class Database<T> {

    private final AbstractDatabaseHandler<T> handler;
    private final Logger logger;
    private static DatabaseSetup databaseSetup = DatabaseSetup.getDatabase();

    /**
     * Construct a database
     * @param plugin - plugin
     * @param type - to store this type
     */
    public Database(BentoBox plugin, Class<T> type)  {
        this.logger = plugin.getLogger();
        handler = databaseSetup.getHandler(type);
    }

    /**
     * Construct a database
     * @param addon - addon requesting
     * @param type - to store this type
     */
    public Database(Addon addon, Class<T> type)  {
        this.logger = addon.getLogger();
        handler = databaseSetup.getHandler(type);
    }

    /**
     * Load all the config objects and supply them as a list
     * @return list of config objects or an empty list if they cannot be loaded
     */
    @NonNull
    public List<T> loadObjects() {
        List<T> result = new ArrayList<>();
        try {
            result = handler.loadObjects();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | ClassNotFoundException | IntrospectionException
                | NoSuchMethodException | SecurityException e) {
            logger.severe(() -> "Could not load objects from database! Error: " + e.getMessage());
        }
        return result;
    }

    /**
     * Loads the config object
     * @param uniqueId - unique id of the object
     * @return the object or null if it cannot be loaded
     */
    @Nullable
    public T loadObject(String uniqueId) {
        T result = null;
        try {
            result = handler.loadObject(uniqueId);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | ClassNotFoundException | IntrospectionException | SecurityException e) {
            logger.severe(() -> "Could not load object from database! " + e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.severe(() -> "Could not load object from database! " + e.getMessage());
            logger.severe(() -> "Did you forget the JavaBean no-arg default constructor?");
        }
        return result;
    }

    /**
     * Save object async. Saving may be done sync, depending on the underlying database.
     * @param instance to save
     * @return true if no immediate errors. If async, errors may occur later.
     * @since 1.13.0
     */
    public CompletableFuture<Boolean> saveObjectAsync(T instance) {
        try {
            return handler.saveObject(instance);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
                | IntrospectionException e) {
            logger.severe(() -> "Could not save object to database! Error: " + e.getMessage());
            return new CompletableFuture<>();
        }
    }

    /**
     * Save object. Saving may be done async or sync, depending on the underlying database.
     * @param instance to save
     * @return true - always.
     * @deprecated As of 1.13.0. Use {@link #saveObjectAsync(Object)}.
     */
    @Deprecated
    public boolean saveObject(T instance) {
        saveObjectAsync(instance).thenAccept(r -> {
            if (Boolean.FALSE.equals(r)) logger.severe(() -> "Could not save object to database!");
        });
        return true;
    }

    /**
     * Checks if a config object exists or not
     * @param name - unique name of the config object
     * @return true if it exists
     */
    public boolean objectExists(String name) {
        return handler.objectExists(name);
    }

    /**
     * Attempts to delete the object with the uniqueId
     * @param uniqueId - uniqueId of object
     * @since 1.1
     */
    public void deleteID(String uniqueId) {
        handler.deleteID(uniqueId);
    }

    /**
     * Delete object from database
     * @param object - object to delete
     */
    public void deleteObject(T object) {
        try {
            handler.deleteObject(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | IntrospectionException e) {
            logger.severe(() -> "Could not delete object! Error: " + e.getMessage());
        }
    }

    /**
     * Close the database
     */
    public void close() {
        handler.close();
    }



}
