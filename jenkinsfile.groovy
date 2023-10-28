pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-south-1'
        S3_BUCKET = 'my-new-angular-bucket'
        EC2_INSTANCE = 'http://3.6.92.89/'
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

        stage('Upload to S3') {
            steps {
                script {
                    sh "aws s3 sync dist/ s3://${S3_BUCKET}"
                }
            }
        }

        // stage('Deploy to EC2') {
        //     steps {
        //         script {
        //             sshagent(credentials: ['my-ssh-credential']) {
        //                 sh "scp -o StrictHostKeyChecking=no -i /path/to/your/key.pem -r dist/ ec2-user@${EC2_INSTANCE}:/path/to/destination/on/ec2/"
        //                 sh "ssh -o StrictHostKeyChecking=no -i /path/to/your/key.pem ec2-user@${EC2_INSTANCE} 'sudo systemctl restart your-app-service'"
        //             }
        //         }
        //     }
        // }
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
