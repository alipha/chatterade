package com.liph.chatterade.common;

import java.util.HashMap;
import java.util.Map;


public class LockManager {

    private final Map<Object, LockCount> locks;

    public LockManager() {
        this.locks = new HashMap<>();
    }

    public synchronized Object get(Object lock) {
        LockCount existingLock = locks.get(lock);

        if(existingLock == null) {
            locks.put(lock, new LockCount(lock, 1));
            return lock;
        } else {
            existingLock.count++;
            return existingLock.lock;
        }
    }

    public synchronized void release(Object lock) {
        LockCount existingLock = locks.get(lock);

        if(existingLock == null)
            throw new RuntimeException("Lock release mismatch: " + lock);

        existingLock.count--;

        if(existingLock.count < 0)
            throw new RuntimeException("Lock count went negative: " + existingLock.lock);

        if(existingLock.count == 0)
            locks.remove(lock);
    }


    private static class LockCount {
        public Object lock;
        public int count;

        public LockCount(Object lock, int count) {
            this.lock = lock;
            this.count = count;
        }
    }
}
