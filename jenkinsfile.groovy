pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-south-1'
        S3_BUCKET = 'my-new-angular-bucket'
        EC2_INSTANCE = '65.1.2.123'
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

                    // sh 'cp -r dist/* /var/www/html'
                    // sh 'sudo systemctl restart apache2'
                    
                    sshagent(credentials: [SSH_CREDENTIALS]) {
                        sh 'cp -r dist/* /var/www/html'
                        sh "ssh -o StrictHostKeyChecking=no 'sudo systemctl restart apache2'"
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
