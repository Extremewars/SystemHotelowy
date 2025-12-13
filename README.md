# SystemHotelowy

## O systemie
Projekt został zrealizowany w języku **Java 17** przy użyciu frameworka **Spring Boot 3.0.5** oraz **Vaadin Flow** dla warstwy prezentacji.

Aplikacja oferuje podział na role użytkowników (Menedżer, Pracownik), zapewniając dedykowane panele i funkcjonalności:
- **Panel Menedżera**: Umożliwia zarządzanie pokojami, przeglądanie kalendarza rezerwacji, analizę statystyk (KPI) oraz przydzielanie zadań personelowi.
- **Panel Pracownika**: Pozwala na podgląd przydzielonych zadań (np. sprzątanie pokoju) oraz aktualizację ich statusu.

System implementuje bezpieczne uwierzytelnianie oparte na **Spring Security**.

## Funkcjonalności
- Rejestracja i logowanie użytkowników z podziałem na role (Menedżer, Pracownik).
- Zarządzanie pokojami hotelowymi (dodawanie, edytowanie, usuwanie).
- Przeglądanie i zarządzanie rezerwacjami pokoi.
- Przydzielanie i monitorowanie zadań dla personelu hotelowego.
- Generowanie raportów dotyczących działalności hotelu.
- Interfejs użytkownika oparty na Vaadin Flow, zapewniający responsywny i intuicyjny design.

## Baza danych

Aplikacja korzysta z relacyjnej bazy danych **MySQL 8.0**, która jest uruchamiana w izolowanym środowisku za pomocą **Docker**.

### Konfiguracja i uruchomienie
Aby uruchomić bazę danych, należy wykonać polecenie w katalogu głównym projektu:

```bash
docker-compose up -d
```

### Przeglądanie bazy danych
Aby zobaczyć zawartość bazy danych w kontenerze Docker, można użyć tego polecenia w terminalu:

```bash
docker exec -it hotel-mysql mysql -u root -p
```
Domyślne hasło dla użykownika root to `rootpass`. 
Domyślna nazwa bazy danych to `hotel`. 
Powinny występować tabele o nazwach `users`, `rooms`, `reservations`, `tasks`.
Po zalogowaniu do MySQL można wykonywać zapytania SQL, takie jak:
```bash
USE hotel; #przełącza na bazę danych hotel
SHOW tables; #wyświetla listę tabel w bazie danych
SELECT * FROM users; #wyświetla zawartość tabeli users
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
        - Konfiguracja Swaggera
            - `OpenApiConfig`
    - **`controller`** - Kontrolery REST obsługujące żądania HTTP
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

## Autorzy
- Svitlana Lysiuk
- Paweł Olech
- Rafał Oleszczak