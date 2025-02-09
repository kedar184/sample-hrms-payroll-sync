# HRMS Payroll Sync Service

A cloud-native microservice that synchronizes HR events (employee updates, leaves) to Datacom Payroll system.

## Overview

This service acts as an event-driven bridge between the HR system and Datacom Payroll, ensuring payroll data stays in sync with HR changes.

## Architecture

```mermaid
sequenceDiagram
    participant HR System
    participant Pub/Sub
    participant HRMS Sync
    participant Secret Manager
    participant Datacom Auth
    participant Datacom API

    HR System->>Pub/Sub: Publish HR Event
    Pub/Sub->>HRMS Sync: Push Event
    
    alt Employee Event
        HRMS Sync->>Secret Manager: Get Datacom Credentials
        HRMS Sync->>Datacom Auth: Get Access Token
        HRMS Sync->>HR System: Get Employee Details
        HRMS Sync->>Datacom API: Update Employee
    else Leave Event
        HRMS Sync->>HR System: Get Leave Details
        HRMS Sync->>Datacom API: Submit Leave
    end

    alt Business Error
        HRMS Sync->>Pub/Sub: Publish to business-notifications
    end
```

## Use Cases

1. **Employee Onboarding**
   - HR creates new employee
   - Service syncs employee to Datacom
   - Sets default payroll settings

2. **Employee Updates**
   - HR updates employee details
   - Service propagates changes to Datacom
   - Handles region-specific validations

3. **Employee Termination**
   - HR marks employee as terminated
   - Service updates end date in Datacom

4. **Leave Management**
   - HR records employee leave
   - Service submits leave to Datacom
   - Handles different leave types

## Cloud-Native Components

### Google Cloud Platform
- **Cloud Run**: Hosts the stateless microservice
- **Pub/Sub**: Event messaging for HR events and business notifications
- **Secret Manager**: Secure storage for API credentials
- **Cloud Build**: CI/CD pipeline
- **Cloud Monitoring**: Observability and metrics

### Spring Cloud GCP
- PubSub integration
- Secret Manager integration
- Tracing and monitoring

## Design Principles

1. **Event-Driven Architecture**
   - Asynchronous processing
   - Loose coupling with HR system
   - Business error notifications

2. **Cloud-Native Patterns**
   - Stateless design
   - Configuration externalization
   - Health checks and metrics
   - Graceful degradation

3. **Resilience Patterns**
   - Circuit breakers for external APIs
   - Retry mechanisms
   - Rate limiting
   - Error handling

## Configuration

### Profiles
- `local`: Local development with mock services
- `cloud`: Production deployment on GCP

### Feature Flags
- PubSub notifications
- Mock API responses
- Debug logging

## Setup

1. **Initial GCP Setup**
   ```bash
   ./scripts/gcp-init.sh
   ```
2. **Post Deployment Setup**
   ```bash
   ./scripts/gcp-post-deploy.sh
   ```

3. **Verify & Test**
   ```bash
   # Test deployment
   ./scripts/gcp-verify-deployment.sh

   # Test APIs locally
   ./scripts/local-test-api.sh
   ```

4. **Local Development**
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS="key.json"
   ./gradlew bootRun
   ```

## API Documentation

OpenAPI documentation available at `/swagger-ui.html`

## Monitoring

- Health endpoint: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

## Error Handling

1. **Technical Errors**
   - Retried automatically
   - Circuit breaker protection
   - Logged for debugging

2. **Business Errors**
   - Published to business-notifications topic
   - No retries
   - Logged for auditing 
