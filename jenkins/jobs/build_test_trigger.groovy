freeStyleJob('build and test trigger') {
  properties {
    githubProjectUrl('https://github.com/momero2424/jenkins-ecs-demo')
  }
  triggers {
    githubPullRequest {
      cron('* * * * *')
      triggerPhrase('jenkins test this please')
    }
  }
  concurrentBuild()
  label('jenkins-ecs')
  steps {
    downstreamParameterized {
      trigger('build and test') {
        block {
          buildStepFailure('FAILURE')
          failure('FAILURE')
          unstable('UNSTABLE')
        }
        parameters {
          predefinedProp('GIT_SELECTOR', '${ghprbSourceBranch}')
        }
      }
    }
  }
}
