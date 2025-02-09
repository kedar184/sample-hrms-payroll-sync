#!/bin/bash

# Exit on error
set -e

# Variables
PROJECT_ID="cloud-exps"
SERVICE_ACCOUNT_NAME="hrms-payroll-sync"
SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
REGION="us-central1"
CLOUD_RUN_URL="https://hrms-payroll-sync-${PROJECT_ID}-${REGION}.a.run.app"

echo "Creating push subscription for Cloud Run..."
gcloud pubsub subscriptions create hr-events-sub \
    --topic hr-events \
    --push-endpoint="${CLOUD_RUN_URL}/hrms/events" \
    --push-auth-service-account="${SERVICE_ACCOUNT_EMAIL}" \
    --ack-deadline=30 \
    --message-retention-duration=7d \
    --expiration-period=never

echo "✅ Push subscription created successfully!"
echo "Endpoint: ${CLOUD_RUN_URL}/hrms/events" 