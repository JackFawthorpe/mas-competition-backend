# mas-competition-backend

## Local Development

### Database

Pre-requisites: Docker  
From the root directory

```
cd ./docker  
docker compose up
```

You can now navigate to localhost:8080 and use the following credentials to log into the database

```
username: root
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