#!/bin/bash
set -e

# Config
STACK_NAME="patient-management"
BUCKET_NAME="cf-templates"
TEMPLATE_FILE="./cdk.out/localstack.template.json"
TEMPLATE_KEY="patient-stack.template.json"
REGION="us-east-1"
LOCALSTACK_URL="http://localhost:4566"

# Required for LocalStack CLI compatibility
export AWS_DEFAULT_REGION=$REGION
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

echo "🪣 Ensuring S3 bucket for templates exists..."
aws --endpoint-url=$LOCALSTACK_URL s3api head-bucket --bucket $BUCKET_NAME 2>/dev/null || {
  echo "🆕 Bucket not found. Creating s3://$BUCKET_NAME..."
  aws --endpoint-url=$LOCALSTACK_URL s3 mb s3://$BUCKET_NAME
}

echo "📤 Uploading CloudFormation template to S3..."
aws --endpoint-url=$LOCALSTACK_URL s3 cp "$TEMPLATE_FILE" "s3://$BUCKET_NAME/$TEMPLATE_KEY"

TEMPLATE_URL="http://$BUCKET_NAME.s3.localhost.localstack.cloud:4566/$TEMPLATE_KEY"

# 🔥 Delete old stack if it exists
echo "🧹 Deleting existing CloudFormation stack (if it exists)..."
if aws --endpoint-url=$LOCALSTACK_URL cloudformation describe-stacks --stack-name $STACK_NAME > /dev/null 2>&1; then
  aws --endpoint-url=$LOCALSTACK_URL cloudformation delete-stack \
    --stack-name $STACK_NAME
  echo "⏳ Waiting for stack to be deleted..."
  aws --endpoint-url=$LOCALSTACK_URL cloudformation wait stack-delete-complete \
    --stack-name $STACK_NAME
fi

# 🚀 Create new stack
echo "🚀 Creating new CloudFormation stack..."
aws --endpoint-url=$LOCALSTACK_URL cloudformation create-stack \
  --stack-name $STACK_NAME \
  --template-url "$TEMPLATE_URL" \
  --capabilities CAPABILITY_NAMED_IAM

# ⏳ Wait for stack creation
echo "⏳ Waiting for stack creation to complete..."
aws --endpoint-url=$LOCALSTACK_URL cloudformation wait stack-create-complete \
  --stack-name $STACK_NAME

# 🌐 Fetch the LAST ALB created (expected to be api-gateway)
echo "🌐 Looking for the last ALB (api-gateway)..."

last_alb_dns=$(aws --endpoint-url=$LOCALSTACK_URL elbv2 describe-load-balancers \
  --query "LoadBalancers[-1].DNSName" \
  --output text)

if [[ -n "$last_alb_dns" ]]; then
  echo "✅ API Gateway ALB: http://$last_alb_dns"
else
  echo "❌ No ALBs found!"
fi

echo "🎉 Deployment and service discovery complete!"
