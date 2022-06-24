First run

```shell
docker run -p 3310:3310 mkodockx/docker-clamav:alpine
```

Then after it started up, run

```shell
mvn clean package && java -jar target/poc-clamd-virusscan-1.0.jar
```


