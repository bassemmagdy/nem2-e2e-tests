pipeline {
  agent {
    label 'cat-server'
  }
  triggers { cron('* H(0-2) * * *') }
  parameters { 
    gitParameter(name: 'TESTS_REPO_BRANCH', defaultValue: 'feature/tests-pipeline', description: 'The branch ', listSize: '10', quickFilterEnabled: false, selectedValue: 'DEFAULT', sortMode: 'ASCENDING_SMART', tagFilter: '*', type: 'PT_BRANCH_TAG')
    string(name: 'TESTNET_API_URL', defaultValue: '', description: 'The URL of the testnet API')
    choice(name: 'ENVIRONMENT', choices: ['testnet', 'bootstrap'], description: 'Test environment')
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
              ./gradlew --debug --project-dir symbol-e2e-tests/ --refresh-dependencies --rerun-tasks clean testClasses
            '''
          }
          else {
            bat '''
              gradlew.bat --debug --project-dir symbol-e2e-tests/ --refresh-dependencies --rerun-tasks clean testClasses
            '''
          }
        }
      }
    }
    stage ('Execute e2e tests') {
      steps{
        script {
          if (isUnix()) {
            sh '''
              ./gradlew --debug --project-dir symbol-e2e-tests/ test
            '''
          }
          else {
            bat '''
              gradlew.bat --debug --project-dir symbol-e2e-tests/ test
            '''
          }
        }
      }
    }
  }
}