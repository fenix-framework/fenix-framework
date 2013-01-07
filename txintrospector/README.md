# TxIntrospector Module

The TxIntrospector module enables internal information gathering during a transaction. When enabled, the programmer may obtain a `pt.ist.fenixframework.txintrospector.TxIntrospector` instance, and use it to query what were the new/modified objects/relationships for a transaction.

## Enabling the Module

To activate this module, the DML Compiler needs to be invoked with the property `ptIstTxIntrospectorEnable` set to `true`. This can be achieved either (1) via the command line by adding the switch `-param ptIstTxIntrospectorEnable=true` (no spaces around `=`) to the invocation of the compiler, or (2) via the maven plugin by adding that same parameter to the code generation phase as shown in the following example:

    <build>
        <plugins>
            <plugin>
                <groupId>pt.ist</groupId>
                <artifactId>dml-maven-plugin</artifactId>
                <version>${project.version}</version>
                <configuration>
                    <codeGeneratorClassName>${fenixframework.code.generator}</codeGeneratorClassName>
                    <params>
                        <ptIstTxIntrospectorEnable>true</ptIstTxIntrospectorEnable>
                    </params>
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

## Accessing the TxIntrospector Instance

The FF client can obtain a TxIntrospector instance for a given transaction by calling
`TxIntrospector.getTxIntrospector(transaction)`, or for the active transaction for the current thread via `TxIntrospector.getTxIntrospector()`.

While not enforced, it is recommended that the TxIntrospector be used only after the transaction commits, as information gathering is not thread-safe.

Note that calling `TxIntrospector.getTxIntrospector()` without activating the TxIntrospector module will result in a run-time exception.

Furthermore, although the TxIntrospector API may be used outside a transactional context, any access or operation on the domain objects contained therein should be performed inside an active transaction.

## TxIntrospector API

Please refer to the FF javadocs for the TxIntrospector class.

## Examples

Please refer to the test/test-txintrospector/ module for a sample test application that uses TxIntrospector.
