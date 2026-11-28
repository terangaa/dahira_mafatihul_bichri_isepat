
pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(
            name: 'EXECUTE_TESTS',
            defaultValue: false,
            description: 'Executer les tests JUnit et Mockito'
        )
    }

    environment {
        JAVA_HOME = 'C:\\ProgramData\\amazon-corretto-21.0.7.6.1-windows-x64-jdk\\jdk21.0.7_6'
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {

        stage('1 - Informations du build') {
            steps {
                echo '===== DAHIRA MAFATIHОUL BICHRI ====='
                echo "JOB : ${JOB_NAME}"
                echo "BRANCHE : ${BRANCH_NAME}"
                echo "BUILD : ${BUILD_NUMBER}"
                echo "WORKSPACE : ${WORKSPACE}"
            }
        }

        stage('2 - Verification Java') {
            steps {
                withEnv(["PATH+JDK21=${env.JAVA_HOME}\\bin"]) {
                    bat 'java -version'
                }
            }
        }

        stage('3 - Verification Maven') {
            steps {
                withEnv(["PATH+JDK21=${env.JAVA_HOME}\\bin"]) {
                    bat 'mvnw.cmd -version'
                }
            }
        }

        stage('4 - Verification Git') {
            steps {
                bat 'git --version'
                bat 'git status --short --branch'
            }
        }

        stage('5 - Validation du POM') {
            steps {
                withEnv(["PATH+JDK21=${env.JAVA_HOME}\\bin"]) {
                    bat 'mvnw.cmd -B validate'
                }
            }
        }

        stage('6 - Compilation') {
            steps {
                withEnv(["PATH+JDK21=${env.JAVA_HOME}\\bin"]) {
                    bat 'mvnw.cmd -B clean compile'
                }
            }
        }

        stage('7 - Tests') {
            when {
                expression {
                    return params.EXECUTE_TESTS
                }
            }
            steps {
                withEnv(["PATH+JDK21=${env.JAVA_HOME}\\bin"]) {
                    bat 'mvnw.cmd -B test'
                }
            }
        }

        stage('8 - Packaging JAR') {
            steps {
                withEnv(["PATH+JDK21=${env.JAVA_HOME}\\bin"]) {
                    bat 'mvnw.cmd -B package -DskipTests'
                }
            }
        }

        stage('9 - Verification du JAR') {
            steps {
                bat 'dir target\\*.jar'
            }
        }

        stage('10 - Archivage') {
            steps {
                archiveArtifacts(
                    artifacts: 'target/*.jar',
                    fingerprint: true
                )
            }
        }
    }

    post {
        success {
            echo 'PIPELINE TERMINE AVEC SUCCES !'
        }

        failure {
            echo 'ECHEC DU PIPELINE : consulte les logs.'
        }

        always {
            echo "Fin du build ${BUILD_NUMBER}"
        }
    }
}
// Test CI/CD - Branche master
