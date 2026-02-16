package com.cyrev.iam.entra.service;

import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntraUserService {

    private final GraphServiceClient<?> graphClient;

    public User createUser(String displayName, String mailNickname, String userPrincipalName, String tempPassword) {
        User user = new User();
        user.displayName = displayName;
        user.mailNickname = mailNickname;
        user.userPrincipalName = userPrincipalName;

        PasswordProfile pwd = new PasswordProfile();
        pwd.password = tempPassword;
        pwd.forceChangePasswordNextSignIn = true;
        user.passwordProfile = pwd;

        return graphClient.users()
                .buildRequest()
                .post(user);
    }

    public void deleteUser(String userId) {
        graphClient.users(userId)
                .buildRequest()
                .delete();
    }

    public void listUsers() {
        graphClient.users()
                .buildRequest()
                .get()
                .getCurrentPage()
                .forEach(u -> System.out.println(u.displayName + " | " + u.userPrincipalName));
    }
}
