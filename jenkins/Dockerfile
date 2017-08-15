FROM jenkins
USER root

RUN mkdir /usr/share/jenkins/ref/secrets &&\
    echo false > /usr/share/jenkins/ref/secrets/slave-to-master-security-kill-switch &&\
    echo 2.0 > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state

RUN apt-get update &&\
    apt-get install -y jq python-pip &&\
    pip install awscli

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt

COPY init.groovy /usr/share/jenkins/ref/init.groovy
COPY seed-job.groovy /usr/share/jenkins/ref/init.groovy.d/
COPY cloud.groovy /usr/share/jenkins/ref/init.groovy.d/
COPY jobs/ /usr/share/jenkins/ref/jobs/seed-job/workspace/jobs/
