# Welcome to the Fenix Framework (FF)

## Quick start

To create a sample application pre-configured with the FF, execute the
following Maven command and follow the interactive mode:

    mvn archetype:generate -DarchetypeGroupId=pt.ist -DarchetypeArtifactId=fenix-framework-application-archetype-clean -DarchetypeVersion=2.0 -DarchetypeRepository=https://fenix-ashes.ist.utl.pt/nexus/content/groups/fenix-ashes-maven-repository

The parameters that you can give to the clean application archetype (using the
`-D` switch) are:

  * `fenixFrameworkVersion`:
    * Description: The version of the Fenix Framework to use
    * Default: Same as the archetype's version.
  * `backEndName`
    * Description: The name of the backend to use.  Some possible values are
      `mem`, `ispn` and `ogm`.
    * Default: `mem`
  * `backEndGroupId`
    * Description: The groupId of the backend module to use.
    * Default: `pt.ist` 
  * `backEndArtifactId`
    * Description: The artifactId of the backend module to use.
    * Default: `fenix-framework-backend-mem`
  * `backEndVersion`
    * Description: The version of the backend module to use.
    * Default: Same as the archetype's version. 
  * `codeGeneratorClassName`
    * Description: The code generator class provided by the chosen backend.
      Some possible values for each backend are:
        - `mem` -> `pt.ist.fenixframework.backend.mem.MemCodeGenerator`
        - `ogm` -> `pt.ist.fenixframework.backend.ogm.OgmCodeGenerator`
        - `ispn` -> `pt.ist.fenixframework.backend.infinispan.InfinispanCodeGenerator`
         
    * Default: `pt.ist.fenixframework.backend.mem.MemCodeGenerator`

If you go this way, then you just need to know how to
[Use the FF in your code](#UsingFF).  Alternatively, read this entire document
for some more information.

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

  * [Make the FF accessible to your code](#MakeFFAccessible)
  * [Use the FF in your code](#UsingFF)

<a name="#MakeFFAccessible"></a> 
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
            <artifactId>fenix-framework-backend-mem</artifactId> <!-- or any other module -->
            <version>2.0</version>                               <!-- or any other version -->
        </dependency>
    </dependencies>
    
Additionally, you will probably want to hook the `ff-maven-plugin` to your
build process, so that your domain classes get properly generated and
post-processed, and any uses of the `Atomic` annotation get processed.  This
can be achieved with the following.

    <build>
        <plugins>
            <plugin>
                <groupId>pt.ist</groupId>
                <artifactId>ff-maven-plugin</artifactId>
                <version>${fenixframework.version}</version>
                <configuration>
                    <codeGeneratorClassName>${fenixframework.code.generator}</codeGeneratorClassName>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>ff-generate-domain</goal>
                            <goal>ff-process-atomic-annotations</goal>
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

Then, you need to invoke the DML Compiler (class
`pt.ist.fenixframework.DmlCompiler`) to generate the source base classes,
before compiling your own code.  After compilation you need to run the
post-processor (class
`pt.ist.fenixframework.core.FullPostProcessDomainClasses`) on your compiled
classes.  Finally, if you've used the Atomic annotation, then you should run
`pt.ist.fenixframework.atomic.ProcessAtomicAnnotations` on the compiled
classes.

<a name="UsingFF"></a> 
## Use the FF in your code

The `docs` directory in the top-level project contains a description of the
Domain Modelling Language, which you need to use to describe your domain
entities.  Please refer to that documentation.

In the future, we plan to add more documentation to this section.

## Backends

### In-Memory Only (mem)

### Hibernate OGM (ogm)

### Direct to Infinispan (ispn)

## Other Modules

### Indexes

Please refer to the documentation in docs/dml-reference.md.


### Data Access Patterns

This module adds the possibility of collecting information about the data
access patterns performed by any target application built with the
fenix-framework.  When active, the code generator in this module adds, at the
start of each setter and getter method, an invocation to a static method in
the DAP framework that updates the statistical information regarding that
particular read/write access operation.

To activate this module, the DML Compiler needs to be invoked with the
property `ptIstDapEnable` set to `true`.  This can be achieved either (1) via
the command line by adding the switch `-param ptIstDapEnable=true` (no spaces
around `=`) to the invocation of the compiler, or (2) via the maven plugin by
adding that same parameter to the code generation phase as shown in the
following example:

    <build>
        <plugins>
            <plugin>
                <groupId>pt.ist</groupId>
                <artifactId>ff-maven-plugin</artifactId>
                <version>${project.version}</version>
                <configuration>
                    <codeGeneratorClassName>${fenixframework.code.generator}</codeGeneratorClassName>
                    <params>
                        <ptIstDapEnable>true</ptIstDapEnable>
                    </params>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>ff-generate-domain</goal>
                            <goal>ff-process-atomic-annotations</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

When this module is active it can be configured via a properties file named
`dap.properties`.  If such file is used, it must be available in the CLASSPATH
as a resource (at `/dap.properties`).  If such file is not found, the Data
Access Patterns module is still started with the default configuration, which
does not have statistics collection active, and thus, the only performance
penalty incurred is on a conditional test (which evaluates to `false`) on
every get/set operation.  Later, the data collection can be activated,
e.g. via JMX.

### Transaction Introspector

The TxIntrospector module enables internal information gathering during a transaction. When enabled, the programmer may obtain a `pt.ist.fenixframework.txintrospector.TxIntrospector` instance, and use it to query what were the new/modified objects/relationships for a transaction.

For more details, see txintrospector/README.md .

### Hibernate Search

The hibernate-search module allows the indexing of domain objects for fast querying using the Hibernate Search framework.

For more details, see hibernate-search/README.md .
