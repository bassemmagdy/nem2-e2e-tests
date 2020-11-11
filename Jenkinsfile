pipeline {
  agent {
    label 'cat-server'
  }
  triggers { cron('* H(0-2) * * *') }
  parameters { 
    gitParameter(
      name: 'TESTS_VERSION', defaultValue: 'origin/feature/tests-pipeline',
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
  environment {
    IS_BOOTSTRAP_RUN  = "${params.ENVIRONMENT == 'bootstrap' ? 'true' : 'false'}"
  }
  tools {
    nodejs 'nodejs-15.0.1'
    gradle 'gradle-6.7'
  }
  stages {
    // stage ('Setup gradle env') {
    //   steps {
    //     script {
    //       if (isUnix()) {
    //         sh '''
    //           gradle --version
    //           gradle wrapper --gradle-version 6.2 --distribution-type all
    //           ./gradlew --version
    //         '''
    //       }
    //       else {
    //         bat '''
    //           gradle --version
    //           gradle wrapper --gradle-version 6.2 --distribution-type all
    //           gradlew.bat --version
    //         '''
    //       }
    //     }
    //   }
    // }
    // stage ('Build e2e tests project') {
    //   steps{
    //     script {
    //       if (isUnix()) {
    //         sh '''
    //           ./gradlew --project-dir symbol-e2e-tests/ --refresh-dependencies --rerun-tasks clean testClasses
    //         '''
    //       }
    //       else {
    //         bat '''
    //           gradlew.bat --project-dir symbol-e2e-tests/ --refresh-dependencies --rerun-tasks clean testClasses
    //         '''
    //       }
    //     }
    //   }
    // }
    // stage ('Execute e2e tests') {
    //   steps{
    //     script {
    //       if (isUnix()) {
    //         sh '''
    //           ./gradlew --project-dir symbol-e2e-tests/ test
    //         '''
    //       }
    //       else {
    //         bat '''
    //           gradlew.bat --project-dir symbol-e2e-tests/ test
    //         '''
    //       }
    //     }
    //   }
    // }
    stage ('Install Symbol bootstrap') {
      when {
        environment ignoreCase: true, name: 'IS_BOOTSTRAP_RUN', value: 'true'
      }
      steps {
        script {
          if (params.BOOTSTRAP_VERSION == '') {
            error('ENVIRONMENT is bootstrap, but BOOTSTRAP_VERSION is not specified.')
          }
          echo "Installing symbol bootstrap version ${params.BOOTSTRAP_VERSION}"
          sh "npm install -g symbol-bootstrap@${params.BOOTSTRAP_VERSION}"
          sh '''
            symbol-bootstrap -v
            symbol-bootstrap start -p bootstrap --detached
          '''
        }
      }
    }
    stage ('Start Symbol bootstrap') {
      when {
        environment ignoreCase: true, name: 'IS_BOOTSTRAP_RUN', value: 'true'
      }
      steps {
        script {
          echo 'Starting symbol bootstrap...'
          sh 'symbol-bootstrap start -p bootstrap --detached'
          sh '''
            curl localhost:3000/node/info
            curl localhost:3000/node/server
            curl localhost:3000/node/health
            curl localhost:3000/chain/info
          '''
        }
      }
    }
  }
}