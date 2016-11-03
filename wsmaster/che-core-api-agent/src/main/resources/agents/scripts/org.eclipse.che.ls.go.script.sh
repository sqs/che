#
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

unset PACKAGES
unset SUDO
command -v curl >/dev/null 2>&1 || { PACKAGES=${PACKAGES}" curl"; }
test "$(id -u)" = 0 || SUDO="sudo"

AGENT_BINARIES_URI=https://codenvy.com/update/repository/public/download/org.eclipse.che.ls.go.binaries
CHE_DIR=$HOME/che
LS_DIR=${CHE_DIR}/ls-go
LS_LAUNCHER=${LS_DIR}/launch.sh
LS_BIN=langserver-go

mkdir -p ${CHE_DIR}
mkdir -p ${LS_DIR}

######################
### Install GoLang LS ###
######################
cd ${LS_DIR}

curl -s ${AGENT_BINARIES_URI} > ${LS_BIN}

touch ${LS_LAUNCHER}
chmod +x ${LS_LAUNCHER}
chmod +x ${LS_BIN}
echo "${LS_DIR}/${LS_BIN} -mode=stdio -trace=true -logfile=${LS_DIR}/gols.log" > ${LS_LAUNCHER}
