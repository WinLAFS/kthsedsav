#!/bin/sh

# Verify if JAVA_HOME is well defined
if [ ! -r "$JAVA_HOME"/bin/java ]; then
  echo "The JAVA_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

JADE_HOME=..

LIB_DIR=$JADE_HOME/lib
EXTERNALS_DIR=$JADE_HOME/externals

# Build the Java virtual machine properties
JVM_PROP=-Djava.security.policy=$JADE_HOME/etc/java.policy
JVM_PROP=$JVM_PROP\ -Dfractal.provider=fr.jade.fractal.julia.JuliaJade
JVM_PROP=$JVM_PROP\ -Djulia.loader=org.objectweb.fractal.julia.loader.DynamicLoader
JVM_PROP=$JVM_PROP\ -Djulia.loader.use-context-class-loader=true
JVM_PROP=$JVM_PROP\ -Djulia.config=$JADE_HOME/etc/julia.cfg,$JADE_HOME/etc/julia-fractal-rmi.cfg,$JADE_HOME/etc/julia-jade.cfg,$JADE_HOME/etc/julia-deploy.cfg

# Build the Classpath
CLASSPATH=$LIB_DIR/jade.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/jade-fractal.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/reflex-fractal.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/fractal-deployment-local-api.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/fractal.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/asm-2.0.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/fractal-adl.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/dtdparser.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/ow_deployment_scheduling.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/julia-runtime.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/julia-mixins.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/julia-asm.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/fractal-rmi.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/fractal-rmi-tmpl.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/jonathan.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/jms.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/joram-client.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/joram-shared.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/joram-mom.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/ow_monolog.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/osgi.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/oscar.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/shell.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/osgi-service.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/bsh.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/beanshell-jade.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/beanshell-fractal.jar
CLASSPATH=$CLASSPATH:$EXTERNALS_DIR/beanshell-joram.jar


exec $JAVA_HOME/bin/java $JVM_PROP -classpath $CLASSPATH bsh.Interpreter
 
 
