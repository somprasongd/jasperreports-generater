# jasperreports-generater

## How to build Docker image
- Run maven Build
- Run command `docker buildx build --push --platform linux/amd64,linux/arm64 -t somprasongd/jasperreports-generater .`

## How to run
- Run `docker compose up -d`

## How to use

```bash
curl --location 'http://localhost:8080/api/v1/jasper/generate' \
--header 'Content-Type: application/json' \
--data '{
    "dbUrl": "jdbc:postgresql://localhost:5432/test?user=fred&password=secret&ssl=true",
    "mainReport": {
        "name": "test",
        "url": "http://minio/report/test.jrxml"
    },
    "parameters": [
        {
            "name": "id",
            "type": "string",
            "value": "1234"
        }
    ]
}'
```