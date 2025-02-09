#!/bin/bash

# Base URL
BASE_URL="http://localhost:8080"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}Testing HRMS Payroll Sync API${NC}\n"

# Function to make API call and display formatted response
call_api() {
    local test_name=$1
    local payload=$2
    
    echo -e "${GREEN}$test_name${NC}"
    echo -e "${YELLOW}Request:${NC}"
    echo "$payload" | jq '.'
    echo -e "\n${YELLOW}Response:${NC}"
    
    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/hrms/events" \
        -H "Content-Type: application/json" \
        -d "$payload")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$ d')
    
    echo -e "${YELLOW}Status:${NC} $http_code"
    echo -e "${YELLOW}Body:${NC}"
    if [ ! -z "$body" ]; then
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    fi
    echo -e "\n-------------------\n"
}

# 1. Employee Created Event
call_api "1. Testing Employee Created Event" '{
    "messageId": "msg-001",
    "publishTime": "2024-03-14T10:00:00Z",
    "data": "eyJldmVudFR5cGUiOiJFTVBMT1lFRV9DUkVBVEVEIiwicmVjb3JkVHlwZSI6IkVNUExPWUVFIiwicmVjb3JkSWQiOiJFTVAwMDEiLCJ0aW1lc3RhbXAiOiIyMDI0LTAzLTE0VDEwOjAwOjAwWiJ9"
}'

# 2. Employee Updated Event
call_api "2. Testing Employee Updated Event" '{
    "messageId": "msg-002",
    "publishTime": "2024-03-14T10:00:00Z",
    "data": "eyJldmVudFR5cGUiOiJFTVBMT1lFRV9VUERBVEVEIiwicmVjb3JkVHlwZSI6IkVNUExPWUVFIiwicmVjb3JkSWQiOiJFTVAwMDEiLCJ0aW1lc3RhbXAiOiIyMDI0LTAzLTE0VDEwOjAwOjAwWiJ9"
}'

# 3. Employee Terminated Event
call_api "3. Testing Employee Terminated Event" '{
    "messageId": "msg-003",
    "publishTime": "2024-03-14T10:00:00Z",
    "data": "eyJldmVudFR5cGUiOiJFTVBMT1lFRV9URVJNSU5BVEVEIiwicmVjb3JkVHlwZSI6IkVNUExPWUVFIiwicmVjb3JkSWQiOiJFTVAwMDEiLCJ0aW1lc3RhbXAiOiIyMDI0LTAzLTE0VDEwOjAwOjAwWiJ9"
}'

# 4. Leave Created Event
call_api "4. Testing Leave Created Event" '{
    "messageId": "msg-004",
    "publishTime": "2024-03-14T10:00:00Z",
    "data": "eyJldmVudFR5cGUiOiJBQlNFTkNFX0NSRUFURUQiLCJyZWNvcmRUeXBlIjoiQUJTRU5DRSIsInJlY29yZElkIjoiQUJTMDAxIiwidGltZXN0YW1wIjoiMjAyNC0wMy0xNFQxMDowMDowMFoifQ=="
}'

# 5. Invalid Event Type
call_api "5. Testing Invalid Event Type" '{
    "messageId": "msg-005",
    "publishTime": "2024-03-14T10:00:00Z",
    "data": "eyJldmVudFR5cGUiOiJVTktOT1dOX0VWRU5UIiwicmVjb3JkVHlwZSI6IkVNUExPWUVFIiwicmVjb3JkSWQiOiJFTVAwMDEiLCJ0aW1lc3RhbXAiOiIyMDI0LTAzLTE0VDEwOjAwOjAwWiJ9"
}'

# 6. Invalid Message Format
call_api "6. Testing Invalid Message Format" '{
    "messageId": "msg-006",
    "publishTime": "2024-03-14T10:00:00Z",
    "data": "invalid-base64-data"
}'

# Note: The base64 encoded data contains:
# {
#   "eventType": "EVENT_TYPE",
#   "recordType": "RECORD_TYPE",
#   "recordId": "ID",
#   "timestamp": "2024-03-14T10:00:00Z"
# } 