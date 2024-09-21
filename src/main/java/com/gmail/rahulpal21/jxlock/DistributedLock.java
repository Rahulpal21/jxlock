package com.gmail.rahulpal21.jxlock;

import com.gmail.rahulpal21.jxlock.cosmosdb.CosmosDBDistributedLock;

import java.util.concurrent.locks.Lock;
/**
 * @author Rahul Pal
 *
 */
public abstract class DistributedLock implements Lock {
    public DistributedLock getInstance(){
        return new CosmosDBDistributedLock(null, null);
    }
}
