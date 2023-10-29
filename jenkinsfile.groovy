pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-south-1'
        S3_BUCKET = 'my-new-angular-bucket'
        EC2_INSTANCE = '13.233.196.170'
        SSH_CREDENTIALS = credentials('my-ssh-credential')
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout your source code from your version control system (e.g., Git)
                checkout scm
            }
        }

        stage('Build Angular App') {
            steps {
                sh 'npm install'
                sh 'npm run ng build --prod'
            }
        }

      // stage('Upload to S3') {
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
                    sshagent(credentials: ['my-ssh-credential']) {
                        sh "scp -r dist/ ubuntu@${EC2_INSTANCE}:/var/www/html/"
                        sh "ssh ubuntu@${EC2_INSTANCE} 'sudo systemctl restart apache2'"
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
