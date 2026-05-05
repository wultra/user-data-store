# Artifact Signing

All User Data Store Docker images are signed using [Cosign](https://github.com/sigstore/cosign). Each image also has an attached Software Bill of Materials (SBOM) in [CycloneDX](https://cyclonedx.org/) format, expect the init image.

## Verify the Docker Image Signature

To verify that a Docker image has been signed by Wultra, run:

```sh
wget https://raw.githubusercontent.com/wultra/wultra-infrastructure/refs/heads/develop/public-keys/cosign.pub
cosign verify \
    --key cosign.pub \
    powerauth/user-data-store:${VERSION}
```

## Download and Inspect the SBOM

To download the attached SBOM:

```sh
cosign download attestation \
    powerauth/user-data-store:${VERSION} \
    | jq -r '.dsseEnvelope.payload' | base64 -d | jq '.predicate'
```

To verify the SBOM attestation signature before trusting it:

```sh
wget https://raw.githubusercontent.com/wultra/wultra-infrastructure/refs/heads/develop/public-keys/cosign.pub
cosign verify-attestation \
    --key cosign.pub \
    --type cyclonedx \
    powerauth/user-data-store:${VERSION}
```
