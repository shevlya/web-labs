-- TRUNCATE очищает таблицу полностью.
-- CASCADE автоматически удалит связанные записи в task и user_roles
TRUNCATE TABLE user_account CASCADE;

-- Cбросить счетчик ID к 1:
ALTER SEQUENCE user_account_id_seq RESTART WITH 1;
ALTER SEQUENCE task_id_seq RESTART WITH 1;

### 1. Регистрация администратора
POST http://localhost:8080/users/register
Content-Type: application/json

{
"username": "admin",
"password": "neuigkeit123"
}