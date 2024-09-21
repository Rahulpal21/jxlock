package com.gmail.rahulpal21.jxlock.cosmosdb;

import lombok.Data;

import java.util.Date;

@Data
public class CosmosLockRecord {
    private String id;
    private String lock_name;
    private Boolean isAquired;
    private Date createdAt;
    private Date lastUpdatedAt;
    private String _etag;
}
