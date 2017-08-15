import jenkins.model.*
import java.util.Arrays
import com.cloudbees.jenkins.plugins.amazonecs.ECSCloud
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.MountPointEntry
import com.cloudbees.jenkins.plugins.amazonecs.ECSTaskTemplate.EnvironmentEntry

def cloud_name = System.getenv('CLOUD_NAME') ?: "ECS-SLAVES"
def ecs_cluster_arn = System.getenv('ECS_CLUSTER_ARN') ?: "arn:aws:ecs:us-east-1:000000000000:cluster/jenkins-ecs"
def aws_region = System.getenv('AWS_REGION') ?: 'us-east-1'
def jenkins_url = System.getenv('JENKINS_URL') ?: 'http://'+"curl -s http://169.254.169.254/latest/meta-data/local-ipv4".execute().text+':8080/'
def slave_label = System.getenv('SLAVE_LABEL') ?: 'jenkins-ecs'
def slave_image = System.getenv('SLAVE_IMAGE') ?: 'jenkinsci/jnlp-slave'
def slave_jenkins_root = System.getenv('SLAVE_JENKINS_ROOT') ?: '/home/jenkins'
def slave_cpu = System.getenv('SLAVE_CPU') ?: 0
def slave_memory = System.getenv('SLAVE_MEMORY') ?: 500
def app_image = System.getenv('APP_IMAGE') ?: '000000000000.dkr.ecr.us-east-1.amazonaws.com/demoApp'

def instance = Jenkins.getInstance()

def mounts = Arrays.asList(
  new MountPointEntry(
    name="docker",
    sourcePath="/var/run/docker.sock",
    containerPath="/var/run/docker.sock",
    readOnly=false)
)

def environments = Arrays.asList(
  new EnvironmentEntry(
    name = 'APP_IMAGE',
    value = app_image
  ),
  new EnvironmentEntry(
    name = 'AWS_REGION',
    value = app_image
  )
)

def ecsTemplate = new ECSTaskTemplate(
  templateName="jenkins-slave",
  label=slave_label,
  image=slave_image,
  remoteFSRoot=slave_jenkins_root,
  memory=slave_memory,
//  memoryReservation=0,
  cpu=slave_cpu,
  privileged=false,
  logDriverOptions=null,
  environments=environments,
  extraHosts=null,
  mountPoints=mounts
)

def ecsCloud = new ECSCloud(
  name=cloud_name,
  templates=Arrays.asList(ecsTemplate),
  credentialsId=null,
  cluster=ecs_cluster_arn,
  regionName=aws_region,
  jenkinsUrl=jenkins_url,
  slaveTimoutInSeconds=60
)

def clouds = instance.clouds
clouds.add(ecsCloud)
instance.save()
