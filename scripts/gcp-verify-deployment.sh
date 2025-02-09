#!/bin/bash

# Exit on error
set -e

# Variables
PROJECT_ID="cloud-exps"
SERVICE_ACCOUNT_NAME="hrms-payroll-sync"
SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "🔍 Verifying deployment..."

echo "Verifying service account..."
gcloud iam service-accounts get-iam-policy ${SERVICE_ACCOUNT_EMAIL}

echo "Verifying secrets access..."
gcloud secrets versions access latest --secret="secrets/integration/services/datacom/client-id"
gcloud secrets versions access latest --secret="secrets/integration/services/datacom/client-secret"
gcloud secrets versions access latest --secret="secrets/integration/services/hr-system/api-key"

echo "Testing PubSub..."
gcloud pubsub topics publish hr-events --message='{
  "eventType": "EMPLOYEE_CREATED",
  "recordType": "EMPLOYEE",
  "recordId": "EMP001",
  "timestamp": "2024-03-20T10:00:00Z"
}'

echo "✅ Verification complete!" 