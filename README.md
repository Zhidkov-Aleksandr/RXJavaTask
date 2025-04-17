# RXJavaTask

## Описание проекта
Реализация упрощенной версии реактивной библиотеки RxJava, включающая основные компоненты для работы с асинхронными потоками данных. Проект предоставляет базовые операторы преобразования, управление потоками выполнения и обработку ошибок.

**Цель**: Создать систему реактивных потоков с возможностью:
- Создания и подписки на Observable
- Применения операторов преобразования (map, filter, flatMap)
- Управления потоками выполнения через Schedulers
- Обработки ошибок и отмены подписок

## Основные компоненты
1. **Observable** - основной класс для создания и управления потоками данных.
2. **Observer** - интерфейс с методами:
   - `onNext(T item)` - получение элементов
   - `onError(Throwable t)` - обработка ошибок
   - `onComplete()` - завершение потока
3. **Операторы**:
   - `map()` - преобразование элементов
   - `filter()` - фильтрация элементов
   - `flatMap()` - преобразование в новые Observable
4. **Schedulers**:
   - `IOThreadScheduler` - пул потоков с кэшированием
   - `ComputationScheduler` - фиксированный пул потоков
   - `SingleThreadScheduler` - однопоточный исполнитель
5. **Disposable** - механизм отмены подписок

## Особенности реализации
### Управление потоками
- **subscribeOn()** - определяет поток для выполнения источника
- **observeOn()** - задает поток для обработки результатов

Observable.create(...)
    .subscribeOn(Schedulers.io())
    .observeOn(Schedulers.computation())
    .map(...)
Обработка ошибок
Ошибки автоматически передаются в onError()

Отмена подписки при ошибках через Disposable

```
observable.subscribe(
    item -> {...},
    error -> {...},
    () -> {...}
);
```

## Операторы преобразования
Map:

```
.map(x -> x * 2)
Filter:
```

```
.filter(x -> x > 10)
FlatMap:
```

```
.flatMap(x -> Observable.create(...))
```

## Структура проекта
```
RXJavaTask
├── src
│   ├── main
│   │   └── java/com/myrxjava/core
│   │       ├── Observable.java    # Основная реализация
│   │       ├── Observer.java      # Интерфейс подписчика
│   │       ├── Disposable.java    # Управление подписками
│   │       ├── schedulers         # Реализации Schedulers
│   │       └── operators          # Реализации операторов
│   └── test
│       └── java/com/myrxjava
│           └── ObservableTest.java # Юнит-тесты
├── pom.xml                        # Конфигурация Maven
└── README.md                      # Документация
```

## Пример использования
```
Observable.create(observer -> {
    observer.onNext(1);
    observer.onNext(2);
    observer.onComplete();
})
.subscribeOn(Schedulers.io())
.flatMap(x -> Observable.create(obs -> {
    obs.onNext(x * 10);
    obs.onComplete();
}))
.subscribe(
    item -> System.out.println("Received: " + item),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);
```

## Ключевые особенности:
```
✅ Реализация реактивного паттерна Observer
✅ Поддержка многопоточности через Schedulers
✅ Цепочки операторов с сохранением контекста выполнения
✅ Механизм отмены подписок через Disposable
✅ Полное покрытие юнит-тестами
```

## Оптимизация производительности:


IO-операции: Использовать Schedulers.io() с неограниченным пулом

Вычисления: Schedulers.computation() с фиксированным пулом

Последовательная обработка: Schedulers.singleThread()

Принципы работы
Создание Observable:
```
Observable.create(observer -> {...})
```
Подписка происходит через цепочку операторов

## Управление потоками:
subscribeOn() влияет на источник данных
observeOn() влияет на обработчики результатов

## Заключение
Данная реализация предоставляет базовый функционал реактивного программирования, позволяя:
Работать с асинхронными потоками данных
Комбинировать операторы преобразования
Эффективно управлять многопоточностью
Обрабатывать ошибки на любом этапе цепочки
