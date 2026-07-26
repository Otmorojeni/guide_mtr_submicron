# ⚙️ АИС «Справочник МТР» — Модуль «Субмикрон»

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-blue?style=for-the-badge&logo=openjfx)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%2B-336791?style=for-the-badge&logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?style=for-the-badge&logo=apachemaven)

Десктопная автоматизированная информационная система (АИС) для учета, ведения и структурирования справочника материально-технических ресурсов (МТР) и их физико-химических свойств. 

Разработано в рамках практики/исследовательской работы по направлению разработки ПО ЭВМ и автоматизированных систем.

---

## 📌 Основные возможности

* **Иерархическая навигация (`TreeView`):** Отображение ресурсов в виде трехуровневого дерева: `Раздел ➔ Подраздел ➔ Материал (Газ/МТР)`.
* **Полный цикл CRUD-операций:**
  * **Create (➕ Добавление):** Вставка нового материала в выбранный подраздел с автоматической связкой внешних ключей.
  * **Read (🔄 Чтение):** Динамическая загрузка структуры категорий и вывод карточки химических и технических свойств выбранного ресурса.
  * **Update (💾 Изменение):** Транзакционное обновление параметров ресурсов и ленивое создание отсутствующих свойств (**UPSERT**).
  * **Delete (❌ Удаление):** Мягкое (логическое) удаление записей путем изменения статуса `deleted = true` без физического уничтожения архивов.
* **Транзакционная надежность (ACID):** Синхронное изменение данных сразу в двух связанных таблицах PostgreSQL с гарантией отката (`rollback()`) при ошибках.
* **Защита от сбоев целостности:** Использование подзапросов `COALESCE` для автоподбора существующих внешних ключей (`idunit`, `idclassclean`).

---

## 🏗️ Архитектура и стек технологий

Проект реализован без применения heavy-ORM фреймворков и FXML-разметки. Это обеспечивает полный прозрачный контроль над SQL-запросами, транзакциями и графическим потоком UI:

* **Язык программирования:** Java 17+
* **Графический интерфейс (GUI):** JavaFX (чистый код на Java, паттерн View)
* **База данных:** PostgreSQL
* **Взаимодействие с БД:** JDBC (`PreparedStatement`, `Connection`, ручное управление транзакциями)
* **Архитектурный паттерн:** DAO (Data Access Object) / Repository Pattern
* **Система сборки:** Apache Maven

---

## 🗄️ Структура базы данных (Схема `dbsubmicron`)

Приложение работает со следующей связкой таблиц:

1. `dbsubmicron.skto_razdel` — Справочник верхнего уровня (Разделы).
2. `dbsubmicron.skto_podrazdel` — Справочник подразделов (`idrazdel` ➔ `skto_razdel.id`).
3. `dbsubmicron.mtr_material` — Таблица материалов и газов (`idpodrazdel` ➔ `skto_podrazdel.id`).
4. `dbsubmicron.mtr_himmaterproperty` — Подробные химические свойства и условия безопасности (`idmtr` ➔ `mtr_material.id`).

```text
[skto_razdel]
      ▲
      │ (1:N)
[skto_podrazdel]
      ▲
      │ (1:N)
[mtr_material] ◀───(1:1)───▶ [mtr_himmaterproperty]
```

---

## 📁 Структура исходного кода

```text
src/main/java/com/guide/
│
├── database/
│   └── DatabaseHandler.java     # Управление подключением к СУБД PostgreSQL через JDBC
│
├── models/                      # Модели данных (POJO)
│   ├── Razdel.java              # Сущность Раздела
│   ├── Podrazdel.java           # Сущность Подраздела
│   ├── Material.java            # Сущность Материала/Газа
│   └── MaterialProperty.java    # Сущность Хим. свойств
│
├── repos/
│   └── MtrRepository.java       # DAO-слой: SQL-запросы, транзакции, CRUD, UPSERT
│
└── views/
    └── MainStage.java           # GUI-слой: Верстка JavaFX (SplitPane, TreeView, GridPane)
```
---

## 🚀 Инструкция по запуску и настройке
1. Подготовка базы данных (PostgreSQL)
Убедитесь, что служба PostgreSQL запущена.

Создайте схему dbsubmicron и соответствующие таблицы.

При рассинхронизации счетчиков первичных ключей (SERIAL) выполните команду выравнивания последовательности:

SELECT setval('dbsubmicron.mtr_material_id_seq', (SELECT MAX(id) FROM dbsubmicron.mtr_material));

2. Конфигурация подключения
Укажите параметры доступа к вашей СУБД в файле src/main/java/com/guide/database/DatabaseHandler.java:

private static final String URL = "jdbc:postgresql://localhost:5432/имя_вашей_базы";
private static final String USER = "postgres";
private static final String PASSWORD = "ваш_пароль";

3. Сборка и запуск через Maven
Из корневой папки проекта выполните команду:

mvn clean compile javafx:run

---

## 🛠️ Технические особенности реализации (Для защиты проекта)
Защита от SQL-инъекций: Все SQL-команды с переменными сформированы с использованием PreparedStatement.

Безопасная обработка UI-потока: Перехват и вывод ошибок СУБД обернуты в Platform.runLater(), что гарантирует отзывчивость главного потока JavaFX.

Безопасный парсинг чисел: Ввод числовых значений («Срок хранения», «Класс опасности») защищен проверками try-catch (NumberFormatException), сбрасывающими некорректный ввод в дефолтный 0.

Сброс кэша UI: При сохранении и обновлении данных TreeView принудительно очищает предыдущую иерархию узлов, исключая дублирование строк в памяти.