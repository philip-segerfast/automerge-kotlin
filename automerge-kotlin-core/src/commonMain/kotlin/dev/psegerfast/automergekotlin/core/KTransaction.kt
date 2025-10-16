package dev.psegerfast.automergekotlin.core

import kotlinx.datetime.Instant
import java.util.Optional

interface KWrite {
    /**
     * Set a string value in a map
     *
     * @param obj the object id of the map
     * @param key the key in the map
     * @param value the string to set
     * @throws AutomergeException
     * if the object is not a map
     */
    operator fun set(obj: KObjectId, key: String, value: String)

    /**
     * Set a string value in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the string to set
     * @throws AutomergeException
     * if the object is not a list
     */
    operator fun set(obj: KObjectId, index: Long, value: String)

    /**
     * Set a double value in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @param value
     * the double to set
     * @throws AutomergeException
     * if the object is not a map
     */
    operator fun set(obj: KObjectId, key: String, value: Double)

    /**
     * Set a double value in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list to set
     * @param value
     * the double to set
     * @throws AutomergeException
     * if the object is not a list
     */
    operator fun set(obj: KObjectId, index: Long, value: Double)

    /**
     * Set an int value in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list to set
     * @param value
     * the int to set
     * @throws AutomergeException
     * if the object is not a list
     */
    operator fun set(obj: KObjectId, index: Long, value: Int)

    /**
     * Set an int value in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @param value
     * the int to set
     * @throws AutomergeException
     * if the object is not a map
     */
    operator fun set(obj: KObjectId, key: String, value: Int)

    /**
     * Set any non-object value in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @param value
     * the [KNewValue] to set
     * @throws AutomergeException
     * if the object is not a map
     */
    operator fun set(obj: KObjectId, key: String, value: KNewValue)

    /**
     * Set any non-object value in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the [KNewValue] to set
     * @throws AutomergeException
     * if the object is not a list
     */
    operator fun set(obj: KObjectId, index: Long, value: KNewValue)

    /**
     * Set a byte array in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @param value
     * the bytes to set
     * @throws AutomergeException
     * if the object is not a map
     */
    operator fun set(obj: KObjectId, key: String, value: ByteArray)

    /**
     * Set a byte array in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the bytes to set
     * @throws AutomergeException
     * if the object is not a list
     */
    operator fun set(obj: KObjectId, index: Long, value: ByteArray)

    /**
     * Set a boolean in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @param value
     * the boolean to set
     * @throws AutomergeException
     * if the object is not a map
     */
    operator fun set(obj: KObjectId, key: String, value: Boolean)

    /**
     * Set a boolean in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the boolean to set
     * @throws AutomergeException
     * if the object is not a list
     */
    operator fun set(obj: KObjectId, index: Long, value: Boolean)

    /**
     * Set a counter in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @param value
     * the counter to set
     * @throws AutomergeException
     * if the object is not a map
     */
    @Deprecated("Doesn't work for Java. Cannot instantiate a counter; it's package-private.")
    operator fun set(obj: KObjectId, key: String, value: KCounter) {
        error("Unsupported method: set(obj: KObjectId, key: String, value: KCounter)")
    }

    /**
     * Set a counter in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the counter to set
     * @throws AutomergeException
     * if the object is not a list
     */
    @Deprecated("Doesn't work for Java. Cannot instantiate a counter; it's package-private.")
    operator fun set(obj: KObjectId, index: Long, value: KCounter) {
        error("Unsupported method: set(obj: KObjectId, key: String, value: KCounter)")
    }

    /**
     * Set a date in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @param value
     * the date to set
     * @throws AutomergeException
     * if the object is not a map
     */
    operator fun set(obj: KObjectId, key: String, value: Instant)

    /**
     * Set a date in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the date to set
     * @throws AutomergeException
     * if the object is not a list
     */
    operator fun set(obj: KObjectId, index: Long, value: Instant)

    /**
     * Set an unsigned integer in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @param value
     * the integer to set
     * @throws AutomergeException
     * if the object is not a map
     * @throws IllegalArgumentException
     * if the value is negative
     */
    fun setUint(obj: KObjectId, key: String, value: Long)

    /**
     * Set an unsigned integer in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the integer to set
     * @throws AutomergeException
     * if the object is not a list
     * @throws IllegalArgumentException
     * if the value is negative
     */
    fun setUint(obj: KObjectId, index: Long, value: Long)

    /**
     * Set a key in a map to null
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map
     * @throws AutomergeException
     * if the object is not a map
     * @throws IllegalArgumentException
     * if the value is negative
     */
    fun setNull(obj: KObjectId, key: String)

    /**
     * Set an index in a list to null
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @throws AutomergeException
     * if the object is not a map
     * @throws IllegalArgumentException
     * if the value is negative
     */
    fun setNull(obj: KObjectId, index: Long)

    /**
     * Create a new object at the given key in a map
     *
     * @param parent
     * the object id of the map to set the key in
     * @param key
     * the key in the map
     * @param objType
     * the type of object to create
     * @return the object id of the new object
     * @throws AutomergeException
     * if the object is not a map
     */
    fun set(parent: KObjectId, key: String, objType: KObjectType): KObjectId

    /**
     * Create a new object at the given index in a list
     *
     * @param parent
     * the object id of the list to set inside
     * @param index
     * the index in the list
     * @param objType
     * the type of object to create
     * @return the object id of the new object
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun set(parent: KObjectId, index: Long, objType: KObjectType): KObjectId

    /**
     * Insert a double into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the double to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insert(obj: KObjectId, index: Long, value: Double)

    /**
     * Insert a string into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the string to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insert(obj: KObjectId, index: Long, value: String)

    /**
     * Insert an int into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the integer to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insert(obj: KObjectId, index: Long, value: Int)

    /**
     * Insert a byte array into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the bytes to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insert(obj: KObjectId, index: Long, value: ByteArray)

    /**
     * Insert a counter into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the counter to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    @Deprecated("Doesn't work for Java. Cannot instantiate a counter; it's package-private.")
    fun insert(obj: KObjectId, index: Long, value: KCounter) {
        error("Unsupported method: insert(obj: KObjectId, index: Long, value: KCounter)")
    }

    /**
     * Insert a date into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the date to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insert(obj: KObjectId, index: Long, value: Instant)

    /**
     * Insert a boolean into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the boolean to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insert(obj: KObjectId, index: Long, value: Boolean)

    /**
     * Insert any non-object value into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the new value to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insert(obj: KObjectId, index: Long, value: KNewValue)

    /**
     * Insert a new object in a list
     *
     * @param parent
     * the object id of the list to insert into
     * @param index
     * the index in the list
     * @param objType
     * the object type to insert
     * @return the object id of the new object
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insert(parent: KObjectId, index: Long, objType: KObjectType): KObjectId

    /**
     * Insert a null into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     */
    fun insertNull(obj: KObjectId, index: Long)

    /**
     * Insert an unsigned integer into a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list
     * @param value
     * the integer to insert
     * @throws AutomergeException
     * if the object is not a list or the index is out of range
     * @throws IllegalArgumentException
     * if the value is negative
     */
    fun insertUint(obj: KObjectId, index: Long, value: Long)

    /**
     * Increment a counter in a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key in the map where the counter is
     * @param amount
     * the amount to increment by
     * @throws AutomergeException
     * if the object is not a map or the key is not a counter
     */
    fun increment(obj: KObjectId, key: String, amount: Long)

    /**
     * Increment a counter in a list
     *
     * @param obj
     * the object id of the list
     * @param index
     * the index in the list where the counter is
     * @param amount
     * the amount to increment by
     * @throws AutomergeException
     * if the object is not a list or the index is not a counter
     */
    fun increment(obj: KObjectId, index: Long, amount: Long)

    /**
     * Delete a key from a map
     *
     * @param obj
     * the object id of the map
     * @param key
     * the key to delete
     * @throws AutomergeException
     * if the object is not a map or the key does not exist
     */
    fun delete(obj: KObjectId, key: String)

    /**
     * Delete an element from a list
     *
     * @param obj
     * the object id of the map
     * @param index
     * the index of the element to delete
     * @throws AutomergeException
     * if the object is not a list or the index is out of bounds
     */
    fun delete(obj: KObjectId, index: Long)

    /**
     * Splice multiple non-object values into a list
     *
     * @param obj
     * the object id of the list
     * @param start
     * the index in the list to start splicing
     * @param deleteCount
     * the number of elements to delete
     * @param items
     * the new values to insert
     * @throws AutomergeException
     * if the object is not a list or the start index is out of range
     */
    fun splice(obj: KObjectId, start: Long, deleteCount: Long, items: List<KNewValue>)

    /**
     * Splice text into a text object
     *
     * @param obj
     * the object id of the text object
     * @param start
     * the index in the text to start splicing
     * @param deleteCount
     * the number of characters to delete
     * @param text
     * the new text to insert
     * @throws AutomergeException
     * if the object is not a text object or the start index is out of
     * range
     */
    fun spliceText(obj: KObjectId, start: Long, deleteCount: Long, text: String)

    /**
     * Create a mark
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the value to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: KNewValue,
        expand: KExpandMark
    )

    /**
     * Create a mark with a string value
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the string to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: String,
        expand: KExpandMark
    )

    /**
     * Create a mark with an integer value
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the integer to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Long,
        expand: KExpandMark
    )

    /**
     * Create a mark with an unsigned integer value
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the integer to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     * @throws IllegalArgumentException
     * if the value is negative
     */
    fun markUint(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Long,
        expand: KExpandMark
    )

    /**
     * Create a mark with a double value
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the double to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Double,
        expand: KExpandMark
    )

    /**
     * Create a mark with a byte array
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the byte array to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: ByteArray,
        expand: KExpandMark
    )

    /**
     * Create a mark with a counter value
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the counter to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    @Deprecated("Doesn't work for Java. Cannot instantiate a counter; it's package-private.")
    fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: KCounter,
        expand: KExpandMark
    ) {
        error("Unsupported method: mark(KObjectId, Long, Long, String, KCounter, KExpandMark)")
    }

    /**
     * Create a mark with an [Instant] value
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the date to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Instant,
        expand: KExpandMark
    )

    /**
     * Create a mark with a boolean value
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param value
     * the boolean to associate with the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    fun mark(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        value: Boolean,
        expand: KExpandMark
    )

    /**
     * Create a mark with a null value
     *
     * @param obj
     * the object id of the text object to create the mark on
     * @param start
     * the index in the text object to start the mark at
     * @param end
     * the index in the text object to end the mark at
     * @param markName
     * the name of the mark
     * @param expand
     * how to expand the mark
     * @throws AutomergeException
     * if the object is not a text object or the start or end index is
     * out of range
     */
    fun markNull(
        obj: KObjectId,
        start: Long,
        end: Long,
        markName: String,
        expand: KExpandMark
    )

    /**
     * remove a mark from a range of characters in a text object
     *
     * @param obj
     * the object id of the text object to remove the mark from
     * @param start
     * the index in the text object to start removing from
     * @param end
     * the index in the text object to end removing at
     * @param markName
     * the name of the mark to remove
     * @param expand
     * how the removed span should expand
     */
    fun unmark(obj: KObjectId, markName: String, start: Long, end: Long, expand: KExpandMark)
}

interface KTransaction : KReadable, KWrite, AutoCloseable {

    /**
     * Commit the transaction
     *
     * Once a transaction has been committed any attempt to use it will throw an
     * exception
     *
     * @return the result of the commit or [Optional.empty] if the transaction made no changes
     */
    fun commit(): KChangeHash?

    /**
     * Close the transaction and reverse any changes made
     *
     * Once a transaction has been closed any attempt to use it will throw an
     * exception
     */
    fun rollback()

    override fun close()
    
}
