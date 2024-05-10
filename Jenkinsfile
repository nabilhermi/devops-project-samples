pipeline {
    agent any
    environment {
        BRANCHE_DEV = 'origin/develop'
        BRANCHE_PROD = 'origin/main'
        NEXUS_DOCKER_REGISTRY = "http://prod.local:5003"
        NEXUS_CREDENTIALS_ID = "nexus-credentials"
        DOCKER_IMAGE_NAME = "api-demo"
        DOCKER_IMAGE_TAG = "prod.local:5003"
    }

    stages {
      stage('Checkout') {
        steps {
          checkout scm
          echo 'Pulling... ' + env.GIT_BRANCH
        }

      }

      stage('Tests') {
        steps {
          sh 'mvn test'
        }
      }


    stage('Sonarqube Analysis') {
            steps {
                script {
                    withSonarQubeEnv('sonar-server') {
                        sh "mvn sonar:sonar -Dintegration-tests.skip=true -Dmaven.test.failure.ignore=true"
                    }
                    timeout(time: 1, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }

            }
    }

    
    stage('Maven Build and Package') {
            steps {
                script {
                    sh 'mvn clean package -DskipTests'
                }
            }
            post {
                success {
                    archiveArtifacts 'target/*.jar'
                }
            }
        }


    stage('Docker Build and Push to Nexus') {
            steps {
                script {
                    envName = "dev"
                    if(env.GIT_BRANCH == BRANCHE_PROD) {
                        envName = "prod"
                    }
                    envVersion  =  getEnvVersion(envName)
                    withCredentials([usernamePassword(credentialsId: "${NEXUS_CREDENTIALS_ID}", usernameVariable: 'USER', passwordVariable: 'PASSWORD')]){
                        sh 'echo $PASSWORD | docker login -u $USER --password-stdin $NEXUS_DOCKER_REGISTRY'
                        sh 'docker system prune -af'
                        sh "docker build -t $DOCKER_IMAGE_TAG/$DOCKER_IMAGE_NAME:$envVersion --no-cache --pull ."
                        sh "docker push $DOCKER_IMAGE_TAG/$DOCKER_IMAGE_NAME:$envVersion"
                    }
                }
            }
        } 




    stage('Ansible job staging') {
            when {
                expression { env.GIT_BRANCH == BRANCHE_DEV }
            }
            steps {
                script {
                    def targetVersion = getEnvVersion("dev")
                    sshagent(credentials: ['ansible-node-manager']) {
                        sh "ssh ansible@192.168.83.173 'cd ansible-projects/devops-ansible-deployment && ansible-playbook -i 00_inventory.yml -l testserver deploy_playbook.yml -e \"docker_image_tag=${targetVersion}\"'"
                    }
                }
            }
        }

    stage('Ansible job production') {
            when {
                expression { env.GIT_BRANCH == BRANCHE_PROD }
            }
            steps {
                script {
                    def targetVersion = getEnvVersion("prod")
                    sshagent(credentials: ['github-credentials']) {
                        sh "git tag -f v${targetVersion}"
                        sh "git push origin --tags HEAD:develop"
                    }
                    sshagent(credentials: ['ansible-node-manager']) {
                        sh "ssh ansible@192.168.83.173 'cd ansible-projects/devops-ansible-deployment && ansible-playbook -i 00_inventory.ini -l prodserver deploy_playbook.yml -e \"docker_image_tag=${targetVersion}\"'"
                    }
                }
            }
        }




    }








}


def getEnvVersion(envName) {
    def pom = readMavenPom file: 'pom.xml'
    // get the current development version
    artifactVersion = "${pom.version}"
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber;
    if (gitCommit == null) {
        versionNumber =artifactVersion+"-${envName}."+env.BUILD_NUMBER;
    } else {
        versionNumber =artifactVersion+"-${envName}."+env.BUILD_NUMBER+'.'+gitCommit.take(8);
    }
    print 'build ${environnement} versions...'
    print versionNumber
    return versionNumber
}
