#!/bin/bash

# Exit on error
set -e

# Check required environment variables
if [[ -z "${DATACOM_CLIENT_ID}" || -z "${DATACOM_CLIENT_SECRET}" || -z "${HR_SYSTEM_API_KEY}" ]]; then
    echo "Error: Required environment variables not set"
    echo "Please set:"
    echo "  DATACOM_CLIENT_ID"
    echo "  DATACOM_CLIENT_SECRET"
    echo "  HR_SYSTEM_API_KEY"
    exit 1
fi

# Variables
PROJECT_ID="cloud-exps"
SERVICE_ACCOUNT_NAME="hrms-payroll-sync"
SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "🔧 Setting up GCP resources for HRMS Payroll Sync..."

# Set project
echo "Setting project to ${PROJECT_ID}..."
gcloud config set project ${PROJECT_ID}

# Create service account
echo "Creating service account..."
if gcloud iam service-accounts describe ${SERVICE_ACCOUNT_EMAIL} >/dev/null 2>&1; then
    echo "✓ Service account ${SERVICE_ACCOUNT_NAME} already exists"
else
    gcloud iam service-accounts create ${SERVICE_ACCOUNT_NAME} \
        --description="Service account for HRMS Payroll Sync" \
        --display-name="HRMS Payroll Sync"
    echo "✓ Service account ${SERVICE_ACCOUNT_NAME} created"
fi

# Grant IAM roles
echo "Granting IAM roles..."
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
    --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
    --role="roles/secretmanager.secretAccessor"

gcloud projects add-iam-policy-binding ${PROJECT_ID} \
    --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
    --role="roles/pubsub.publisher"

# Create secrets with correct paths
echo "Creating secrets..."
for secret in "datacom-client-id" "datacom-client-secret" "hr-system-api-key"; do
    if gcloud secrets describe ${secret} >/dev/null 2>&1; then
        echo "✓ Secret ${secret} already exists"
    else
        gcloud secrets create ${secret} --replication-policy="automatic"
        echo "✓ Secret ${secret} created"
    fi
done

# Add secret versions from environment variables
echo "Adding secret versions..."
echo "${DATACOM_CLIENT_ID}" | \
gcloud secrets versions add datacom-client-id --data-file=-

echo "${DATACOM_CLIENT_SECRET}" | \
gcloud secrets versions add datacom-client-secret --data-file=-

echo "${HR_SYSTEM_API_KEY}" | \
gcloud secrets versions add hr-system-api-key --data-file=-

# Create PubSub topics
echo "Creating PubSub topics..."
for topic in "business-notifications" "hr-events"; do
    if gcloud pubsub topics describe ${topic} >/dev/null 2>&1; then
        echo "✓ Topic ${topic} already exists"
    else
        gcloud pubsub topics create ${topic}
        echo "✓ Topic ${topic} created"
    fi
done

echo "🔍 Verifying initial setup..."

# Verify service account
echo "Verifying service account roles..."
roles=$(gcloud projects get-iam-policy ${PROJECT_ID} \
  --flatten="bindings[].members" \
  --format="table(bindings.role)" \
  --filter="bindings.members:${SERVICE_ACCOUNT_EMAIL}")
echo "Assigned roles:"
echo "${roles}"

# Verify secrets
echo "Verifying secret creation..."
for secret in "datacom-client-id" \
              "datacom-client-secret" \
              "hr-system-api-key"; do
    if gcloud secrets describe ${secret} >/dev/null 2>&1; then
        echo "✓ Secret ${secret} exists"
    else
        echo "✗ Secret ${secret} not found"
        exit 1
    fi
done

# Verify PubSub topics
echo "Verifying PubSub topics..."
for topic in "business-notifications" "hr-events"; do
    if gcloud pubsub topics describe ${topic} >/dev/null 2>&1; then
        echo "✓ Topic ${topic} exists"
    else
        echo "✗ Topic ${topic} not found"
        exit 1
    fi
done

