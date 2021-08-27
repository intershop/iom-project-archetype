#!/bin/bash
# TODO this is platform specific for Docker Desktop
kubectl config use-context docker-desktop
export DEVENV4IOM_CONFIG="$(pwd)/devenv-config.properties"
export PATH="$(pwd)/devenv-4-iom/bin:$PATH"
# TODO this is platform specific for Windows Host + WSL2 + Docker Desktop!
export DEVENV_SHARE_BASE_PATH="/run/desktop/mnt/host$(pwd)"