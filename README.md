# mas-competition-backend

## Local Development

### Database

Pre-requisites: Docker  
From the root directory

```
cd ./docker  
docker compose up
```

You can now navigate to localhost:8300 and use the following credentials to log into the database

```
username: user
password: password
```

Once you are adminer you will want to create a database called ```mascompetition```

### Default Users

Administrator

```
email: admin
password: admin
```

### Creating users

You can create more users by using the postman endpoint suite. Simply hit the login endpoint as the administrator and
then use the AddUsers endpoint

### Deployment

The backend application is deployed with the use of docker containers. The following steps are required in order to
deploy the application.

For staging:

1. Generate the JAR of the backend with the command ```./gradlew bootJar```
2. The docker backend docker image needs to be built. This can be achieved
   with ```docker build -t mas-competition-backend -f docker/Dockerfile .``` In the root directory of the project.
3. With the image built, navigate to ```./docker``` and run the following command ```docker compose up```.

The staging application will be deployed with the context ```/test/``` meaning to access what is usually ```/login```
you must hit ```/test/login```

For Production:  
To be completed
