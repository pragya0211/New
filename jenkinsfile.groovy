pipeline {
    agent any

    environment {
        S3_BUCKET = 'my-new-angular-bucket'
        INVALIDATION_ID = 'E2CKHSL2OK1H7A'
        PATHS = "/*"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Angular App') {
            steps {
                sh 'npm install'
                sh 'npm run ng build --prod'
            }
        }

      // stage('Deploy to S3') {
      //   steps {
      //       script {
      //           withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'my-aws-credential']]) {
      //               sh "aws s3 sync dist/pragya/ s3://${S3_BUCKET}"
      //           }
      //       }
      //   }
      // }


        stage('Deploy to EC2') {
            steps {
                script {
                    sh 'cp -r dist/pragya/* /var/www/html'
                }
            }
        }

      stage('cloud front invalidation') {
        steps {
          script {
            withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'my-aws-credential']]) {
              cmd = "aws cloudfront list-distributions | jq '.DistributionList.Items[]|[ .Id, .Status, .Origins.Items[0].DomainName, .Aliases.Items[0] ] | @tsv ' -r"
            def list = sh(script: cmd, returnStdout: true)
            echo list 

            writeFile file: 'cloudfront_list.txt', text: list 
            if(params.domain){
              // get distribution id
              get_cmd = """grep ${params.domain} 'cloudfront_list.txt' | awk '{print \$1}'"""
              DISTRIBUTION_ID = sh(script: get_cmd, returnStdout: true)
              println "$key = $DISTRIBUTION_ID"

              // create invalidation id
              create_cmd = """aws cloudfront create-invalidation --distribution-id $DISTRIBUTION_ID --paths "/*" | jq -r .Invalidation.Id"""
              echo create_cmd 
              INVALIDATION_ID = sh(script: create_cmd, returnStdout: true)

              // invalidate distribution and wait for finish
              wait_cmd = """aws cloudfront wait invalidation-completed --distribution-id $DISTRIBUTION_ID --id $INVALIDATION_ID"""
              sh(wait_cmd)
              // sh 'aws cloudfront create-invalidation --distribution-id ${INVALIDATION_ID} --paths ${PATHS}'
            }
          }
        }
      }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed. Check the build logs for details.'
        }
    }
}
