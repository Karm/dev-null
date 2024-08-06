package org.acme.lambda.config;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class QueueClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(QueueClientConfig.class);

    @ConfigProperty(name = "app.env.azure.storage.connectionString")
    String connectStr;

    @ConfigProperty(name = "app.env.azure.storage.queueName")
    String queueName;

    private QueueClient queueClient;

    @Produces
    public QueueClient queueClient() {
        if (queueClient == null) {
            logger.info("Initializing Queue Client..!!");
            queueClient = new QueueClientBuilder()
                    .connectionString(connectStr)
                    .queueName(queueName)
                    .buildClient();
            queueClient.createIfNotExists();
        }
        logger.info("Successfully Initialized {} ", queueClient.getQueueUrl());
        return queueClient;
    }
/*
    @Produces
    public QueueClient queueClient() {
        return new QueueClient();
    }
    public class QueueClient {
public QueueClient sendMessage(String encodeToString) {
    return this;
}
public String getInsertionTime() {
    return "";
}
    }
*/
}
