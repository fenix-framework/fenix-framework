#!/bin/bash

\mvn clean test -DforkCount=1.5C -Dcode.generator.class=pt.ist.fenixframework.backend.jvstm.JVSTMCodeGenerator && \
    \mvn clean test -DforkCount=1 -Dcode.generator.class=pt.ist.fenixframework.backend.jvstm.infinispan.JvstmIspnCodeGenerator -Dtest=test.backend.jvstm.ConcurrentUpdatesTest,test.backend.jvstm.SequentialUpdatesTest,test.backend.jvstm.SimpleTest
