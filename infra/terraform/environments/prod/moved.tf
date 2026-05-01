moved {
  from = google_compute_network.morupark_vpc
  to   = module.vpc.google_compute_network.this
}

moved {
  from = google_compute_subnetwork.public_subnet
  to   = module.vpc.google_compute_subnetwork.this
}

moved {
  from = google_compute_firewall.allow_lb
  to   = module.vpc.google_compute_firewall.allow_lb
}

moved {
  from = google_compute_firewall.allow_gke_traffic
  to   = module.vpc.google_compute_firewall.allow_gke_traffic
}

moved {
  from = google_compute_firewall.allow_ssh
  to   = module.vpc.google_compute_firewall.allow_ssh
}

moved {
  from = google_container_cluster.morupark_gke
  to   = module.gke.google_container_cluster.this
}

moved {
  from = google_sql_database_instance.mysql_instance
  to   = module.db.google_sql_database_instance.this
}

moved {
  from = google_sql_database.morupark_db
  to   = module.db.google_sql_database.this
}

moved {
  from = google_sql_user.db_user
  to   = module.db.google_sql_user.this
}
