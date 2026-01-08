terraform {
  backend "s3" {
    bucket       = "terraform-s3-backend-fined-mentor"
    key          = "backend-locking"
    region       = "eu-central-1"
    use_lockfile = true
  }
}