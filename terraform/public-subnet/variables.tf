variable "project_id" {
  description = "GCP 프로젝트 ID"
  type        = string
  default     = "yourssu-morupark" # 실제 프로젝트 ID 입력
}

variable "region" {
  description = "인프라가 배포될 리전"
  type        = string
  default     = "asia-northeast3" # 서울 리전
}

variable "zone" {
  description = "인프라가 배포될 가용 영역"
  type        = string
  default     = "asia-northeast3-a"
}

variable "db_username" {
  description = "Database admin user name"
  type        = string
}

variable "db_password" {
  description = "Cloud SQL 루트 비밀번호"
  type        = string
  sensitive   = true # 콘솔에 출력되지 않도록 설정
}

variable "admin_key" {
  description = "관리자 API용 비밀키"
  type        = string
  sensitive   = true
}

variable "domain" {
  description = "서비스 도메인 주소"
  type        = string
  default     = "api.morupark.yourssu.com"
}

variable "authorized_ip_cidrs" {
  description = "GKE 마스터에 접근을 허용할 IP CIDR 목록"
  type        = list(string)
  default     = ["175.198.119.106/32"]
}
