#!/usr/bin/env bash

SRC=(
https://www.w3.org/1999/02/22-rdf-syntax-ns
https://www.w3.org/2000/01/rdf-schema
http://xmlns.com/foaf/spec/index.rdf
http://www.w3.org/2002/07/owl
http://www.adampease.org/OP/WordNet.owl
http://www.adampease.org/OP/SUMO.owl
https://files.ifi.uzh.ch/ddis/ontologies/evoont/2008/11/som/
https://files.ifi.uzh.ch/ddis/ontologies/evoont/2008/11/bom/
https://files.ifi.uzh.ch/ddis/ontologies/evoont/2008/11/vom/
)
for i in ${SRC[*]}
do
rapper $i
done
# http://resources.mpi-inf.mpg.de/yago-naga/yago/download/yago/yago3_entire_ttl.7z
