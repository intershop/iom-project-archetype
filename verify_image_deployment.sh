#!/bin/bash
# --> can we enforce bash for everyone?
# 1) call caas2docker
target/test-classes/projects/basic/project/blueprint-test/build_image.sh
# 2) install images
# TODO: dynamic tag + version required for CI? might be overkill
# TODO: adjust helm chart repo for CI environment
# TODO: ingress stuff can probably be removed/disabled
helm install bp-test -n $NAMESPACE_IDENTIFIER --create-namespace \
    --set image.tag=1.0.0.LOCAL-SNAPSHOT \
    --set config.image.tag=1.0.0.LOCAL-SNAPSHOT \
    --set ingress.hosts[0].host=blueprint.localhost \
    -f helm-values-test.yaml \
    --wait --timeout 600s \
    https://repo.rnd.intershop.de:443//helm-dev/iom/iom-1.4.0.tgz
