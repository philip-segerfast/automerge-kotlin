package dev.psegerfast.automergekotlin.core

interface KRead {

    /**
     * Get a value from the map given by obj
     *
     * Note that if there are multiple conflicting values for the key this method
     * will arbirtarily return one of them. The choice will be deterministic, in the
     * sense that any other document with the same set of changes will return the
     * same value. To get all the values use [getAll]
     *
     * @param obj
     * - The ID of the map to get the value from
     * @param key
     * - The key to get the value for
     * @return The value of the key or `Optional.empty` if not present
     * @throws AutomergeException
     * if the object ID is not a map
     */
    operator fun get(obj: KObjectId, key: String): KAmValue?

    /**
     * Get a value from the map given by obj as at heads
     *
     * Note that if there are multiple conflicting values for the key this method
     * will arbirtarily return one of them. The choice will be deterministic, in the
     * sense that any other document with the same set of changes will return the
     * same value. To get all the values use [getAll]
     *
     * @param obj
     * - The ID of the map to get the value from
     * @param key
     * - The key to get the value for
     * @param heads
     * - The heads of the version of the document to get the value from
     * @return The value of the key or `Optional.empty` if not present
     * @throws AutomergeException
     * if the object ID is not a map
     */
    operator fun get(obj: KObjectId, key: String, heads: Array<KChangeHash>): KAmValue?

    /**
     * Get a value from the list given by obj
     *
     * Note that if there are multiple conflicting values for the index this method
     * will arbirtarily return one of them. The choice will be deterministic, in the
     * sense that any other document with the same set of changes will return the
     * same value. To get all the values use [getAll]
     *
     * @param obj
     * - The ID of the list to get the value from
     * @param index
     * - The index to get the value for
     * @return The value at the index or `Optional.empty` if the index is out of
     * range
     * @throws AutomergeException
     * if the object ID is not a list
     */
    operator fun get(obj: KObjectId, index: Int): KAmValue?

    /**
     * Get a value from the list given by obj as at heads
     *
     * Note that if there are multiple conflicting values for the index this method
     * will arbirtarily return one of them. The choice will be deterministic, in the
     * sense that any other document with the same set of changes will return the
     * same value. To get all the values use [getAll]
     *
     * @param obj
     * - The ID of the list to get the value from
     * @param index
     * - The index to get the value for
     * @param heads
     * - The heads of the version of the document to get the value from
     * @return The value at the index or `Optional.empty` if the index is out of
     * range
     * @throws AutomergeException
     * if the object ID is not a list
     */
    operator fun get(obj: KObjectId, index: Int, heads: Array<KChangeHash>): KAmValue?

    /**
     * Get all the possibly conflicting values for a key from the map given by obj
     *
     * If there are concurrent set operations to a key in a map there is no way to
     * resolve that conflict so automerge retains all concurrently set values which
     * can then be obtained via this method. If you don't care about conflicts and
     * just want to arbitrarily (but deterministically) choose a value use
     * [get]
     *
     * @param obj
     * - The ID of the map to get the value from
     * @param key
     * - The key to get the value for
     * @return The values
     * @throws AutomergeException
     * if the object ID refers to an object which is not a map
     */
    fun getAll(obj: KObjectId, key: String): KConflicts? {
        error("Unsupported method: getAll(obj: KObjectId, key: String): KConflicts?")
    }

    /**
     * Get all the possibly conflicting values for a key from the map given by obj
     * as at the given heads
     *
     * If there are concurrent set operations to a key in a map there is no way to
     * resolve that conflict so automerge retains all concurrently set values which
     * can then be obtained via this method. If you don't care about conflicts and
     * just want to arbitrarily (but deterministically) choose a value use
     * [get]
     *
     * @param obj
     * - The ID of the map to get the value from
     * @param key
     * - The key to get the value for
     * @param heads
     * - The heads of the version of the document to get the value from
     * @return The values
     * @throws AutomergeException
     * if the object ID refers to an object which is not a map
     */
    fun getAll(obj: KObjectId, key: String, heads: Array<KChangeHash>): KConflicts? {
        error("Unsupported method: getAll(obj: KObjectId, key: String, heads: Array<KChangeHash>): KConflicts?")
    }

    /**
     * Get all the possibly conflicting values for an index in the list given by obj
     *
     * If there are concurrent set operations to an index in a list there is no way
     * to resolve that conflict so automerge retains all concurrently set values
     * which can then be obtained via this method. If you don't care about conflicts
     * and just want to arbitrarily (but deterministically) choose a value use
     * [get]
     *
     * @param obj
     * - The ID of the map to get the value from
     * @param index
     * - The index to get the value for
     * @return The values
     * @throws AutomergeException
     * if the object ID refers to an object which is not a map
     */
    fun getAll(obj: KObjectId, index: Int): KConflicts? {
        error("Unsupported method: getAll(obj: KObjectId, index: Int): KConflicts?")
    }

    /**
     * Get all the possibly conflicting values for an index in the list given by obj
     * as at the given heads
     *
     * If there are concurrent set operations to an index in a list there is no way
     * to resolve that conflict so automerge retains all concurrently set values
     * which can then be obtained via this method. If you don't care about conflicts
     * and just want to arbitrarily (but deterministically) choose a value use
     * [get]
     *
     * @param obj
     * - The ID of the map to get the value from
     * @param index
     * - The index to get the value for
     * @param heads
     * - The heads of the version of the document to get the value from
     * @return The values
     * @throws AutomergeException
     * if the object ID refers to an object which is not a map
     */
    fun getAll(obj: KObjectId, index: Int, heads: Array<KChangeHash>): KConflicts? {
        error("Unsupported method: getAll(obj: KObjectId, index: Int, heads: Array<KChangeHash>): KConflicts?")
    }

    /**
     * Get the value of a text object
     *
     * @param obj - The ID of the text object to get the value from
     * @return The text or None if no such object exists
     * @throws AutomergeException
     * if the object ID refers to an object which is not a text object
     */
    fun text(obj: KObjectId): String?

    /**
     * Get the value of a text object as at the given heads
     *
     * @param obj - The ID of the text object to get the value from
     * @param heads - The heads of the version of the document to get the value from
     * @return The text or None if it does not exist
     * @throws AutomergeException
     * if the object ID refers to an object which is not a text object
     */
    fun text(obj: KObjectId, heads: Array<KChangeHash>): String?

    /**
     * Get the keys of the object given by obj
     *
     * @param obj
     * - The ID of the object to get the keys from
     * @return The keys of the object or None if the object is not a map
     */
    fun keys(obj: KObjectId): List<String>?

    /**
     * Get the keys of the object given by obj as at the given heads
     *
     * @param obj
     * - The ID of the object to get the keys from
     * @param heads
     * - The heads of the version of the document to get the keys from
     * @return The keys of the object or None if the object is not a map
     */
    fun keys(obj: KObjectId, heads: Array<KChangeHash>): List<String>?

    /**
     * Get the entries of the map given by obj
     *
     * @param obj
     * - The ID of the map to get the entries from
     * @return The entries of the map or None if the object is not a map
     */
    fun mapEntries(obj: KObjectId): List<KMapEntry>?

    /**
     * Get the entries of the map given by obj as at the given heads
     *
     * @param obj
     * - The ID of the map to get the entries from
     * @param heads
     * - The heads of the version of the document to get the entries from
     * @return The entries of the map or None if the object is not a map
     */
    fun mapEntries(obj: KObjectId, heads: Array<KChangeHash>): List<KMapEntry>?

    /**
     * Get the values in the list given by obj
     *
     * @param obj
     * - The ID of the list to get the values from
     * @return The values of the list or None if the object is not a list
     */
    fun listItems(obj: KObjectId): List<KAmValue>?

    /**
     * Get the values in the list given by obj as at the given heads
     *
     * @param obj
     * - The ID of the list to get the values from
     * @param heads
     * - The heads of the version of the document to get the values from
     * @return The values of the list or None if the object is not a list
     */
    fun listItems(obj: KObjectId, heads: Array<KChangeHash>): List<KAmValue>?

    /**
     * Get the length of the list given by obj
     *
     * @param obj
     * - The ID of the list to get the length of
     * @return The length of the list (this will be zero if the object is not a
     * list)
     */
    fun length(obj: KObjectId): Long

    /**
     * Get the length of the list given by obj as at the given heads
     *
     * @param obj
     * - The ID of the list to get the length of
     * @param heads
     * - The heads of the version of the document to get the length from
     * @return The length of the list (this will be zero if the object is not a
     * list)
     */
    fun length(obj: KObjectId, heads: Array<KChangeHash>): Long

    /**
     * Get the marks for the text object given by obj
     *
     * @param obj
     * - The ID of the text object to get the marks from
     * @return The marks of the text object or None if the object is not a text
     * object
     */
    fun marks(obj: KObjectId): List<KMark>?

    /**
     * Get the marks for the text object given by obj as at the given heads
     *
     * @param obj
     * - The ID of the text object to get the marks from
     * @param heads
     * - The heads of the version of the document to get the marks from
     * @return The marks of the text object or None if the object is not a text
     * object
     */
    fun marks(obj: KObjectId, heads: Array<KChangeHash>): List<KMark>?

    /**
     * Get the marks defined at the given index in a text object
     *
     * @param obj
     * - The ID of the text object to get the marks from
     * @param index
     * - The index to get the marks at
     * @return The marks at the given index or None if the object is not a text
     * object
     */
    fun getMarksAtIndex(obj: KObjectId, index: Int): Map<String, KAmValue>?

    /**
     * Get the marks defined at the given index in a text object
     *
     * @param obj
     * - The ID of the text object to get the marks from
     * @param index
     * - The index to get the marks at
     * @param heads
     * - The heads of the version of the document to get the marks from
     * @return The marks at the given index or None if the object is not a text
     * object
     */
    fun getMarksAtIndex(
        obj: KObjectId,
        index: Int,
        heads: Array<KChangeHash>
    ): Map<String, KAmValue>?

    /**
     * Get the heads of the object
     *
     * @return The heads of the document
     *
     * The returned heads represent the current version of the document and
     * can be passed to many other methods to refer to the document as at
     * this moment.
     */
    val heads: Array<KChangeHash>

    /**
     * Get a cursor which refers to the given index in a list or text object
     *
     * @param obj
     * - The ID of the list or text object to get the cursor for
     * @param index
     * - The index to get the cursor for
     *
     * @return The cursor
     *
     * @throws AutomergeException
     * if the object ID refers to an object which is not a list or text
     * object or if the index is out of range
     */
    fun makeCursor(obj: KObjectId, index: Long): KCursor

    /**
     * Get a cursor which refers to the given index in a list or text object as at
     * the given heads
     *
     * @param obj
     * - The ID of the list or text object to get the cursor for
     * @param index
     * - The index to get the cursor for
     * @param heads
     * - The heads of the version of the document to make the cursor from
     *
     * @return The cursor
     *
     * @throws AutomergeException
     * if the object ID refers to an object which is not a list or text
     * object or if the index is out of range
     */
    fun makeCursor(obj: KObjectId, index: Long, heads: Array<KChangeHash>): KCursor

    /**
     * Given a cursor for an object, get the index the cursor points at
     *
     * @param obj
     * - The ID of the object the cursor refers into
     * @param cursor
     * - The cursor
     *
     * @return The index the cursor points at
     * @throws AutomergeException
     * if the object ID refers to an object which is not a list or text
     * object or if the cursor does not refer to an element in the
     * object
     */
    fun lookupCursorIndex(obj: KObjectId, cursor: KCursor): Long

    /**
     * Given a cursor for an object, get the index the cursor points at as at the
     * given heads
     *
     * @param obj
     * - The ID of the object the cursor refers into
     * @param cursor
     * - The cursor
     * @param heads
     * - The heads of the version of the document to make the cursor from
     *
     * @return The index the cursor points at
     * @throws AutomergeException
     * if the object ID refers to an object which is not a list or text
     * object or if the cursor does not refer to an element in the
     * object
     */
    fun lookupCursorIndex(obj: KObjectId, cursor: KCursor, heads: Array<KChangeHash>): Long

    /**
     * Get the object type of the object given by obj
     *
     * @param obj
     * - The ID of the object to get the type of
     *
     * @return The type of the object or Optional.empty if the object does not exist
     * in this document
     */
    fun getObjectType(obj: KObjectId): KObjectType?

}