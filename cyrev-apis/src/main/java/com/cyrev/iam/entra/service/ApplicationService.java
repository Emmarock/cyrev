package com.cyrev.iam.entra.service;

import com.microsoft.graph.models.Application;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final GraphServiceClient<?> graphClient;

    public Application createApplication(String displayName) {
        Application app = new Application();
        app.displayName = displayName;
        return graphClient.applications()
                .buildRequest()
                .post(app);
    }

    public void deleteApplication(String appId) {
        graphClient.applications(appId)
                .buildRequest()
                .delete();
    }
}
