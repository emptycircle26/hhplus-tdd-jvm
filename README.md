# 동시성 제어 방식 분석 및 보고서

## 1. 동시성 제어의 필요성
포인트 충전/사용 시스템에서는 여러 사용자가 동시에 같은 계정에 대해 포인트를 충전하거나 사용할 수 있습니다. 이때 동시성 문제가 발생하면 잔고 불일치, 중복 처리, 데이터 유실 등의 심각한 문제가 생길 수 있습니다.

## 2. 현재 구현 방식
- 유저별 Lock 객체를 `ConcurrentHashMap<Long, Any>`로 관리
- 포인트 충전/사용 시, 해당 유저의 lock 객체로 `synchronized(lock)` 블록을 사용하여 임계영역을 보호
- UserPointTable, PointHistoryTable 등 데이터 저장소는 변경하지 않고, UseCase에서만 동시성 제어를 수행

### 코드 예시
```kotlin
private val locks = ConcurrentHashMap<Long, Any>()

override fun charge(id: Long, amount: Long): UserPoint {
    val lock = locks.computeIfAbsent(id) { Any() }
    synchronized(lock) {
        // ...
    }
}
```

## 3. 동작 원리
- 한 스레드가 synchronized(lock) 블록에 진입하면, 해당 lock 객체에 대한 모니터 락을 획득합니다.
- 락을 획득한 스레드만 임계영역(블록 내부 코드)을 실행할 수 있습니다.
- 임계영역 실행이 끝나면(블록을 빠져나가면), 락이 해제됩니다.
- 락이 해제되면, 대기 중이던 다른 스레드 중 하나가 락을 획득하여 임계영역에 진입할 수 있습니다.
- 즉, 한 번에 하나의 스레드만 임계영역을 실행하며, 나머지 스레드는 락이 풀릴 때까지 대기합니다.

## 4. 장점
- Application 레벨에서 안전한 동시성 보장
- 유저별로 락이 분리되어 있어, 서로 다른 유저 작업은 병렬 처리 가능
- 구현이 직관적이고, race condition을 효과적으로 방지

## 5. 단점 및 한계
- synchronized는 임계영역이 길거나, 락 경합이 많을 때 성능 저하가 발생할 수 있음
- 매우 많은 유저 수와 임계영역이 길어졌을 경우, 락 객체가 메모리에 계속 남아 있을 수 있음(메모리 누수 위험)
- 분산 환경(여러 서버)에서는 JVM 내 락만으로는 완전한 동시성 제어가 불가
- 실제 DB 환경에서는 DB 트랜잭션/락도 반드시 고려해야 합니다.

## 6. 대안
- 분산 환경에서는 Redis Lock 등 외부 분산 락 시스템 필요
- `@Transactional`, row-level lock, optimistic lock 등 DB 자체의 동시성 제어 기능을 반드시 활용

## 7. 결론
- 현재 구조에서는 유저별 lock + synchronized가 가장 안전하고 현실적인 동시성 제어 방식입니다. 분산 시스템에서는 외부 분산 락 도입을 고려해야 합니다.
