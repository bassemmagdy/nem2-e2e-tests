pipeline {
  agent {
    label 'cat-server'
  }
  triggers { cron('* H(0-2) * * *') }
  parameters { 
    gitParameter(
      name: 'TESTS_VERSION', defaultValue: 'feature/tests-pipeline',
      description: 'Name of the branch or tag from Symbol e2e tests repo to checkout and run tests from.',
      listSize: '10', quickFilterEnabled: false, selectedValue: 'DEFAULT', sortMode: 'ASCENDING_SMART',
      tagFilter: '*', type: 'PT_BRANCH_TAG'
    )
    choice(name: 'ENVIRONMENT', choices: ['testnet', 'bootstrap'], description: '''Environment to run the tests against.
    testnet: The tests will be executed against the given testnet environment specified by the TESTNET_API_URL param.
    bootstrap: The tests will be executed against a clean bootstrap environment brought up locally from the branch specified by BOOTSTRAP_VERSION param.''')
    string(name: 'TESTNET_API_URL', defaultValue: '', description: 'The URL of the testnet API.')
    string(name: 'BOOTSTRAP_VERSION', defaultValue: '', description: 'Name of the branch or tag from Symbol bootstrap repo to checkout and start the bootstrap from.')
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