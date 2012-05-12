#!/bin/bash -e

#!/bin/bash -e

cd ../..
mvn clean
mvn install

cd deployment/maven
ant -f build.xml -Dplatform=gtk2-linux-x86_64

chmod a+x dist_gtk2-linux-x86_64/olifant.sh

