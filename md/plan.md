# GCP 및 GKE 기반 티켓팅 시스템 구축 계획서

본 문서는 GCP 인프라와 Kubernetes(GKE) 리소스를 활용하여 500명 동시 접속을 버틸 수 있는 티켓팅 시스템을 구축하기 위한 구체적이고 단계적인 실행 계획입니다.

## [1단계] 기반 인프라 및 보안 설정 (Foundation)
가장 먼저 서비스 간 통신과 DB 접근을 위한 뼈대를 잡습니다. 논리적인 망 분리와 최소 권한의 원칙을 적용하여 보안성을 높입니다.

*   **VPC 및 서브넷 구성**
    *   **Public/Private 서브넷 분리**: API Gateway 및 Load Balancer가 위치할 Public 영역과 GKE 노드, DB가 위치할 Private 영역을 분리하여 외부의 직접적인 접근을 차단합니다.
    *   **이유**: 보안 강화를 위해 데이터베이스와 내부 마이크로서비스는 인터넷에 직접 노출되지 않도록 하는 것이 클라우드 아키텍처의 베스트 프랙티스입니다.
*   **IAM 및 Workload Identity**
    *   `morupark-sa` 서비스 계정에 `roles/cloudsql.client` 권한 및 Managed Kafka 접근 권한 부여
    *   GKE의 Workload Identity를 매핑하여 Pod가 GCP 서비스(Cloud SQL, Kafka)에 접근할 때 안전하게 인증합니다.
    *   **이유**: 서비스 어카운트 키(JSON)를 K8s Secret으로 관리하는 것보다 Workload Identity를 사용하는 것이 키 유출 위험이 없고 관리가 용이합니다.
*   **Cloud SQL (PostgreSQL/MySQL) 구축**
    *   Private IP 설정으로 내부망에서만 접근 가능하도록 구성
    *   **비용 절감 전략**: 개발 및 테스트 환경에서는 Shared Core 최소 사양으로 운영하고, 실제 부하 테스트나 이벤트 오픈 시 vCPU 2개/4GB RAM 이상으로 스케일업(Scale-up)하여 대응합니다.

## [2단계] 관리형 메시지 브로커 설정 (Managed Kafka)
마이크로서비스 간 비동기 통신과 트래픽 스파이크를 완충하기 위해 클라우드 네이티브 기반의 메시지 큐를 도입합니다.

*   **서비스 선택**
    *   GCP의 Managed Service for Apache Kafka 또는 Confluent Cloud 활용
    *   **이유**: Zookeeper 및 브로커 노드를 직접 관리하는 오버헤드를 줄이고 비즈니스 로직(Queue, Goods) 구현에 집중하기 위함입니다.
*   **토픽(Topic) 설계**
    *   `waiting-queue-topic`: Queue 서비스에서 생성된 대기표가 발행(Produce)되고, Goods 서비스에서 이를 소비(Consume)합니다.
    *   `result-notification-topic`: Goods 서비스에서 처리된 당첨/낙첨 결과를 발행하고, Queue 서비스(혹은 Notification 서비스)가 이를 소비하여 유저에게 반환합니다.
*   **네트워크 연결 설정**
    *   GKE 내부(Private Subnet)에서 Managed Kafka 엔드포인트에 원활히 접근할 수 있도록 VPC 피어링 또는 Private Service Connect를 구성합니다.

## [3단계] 캐시 레이어 배포 (Redis StatefulSet)
대기열 순번 발급과 실시간 순위 조회를 위해 In-memory 데이베이스인 Redis를 GKE 위에 StatefulSet으로 구축합니다.

*   **StatefulSet 및 PVC 구성**
    *   Redis는 Pod가 재시작되어도 데이터가 유지되어야 하므로 `StatefulSet`을 사용합니다.
    *   디스크 할당 시 **PVC (Persistent Volume Claim)**를 사용하여 `pd-ssd`(SSD 기반 Persistent Disk)를 연결하여 높은 I/O 성능을 확보합니다.
*   **Headless Service 설정**
    *   `ClusterIP: None`인 Headless Service를 생성하여 Queue 서비스가 Redis Pod에 안정적인 내부 도메인(예: `redis-0.redis.default.svc.cluster.local`)으로 접근하도록 설정합니다.

## [4단계] 애플리케이션 서비스 배포 (GKE Deployment)
비즈니스 로직을 담당하는 각 컨테이너를 GKE Deployment로 배포합니다.

*   **API Gateway**
    *   클라이언트의 모든 요청을 받아들이는 단일 진입점(Entrypoint).
    *   요청 라우팅과 함께 Auth Service를 통해 넘어온 JWT 토큰의 유효성을 1차 검증(Filter 역할)합니다.
*   **Auth Service**
    *   사용자 로그인 처리 및 JWT 형태의 Access Token 발행을 담당합니다.
*   **Queue Service**
    *   티켓팅 이벤트 진입 시 Redis의 Sorted Set(ZSET)을 활용하여 타임스탬프 기반의 대기열 순번을 발급/관리합니다.
    *   자신의 순번이 된 유저의 요청을 `waiting-queue-topic`으로 Kafka에 전송하고, 반환된 결과를 클라이언트에 SSE(Server-Sent Events) 또는 Polling 방식으로 전달합니다.
*   **Goods Service (Core Logic)**
    *   Kafka에서 메시지를 소비하여 실제 5% 확률 당첨 로직 및 재고(결제) 차감처리를 수행합니다.
    *   동시성 제어를 위해 배타적 락 혹은 데이터베이스의 원자적 연산을 사용하며, 결과를 Cloud SQL에 기록한 뒤 `result-notification-topic`으로 전송합니다.

## [5단계] 네트워크 및 오토스케일링 (Traffic Management)
이벤트 시점의 트래픽 스파이크(500명 동시 접속 이상)를 유연하게 처리하기 위한 인프라 설정입니다.

*   **L7 Load Balancer 및 Ingress**
    *   GCP Global HTTP(S) Load Balancer를 K8s Ingress 리소스를 통해 프로비저닝합니다.
    *   구글 관리형 SSL 인증서를 연결하여 안전한 HTTPS 통신을 보장합니다.
*   **HPA (Horizontal Pod Autoscaler)**
    *   CPU 및 Memory 사용량을 수집하는 Metrics Server를 기반으로 HPA를 설정합니다.
    *   `Queue` 및 `Goods` 파드에 대해 CPU 사용량 60%를 임계치로 설정하여 트래픽 증가 시 Pod가 자동으로 스케일 아웃(Scale-out) 되도록 구성합니다.

## [6단계] 검증 및 부하 테스트 (Verification)
실제 운영 환경과 동일한 부하를 발생시켜 시스템의 안정성을 검증합니다.

*   **기능 단위 테스트**
    *   Client -> API Gateway -> Queue -> Redis -> Kafka -> Goods -> SQL로 이어지는 전체 1 Cycle 통신이 정상적으로 처리되는지 확인합니다.
*   **부하 테스트 (k6 / nGrinder)**
    *   가상의 유저 0명부터 시작해 500명까지 점진적으로 트래픽(Ramp-up)을 증가시킵니다.
    *   Redis의 순번 발급 처리량(TPS), Kafka 메시지 지연 시간(Lag), 서비스별 응답 시간(Latency)을 모니터링합니다.
*   **데이터 정합성 검증**
    *   초당 수백 건의 동시 요청이 들어왔을 때, 5% 당첨 확률이 정확히 지켜지는지, 그리고 재고 감소 시 Race Condition(경쟁 상태) 오류가 없는지 Cloud SQL의 데이터를 교차 검증합니다.
