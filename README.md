## Запуск базы данных в Docker

Для локального запуска базы данных PostgreSQL с вашими настройками используйте следующую команду:

```bash
docker run -d \
  --name jdbc-resale-postgres \
  -e POSTGRES_DB=postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -c max_connections=30 \
  postgres:17
```

### Параметры подключения (согласно DbConfig):
* **URL:** `jdbc:postgresql://localhost:5432/postgres`
* **Пользователь:** `postgres`
* **Пароль:** `postgres`

