# SystemHotelowy

## Baza danych

System współpracuje z bazą danych MySQL oparta na kontenerze Docker. Aby utworzyć bazę danych, trzeba użyć następującego polecenia:

```bash
docker-compose up -d
```

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