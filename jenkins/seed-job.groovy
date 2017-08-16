import jenkins.model.*
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval
import org.jenkinsci.plugins.scriptsecurity.scripts.languages.GroovyLanguage

def jobName = "seed-job"

def git_url = System.getenv('SEEDJOB_GIT')
def svn_url = System.getenv('SEEDJOB_SVN')

def scm = '''<scm class="hudson.scm.NullSCM"/>'''

if ( git_url ) {
  scm = """\
    <scm class="hudson.plugins.git.GitSCM">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url><![CDATA[${git_url}]]></url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>**</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
  """
} else if ( svn_url ) {
  scm = """\
    <scm class='hudson.scm.SubversionSCM'>
      <locations>
        <hudson.scm.SubversionSCM_-ModuleLocation>
          <remote><![CDATA[${svn_url}]]></remote>
          <local>.</local>
          <depthOption>infinity</depthOption>
        </hudson.scm.SubversionSCM_-ModuleLocation>
      </locations>
      <workspaceUpdater class='hudson.scm.subversion.UpdateUpdater'></workspaceUpdater>
      <excludedRegions></excludedRegions>
      <includedRegions></includedRegions>
      <excludedUsers></excludedUsers>
      <excludedCommitMessages></excludedCommitMessages>
      <excludedRevprop></excludedRevprop>
    </scm>
  """
}

def configXml = """\
  <?xml version='1.0' encoding='UTF-8'?>
  <project>
    <actions/>
    <description>Create Jenkins jobs from DSL groovy files</description>
    <keepDependencies>false</keepDependencies>
    <properties>
    </properties>
    ${scm}
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers>
      <org.jvnet.hudson.plugins.triggers.startup.HudsonStartupTrigger plugin="startup-trigger-plugin@2.7">
        <spec></spec>
        <label>master</label>
        <quietPeriod>0</quietPeriod>
        <runOnChoice>ON_CONNECT</runOnChoice>
      </org.jvnet.hudson.plugins.triggers.startup.HudsonStartupTrigger>
    </triggers>
    <concurrentBuild>false</concurrentBuild>
    <builders>
      <javaposse.jobdsl.plugin.ExecuteDslScripts plugin="job-dsl@1.37">
        <targets>jobs/*.groovy</targets>
        <usingScriptText>false</usingScriptText>
        <ignoreExisting>false</ignoreExisting>
        <removedJobAction>IGNORE</removedJobAction>
        <removedViewAction>IGNORE</removedViewAction>
        <lookupStrategy>JENKINS_ROOT</lookupStrategy>
        <additionalClasspath></additionalClasspath>
      </javaposse.jobdsl.plugin.ExecuteDslScripts>
    </builders>
    <publishers/>
    <buildWrappers/>
  </project>
  """.stripIndent()

def language = GroovyLanguage.get()
def scriptApproval = ScriptApproval.get()

def scriptsToApprove = [
  'jobs/build_test_trigger.groovy',
  'jobs/build_test.groovy',
  'jobs/pipelines/build_test.groovy',
]

def jenkins_home = Jenkins.getInstance().root

scriptsToApprove.each {
  def scriptText = new File("$jenkins_home/jobs/seed-job/workspace/$it").text
  scriptApproval.preapprove(scriptText, language)
}

if (!Jenkins.instance.getItem(jobName)) {
  def xmlStream = new ByteArrayInputStream( configXml.getBytes() )
  try {
    def seedJob = Jenkins.instance.createProjectFromXML(jobName, xmlStream)
    seedJob.scheduleBuild(0, null)
  } catch (ex) {
    println "ERROR: ${ex}"
    println configXml
  }
}
