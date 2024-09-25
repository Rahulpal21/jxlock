package org.jxlock.cosmosdb;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.*;
import org.jxlock.DistributedLock;
import com.sun.jna.platform.win32.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Rahul Pal
 */
public class CosmosDBDistributedLock extends DistributedLock {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDBDistributedLock.class);
    private CosmosContainer container;
    private CosmosLockRecord lockRecord;
    private String uniqueName;
    private Long pid;

    private static CosmosItemRequestOptions readRequestOptions = new CosmosItemRequestOptions().setConsistencyLevel(ConsistencyLevel.STRONG);
    private static CosmosItemRequestOptions createRequestOptions = new CosmosItemRequestOptions().setConsistencyLevel(ConsistencyLevel.STRONG);

    public CosmosDBDistributedLock(String uniqueName, CosmosContainer container) {
        super();
        this.container = container;
        this.uniqueName = uniqueName;
        this.pid = ProcessHandle.current().pid();
        init();
    }

    private synchronized void init() {
        Optional<CosmosLockRecord> response = container.queryItems("select * from c where c.lock_name = '" + uniqueName + "' order by c.lock_name", new CosmosQueryRequestOptions(), CosmosLockRecord.class).stream().findFirst();
        if (response.isEmpty()) {
            LOGGER.debug("lock {} is not found in store. Creating a new lock instance..");
            CosmosLockRecord record = new CosmosLockRecord();
            record.setLock_name(uniqueName);
            record.setIsAquired(false);
            record.setId(Guid.GUID.newGuid().toGuidString());
            record.setOwner(pid);
            container.createItem(record, createRequestOptions);
            lockRecord = container.readItem(record.getId(), new PartitionKey(record.getLock_name()), readRequestOptions, CosmosLockRecord.class).getItem();
        } else {
            lockRecord = response.get();
        }
    }

    private CosmosPatchItemRequestOptions getCosmosPatchItemRequestOptions(String etag) {
        CosmosPatchItemRequestOptions patchItemRequestOptions = new CosmosPatchItemRequestOptions();
        patchItemRequestOptions.setConsistencyLevel(ConsistencyLevel.STRONG);
        patchItemRequestOptions.setIfMatchETag(etag);
        return patchItemRequestOptions;
    }

    @Override
    public synchronized boolean tryLock() {
        lockRecord = container.readItem(lockRecord.getId(), new PartitionKey(lockRecord.getLock_name()), readRequestOptions, CosmosLockRecord.class).getItem();
        if (lockRecord.getIsAquired()) {
            LOGGER.debug("lock {} is already aquired", lockRecord.getLock_name());
            return false;
        }
        CosmosPatchOperations patchOperations = CosmosPatchOperations.create().set("/isAquired", true).set("/owner", pid);
        container.patchItem(lockRecord.getId(), new PartitionKey(lockRecord.getLock_name()), patchOperations, getCosmosPatchItemRequestOptions(lockRecord.get_etag()), CosmosLockRecord.class);
        lockRecord = container.readItem(lockRecord.getId(), new PartitionKey(lockRecord.getLock_name()), readRequestOptions, CosmosLockRecord.class).getItem();
        return true;
    }

    @Override
    public synchronized void unlock() {
        lockRecord = container.readItem(lockRecord.getId(), new PartitionKey(lockRecord.getLock_name()), readRequestOptions, CosmosLockRecord.class).getItem();
        if (lockRecord.getOwner() != pid) {
            LOGGER.warn("Current process is not owning lock {}, unlock is not allowed", lockRecord.getLock_name());
        }
        CosmosPatchOperations patchOperations = CosmosPatchOperations.create().set("/isAquired", false).set("/owner", null);
        container.patchItem(lockRecord.getId(), new PartitionKey(lockRecord.getLock_name()), patchOperations, getCosmosPatchItemRequestOptions(lockRecord.get_etag()), CosmosLockRecord.class);
        lockRecord = container.readItem(lockRecord.getId(), new PartitionKey(lockRecord.getLock_name()), readRequestOptions, CosmosLockRecord.class).getItem();
    }
}
