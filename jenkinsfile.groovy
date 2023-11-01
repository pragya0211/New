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
              sh 'aws cloudfront create-invalidation --distribution-id ${INVALIDATION_ID} --paths ${PATHS}'
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
