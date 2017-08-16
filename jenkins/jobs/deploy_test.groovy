freeStyleJob('deploy') {
  label('jenkins-ecs')
  parameters {
    stringParam('TAG', 'master', 'name of the tag to deploy')
  }
  steps {
    shell('''#!/bin/bash
    CLUSTER_NAME="jere-test-3-appCluster"
    echo "cluster name is ${CLUSTER_NAME}"

    SERVICES=$(aws ecs list-services --region ${AWS_REGION} --cluster ${CLUSTER_NAME} | jq ".serviceArns[]" | cut -d "/" -f2 | tr -d \\")

    echo "updating tags"
    for SERVICE in ${SERVICES}
    do
    	TASK_ARN=$(aws ecs describe-services --region  ${AWS_REGION} --cluster ${CLUSTER_NAME} --service $SERVICE | jq ".services[].taskDefinition")
    	TASK_ARN=$(echo $TASK_ARN | tr -d \\")
    	TASK_NAME=${TASK_ARN#*/}
    	TASK_NAME=${TASK_NAME%:*}
    	echo $TASK_NAME
    	CONTAINER_DEFINITIONS=$(aws ecs describe-task-definition --task-definition $TASK_NAME --region  ${AWS_REGION} | jq ".taskDefinition.containerDefinitions | map( .image = (.image | split(\\":\\"))[0] + \\":\\" + \\"${TAG}\\")")
    	VOLUMES=$(aws ecs describe-task-definition --task-definition $TASK_NAME --region  ${AWS_REGION} | jq ".taskDefinition.volumes")
      TASK_ARN=$(aws ecs register-task-definition --region ${AWS_REGION} --family $TASK_NAME --container-definitions "$CONTAINER_DEFINITIONS" --volumes "$VOLUMES" | jq ".taskDefinition.taskDefinitionArn")
      TASK_ARN=$(echo $TASK_ARN | tr -d \\")
    	aws ecs update-service --region  ${AWS_REGION} --cluster ${CLUSTER_NAME} --service $SERVICE --task-definition $TASK_ARN
    done
          ''')
  }
}
