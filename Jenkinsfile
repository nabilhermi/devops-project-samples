pipeline {
    agent any
    environment {
        BRANCHE_DEV = 'origin/develop'
        BRANCHE_PROD = 'origin/main'
        NEXUS_DOCKER_REGISTRY = "http://prod.local:5003"
        NEXUS_CREDENTIALS_ID = "nexus-credentials"
        DOCKER_IMAGE_NAME = "devops-project-samples"
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
                        sh "mvn sonar:sonar -Dsonar.projectKey=devops-project-samples -Dsonar.host.url=http://192.168.83.137:9000 -Dsonar.token=sqa_c8c31468f74ece241bfb3f3089e70a738cdcea83"
                    }
                    timeout(time: 10, unit: 'MINUTES') {
                        echo "Waiting for SonarQube quality gate..."
                        def qg = waitForQualityGate()
                        echo "Quality gate status: ${qg.status}"
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
