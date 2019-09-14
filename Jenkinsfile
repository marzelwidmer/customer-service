// https://piotrminkowski.wordpress.com/2019/05/17/continuous-delivery-with-openshift-and-jenkins-a-b-testing/
pipeline {
    // Agent Maven
    agent {
        node {
            label 'maven'
        }
    }
    // Environment
    environment {
        GIT_COMMIT_SHORT = env.GIT_COMMIT.take(7)
        POM_PROPERTIES_FILE = 'target/maven-archiver/pom.properties'
        TAG_MESSAGE = 'ci-release-bot'
        NEXT_VERSION = '0.0.0'
        DEV_ENVIRONMENT = 'development'
        TEST_ENVIRONMENT = 'testing'
        PROD_ENVIRONMENT = 'production'
        APP_NAME = 'customer-service'
    }
    // Stages
    stages {
        // Setup
        stage('initialize pipeline') {
            steps {
                script {
                    // setup git with API-TOKEN
                    withCredentials([string(credentialsId: 'GITHUB_TOKEN', variable: 'token')]) {
                        git(branch: 'master', changelog: true, url: "https://$token:x-oauth-basic@github.com/marzelwidmer/customer-service.git", credentialsId: 'GITHUB_TOKEN')
                    }
                    // Last Git commit
                    LAST_GIT_COMMIT = sh (
                            script: 'git --no-pager show -s --format=\'%Cblue %h %Creset %s %Cgreen %an %Creset (%ae)\'',
                            returnStdout: true
                    ).trim()
                    echo "Last Git commit: ${LAST_GIT_COMMIT}"

                    // Compute next version
                    NEXT_VERSION = sh (
                            script: "./jenkins/semver.sh",
                            returnStdout: true
                    ).trim()
                    echo "NEXT_VERSION : ${NEXT_VERSION}"
                }
            }
        }
        // Build
        stage('build application') {
            steps {
                script {
                    sh 'git fetch --all'
                    sh "./mvnw validate"
                    echo "next version will be ${NEXT_VERSION}"
                    sh "./mvnw validate -Djgitver.use-version=$NEXT_VERSION"
                    sh "./mvnw package -DskipTests -Djgitver.use-version=$NEXT_VERSION"
                }
            }
        }
        // Test
        stage('test application') {
            steps {
                echo "run tests"
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        // Create Tag
        stage('git create tag') {
            steps {
                script {
                    echo "Create tag with version: ${NEXT_VERSION}"
                    sh("""
                        if [ \$(git tag -l $NEXT_VERSION) ]; then
                            echo tag exist already
                        else    
                            git tag -a -m '${TAG_MESSAGE}' ${NEXT_VERSION}
                            git push --follow-tags
                        fi
                    """)
                }
            }
        }
        // Dev Deployment
        stage('deploy to development') {
            steps {
                sh "./mvnw  fabric8:deploy -DskipTests -Dfabric8.namespace=development  -Djgitver.use-version=$NEXT_VERSION"
            }
        }
        // Verify Deployment
        stage('verify deployment in development environment') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftVerifyDeployment(namespace: DEV_ENVIRONMENT,
                                    depCfg: APP_NAME,
                                    replicaCount: '1',
                                    verifyReplicaCount: 'true',
                                    waitTime: '300000')
                        }
                    }
                }
            }
        }
        // Tag latest image
        stage("tag latest image") {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftTag(namespace: DEV_ENVIRONMENT,
                                    sourceStream: APP_NAME,
                                    sourceTag: NEXT_VERSION,
                                    destinationStream: APP_NAME,
                                    destinationTag: 'latest')
                        }
                    }
                }
            }
        }
        // Tag image to promote
        stage('tag image for testing') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftTag(namespace: DEV_ENVIRONMENT,
                                    sourceStream: APP_NAME,
                                    sourceTag: NEXT_VERSION,
                                    destinationStream: APP_NAME,
                                    destinationTag: 'promoteQA')
                        }
                    }
                }
            }
        }
        // Approval
        stage('approve to testing') {
            steps {
                timeout(time: 2, unit: 'DAYS') {
                    input 'Approve to testing'
                }
            }
        }
        // Testing Deployment
        stage('deploy to testing') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftDeploy(namespace: TEST_ENVIRONMENT,
                                    deploymentConfig: APP_NAME,
                                    waitTime: '300000')
                        }
                    }
                }
            }
        }
        // Scale POD in testing
        stage('scale pod in testing to 2 replicas') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftScale(namespace: TEST_ENVIRONMENT,
                                    deploymentConfig: APP_NAME,
                                    waitTime: '300000',
                                    replicaCount: '2')
                        }
                    }
                }
            }
        }
        // Verify deployment
        stage('verify deploy in testing') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftVerifyDeployment(namespace: TEST_ENVIRONMENT,
                                    depCfg: APP_NAME,
                                    replicaCount: '2',
                                    verifyReplicaCount: 'true',
                                    waitTime: '300000')
                        }
                    }
                }
            }
        }
        // Approval
        stage('approve to production') {
            steps {
                timeout(time: 2, unit: 'DAYS') {
                    input 'Approve to production'
                }
            }
        }
        // Deploy to production
        stage('tag image for production') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftTag(namespace: DEV_ENVIRONMENT,
                                    sourceStream: APP_NAME,
                                    sourceTag: 'promoteQA',
                                    destinationStream: APP_NAME,
                                    destinationTag: 'promotePROD')
                        }
                    }
                }
            }
        }
        // Deploy to production
        stage('deploy to production') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftDeploy(namespace: PROD_ENVIRONMENT,
                                    deploymentConfig: APP_NAME,
                                    waitTime: '300000')

                            openshiftScale(namespace: PROD_ENVIRONMENT,
                                    deploymentConfig: APP_NAME,
                                    waitTime: '300000',
                                    replicaCount: '2')
                        }
                    }
                }
            }
        }
        // Scale POD in production
        stage('scale pod in production to 2 replicas') {
            steps {
                script {
                    openshift.withCluster() {
                        openshiftScale(namespace: PROD_ENVIRONMENT,
                                deploymentConfig: APP_NAME,
                                waitTime: '300000',
                                replicaCount: '2')
                    }
                }
            }
        }
        // Verify deployment
        stage('verify deployment in production') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshiftVerifyDeployment(namespace: PROD_ENVIRONMENT,
                                    depCfg: APP_NAME,
                                    replicaCount: '2',
                                    verifyReplicaCount: 'true',
                                    waitTime: '300000')
                        }
                    }
                }
            }
        }
    }
}