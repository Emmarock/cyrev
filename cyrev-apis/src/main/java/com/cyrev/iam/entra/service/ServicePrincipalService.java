package com.cyrev.iam.entra.service;

import com.microsoft.graph.models.ServicePrincipal;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicePrincipalService {

    private final GraphServiceClient<?> graphClient;

    public ServicePrincipal createServicePrincipal(String appId) {
        ServicePrincipal sp = new ServicePrincipal();
        sp.appId = appId;
        return graphClient.servicePrincipals()
                .buildRequest()
                .post(sp);
    }

    public void deleteServicePrincipal(String spId) {
        graphClient.servicePrincipals(spId)
                .buildRequest()
                .delete();
    }
}
