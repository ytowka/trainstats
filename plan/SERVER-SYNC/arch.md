# Архитектурные решения бэкенда


## Стэк
Kotlin + Spring на все сервисы


## Границы сервисов и коммуникации
Деление на микросервисы:
1. Auth, 
2. Workouts, 
3. Statistics,
4. Notifications

REST везде, для notification kafka

## Данные
- База данных на каждый сервис своя
- 