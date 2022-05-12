run cmd as Administrator

move to libs folder like: D:\ncc\CryptoClash\simulation\libs>
run following cmd:

mvn install:install-file –Dfile=sfs2x-client-core.jar -DgroupId=com.smartfoxserver -DartifactId=sfs2x-client-core -Dversion=1.0 -Dpackaging=jar

mvn install:install-file –Dfile=sfs2x-api-java.jar -DgroupId=sfs2x.client.core -DartifactId=sfs2x-api-java -Dversion=1.0 -Dpackaging=jar

mvn install:install-file –Dfile=tools.jar -DgroupId=falcon.libs -DartifactId=tools -Dversion=1.0 -Dpackaging=jar

mvn install:install-file –Dfile=netty-3.2.2.Final.jar -DgroupId=org.jboss.netty.channel -DartifactId=netty -Dversion=3.2.2.Final -Dpackaging=jar

build:
D:\ncc\CryptoClash\simulation>
mvnw clean package -DskipTests
