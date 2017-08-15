#!/bin/bash
INSTANCE_ID=$(curl http://169.254.169.254/latest/meta-data/instance-id)
EC2_REGION=$(curl http://169.254.169.254/latest/dynamic/instance-identity/document | jq --raw-output '.region')
AUTO_SCALING_GROUP_NAME=$(aws autoscaling describe-auto-scaling-instances --region $EC2_REGION --instance-ids=$INSTANCE_ID --output text --query "AutoScalingInstances[0].AutoScalingGroupName")
aws autoscaling set-instance-protection --region $EC2_REGION --instance-ids $INSTANCE_ID --auto-scaling-group-name $AUTO_SCALING_GROUP_NAME --protected-from-scale-in

jenkins-slave "$@"

COUNT=$(docker ps --format "table {{.Image}}" | grep -c "jenkins")
if [ "$COUNT" -eq "1" ]
then
  aws autoscaling set-instance-protection --region $EC2_REGION --instance-ids $INSTANCE_ID --auto-scaling-group-name $AUTO_SCALING_GROUP_NAME --no-protected-from-scale-in
fi
