pipeline {
    agent any
    environment {
        ANGULAR_APP_DIR = "/var/www/angular-app"
        SSH_KEY = credentials('my-ssh-key-credential')
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build Angular App') {
            steps {
                sh 'cd $ANGULAR_APP_DIR && ng build --prod'
            }
        }

        // stage('Deploy to EC2') {
        //     steps {
        //         script {
        //             sh "scp -i $SSH_KEY -r $ANGULAR_APP_DIR/dist/* ec2-user@localhost:/var/www/html/"
        //         }
        //     }
        // }

        // stage('Restart Web Server on EC2') {
        //     steps {
        //         script {
        //             sh "ssh -i $SSH_KEY ec2-user@localhost 'sudo systemctl restart nginx'"
        //         }
        //     }
        // }
    }
}
