# Welcome to the Fenix Framework (FF)

To create a sample application pre-configured with the FF, please execute the
following Maven command and follow the interactive mode:

    mvn archetype:generate -DarchetypeGroupId=pt.ist -DarchetypeArtifactId=fenix-framework-archetype -DarchetypeVersion=1.0 -DarchetypeRepository=https://fenix-ashes.ist.utl.pt/nexus/content/repositories/fenix-framework-releases

If you go this way, then you just need to know how to
[Use the FF in your code].  Alternatively, read this entire document for some
more information.


# Compiling

To compile all the modules in the FF and create one JAR per module, run this
command:

    mvn package
    
# Installing

To use the FF in your application, you need the packaged JAR files.  You can
install them in your local Maven repository with:

    mvn install

If you do not use Maven, just copy all the JARs produced with the `mvn
package` command to your project and then

# Developing with the FF

There are two aspects to this topic:

  * [Make the FF accessible to your code]
  * [Use the FF in your code]

## Make the FF accessible to your code

If, instead of starting from an archetype as mention in the beginning of this
document, you opt to integrate the FF into your project, you need to make the
necessary framework's modules available.

### If you use Maven

If you use Maven to build your application, you can just depend on the Fenix
Framework modules that you need, using something like the following in your
pom.xml:

    <dependencies>
        <dependency>
            <groupId>pt.ist</groupId>
            <artifactId>fenix-framework-core</artifactId> <!-- or any other module -->
            <version>2.0-cloudtm-SNAPSHOT</version>       <!-- or any other version -->
        </dependency>
    </dependencies>
    
Additionally, you will probably want to hook the `dml-maven-plugin` to your
build process, so that your domain classes get properly generated and
post-processed.  This can be achieved by adding the plugin to the build phase.

    <build>
        <plugins>
            <plugin>
                <groupId>pt.ist</groupId>
                <artifactId>dml-maven-plugin</artifactId>
                <version>${fenixframework.version}</version>
                <configuration>
                    <codeGeneratorClassName>${fenixframework.code.generator}</codeGeneratorClassName>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate-domain</goal>
                            <goal>post-compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

Where the `fenixframework.code.generator` property could be set like this in
your properties section of the POM file, e.g.:

    <properties>
        <fenixframework.code.generator>pt.ist.fenixframework.backend.infinispan.InfinispanCodeGenerator</fenixframework.code.generator>
        <!-- <fenixframework.code.generator>pt.ist.fenixframework.backend.ogm.OgmCodeGenerator</fenixframework.code.generator> -->
        <!-- <fenixframework.code.generator>pt.ist.fenixframework.backend.mem.MemCodeGenerator</fenixframework.code.generator> -->
    </properties>

Just make sure that `fenixframework.version` property is set in accordance
with the version used for the other FF modules.  This ensures that you use a
plugin that matches the version of the framework you are using.

### If you use any other build system

First make sure that the jars resulting from the `mvn package` are visible in
your application's build and runtime classpaths, plus any additional
dependencies.  You can check for dependencies using the
[Maven dependency plugin][1]:

    mvn dependency:list

[1]: http://www.google.com/search?q=maven+dependency+plugin

Then, you need to invoke the DML-Compiler (class
`pt.ist.fenixframework.DmlCompiler`) to generate the source base classes,
before compiling your own code.  After compilation you need to run the
post-processor (class
`pt.ist.fenixframework.core.FullPostProcessDomainClasses`) on your compiled
classes.

## Use the FF in your code

The `docs` directory in the top-level project contains a description of the
Domain Modelling Language, which you need to use to describe your domain
entities.  Please refer to that documentation.

In the future, we plan to add more documentation to this section.

