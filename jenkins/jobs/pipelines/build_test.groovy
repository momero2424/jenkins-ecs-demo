node ('jenkins-ecs') {
  stage('Pull repo') {
    checkout([$class: 'GitSCM', branches: [[name: "${GIT_SELECTOR}"]], userRemoteConfigs: [[url: 'https://github.com/momero2424/jenkins-ecs-demo']]])
  }

  stage('Docker build') {
    sh "docker build -t ${APP_IMAGE}:CI${BUILD_NUMBER} ./demoApp"
  }

  stage('Test') {
    def externalPort = 8000 + ((BUILD_NUMBER as Integer) % 1000)
    sh "docker run --detach --publish ${externalPort}:80 --name demo-test-CI${BUILD_NUMBER} ${APP_IMAGE}:CI${BUILD_NUMBER}"

    def instanceIp = sh(returnStdout: true, script: 'curl -s http://169.254.169.254/latest/meta-data/local-ipv4')

    try {
      sh "curl -s ${instanceIp}:${externalPort}/app/ | grep 'Jenkins ECS demo'"
    } catch (e) {
      throw e
    } finally {
      sh "docker stop demo-test-CI${BUILD_NUMBER}"
      sh "docker rm demo-test-CI${BUILD_NUMBER}"
    }
  }

  stage('Push image') {
    sh "\$(aws ecr get-login --no-include-email --region ${AWS_REGION})"
    sh "docker tag ${APP_IMAGE}:CI${BUILD_NUMBER} ${APP_IMAGE}:CI${BUILD_NUMBER}"
    sh "docker push ${APP_IMAGE}:CI${BUILD_NUMBER}"
  }
}
