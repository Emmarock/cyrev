package com.cyrev.iam.domain;

import com.cyrev.iam.entities.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScimUser {

    private String id;
    private String userName;
    private boolean active;
    private Name name;
    private List<String> emails;

    @Data
    @Builder
    public static class Name {
        private String givenName;
        private String familyName;
    }
    public static ScimUser from(User user) {
        return ScimUser.builder()
                .userName(user.getUsername())
                .active(user.getStatus()==UserStatus.ACTIVE)
                .name(Name.builder().givenName(user.getFirstName()).familyName(user.getLastName()).build())
                .emails(List.of(user.getEmail()))
                .build();
    }
}
