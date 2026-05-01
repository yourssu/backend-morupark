variable "region" {
  type = string
}

variable "zone" {
  type = string
}

variable "instance_name" {
  type = string
}

variable "database_name" {
  type = string
}

variable "db_username" {
  type = string
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "tier" {
  type    = string
  default = "db-g1-small"
}

variable "availability_type" {
  type    = string
  default = "ZONAL"
}

variable "admin_authorized_cidr" {
  type = string
}
