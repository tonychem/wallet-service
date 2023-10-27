Wallet-service
-----
Приложение, имитирующее сервис по переводу денег между игроками.

Допустимы следующие взаимодействия: 
1. Пользователь может переводить со своего баланса на другой любую доступную сумму. 
2. Пользователь может отправить другому игроку запрос на получение денежной суммы с его аккаунта.
3. Игрок, получивший запрос на пополнение чужого баланса может принять или отклонить эту заявку.
4. Просмотр истории пополнений/списаний.

Взаимодействие с пользователем происходит через REST API. Для целей тестирования к моменту первого запуска в БД 
содержится два пользователя: admin(login=admin, pwd=admin) и user(login=user, pwd=user).

Список endpoint-ов:
1. POST /registration - зарегистрировать игрока
2. POST /login - зайти в приложение
3. DELETE /logout - выйти из приложения
4. GET /player-management/wallet/balance - получить баланс пользователя
5. GET /player-management/wallet/history - получить историю транзакций пользователя
6. POST /player-management/money-transfer - отправить деньги другому игроку
7. POST /player-management/money-request - отправить запрос на получение денег от другого игрока
8. GET /player-management/money-request - получить список всех неодобреных заявок на перевод средств другим игрокам
9. POST /player-management/money-request/approve - подтвердить заявки на перевод денежных средств
10. POST /player-management/money-request/decline - отклонить заявки на перевод денежных средств
----
Под капотом на уровне приложения создан примитивный механизм безопасности посредством создания сессий: 
каждый клиент после авторизации/регистрации получает UUID токен, который должен быть предоставлен контроллеру
для подтверждения наличия открытой сессии со стороны сервиса. 

Передача токена транзакции в явном виде не предусмотрена. При переводе денег или запросе денежной суммы этот токен
генерируется автоматически.
----

Для запуска приложения:
1. Клонировать проект 
```shell
git clone --branch hw2 https://github.com/tonychem/wallet-service.git
```
2. Перейти в папку проекта *wallet-service* в командной строке и собрать проект локально, используя Maven:
```shell
mvn package
```
3. Запустить PostgreSQL в докере. Для этого запустить команду из той же папки:
```shell
docker compose -f ./src/main/java/docker-compose.yml up -d
```
4. Развернуть .war файл из папки ./target в контейнере сервлетов. 
