
pipeline {
    agent any

    tools {
        jdk 'JDK21'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {

        stage('1 - Informations du build') {
            steps {
                echo '======================================'
                echo 'PROJET : DAHIRA MAFATIHОUL BICHRI'
                echo "JOB : ${JOB_NAME}"
                echo "BRANCHE : ${BRANCH_NAME}"
                echo "BUILD : ${BUILD_NUMBER}"
                echo "WORKSPACE : ${WORKSPACE}"
                echo '======================================'
            }
        }

        stage('2 - Verification Java') {
            steps {
                bat 'java -version'
            }
        }

        stage('3 - Verification Maven') {
            steps {
                bat 'mvnw.cmd -version'
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
                bat 'mvnw.cmd -B validate'
            }
        }

        stage('6 - Compilation') {
            steps {
                bat 'mvnw.cmd -B clean compile'
            }
        }

        stage('7 - Tests') {
            when {
                expression { return params.EXECUTE_TESTS }
            }
            steps {
                bat 'mvnw.cmd -B test'
            }
        }

        stage('8 - Packaging JAR') {
            steps {
                bat 'mvnw.cmd -B package -DskipTests'
            }
        }

        stage('9 - Verification du JAR') {
            steps {
                script {
                    def jarFiles = findFiles(glob: 'target/*.jar')

                    if (jarFiles.length == 0) {
                        error 'Aucun fichier JAR trouve dans target/'
                    }

                    jarFiles.each {
                        echo "JAR genere : ${it.name} (${it.length} octets)"
                    }
                }
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
