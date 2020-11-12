pipeline {
  agent {
    label 'cat-server'
  }
  triggers {
    cron '''TZ=America/New_York
      # This job needs to be started once every day between midnight and 2AM US Eastern time
      H H(0-2) * * *
    '''
  }
  parameters {
    gitParameter(
      name: 'TESTS_VERSION', defaultValue: 'origin/feature/tests-pipeline',
      description: 'Name of the branch or tag from Symbol e2e tests repo to checkout and run tests from.',
      listSize: '10', quickFilterEnabled: false, selectedValue: 'DEFAULT', sortMode: 'ASCENDING_SMART',
      tagFilter: '*', type: 'PT_BRANCH_TAG'
    )
    choice(name: 'ENVIRONMENT', choices: ['testnet', 'bootstrap'], description: '''Environment to run the tests against.
testnet: The tests will be executed against the given testnet environment specified by the TESTNET_API_URL param.
bootstrap: The tests will be executed against a clean bootstrap environment brought up locally using symbol-bootstrap tool version specified by BOOTSTRAP_VERSION param.''')
    string(name: 'TESTNET_API_URL', defaultValue: 'http://api-01.us-west-2.0.10.0.x.symboldev.network:3000', description: 'The URL of the testnet API.')
    string(name: 'BOOTSTRAP_VERSION', defaultValue: '', description: 'symbol-bootstrap tool version to install and start bootstrap with.')
    string(name: 'AUTOMATION_USER_PRIVATE_KEY', defaultValue: '4191972F8F40CF2D7132A0F26B4839C606259AC872DA78318945E1A2039B4A3D', description: 'Automation user private key.')
  }
  environment {
    IS_BOOTSTRAP_RUN  = "${params.ENVIRONMENT == 'bootstrap' ? 'true' : 'false'}"
    API_URL = "${params.ENVIRONMENT == 'testnet' ? params.TESTNET_API_URL : 'http://localhost:3000'}"
  }
  tools {
    nodejs 'nodejs-15.0.1'
    gradle 'gradle-6.7'
  }
  stages {
    stage ('Setup gradle env') {
      steps {
        script {
          if (isUnix()) {
            sh '''
              gradle --version
              gradle wrapper --gradle-version 6.7 --distribution-type bin
              ./gradlew --version
            '''
          }
          else {
            bat '''
              gradle --version
              gradle wrapper --gradle-version 6.7 --distribution-type bin
              gradlew.bat --version
            '''
          }
        }
      }
    }
    stage ('Build e2e tests project') {
      steps{
        catchError(buildResult: 'UNSTABLE', message: 'e2e tests compile failed', stageResult: 'FAILURE') {
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
    stage ('Install Symbol bootstrap') {
      when {
        environment ignoreCase: true, name: 'IS_BOOTSTRAP_RUN', value: 'true'
      }
      steps {
        script {
          if (params.BOOTSTRAP_VERSION == '') {
            error('ENVIRONMENT is bootstrap, but BOOTSTRAP_VERSION is not specified.')
          }
        }
        echo "Installing symbol bootstrap version ${params.BOOTSTRAP_VERSION}"
        sh "npm install -g symbol-bootstrap@${params.BOOTSTRAP_VERSION}"
        sh '''
          symbol-bootstrap -v
          symbol-bootstrap start -p bootstrap --detached
        '''
      }
    }
    stage ('Start Symbol bootstrap') {
      when {
        environment ignoreCase: true, name: 'IS_BOOTSTRAP_RUN', value: 'true'
      }
      steps {
        echo 'Starting symbol bootstrap...'
        sh label: 'Start Symbol bootstrap', script: 'symbol-bootstrap start -p bootstrap --detached'
      }
    }
    stage ('Check Symbol is running') {
      steps {
        script {
          echo 'Checking whether symbol is running at the given URL...'
          def nodeInfo = sh label: 'get node info', returnStdout: true, script: "curl ${API_URL}/node/info"
          def nodeHealth = sh label: 'get node health', returnStdout: true, script: "curl ${API_URL}/node/health"
          def versions = sh label: 'get server versions', returnStdout: true, script: "curl ${API_URL}/node/server"
          def chainInfo = sh label: 'get chain info', returnStdout: true, script: "curl ${API_URL}/chain/info"
          
          echo nodeInfo
          echo nodeHealth
          echo versions
          echo chainInfo
        }
      }
    }
    // stage ('Execute e2e tests') {
    //   steps{
    //     script {
    //       if (params.ENVIRONMENT == 'testnet') {

    //       }
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
  }
}