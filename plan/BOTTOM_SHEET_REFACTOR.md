Я хочу отрефакторить текущую архитекутуру управдения bottom sheet'ами.

Проблемы которые есть сейчас: 
1) Сильная связность: 1 скоуп вьюмоделей с экран открывшим bottom sheet. Особенно остро проблема стоит в workout editor, где exercise selector открывает exercise editor через сам workout editor;
2) Невозможность открыть bottom sheet из другого bottom sheet;
3) Нужно думать о том, где разместить Composable функцию bottom sheet.


Как я хочу решить вопросы:
Создать сущность BottomSheetNavigator, которая будет иметь 2 функции:
openBottomSheet(key, onHide, onResult, tag?)
hideBottomSheet(tag)

далее где-то должен храниться стэк текущих открытых bottom sheet с их controller'ами. Должно отслеживаться закрытие: убирать entry из стэка, только после завершения анимации закрытия.
Должен быть какой-то @Composable fun BottomSheetHost, на вход функция должен принимать list<Key>. Хост а) рисуется в rootScreen, чтобы bottom sheet всегда открывался на весь экран, б) маппил key -> @Composable отображение контента bottom sheet.

Каждый bottom sheet должен иметь свой viewModel scope. Скорее всего это можно сделать, сделав какую-нибудь универсальную ViewModel, которая будет созаваться для каждого bottom sheet и хранить его viewmodel store

