# Monitoring Guide - Google Cloud Operations

This guide provides monitoring queries and dashboard setup for the HRMS Payroll Sync service using Google Cloud Monitoring.

## Custom Metrics

Our application exports these custom metrics:
- `custom.googleapis.com/hrms/events` (Counter)
  - Labels: 
    - `type`: Event type (EMPLOYEE_CREATED, EMPLOYEE_TERMINATED, etc.)
    - `status`: success/failure
  - Purpose: Track event processing outcomes

## Useful Monitoring Queries

### Event Processing Success Rate 

## Recommended Alerting Policies

### Critical Alerts

1. **High Event Failure Rate**
   - Condition: Success rate < 95% over 5 minutes
   - Severity: Critical
   ```
   fetch generic_task
   | metric 'custom.googleapis.com/hrms/events'
   | filter status == 'success'
   | sum(value.hrms_events)
   / (
     fetch generic_task
     | metric 'custom.googleapis.com/hrms/events'
     | sum(value.hrms_events)
   ) * 100 < 95
   ```

2. **High API Error Rate**
   - Condition: 5xx errors > 5% over 5 minutes
   - Severity: Critical

### Warning Alerts

1. **Elevated Latency**
   - Condition: 95th percentile latency > 2s
   - Severity: Warning

2. **Memory Usage**
   - Condition: Memory usage > 85%
   - Severity: Warning

## Dashboard Sections

1. **Overview**
   - Event success rate
   - Events processed per minute
   - Current error rate
   - Active instances

2. **Event Processing**
   - Success rate by event type
   - Event volume trends
   - Processing latency

3. **API Health**
   - Datacom API latency
   - HR System API latency
   - Error rates by endpoint

4. **System Health**
   - Memory usage
   - CPU utilization
   - Instance count
   - Log errors

## Log-based Metrics

Consider creating log-based metrics for:
- Failed authentication attempts
- Non-NZ employee skips
- Backdated leave requests

Example log filter:

## Notes

- All rate calculations use a 5-minute window by default
- Success rates are calculated as percentages (0-100)
- Consider adjusting time windows based on your traffic patterns
- Alert thresholds should be adjusted based on your SLOs
- Cloud Monitoring automatically collects standard metrics for Cloud Run including:
  - Request count
  - Request latency
  - Container memory utilization
  - Container CPU utilization
  - Container instance count

## Additional Considerations

1. **Tracing Integration**
   - Cloud Trace is automatically integrated via OpenTelemetry
   - Trace sampling rate is set to 1.0 (100%)
   - Traces include HTTP client calls to both HR System and Datacom APIs

2. **Logging Best Practices**
   - All logs include MDC context (recordId, recordType, eventType)
   - Payload logging is controlled by log level (DEBUG)
   - Error logs include full stack traces
   - Structured logging format for better querying

3. **Cost Optimization**
   - Consider adjusting sampling rates for high-volume metrics
   - Use appropriate aggregation intervals for long-term metrics
   - Set up log exclusion filters for noisy debug logs in production