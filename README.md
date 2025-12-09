# SystemHotelowy

## O systemie
Projekt został zrealizowany w języku **Java 17** przy użyciu frameworka **Spring Boot 3.0.5** oraz **Vaadin Flow** dla warstwy prezentacji.

Aplikacja oferuje podział na role użytkowników (Menedżer, Pracownik), zapewniając dedykowane panele i funkcjonalności:
- **Panel Menedżera**: Umożliwia zarządzanie pokojami, przeglądanie kalendarza rezerwacji, analizę statystyk (KPI) oraz przydzielanie zadań personelowi.
- **Panel Pracownika**: Pozwala na podgląd przydzielonych zadań (np. sprzątanie pokoju) oraz aktualizację ich statusu.

System implementuje bezpieczne uwierzytelnianie oparte na **Spring Security**.

## Baza danych

Aplikacja korzysta z relacyjnej bazy danych **MySQL 8.0**, która jest uruchamiana w izolowanym środowisku za pomocą **Docker**.

### Konfiguracja i uruchomienie
Aby uruchomić bazę danych, należy wykonać polecenie w katalogu głównym projektu:

```bash
docker-compose up -d
```

### Dane dostępowe (konfigurowalne w `docker-compose.yml`):
- **Host**: `localhost`
- **Port**: `3306`
- **Baza danych**: `hotel`
- **Użytkownik**: `user`
- **Hasło**: `userpass`
- **Hasło root**: `rootpass`

Struktura bazy danych jest automatycznie zarządzana przez Hibernate
```properties
# w pliku resources/application.propeties
spring.jpa.hibernate.ddl-auto=update
```
Ta adnotacja oznacza, że tabele są tworzone lub aktualizowane przy każdym starcie aplikacji.

## Podstawowe strony

- **Strona główna**: [http://localhost:8080](http://localhost:8080)
- **Swagger API**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Struktura projektu

Poniżej znajduje się opis struktury katalogów i kluczowych plików w aplikacji:

- **`src/main/java/org/systemhotelowy`** - Główny katalog z kodem źródłowym Java.
    - **`config`** - Konfiguracja aplikacji 
        - Kwestie bezpieczeństwa
            - `SecurityConfig`
            - `UserSecurity`
            - `VaadinSecurityConfig`
        - Inicjalizacja danych
            - `DataInitializer`
    - **`dto`** - Obiekty transferu danych (Data Transfer Objects), służące do przesyłania danych między warstwami.
    - **`model`** - Encje JPA reprezentujące tabele w bazie danych (główne modele: `Room`, `Task`, `User`, `Reservation`).
    - **`repository`** - Interfejsy repozytoriów Spring Data JPA do komunikacji z bazą danych.
    - **`service`** - Warstwa logiki biznesowej. Zawiera interfejsy i ich implementacje w folderze `service/impl`.
    - **`ui`** - Warstwa prezentacji oparta na frameworku Vaadin.
        - **`components`** - Reużywalne komponenty UI, takie jak dialogi (`TaskBatchDialog`, `ReservationFormDialog`), gridy (`TaskGrid`, `RoomGrid`) czy pasek nawigacji (`DashboardTopBar`).
        - **`EmployeeDashboard`** - Widoki dedykowane dla panelu pracownika.
        - **`ManagerDashboard`** - Widoki dedykowane dla panelu menedżera.
        - główne widoki aplikacji `LoginView` i `MainView`.
    - **`utils`** - Klasy narzędziowe
        - `NotificationUtils` do obsługi powiadomień
        - `JwtAuthenticationFilter` do filtrowania uwierzytelniania 
        - `VaadinSecurityHelper` do wspierania logiki bezpieczeństwa Vaadin

- **`src/main/resources`**
    - `application.properties` - Główny plik konfiguracyjny aplikacji (ustawienia bazy danych, portu itp.).

- **`frontend`** - Pliki zasobów statycznych i stylów dla Vaadin Flow.

- **`docker-compose.yml`** - Plik tworzący kontener bazodanowy MySQL.

## Kryteria oceny (Git)

### Wykonanie commita

| Poziom | Opis                    | Punkty |
| ------ | ----------------------- | ------ |
| 1      | Brak wykonania commita  | 0      |
| 2      | Jeden commit            | 1      |
| 3      | Dwa lub więcej commitów | 2      |

### Wykonanie operacji Pull Requesta

| Poziom | Opis                                                          | Punkty |
| ------ | ------------------------------------------------------------- | ------ |
| 1      | Nie wykonanie MR                                              | 0      |
| 2      | Wykorzystanie zdalnego repozytorium bez MR                    | 1      |
| 3      | Wykonanie jednego MR                                          | 2      |
| 4      | Wykonanie dwóch lub więcej MR                                 | 3      |
| 5      | Ułożenie historii kodu w postaci „schodków” – squash commitów | 4      |

### Zasady SOLID - zastosowanie ze wskazaniem w kodzie

| Poziom | Opis                        | Punkty |
| ------ | --------------------------- | ------ |
| 1      | Brak stosowania zasad SOLID | 0      |
| 2      | 1 zasada                    | 1      |
| 3      | 2 zasady                    | 2      |
| 4      | 3 zasady                    | 3      |
| 5      | 4 zasady                    | 4      |

### Zasady SOLID - liczba błędów

| Poziom | Opis               | Punkty |
| ------ | ------------------ | ------ |
| 1      | Więcej niż 3 błędy | 0      |
| 2      | 1 błąd             | 1      |
| 3      | 2 błędy            | 2      |
| 4      | 0 błędów           | 3      |

### Czysty kod - poprawność nazywania metod klas

| Poziom | Opis               | Punkty |
| ------ | ------------------ | ------ |
| 1      | Więcej niż 3 błędy | 0      |
| 2      | 2 błędy            | 1      |
| 3      | 1 błąd             | 2      |
| 4      | 0 błędów           | 3      |

### Testy jednostkowe

| Poziom | Opis                                 | Punkty |
| ------ | ------------------------------------ | ------ |
| 1      | Brak testów jednostkowych            | 0      |
| 2      | Zastosowanie jednego testu           | 2      |
| 3      | Zastosowanie więcej niż dwóch testów | 4      |

### Testy integracyjne

| Poziom | Opis                        | Punkty |
| ------ | --------------------------- | ------ |
| 1      | Brak testów integracyjnych  | 0      |
| 2      | Testowanie metody GET       | 2      |
| 3      | Testowanie metod GET i POST | 4      |

### Obrona projektu

| Poziom | Opis                                          | Punkty |
| ------ | --------------------------------------------- | ------ |
| 1      | Brak informacji o tym, co dzieje się w kodzie | 0      |
| 2      | Częściowa informacja o działaniu kodu         | 2      |
| 3      | Pełna informacja o działaniu kodu             | 4      |

### Podział na moduły

| Poziom | Opis              | Punkty |
| ------ | ----------------- | ------ |
| 1      | Brak podziału     | 0      |
| 2      | Częściowy podział | 1      |
| 3      | Pełny podział     | 2      |

## Kryteria oceny (Java)

```
Warunkiem zaliczenia przedmiotu jest oddanie projektu oraz prezentacja działającej
aplikacji. Projekt może być wykonany samodzielnie lub w zespole 2-3 osobowym.
Zakres projektu obejmuje:
• aplikację w architekturze klient – serwer (Android, JavaFX, Vaadin, …, - Spring Boot)
• połączenie z bazą danych (co najmniej podstawowe operacje bazodanowe),
• uwierzytelnianie użytkowników z podziałem na role (np. token JWT, Spring Security),
• serializację danych (import/export plików XML/JSON),
• testy jednostkowe i integracyjne,
• dokumentację projektu (co najmniej opis REST API [Swagger] i opis działania
projektu/uruchomienia/wymagań systemowych [README.md])
Mile widziany jest:
• harmonogram rozwoju projektu (Jira, Asana, Kate, Notatnik),
• system kontroli wersji (GIT),
• Docker.
W przypadku zespołów wieloosobowych należy bezsprzecznie określić udział
poszczególnych osób w wykonaniu zadań. 
```