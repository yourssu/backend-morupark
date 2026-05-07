# Terraform Prod Ops

## 대상 디렉토리
- `infra/terraform/environments/prod`

## 주요 파일
- `gke.tf`, `vpc.tf`, `db.tf`, `secret_manager.tf`, `kubernetes_config.tf`, `ingress.tf`, `iam.tf`

## 권장 순서
1. `terraform -chdir=infra/terraform/environments/prod fmt -check`
2. `terraform -chdir=infra/terraform/environments/prod validate`
3. `terraform -chdir=infra/terraform/environments/prod plan -out=prod.tfplan`
4. `terraform -chdir=infra/terraform/environments/prod apply prod.tfplan`

## 가드레일
- `terraform apply -auto-approve` 금지
- plan 파일 없는 apply는 경고
- prod 외 경로에서 mutate 금지
