Java implementation of OO7 benchmark.

Dependencies:

java 1.4+
ant 1.6+
A DBMS supported by Hibernate and its JDBC JAR file.

Right now there is only a hibernate implementation which uses Hibernate 3.2.

To run create a sample OO7 database using Hibernate:

1. Build the project.
> ant

2. Copy the hibernate.properties.sample file in /etc/hibernate to hibernate.properties.
Modify the database connection properties for your database.
See the hibernate documentation for more information.

3. Run the HibernateOO7Database class with the appropriate classpath.
The main method accepts a single argument which represents the size of the database:
0: Tiny
1: Small
2: Medium
3: Large

The classpath should include:
a) The etc/hibernate directory
b) The directory containing your hibernate.properties file.
c) The build/classes directory
d) All the JAR files in lib/hibernate
e) The JDBC driver JAR file.


To run the hibernate benchmark after creating a database:

1. Build the project.
> ant

2. Copy the hibernate.properties.sample file in /etc/hibernate to hibernate.properties.
Modify the database connection properties for your database.
See the hibernate documentation for more information.

3. Run the HibernateOO7Benchmark class.

The classpath should include:
a) The etc/hibernate directory
b) The directory containing your hibernate.properties file.
c) The build/classes directory
d) All the JAR files in lib/hibernate
e) The JDBC driver JAR file.
