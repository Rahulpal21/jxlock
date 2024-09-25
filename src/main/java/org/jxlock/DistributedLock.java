package org.jxlock;

import com.azure.cosmos.implementation.apachecommons.lang.NotImplementedException;
import org.jxlock.cosmosdb.CosmosDBDistributedLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
/**
 * @author Rahul Pal
 *
 */
public abstract class DistributedLock implements Lock {
    private static final String NOT_IMPLEMENTED_MESSAGE = "Method not implemented";

    public DistributedLock getInstance(){
        return new CosmosDBDistributedLock(null, null);
    }

    @Override
    public void lock() {
        throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
    }

    @Override
    public Condition newCondition() {
        throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
    }
}
