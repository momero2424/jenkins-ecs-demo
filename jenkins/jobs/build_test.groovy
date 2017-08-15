pipelineJob('build and test') {
  parameters {
    stringParam('GIT_SELECTOR', 'master', 'Git Branch to build')
  }
  definition {
    cps {
      script(readFileFromWorkspace('jobs/pipelines/build_test.groovy'))
      sandbox()
    }
  }
}
