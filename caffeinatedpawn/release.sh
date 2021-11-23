#! /bin/sh

git checkout master

mvn package

DIR=C.P.
rm -f $DIR
mkdir $DIR

GITHASH=`git rev-parse --short HEAD`

cp target/CaffeinatedPawn-1.0.jar ${DIR}/CaffeinatedPawn-${GITHASH}.jar
cp ../chesslib/target/chesslib-1.3.2.jar ${DIR}/

WRAPPER=${DIR}/wrapper.sh
echo '#! /bin/sh' > ${WRAPPER}
echo "java -cp CaffeinatedPawn-${GITHASH}.jar:chesslib-1.3.2.jar com.vanheusden.CaffeinatedPawn.CaffeinatedPawn" >> ${WRAPPER}
chmod +x ${WRAPPER}

cp ../README.md ${DIR}

ZIP=CaffeinatedPawn-${GITHASH}.zip
zip -9vr ${ZIP} ${DIR}

scp -C ${ZIP} @belle.intranet.vanheusden.com:site/chess/CaffeinatedPawn/

rm -rf ${ZIP} ${DIR}
