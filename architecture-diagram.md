---
title: High-Level IAM System Design
config:
  layout: elk
---
flowchart LR
Clients["External Clients"]
Gateway["Security Gateway"]
API["IAM API"]
Services["Application Services"]
Data[("PostgreSQL")]
Graph["Microsoft Graph Clients"]
Adapters["Third-Party Adapters"]
Notifications["Notification Services"]
Identity["Microsoft Entra ID"]
Exchange["Exchange Online"]
Integrations["External Integrations"]

    Clients -->|"REST, OAuth, SCIM"| Gateway
    Gateway -->|"Authenticated requests"| API
    API --> Services

    Services --> Data
    Services --> Graph
    Services --> Adapters
    Services --> Notifications

    Graph --> Identity
    Graph --> Exchange
    Adapters --> Integrations
    Notifications --> Integrations

    subgraph C["Client Layer"]
        Clients
        Frontend["Web Frontend"]
        HR["HR / IdP SCIM Source"]
        OAuth["OAuth Providers"]
        Frontend --> Clients
        HR --> Clients
        OAuth --> Clients
    end

    subgraph G["Security Layer"]
        Gateway
        JWT["JWT Authentication"]
        Tenant["Tenant Context"]
        Scim["SCIM Authentication"]
        Gateway --> JWT
        Gateway --> Tenant
        Gateway --> Scim
    end

    subgraph A["Application Layer"]
        API
        Core["Core IAM APIs"]
        EntraAPI["Entra Governance APIs"]
        ScimAPI["SCIM v2 APIs"]
        API --> Core
        API --> EntraAPI
        API --> ScimAPI
    end

    subgraph S["Service Layer"]
        Services
        Auth["Authentication & Identity"]
        User["User, Organization & Tenant Management"]
        Governance["Governance & Approvals"]
        Provisioning["Provisioning & Reconciliation"]
        Services --> Auth
        Services --> User
        Services --> Governance
        Services --> Provisioning
    end

    subgraph X["Integration Layer"]
        Graph
        Adapters
        Notifications
    end

    subgraph E["External Systems"]
        Identity
        Exchange
        Integrations
        SendGrid["SendGrid"]
        Slack["Slack"]
        Atlassian["Atlassian SCIM"]
        Bitbucket["Bitbucket"]
        Automation["Azure Automation"]
        Integrations --> SendGrid
        Integrations --> Slack
        Integrations --> Atlassian
        Integrations --> Bitbucket
        Integrations --> Automation
    end

    classDef client fill:#eef2ff,stroke:#818cf8,color:#1e1b4b
    classDef security fill:#fefce8,stroke:#facc15,color:#713f12
    classDef api fill:#f0fdf4,stroke:#4ade80,color:#14532d
    classDef service fill:#f5f3ff,stroke:#a78bfa,color:#4c1d95
    classDef integration fill:#ecfeff,stroke:#22d3ee,color:#164e63
    classDef data fill:#f1f5f9,stroke:#475569,color:#1e293b
    classDef external fill:#fff1f2,stroke:#fb7185,color:#881337

    class Clients,Frontend,HR,OAuth client
    class Gateway,JWT,Tenant,Scim security
    class API,Core,EntraAPI,ScimAPI api
    class Services,Auth,User,Governance,Provisioning service
    class Graph,Adapters,Notifications integration
    class Data data
    class Identity,Exchange,Integrations,SendGrid,Slack,Atlassian,Bitbucket,Automation external