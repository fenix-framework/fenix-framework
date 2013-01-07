# Hibernate-Search Module

The hibernate-search module allows the indexing of domain objects for fast querying using the Hibernate Search framework.

## Enabling the Module

The hibernate-search module depends on the TxIntrospector module. See ../txintrospector/README.md on how to enable the TxIntrospector module.

To enable the module, create a properties file named `fenix-framework-hibernate-search.properties`, and place it in the project resources directory.

This properties file contains the configuration for the Hibernate Search framework. For more details on how to configure Hibernate Search, refer to the framework documentation at <https://docs.jboss.org/hibernate/search/4.2/reference/en-US/html/search-configuration.html>.

Refer to the examples section for a sample Hibernate Search configuration.

## Indexing Domain Objects

Refer to the Hibernate Search framework documentation for examples on how to annotate client code for indexing at <https://docs.jboss.org/hibernate/search/4.2/reference/en-US/html/search-mapping.html>.

Note that the hibernate-search FF module automatically configures the `@Id`/`@DocumentId` for domain objects, so you should not provide one. This operation should be performed inside an active transaction.

## Querying

Refer to the Hibernate Search framework documentation on how to build a query at <https://docs.jboss.org/hibernate/search/4.2/reference/en-US/html/search-query.html>.

Whenever a query matches objects, a list of their external Ids is returned. To obtain a reference to an object given its external Id, use the `FenixFramework.getDomainObject(Id)` API.

## Examples

Sample `fenix-framework-hibernate-search.properties` for configuring Lucene to use an in-memory back-end:

    # in-memory back-end
    hibernate.search.default.directory_provider=ram

Please refer to the test/test-hibernate-search/ module for a sample test application that uses hibernate-search.

Another example is available on the FF examples repository, under the name hs-example.
