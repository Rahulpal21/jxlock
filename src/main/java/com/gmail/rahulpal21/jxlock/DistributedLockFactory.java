package com.gmail.rahulpal21.jxlock;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.*;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.gmail.rahulpal21.jxlock.cosmosdb.CosmosDBDistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DistributedLockFactory {
    private static final Logger logger = LoggerFactory.getLogger(DistributedLockFactory.class);
    private static CosmosClient cosmosClient;
    private static CosmosDatabase lockDatabase;
    private static CosmosContainer lockContainer;
    private static String uri;
    private static String databaseName = "jxlock-db"; //default
    private static String containerName = "lock"; //default
    private static String PARTITION_KEY = "/lock_name"; //default
    private static final String AZURE_COSMOS_URI = "azure.cosmos.uri";
    private static final String AZURE_COSMOS_DB = "azure.cosmos.db";
    private static final String AZURE_COSMOS_CONTAINER = "azure.cosmos.container";
    private static final String AZURE_COSMOS_KEY = "azure.cosmos.key";

    static {
        String u = System.getProperty(AZURE_COSMOS_URI);
        if (u == null || u.isEmpty() || u.isBlank()) {
            u = System.getenv("AZURE_COSMOS_URI");
        }
        if (u != null && u.length() > 0) {
            uri = u;
        }
        if (uri == null) {
            logger.error("cosmos uri could not be resolved");
            throw new RuntimeException();
        }

        String d = System.getProperty(AZURE_COSMOS_DB);
        if (d == null || d.isEmpty() || d.isBlank()) {
            d = System.getenv("AZURE_COSMOS_DB");
        }
        if (d != null && d.length() > 0) {
            databaseName = d;
        } else {
            logger.info("using defaults for cosmos db name");
        }

        String c = System.getProperty(AZURE_COSMOS_CONTAINER);
        if (c == null || c.isEmpty() || c.isBlank()) {
            c = System.getenv("AZURE_COSMOS_CONTAINER");
        }
        if (c != null && c.length() > 0) {
            containerName = c;
        } else {
            logger.info("using defaults for cosmos container name");
        }

        CosmosClientBuilder builder = new CosmosClientBuilder().endpoint(uri);

        String key = System.getenv("AZURE_COSMOS_KEY");
        if (key != null) {
            logger.info("cosmos key is provided, proceeding with key based authentication");
            builder.credential(new AzureKeyCredential(key));
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        cosmosClient = builder.buildClient();

        try {
            lockDatabase = cosmosClient.getDatabase(databaseName);
            lockDatabase.read();
        } catch (NotFoundException notFound) {
            cosmosClient.createDatabase(databaseName);
            lockDatabase = cosmosClient.getDatabase(databaseName);
            lockDatabase.read();
            logger.info("created a new cosmosdb {}", databaseName);
        }

        try {
            lockContainer = lockDatabase.getContainer(containerName);
            lockContainer.read();
        } catch (CosmosException notFound) {
            if (notFound.getStatusCode() == 404) {
                lockDatabase.createContainer(containerName, PARTITION_KEY);
                lockContainer = lockDatabase.getContainer(containerName);
                lockContainer.read();
                logger.info("created a new container {}", containerName);
            }else{
                throw notFound;
            }
        }
    }

    public static DistributedLock getLock(String name) {
        return new CosmosDBDistributedLock(name, lockContainer);
    }
}
