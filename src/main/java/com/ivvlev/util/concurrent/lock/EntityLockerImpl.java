package com.ivvlev.util.concurrent.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class is supposed to be used by the components that are responsible for managing storage and caching of different
 * type of entities in the application. EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities.
 * <p>
 * Requirements:
 * <p>
 * 1. EntityLocker should support different types of entity IDs.
 * <p>
 * 2. EntityLocker’s interface should allow the caller to specify which entity does it want to work with (using entity ID),
 * and designate the boundaries of the code that should have exclusive access to the entity (called “protected code”).
 * <p>
 * 3. For any given entity, EntityLocker should guarantee that at most one thread executes protected code on that entity.
 * If there’s a concurrent request to lock the same entity, the other thread should wait until the entity becomes available.
 * <p>
 * 4. EntityLocker should allow concurrent execution of protected code on different entities.
 * *
 * 5. Allow reentrant locking.
 * <p>
 * 6. Allow the caller to specify timeout for locking an entity.
 * <p>
 * 7. Implement protection from deadlocks (but not taking into account possible locks outside EntityLocker).
 */
public class EntityLockerImpl<K> extends EntityLockerAbst<K> {

    private final Map<K, Entry<K>> keyEntryMap_ = new ConcurrentHashMap<>();

    protected Entry<K> acquireEntry(K key, Transaction<K> tx, long timeout) {
        return keyEntryMap_.compute(key, (k, e) -> (e != null ? e : new Entry<K>()).acquire());
    }

    protected Entry<K> findEntry(K key) {
        return keyEntryMap_.get(key);
    }

    protected void releaseEntry(K key) {
        keyEntryMap_.computeIfPresent(key, (k, e) -> e.release());
    }

    protected int getEntryCount() {
        return keyEntryMap_.size();
    }


}