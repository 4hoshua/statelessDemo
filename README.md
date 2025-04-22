## Como rodar

- montar a imagem do postgress `docker-compose up --build -d`
- rodar o backend `mvn spring-boot:run`
- rodar o client em backend\src\main\resources\client `python -m http.server 3000`

## Endpoints

- `POST /people` - para criar uma pessoa com seu departamento e inserir na fila correspondente
- `GET /people/getAll/departamento_{department}` - obter todas as pessoas que estão em uma fila de departamento
- `DELETE /people/getNext/departamento_{department}` - deleta a pessoa do banco de dados e a chama na fila

## Documentação usada

- [Spring WebSocket](https://docs.spring.io/spring-framework/docs/5.3.9/reference/html/web.html#websocket)