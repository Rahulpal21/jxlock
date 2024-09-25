package org.jxlock.cosmosdb;

import lombok.Data;

import java.util.Date;

/**
 * @author Rahul Pal
 *
 */
@Data
public class CosmosLockRecord {
    private String id;
    private String lock_name;
    private long owner;
    private Boolean isAquired;
    private Date createdAt;
    private Date lastUpdatedAt;
    private String _etag;
}
