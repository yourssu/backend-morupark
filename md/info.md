# Morupark 프로젝트 개요

이 문서는 마이크로서비스 기반 큐 시스템인 Morupark 프로젝트의 구조, 역할 및 전반적인 애플리케이션 흐름을 설명합니다.

## 1. 프로젝트 구조 및 역할

프로젝트는 몇 가지 주요 디렉토리로 구성됩니다:

*   **`.github/`**: CI/CD(지속적 통합 및 지속적 배포)를 위한 GitHub Action 워크플로우, 이슈 템플릿 및 풀 리퀘스트 템플릿을 포함합니다.
    *   `workflows/`: 자동화된 빌드, 테스트 및 배포 파이프라인(예: `image-build-and-upload.yaml`)을 정의합니다.
*   **`gradle/`**: Gradle 래퍼 파일로, 여러 환경에서 일관된 Gradle 버전 사용을 보장합니다.
*   **`infra-aws/`**: AWS 배포에 특화된 Kubernetes 매니페스트 및 Kustomize로 관리되는 구성입니다.
    *   `base/`: 네임스페이스, ConfigMap, 시크릿과 같은 공통 Kubernetes 리소스를 포함합니다.
    *   `components/`: 개별 마이크로서비스 및 인프라 구성 요소(예: `auth-service`, `queue-service`, `mysql`, `kafka`, `redis`)를 위한 Kustomize 구성입니다.
    *   `overlays/`: `dev` 및 `prod` 환경별 구성으로, 기본 구성 요소에 패치(예: 인그레스 규칙, HPA 설정, 리소스 제한)를 적용합니다.
*   **`infra-gcp/`**: GCP 배포에 특화된 Kubernetes 매니페스트 및 Kustomize로 관리되는 구성입니다.
    *   `base/`: `namespace.yaml`, `configmap.yaml`, `secret-provider-class.yaml`, `morupark-sql-config.yaml`을 포함한 공통 Kubernetes 리소스를 포함합니다.
    *   `components/`: GCP의 개별 마이크로서비스 및 인프라 구성 요소(예: `auth-service`, `queue-service`, `kafka`, `redis`)를 위한 Kustomize 구성입니다.
    *   `overlays/`: `infra-aws/overlays`와 유사하게 GCP의 `dev` 및 `prod` 환경별 구성을 적용합니다.
*   **`script/`**: 개발 환경 배포를 위한 `deploy-dev.ps1`과 같은 유틸리티 스크립트를 포함합니다.
*   **`services-auth/`**: 인증/인가 마이크로서비스입니다.
    *   `build.gradle.kts`: 이 서비스의 Gradle 빌드 스크립트입니다.
    *   `Dockerfile`: Auth Service의 Docker 이미지를 빌드하는 방법을 정의합니다.
    *   `src/main/kotlin/com/yourssu/morupark/auth/AuthApplication.kt`: Spring Boot 애플리케이션의 메인 진입점입니다.
    *   `src/main/resources/application.yml`: 서비스별 구성입니다.
*   **`services-common/`**: 다른 마이크로서비스에서 사용되는 공통 유틸리티, 구성, DTO 및 예외를 포함하는 공유 라이브러리/모듈입니다. 독립적으로 실행되는 애플리케이션이 아닙니다.
    *   `build.gradle.kts`: 이 모듈의 Gradle 빌드 스크립트입니다.
*   **`services-queue/`**: 큐 관리 마이크로서비스입니다.
    *   `build.gradle.kts`: 이 서비스의 Gradle 빌드 스크립트입니다.
    *   `Dockerfile`: Queue Service의 Docker 이미지를 빌드하는 방법을 정의합니다.
    *   `src/main/kotlin/com/yourssu/morupark/queue/QueueApplication.kt`: `@EnableScheduling`을 포함하는 Spring Boot 애플리케이션의 메인 진입점입니다.
    *   `src/main/resources/application.yml`: 서비스별 구성입니다.
*   **`terraform/`**: 클라우드 리소스를 프로비저닝하기 위한 Terraform을 사용하는 IaC(Infrastructure as Code)입니다.
    *   `private-subnet/`: 프라이빗 네트워크 서브넷 내에서 리소스를 프로비저닝하기 위한 Terraform 구성입니다.
    *   `public-subnet/`: 퍼블릭 네트워크 서브넷 내에서 리소스를 프로비저닝하기 위한 Terraform 구성입니다.
    *   둘 다 Terraform 상태 관리를 위해 Google Cloud Storage(GCS)를 사용하고 `google` 프로바이더를 구성합니다.
*   **`build.gradle.kts` (루트)**: 모든 하위 프로젝트의 공통 플러그인, 리포지토리 및 종속성을 정의하는 멀티 프로젝트 설정용 메인 Gradle 빌드 스크립트입니다.
*   **`settings.gradle.kts`**: 프로젝트의 루트 이름(`morupark`)을 정의하고 하위 프로젝트(`services-common`, `services-auth`, `services-queue`)를 포함합니다.
*   **`README.md`**: 프로젝트, 그 구조, 기술 스택 및 설정 지침에 대한 높은 수준의 개요를 제공합니다.
*   **`.dockerignore`, `.gitignore`**: Docker 빌드 및 Git 버전 관리를 위한 표준 무시 파일입니다.
*   **`kubectl.exe`**: 로컬 또는 수동 Kubernetes 클러스터 상호 작용에 사용될 가능성이 있는 Kubernetes 명령줄 도구입니다.

## 2. 기술 스택

*   **언어**: Kotlin
*   **프레임워크**: Spring Boot 3.4.1
*   **JDK**: OpenJDK 21
*   **빌드 도구**: Gradle (Kotlin DSL)
*   **데이터베이스**: MySQL 8.0 (운영 환경용 RDS, 개발 환경용 H2 인메모리)
*   **캐시**: Redis
*   **메시지 큐**: Apache Kafka
*   **컨테이너화**: Docker
*   **오케스트레이션**: Kubernetes (구성 관리를 위한 Kustomize)
*   **코드형 인프라 (IaC)**: Terraform (GCP 리소스 프로비저닝용)
*   **CI/CD**: GitHub Actions
*   **클라우드 플랫폼**: AWS 및 GCP (멀티 클라우드 배포 전략)
*   **아키텍처**: 마이크로서비스, 서비스당 4계층 (애플리케이션, 비즈니스, 구현, 스토리지)

## 3. 애플리케이션 흐름

Morupark 애플리케이션은 여러 클라우드 환경에 배포되는 큐 시스템을 위해 설계된 마이크로서비스 아키텍처를 따릅니다.

1.  **개발 워크플로우**:
    *   개발자는 `services-common`의 공유 구성 요소를 활용하여 `services-auth` 및 `services-queue` 마이크로서비스를 위한 Kotlin 코드를 작성합니다.
    *   로컬 개발 환경에서는 빠른 반복 작업을 위해 H2 인메모리 데이터베이스와 로컬 Kafka/Redis를 사용합니다.
    *   코드 변경 사항은 Git을 통해 관리되고 GitHub 저장소에 커밋됩니다.

2.  **지속적 통합 (CI)**:
    *   코드 푸시 시, GitHub Actions 워크플로우(`.github/workflows/`)가 트리거됩니다.
    *   이 워크플로우는 Kotlin 코드를 컴파일하고, 테스트(`./gradlew test`)를 실행하며, 코드 품질을 보장합니다.

3.  **Docker 이미지 빌드**:
    *   각 마이크로서비스(`services-auth`, `services-queue`)에 대해 다단계 `Dockerfile`을 사용하여 Docker 이미지가 빌드됩니다.
    *   빌드 단계에서는 `gradle:jdk21-corretto`를 사용하여 Spring Boot 애플리케이션을 JAR 파일로 컴파일합니다.
    *   배포 단계에서는 `amazoncorretto:21-alpine`을 경량 기본 이미지로 사용하고, JAR를 복사하며, 보안 강화를 위해 비루트 사용자(`authservice` 또는 `queueservice`)를 설정합니다.
    *   `SERVER_PORT` 환경 변수가 설정됩니다 (Auth의 경우 8081, Queue의 경우 8082).
    *   이 Docker 이미지는 컨테이너 레지스트리(배포 대상에 따라 Google Container Registry 또는 AWS ECR일 가능성)로 푸시됩니다.

4.  **인프라 프로비저닝 (Terraform - 주로 GCP)**:
    *   Terraform 스크립트(`terraform/`)는 클라우드 인프라를 프로비저닝하고 관리하는 데 사용됩니다.
    *   여기에는 Virtual Private Clouds (VPC), 서브넷(예: 데이터베이스/내부 서비스를 위한 `private-subnet`, 퍼블릭 서비스용 `public-subnet`), Kubernetes 클러스터(GKE), 데이터베이스(MySQL RDS), 캐싱 서비스(Redis), 메시지 브로커(Kafka) 및 GCP의 시크릿 관리 서비스 생성이 포함됩니다.
    *   Terraform 상태는 GCS 버킷(`morupark-tfstate-bucket`)에 원격으로 저장됩니다.

5.  **Kubernetes를 사용한 지속적 배포 (CD) (Kustomize)**:
    *   Kustomize 구성(`infra-aws/`, `infra-gcp/`)은 Kubernetes 클러스터(AWS용 EKS, GCP용 GKE)에 애플리케이션을 배포하고 관리하는 데 사용됩니다.
    *   기본 구성은 공통 리소스를 정의합니다.
    *   구성 요소 구성은 개별 마이크로서비스 및 해당 종속성(예: Kafka, Redis가 자체 호스팅되는 경우)에 대한 배포, 서비스 및 HPA를 정의합니다.
    *   오버레이 구성은 인그레스 컨트롤러, 리소스 요청/제한 및 스케일링 정책과 같은 환경별(개발/운영) 사용자 정의를 적용합니다.

6.  **런타임 운영**:
    *   **Auth Service (포트 8081)**: 사용자 등록, 로그인 및 토큰 발급을 처리합니다. 보안 구성 및 JWT 유틸리티를 포함합니다.
    *   **Queue Service (포트 8082)**: 대기 큐를 관리하며, Kafka를 통해 요청 또는 이벤트를 처리할 수 있습니다. 큐 관리를 위한 예약된 작업(`@EnableScheduling`)과 Auth와 같은 외부 서비스와의 상호 작용을 위한 어댑터를 포함합니다.
    *   **API Gateway**: URL 경로(예: `/auth/**`는 Auth Service로, `/queue/**`는 Queue Service로)를 기반으로 들어오는 요청을 적절한 마이크로서비스로 라우팅합니다.
    *   **데이터 흐름**: 서비스는 영구 데이터를 위해 MySQL, 캐싱을 위해 Redis, 비동기 메시지 전달 및 이벤트 기반 통신을 위해 Kafka와 상호 작용합니다.
    *   **확장성**: 마이크로서비스는 Kubernetes 클러스터 내에 배포되며, 확장 및 복원력을 위해 Horizontal Pod Autoscalers (HPA) 및 기타 Kubernetes 기능을 활용합니다.

## 4. 주요 아키텍처 결정

*   **마이크로서비스 아키텍처**: 분리된 서비스를 통해 독립적인 개발, 배포 및 확장이 가능합니다.
*   **멀티 클라우드 전략**: AWS 및 GCP를 모두 지원하여 유연성과 재해 복구 옵션을 제공합니다.
*   **코드형 인프라 (IaC)**: Terraform은 일관되고 반복 가능한 인프라 프로비저닝을 보장합니다.
*   **컨테이너화 (Docker) 및 오케스트레이션 (Kubernetes)**: 애플리케이션의 표준화된 배포 및 관리입니다.
*   **이벤트 기반 통신**: Apache Kafka는 서비스 간 비동기 통신을 용이하게 하여 응답성과 복원력을 향상시킵니다.
*   **계층형 아키텍처**: 각 마이크로서비스는 관심사의 명확한 분리를 위해 4계층 구조(애플리케이션, 비즈니스, 구현, 스토리지)를 따릅니다.
*   **환경별 구성**: Kustomize 오버레이는 다양한 환경(개발/운영)에 대한 맞춤형 배포를 가능하게 합니다.
*   **보안 모범 사례**: Docker 이미지의 비루트 사용자, Kubernetes의 전용 서비스 계정 (GCP).
