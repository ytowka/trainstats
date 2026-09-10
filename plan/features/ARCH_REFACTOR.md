Построй план, как я могу отрефакторить текущий @sources/shared/ модуль в многомодульную архитектуру, которая задается папками в sources/features -> основные фичи, sources/app/navigation -> навигация которая "знает" про все ui модулю и как их открыть. 
Файл agents.md не актуальный.

Цикл workout ↔ exercises давай решим так: завязка на workout в модуле exercise есть только в истории. Историю упражнений переносим в в workout, чтобы в exercise не было зависимостей на workout.

Граф зависимостей workout:
workouts:domain-api -> exercises:domain-api (через api())


Общий концепт завимисимостей модулей:

ui -> domain-api,
domain -> domain-api
data -> domain-api
data -> data-api

ui - слой с ui и viewmodels
domain-api - модельки, интерфейсы юзкейсов, интерфейсы репозиториев
domain - реализации интерфейсов из domain-api
data-api - интерфейсы слоя данных
data - слой с реализацией репозиториев и слоя данных


модуль common-db-api удаляем, сущности оттуда раскидываем по соответсвтующим data-api модулям, эти data-api потом подключаем в common-db.
Dao сущности так же переносим в data-api. в common-db должен остаться только TrainStatsDb.kt и платформа специфический код.


settings/import/export - это backup

profile - пока будет состоять только из ui модуля 

:shared модуль станет агрегатором всех фич 