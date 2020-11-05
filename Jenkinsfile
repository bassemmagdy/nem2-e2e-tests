pipeline {
  agent {
    label 'cat-server'
  }
  stages {
    stage ('Setup gradle env') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              gradle --version
              gradle wrapper --gradle-version 6.2 --distribution-type all
              ./gradlew --version
            '''
          }
          else {
            bat '''
              gradle --version
              gradle wrapper --gradle-version 6.2 --distribution-type all
              gradlew.bat --version
            '''
          }
        }
      }
    }
    stage ('Build e2e tests project') {
      steps{
        script {
          if (isUnix()) {
            sh '''
              ./gradlew --project-dir symbol-e2e-tests/ --refresh-dependencies --rerun-tasks clean testClasses
            '''
          }
          else {
            bat '''
              gradlew.bat --project-dir symbol-e2e-tests/ --refresh-dependencies --rerun-tasks clean testClasses
            '''
          }
        }
      }
    }
  }
}