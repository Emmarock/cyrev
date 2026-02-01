package com.cyrev.iam.config;

import com.cyrev.iam.temporal.activity.AppProvisioningActivitiesImpl;
import com.cyrev.iam.temporal.activity.UserProvisioningActivities;
import com.cyrev.iam.temporal.workflow.AppProvisioningWorkflowImpl;
import com.cyrev.iam.temporal.workflow.UserProvisioningWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {

    @Value("${temporal.host:172.17.0.2}")
    private String temporalHost;

    @Value("${temporal.port:7233}")
    private int temporalPort;

    @Value("${temporal.namespace:temporal-system}")
    private String namespace;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalHost + ":" + temporalPort)
                .build();
        return WorkflowServiceStubs.newInstance(options);
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs, WorkflowClientOptions.newBuilder()
                .setNamespace(namespace)
                .build());
    }

    @Bean
    public WorkerFactory workerFactory(
            WorkflowClient client,
            AppProvisioningActivitiesImpl appProvisioningActivities,
            UserProvisioningActivities userProvisioningActivities // new activities bean
    ) {
        WorkerFactory factory = WorkerFactory.newInstance(client);

        // --- Worker for App Provisioning ---
        Worker appWorker = factory.newWorker("APP_PROVISION_TASK_QUEUE");
        appWorker.registerWorkflowImplementationTypes(AppProvisioningWorkflowImpl.class);
        appWorker.registerActivitiesImplementations(appProvisioningActivities);

        // --- Worker for User Provisioning ---
        Worker userWorker = factory.newWorker("USER_PROVISION_TASK_QUEUE");
        userWorker.registerWorkflowImplementationTypes(UserProvisioningWorkflowImpl.class);
        userWorker.registerActivitiesImplementations(userProvisioningActivities);

        factory.start();
        return factory;
    }



}
