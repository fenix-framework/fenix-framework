# Consistency Predicates

## Using Regular Consistency Predicates

The consistency predicates are a tool provided by the Fenix Framework that allows you to define consistency rules inside your application’s domain.
To use them correctly, all your application’s domain entities must be fully specified in DML.
The consistency predicates allow you to create rules about the contents of domain slots and relations defined in DML.
Unlike an invariant, a consistency predicate implements a rule that is verified in runtime.
The consistency predicates were designed to be used in a production environment.
If a certain operation or transaction breaks a rule, the framework will abort the transaction, and revert its effects.
The framework will automatically verify the rules when necessary, and prevent your domain objects from getting inconsistent.
All you need to do is to provide a correct implementation of the consistency predicates.

To create a consistency predicate, you should place the @ConsistencyPredicate annotation in a method of a domain class.
If you place the annotation elsewhere, the framework will ignore it.
A consistency predicate method must receive no arguments and return a boolean.
The implementation of a consistency predicate should always be deterministic and based only on the domain.
Its implementation should never rely on any random values or on user input.
The only changeable input that it should access are the slots and relations of domain objects declared in DML.
On a certain domain class, you can implement a consistency predicate that accesses the object’s own slots by using its getter and setter methods.
You can also use the relations of that object to access the slots of other related objects.
So, the framework supports consistency predicates where an object uses the state of other objects to define its consistency.

Here's an example of a Client that has a consistency predicate that accesses Account:
    public class Client extends Client_Base {
        // (...)

        public int getTotalBalance() {
            int totalBalance = 0;
            for (Account account : getAccounts()) {
                totalBalance += account.getBalance();
            }
            return totalBalance;
        }

        @ConsistencyPredicate
        public boolean checkTotalBalancePositive() {
            return (getTotalBalance() >= 0);
        }
    }

Also, each consistency predicate should be as small and independent as possible.
One consistency predicate should implement exactly one consistency rule.
If a certain class needs to enforce two rules, then you should implement two distinct consistency predicates.
You should avoid having one predicate per class to enforce all the rules in conjunction.

If the object being checked turns out to be consistent, your predicate should return true.
If the object turns out to be inconsistent, your predicate should return false.
If the object is inconsistent, you can also choose to explicitly throw a ConsistencyException or a subclass of this exception.
Alternatively, you may choose to pass a subclass of ConsistencyException as the value argument to the @ConsistencyPredicate annotation.
If you do, and the predicate returns false, the active transaction will throw that exception on its own.
The default value of the annotation’s argument is the ConsistencyException, that the transaction throws when no explicit argument is provided.
You may, for instance, create a subclass of ConsistencyException with a specific implementation of the getMessage() method.
It may be useful to catch that subclass at an exception handler, and provide a user-friendly message at the presentation layer of your application.

In summary, a transaction that is executing a predicate will throw an exception that is determined by the following procedures:

 * If the predicate returns false, the transaction throws the ConsistencyException passed as argument to the @ConsistencyPredicate annotation.
 * If the predicate throws a ConsistencyException or a subclass, the transaction throws that same exception.
 * If the predicate throws another exception, the transaction wraps that exception inside the ConsistencyException passed as argument to the annotation.

Obviously, if all the predicates return true, then all the objects involved are consistent.
In this case, the transaction simply commits and does not throw any exception.

## Using Consistency Predicates Inside a Class Hierarchy

The consistency predicates were designed to work inside a domain class hierarchy.
A predicate at a certain class will be verified for objects of that class and those of all subclasses.
However, if the predicate method is public, the subclasses may override that method and implement a refinement of that predicate.

You can make a consistency predicate final to prevent other developers from overriding it at the subclasses.
You should make it final when you believe that all objects of any subclass should always follow this rule, without exclusions.
You can also make a predicate private to prevent people from overriding it, and from even seeing or invoking it from subclasses.
You should make it private when all object of subclasses should always follow this rule, and the method itself should be hidden from other developers.
All other predicates that are public or protected can be overridden at the subclasses.
You can make a predicate public if you want to define a normal rule that may have exclusions at the subclasses, depending on the case.
The use of package visible consistency predicates is forbidden.
Package visibility is the default visibility for methods that specify no explicit visibility modifier in Java.
You must always specify an explicit visibility for every consistency predicate method.

Any method that overrides a consistency predicate must always have the @ConsistencyPredicate annotation.
In other words, you cannot override a consistency predicate with a regular (non-predicate) method.
The @ConsistencyPredicate annotation itself is not inherited.
Therefore, you must manually place that annotation and redefine the value and inconsistencyTolerant arguments if you want predicates at subclasses to define them.
For instance, if you do not redefine the value argument, inconsistent objects of subclasses will throw ConsistencyException by default.

If your domain class hierarchy includes an abstract superclass, you may implement a (concrete) consistency predicate at that class.
You may invoke other abstract methods inside the implementation of your consistency predicate.
Even though the abstract class will never have objects, the framework will ensure that the predicate will be checked for objects of subclasses.
You can also define an abstract consistency predicate at an abstract superclass.
The abstract consistency predicate is a way to force other developers to implement that consistency predicate at the subclasses.
Although the framework will ignore the abstract predicate because it has no implementation, the framework will verify the concrete predicates at the subclasses.
Finally, note that the Fenix Framework already provides atomic transactions to ensure the correct access of concurrent operations to shared data.
The synchronized method modifier forces the JVM to use a lock in the access to the method, which may cause performance issues and deadlocks.
Therefore, even though the framework supports them, the use of synchronized consistency predicates is discouraged.

In summary, the method modifiers of your consistency predicates can influence their behavior.
Most importantly, you are allowed to create a public predicate, and to override it in subclasses later.
You may, for instance, wish to indicate that this predicate applies for objects of a class, but not for objects of the subclass.
To do so, you can create an overriding predicate at the subclass that simply returns true.
However, this use case does not respect the Liskov Substitution Principle (LSP).
In object-oriented programming, the LSP states that if a supertype defines a certain rule, all subtypes must also follow that rule.
It intends to guarantee the semantic interoperability of classes of the same hierarchy.

Consider a certain method of your application that deals with objects of a class, and receives one as argument.
The method will perform operations on this object.
It is probably implemented with the assumption that the object follows the rules of its class.
However, an object of a subclass can be passed as argument to that method.
If you choose not to follow the LSP, this object may not follow the rules that the method expects it to.
Then, the method may have an unexpected behavior.
Therefore, if you wish to respect the LSP, you should only override a predicate to make a rule more restrictive, and never more permissive.
Alternatively, you can also decide to make all your predicates final, and keep them from ever being overridden.

## Tracking Existing Inconsistencies

This section briefly summarizes the behavior of the framework when you create a new predicate in the code of your application.
It explains some of the entities that the framework creates and manages, to keep track of your inconsistent objects.
You may use these framework entities to easily access all the existing inconsistent objects.

Whenever you introduce a new consistency predicate in a certain domain class, the framework will detect this new predicate.
Then, for each existing object of that domain class, the framework will execute the predicate and create a DomainDependenceRecord.
Each of these existing objects may have been consistent or not.
If an object is consistent, its DomainDependenceRecord will keep it consistent in the future.
If an object is inconsistent, its DomainDependenceRecord will prevent operations from changing it, unless it is corrected.
Either way, each DomainDependenceRecord keeps information about the consistency of its object.

So, if you need to know if an object is inconsistent, you can iterate through all its DomainDependenceRecords.
An object will have one DomainDependenceRecord for each predicate that its class defines.
If any DomainDependenceRecord has a false value in the consistent slot, then the object is inconsistent according to that predicate.
Here's an example of how to determine if an object is inconsistent, by using the DomainDependenceRecords:
    for(PersistentDependenceRecord record : myObject.getMetaObject().getDependenceRecords()) {
        if (!record.isConsistent()) {
            System.out.println("The object " + myObject + 
                " is inconsistent according to " + record.getPredicate());
        }
    }

But you may also want to find, among all the existing objects of a class, which ones are inconsistent according to a certain consistency predicate.
You will need to obtain the framework’s entity that represents that consistency predicate.
To do so, you will need to use the DomainConsistencyPredicate’s static readKnownConsistencyPredicate() method.
This method receives as argument the class and the name of the consistency predicate.
Here's an example of the invocation of this method to obtain a consistency predicate:
    DomainConsistencyPredicate consistencyPredicate = DomainConsistencyPredicate
        .readKnownConsistencyPredicate(Account.class, "checkBalancePositive");

Once you have the DomainConsistencyPredicate that you want, you can call the getInconsistentDependenceRecords() method.
This method will provide you with a complete set of DomainDependenceRecords of all the objects that are inconsistent, according to that predicate.
You can then access and correct these objects whenever you see fit.
Here's an example of how to obtain all inconsistent objects of a certain predicate:
    for (PersistentDependenceRecord record : consistencyPredicate.getInconsistentDependenceRecords()) {
        System.out.println("The object " + record.getDependent() +
            " is inconsistent according to " + record.getPredicate());
    }

Whenever you remove an existing predicate from your code, the framework will detect the missing predicate, and remove its DomainDependenceRecords.
Whenever you introduce a new predicate on a domain class, the framework will detect and reexecute the predicate for all existing objects of that class.
Also, whenever you change the name or signature of an existing predicate, the framework will detect the change and reexecute the predicate.
So, the framework will keep the PersistentDependenceRecords, and the list of inconsistent objects up-to-date.
However, the framework does not yet detect code changes to the implementation of a method’s body.
Therefore, if you change the implementation of a consistency predicate, the framework will not automatically reexecute it.
Still, you can always change the signature of the predicate to force the framework to reexecute it.

## Using Inconsistency Tolerance

This section explains how you can make the framework’s transactions tolerant to already-existing inconsistencies.
It also explains under what circumstances can inconsistency-tolerant transactions be useful.
As seen previously, since the framework detects new predicates in your code, it may happen that existing objects are already inconsistent.
Any transaction that changes objects will execute the predicates depending on these objects.
If the objects were previously inconsistent (and not corrected), the predicate will return false and the transaction will abort.
This means that the operation will not produce any effects, and will simply throw an exception.
However, this transaction does not necessarily create new inconsistencies.
It may simply perform operations on some objects that another event had made inconsistent before.
The transaction is not responsible for the inconsistencies that it passed through.
And yet, the transaction will abort and present an error to the user, because a certain predicate returned false.
So, a new predicate inserted on inconsistent objects may prevent many operations from being successful, which may endanger your system’s liveness.

To avoid this problem, you can make the predicates of your choice inconsistency-tolerant.
The @ConsistencyPredicate annotation can receive a value argument to specify the type of exception that is thrown.
The @ConsistencyPredicate also has a boolean inconsistencyTolerant argument to specify if the predicate is tolerant to inconsistencies, or not.
The default value of this last argument is false; by default, predicates are not inconsistency-tolerant.
If you explicitly set the argument to true, the annotated predicate will become inconsistency-tolerant.
Consider a certain predicate that is now inconsistency-tolerant, and a transaction that writes to an already-inconsistent object.
The execution of the predicate will return false, representing the already-inconsistent object that was not corrected.
But the dependence record already stored a false value in its consistent slot.
So, the transaction will commit and produce its effects, because it knows that it did not create this inconsistency.
Otherwise, any transaction that changes a consistent object and makes it inconsistent will always abort.

