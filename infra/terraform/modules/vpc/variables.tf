variable "region" {
  type = string
}

variable "network_name" {
  type = string
}

variable "subnet_name" {
  type = string
}

variable "subnet_cidr" {
  type = string
}

variable "allow_lb_firewall_name" {
  type = string
}

variable "allow_gke_firewall_name" {
  type = string
}

variable "allow_ssh_firewall_name" {
  type = string
}
