import java.nio.file.Paths

pipeline {
  agent {
    label 'ubuntu-20.04-8cores-16Gig'
  }
  options{
    timestamps()
    ansiColor('xterm')
  }
  triggers {
    cron '''TZ=America/New_York
      # This job needs to be started once every day between midnight and 2AM US Eastern time
      H H(0-2) * * *
    '''
  }
  parameters {
    gitParameter(
      name: 'TESTS_VERSION', defaultValue: 'origin/main',
      description: 'Name of the branch or tag from Symbol e2e tests repo to checkout and run tests from.',
      listSize: '10', quickFilterEnabled: false, selectedValue: 'DEFAULT', sortMode: 'ASCENDING_SMART',
      tagFilter: '*', type: 'PT_BRANCH_TAG'
    )
    choice(name: 'ENVIRONMENT', choices: ['bootstrap', 'testnet'], description: '''Environment to run the tests against.
testnet: The tests will be executed against the given testnet environment specified by the TESTNET_API_URL param.
bootstrap: The tests will be executed against a clean bootstrap environment brought up locally using symbol-bootstrap tool version specified by BOOTSTRAP_VERSION param.''')
    string(name: 'TESTNET_API_URL', defaultValue: 'http://54.176.80.148:3000', description: 'The URL of the testnet API.')
    string(name: 'BOOTSTRAP_VERSION', defaultValue: 'latest', description: 'symbol-bootstrap tool version to install and start bootstrap with.')
    string(name: 'E2E_TEST_USER_PRIVATE_KEY', defaultValue: '4294AD641E2A04C312D44D1F3ECF2A70629184FCCACC366ABA02AC79D80FB237', description: 'Automation user private key.')
  }
  environment {
    IS_BOOTSTRAP_RUN  = "${params.ENVIRONMENT == 'bootstrap' ? 'true' : 'false'}"
    SYMBOL_API_URL = "${params.ENVIRONMENT == 'testnet' ? params.TESTNET_API_URL : 'http://localhost:3000'}"
  }
  tools {
    nodejs 'nodejs-15.0.1'
    gradle 'gradle-6.7'
  }
  stages {
    stage ('Setup gradle env') {
      steps {
        script {
          runScript('''gradle --version
              gradle wrapper --gradle-version 6.7 --distribution-type bin
            ''')
          runGradle('--version')
        }
      }
    }
    stage ('Build e2e tests project') {
      steps{
        catchError(buildResult: 'UNSTABLE', message: 'e2e tests compile failed', stageResult: 'FAILURE') {
          script {
            runGradle('--project-dir symbol-e2e-tests/ --refresh-dependencies --rerun-tasks clean testClasses')
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
        runScript("""npm install -g symbol-bootstrap@${params.BOOTSTRAP_VERSION}
          symbol-bootstrap -v
        """)
      }
    }
    stage ('Start Symbol bootstrap') {
      when {
        environment ignoreCase: true, name: 'IS_BOOTSTRAP_RUN', value: 'true'
      }
      steps {
        dir ('symbol-bootstrap') {
          deleteDir()
          echo 'Starting symbol bootstrap...'
          script {
            def customPreset = ["logLevel": "Debug",
                                "dockerComposeDebugMode": true,
                                "fileDatabaseBatchSize": 100,
                                "importanceGrouping": 45,
                                "votingSetGrouping": 180,
                                "blockGenerationTargetTime": "10s",
                                "throttlingBurst": 950,
                                "throttlingRate": 900
                              ]

            writeYaml file: 'customPreset.yaml', data: customPreset
          }
          runScript('export COMPOSE_HTTP_TIMEOUT=200;symbol-bootstrap start -p bootstrap --detached  --noPassword -c customPreset.yaml', 'Start Symbol bootstrap')
        }
      }
    }
    stage ('Check Symbol is running') {
      steps {
        script {
          echo 'Checking whether symbol is running at the given URL...'
          if (IS_BOOTSTRAP_RUN.toLowerCase() == 'true') {
            dir ('symbol-bootstrap') {
              runScript("sleep 60;symbol-bootstrap healthCheck", 'bootstrap health check')
            }
          }
          def curlCmd = 'curl --silent --show-error'
          def nodeInfo = runScript("${curlCmd} ${SYMBOL_API_URL}/node/info", 'get node info', true)
          def nodeHealth = runScript("${curlCmd} ${SYMBOL_API_URL}/node/health", 'get node health', true)
          def versions = runScript("${curlCmd} ${SYMBOL_API_URL}/node/server", 'get server versions', true)
          def chainInfo = runScript("${curlCmd} ${SYMBOL_API_URL}/chain/info", 'get chain info', true)
          
          echo "Node info: ${nodeInfo}"
          echo "Node health: ${nodeHealth}"
          echo "Symbol versions: ${versions}"
          echo "Chain info: ${chainInfo}"
        }
      }
    }
    stage ('Extract info from Symbol') {
      when {
        environment ignoreCase: true, name: 'IS_BOOTSTRAP_RUN', value: 'true'
      }
      steps {
        script {
          dir ('symbol-bootstrap') {
            def addresses = readYaml file: 'target/addresses.yml'
            // variable declared without def becomes a global scoped variable and can be reassigned too
            automationUserPrivateKey = addresses.mosaics[0].accounts[0].privateKey
            echo "automationUserPrivateKey: ${automationUserPrivateKey}"
          }
          dir ('symbol-e2e-tests') {
            def props = readYaml file: 'src/test/resources/configs/config-default.yaml'
            echo "config-default.yaml read: ${props}"

            props.userPrivateKey = automationUserPrivateKey
            props.restGatewayUrl = env.SYMBOL_API_URL

            echo "config-default.yaml after update: ${props}"
            echo "apiHost value: ${props.apiHost}"

            writeYaml file: 'src/test/resources/configs/config-default.yaml', data: props, overwrite: true

            def propsAfterUpdate = readYaml file: 'src/test/resources/configs/config-default.yaml'
            echo "config-default.yaml after written update: ${propsAfterUpdate}"
          }
        }
      }
    }
    stage ('Execute e2e tests') {
      steps{
        script {
          AUTOMATION_TEST_USER_PRIVATE_KEY = "${params.ENVIRONMENT == 'testnet' ? params.E2E_TEST_USER_PRIVATE_KEY : automationUserPrivateKey}"
          echo "Automation user private key: ${AUTOMATION_TEST_USER_PRIVATE_KEY}"
          echo "Symbol API URL: ${env.SYMBOL_API_URL}"
          try {
              runGradle('--project-dir symbol-e2e-tests/ tests')
          }
          finally {
            dir ('symbol-e2e-tests') {
              sh 'ls -altr'
              stash includes: 'cucumber-report.json,cucumber-report.html', name: 'cucumber-reports'
            }
          }
        }
      }
    }
  }
  post{
    failure {
      echo "Tests failed"
      unstash 'cucumber-reports'
      cucumber failedFeaturesNumber: -1, 
              failedScenariosNumber: -1, 
              failedStepsNumber: -1, 
              fileIncludePattern: 'cucumber-report.json', 
              pendingStepsNumber: -1, 
              skippedStepsNumber: -1, 
              sortingMethod: 'ALPHABETICAL',
              undefinedStepsNumber: -1
      publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '.', reportFiles: 'cucumber-report.html', reportName: 'Cucumber HTML Report', reportTitles: ''])
    }
    success {
      echo "Tests passed"
      unstash 'cucumber-reports'
      cucumber failedFeaturesNumber: -1, 
              failedScenariosNumber: -1, 
              failedStepsNumber: -1, 
              fileIncludePattern: 'cucumber-report.json', 
              pendingStepsNumber: -1, 
              skippedStepsNumber: -1, 
              sortingMethod: 'ALPHABETICAL',
              undefinedStepsNumber: -1
      publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '.', reportFiles: 'cucumber-report.html', reportName: 'Cucumber HTML Report', reportTitles: ''])
    }
    always{
      dir ('symbol-bootstrap') {
        script {
          if (env.IS_BOOTSTRAP_RUN == 'true') {
            echo 'Stopping symbol bootstrap...'
            runScript('symbol-bootstrap stop', 'Stop symbol bootstrap')
          }
        }
      }
    }
  }
}

def runScript(String script, String label='', Boolean returnStdout=false, Boolean returnStatus=false, String encoding='') {
  if (isUnix()) {
    sh label: label, script: script, encoding: encoding, returnStdout: returnStdout, returnStatus: returnStatus
  }
  else {
    bat label: label, script: script, encoding: encoding, returnStdout: returnStdout, returnStatus: returnStatus
  }
}

def runGradle(String command) {
  if (isUnix()) {
    sh "./gradlew ${command}"
  }
  else {
    bat "gradlew.bat ${command}"
  }
}
