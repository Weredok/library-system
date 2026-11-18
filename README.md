# Бібліотека — облік фонду, читачів і видач

Веб-система для бібліотеки: книжковий фонд, читачі, видача й повернення, автоматичне визначення боржників.

**Стек:** Java 17 · Spring Boot 3.3 · Spring Data JPA · Spring Security · Flyway · PostgreSQL · GWT 2.12 · Domino UI 2 · Domino REST

## Структура

```
library/
├── library-shared/     DTO, бізнес-правила, валідація — спільні для бекенду і GWT
├── library-backend/    Spring Boot: REST API, БД, безпека; віддає зібраний фронт як static
└── library-frontend/   GWT + Domino UI: односторінковий інтерфейс
```

Порядок модулів у батьківському `pom.xml` важливий: фронт збирається раніше, і бекенд копіює його JS у `static/library/`.

## Запуск

### 1. База даних

Створіть **нову порожню** базу. Таблиці створить Flyway з міграцій у `library-backend/src/main/resources/db/migration`:

```sql
CREATE DATABASE library;
```

> Схема відрізняється від початкової (див. розділ «Рішення» нижче), тому стару базу з ручними таблицями не використовуйте: Flyway на ній не запуститься.

Підключення задається змінними середовища (значення за замовчуванням вказані в дужках):

| Змінна | За замовчуванням |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/library` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `PORT` | `8080` |

### 2. Збірка і запуск

```bash
mvn clean package
java -jar library-backend/target/library-backend-1.0.0-SNAPSHOT.jar
```

Відкрийте http://localhost:8080 і увійдіть.

**Тестові користувачі** (з міграції `V3__seed.sql`):

| Логін | Пароль | Роль |
|---|---|---|
| `admin` | `admin` | ADMIN — усе, включно з категоріями і списанням |
| `librarian` | `librarian` | LIBRARIAN — усе, крім категорій і списання |

Паролі в міграції записані як `{noop}`, і при першому вході автоматично перехешовуються в bcrypt. Якщо база доступна не тільки вам, змініть їх.

### 3. Режим розробки (зміни у фронті без перезбірки)

1. Запустіть бекенд з IDE (`LibraryApplication`), задавши змінні БД.
2. В іншому терміналі запустіть GWT codeserver:
   ```bash
   mvn gwt:codeserver -pl library-frontend -am
   ```
   Він кладе `library.nocache.js` у `library-backend/target/classes/static/library/`.
3. Відкрийте http://localhost:8080. Після змін у коді фронту просто оновіть сторінку, і GWT перекомпілює зміни.

Якщо зробили `mvn clean`, codeserver треба перезапустити.

### GitHub Codespaces

У репозиторії є `.devcontainer/`: Codespace сам піднімає Java 17, Maven і окремий контейнер з Postgres (база `library`, логін і пароль `postgres`). Змінні `DB_*` вже задані.

```bash
mvn clean package -DskipTests
java -jar library-backend/target/library-backend-1.0.0-SNAPSHOT.jar
```

Відкрий порт **8080** на вкладці *Ports*.

GWT dev-режим (`gwt:codeserver`) нормально працює тільки якщо Codespace відкритий у **VS Code на комп'ютері**: тоді порти 8080 і 9876 прокидаються на `localhost`. У браузерній версії Codespaces codeserver живе на іншому піддомені, і сторінка до нього не достукається. Там після змін у фронті перезбирай: `mvn -pl library-frontend -am package -DskipTests`, потім `mvn -pl library-backend package -DskipTests` і перезапуск бекенду.

### Тести

```bash
mvn test                       # юніт-тести правил видачі (LoanServiceTest), БД не потрібна
mvn verify -Pit                # + інтеграційний тест конкурентності на реальній БД (DB_URL)
```

`LoanConcurrencyIT` запускає 8 потоків, які одночасно видають останній примірник. Пройти має рівно одна видача. Тест створює свої книгу й читачів і прибирає за собою.

## Бізнес-правила

Константи лежать у `library-shared/.../LibraryRules.java`.

- Термін видачі — **14 днів**. Продовження — ще на **14 днів**, максимум **1 раз** і тільки якщо термін ще не минув.
- Не більше **5 книг** на руках в одного читача.
- Читачу з **простроченою** книгою нова видача заблокована.
- Ту саму книгу не можна видати тому ж читачу вдруге, поки він не поверне першу.
- Видача можлива, лише якщо є вільний примірник: `total_copies − активні видачі > 0`.
- Кількість примірників не можна зменшити нижче кількості виданих зараз.
- Книгу не можна списати, а читача архівувати, поки в них є неповернені видачі.
- Номер квитка генерується з послідовності БД у форматі `R-<рік>-<000001>` і ніколи не змінюється.
- «Сьогодні» рахується в поясі `library.zone` (Europe/Kyiv).

## Рішення, що відрізняються від початкового ТЗ / схеми

| Було | Стало | Чому |
|---|---|---|
| `available_copies` у таблиці | рахується з `loan` у запиті | значення не може розійтися з реальністю |
| `status` у `loan` | рахується з `return_date` і `due_date` | без дублювання; прострочка не потребує cron |
| `ON DELETE CASCADE` | `RESTRICT` + архівування (`archived_at`) | історія видач не зникає |
| — | `app_user`, `issued_by`, `received_by` | журнал аудиту: хто видав і хто прийняв |
| — | `renewal_count` | продовження видачі |
| — | CHECK-обмеження, часткові індекси | цілісність і швидкість на рівні БД |

**Конкурентність:** видача блокує рядки читача й книги (`SELECT … FOR UPDATE`, див. `LoanService`), тому два бібліотекарі не можуть одночасно видати останній примірник або обійти ліміт.

**Валідація** — у трьох місцях: на клієнті (той самий клас `Validation` із shared, помилку видно одразу), на сервері (клієнту не довіряємо) і в БД (UNIQUE/CHECK як останній рубіж, порушення перетворюється на зрозуміле повідомлення в `GlobalExceptionHandler`).

**CSRF:** токени вимкнено. Натомість сесійна cookie має `SameSite=Strict`, і цього достатньо для навчального проєкту. Для продакшну варто ввімкнути CSRF-токени Spring Security.

## REST API

Усі ендпоінти, крім входу, вимагають сесії. Помилки повертаються у форматі `{"code": "...", "message": "...", "details": [...]}`.

| Метод | Шлях | Опис |
|---|---|---|
| POST | `/api/auth/login` | `{username, password}` → поточний користувач |
| GET | `/api/auth/me` | хто увійшов |
| POST | `/api/auth/logout` | вихід |
| GET | `/api/categories` | список категорій |
| POST / PUT / DELETE | `/api/categories[/{id}]` | керування (ADMIN) |
| GET | `/api/books?q=&categoryId=&availableOnly=&page=&size=` | пошук книг |
| GET / POST / PUT | `/api/books[/{id}]` | книга |
| DELETE | `/api/books/{id}` | списання (ADMIN) |
| GET | `/api/readers?q=&page=&size=` | пошук читачів |
| GET / POST / PUT | `/api/readers[/{id}]` | читач |
| GET | `/api/readers/{id}/loans` | історія видач читача |
| DELETE | `/api/readers/{id}` | архівування (ADMIN) |
| GET | `/api/loans?filter=ALL\|ON_HAND\|OVERDUE\|RETURNED&readerId=&bookId=&page=&size=` | журнал видач |
| POST | `/api/loans` | `{readerId, bookId}` — видача |
| POST | `/api/loans/{id}/return` | повернення |
| POST | `/api/loans/{id}/renew` | продовження |
| GET | `/api/overdue` | боржники |

Перевірка через curl:

```bash
curl -c c.txt -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin"}' localhost:8080/api/auth/login
curl -b c.txt 'localhost:8080/api/books?q=java'
curl -b c.txt localhost:8080/api/overdue
```

## Що може не зібратись з першого разу

Проєкт писався без доступу до Maven Central, тому повністю не компілювався (перевірено тільки синтаксис). Найімовірніші місця:

1. **Версії в батьківському `pom.xml`**: `domino-ui`, `domino-rest`, `domino-jackson`. Звірте з тими, що у вас на роботі, або з Maven Central.
2. **Пакет JAX-RS-анотацій**: `jakarta.ws.rs` чи `javax.ws.rs`, залежить від версії domino-rest. Імпорти лежать тільки в `library-frontend/.../rest/*.java`.
3. **API Domino UI 2.x.** Усі виклики компонентів (Button, TextBox, IntegerBox, CheckBox, Dialog, Notification) зібрані в `library-frontend/.../ui/` (`Ui`, `Modal`, `Notifier`, `Picker`, `Pager`). Сторінки напряму з Domino майже не працюють, тож правки будуть локальні.
4. **Імена GWT-модулів** у `library-frontend/src/main/module.gwt.xml`: `org.dominokit.domino.ui.DominoUI`, `org.dominokit.rest.Rest`.
5. **Шлях до CSS Domino UI** в `library-backend/src/main/resources/static/index.html`. Подивіться, що реально лежить у `target/classes/static/library/css/`.
