# tn-user-service
## Image

The service builds its own Docker image: `Dockerfile` and `src/main/assembly/` (tn-parent's `assembly` and `docker` profiles)
lay the service's jar and its runtime libraries out in `target/tn-user-service-<version>` and `mvn install` builds
`tn-user-service:<version>` from it, which needs Docker (`-DskipDocker` skips it). A release publishes the jar and pushes
`ghcr.io/nickersan/tn-user-service:<version>` and `latest` in the same CI run; there is no separate container repo.
Configuration lives in the deploying project's Kustomize (`okayat-kustomize`), not in the image.

The ghcr.io package `tn-user-service` was first pushed by the old container repo, so this repo is added to it under
**Manage Actions access** (role Write); without that the release's image push is refused (`permission_denied: write_package`).
