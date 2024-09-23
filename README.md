# jxlock

A simple distributed lock implementation using Azure Cosmos DB as the underlying synchronization method. It is suited for distributed applications or microservices projects that are already using Cosmos DB and have a need to synchronize access shared resources/services. If projects are already using Redis Cache, Redisson library is more suitable. This is intended mainly for teams that dont have a real need to invest into other resources like Redis, and only want to achieve some course synchronization.

The lock implementation implements java.util.concurrent. Currently it provides only non-blocking tryLock method. More capabilities will be added incrementally.
