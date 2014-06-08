# COMP3608 - Assignment 2 Code

## Variable Elimination

To run the Variable Elimination algorithm, create a VE Object and pass in the elimination ordering as arguments. The arguments are of VariableType, an enumeration type and represents the nodes in the Bayes Net.

```java
VE ve = new VE(VariableType.M, VariableType.S, VariableType.C, VariableType.B, VariableType.I);
```

To set an evidence observation before computing a query, simply use the follow command:

```java
ve.setEvidenceObservation(VariableType.S, true);
```

To actually compute a query, simply call the following method:

```java
double result = ve.computeQuery(VariableType.M);
```

That computes the probability, given any evidence supplied, of M being `true`. To know the probability of M being `false`, simply take `1 - result`.

Finally, if you would like to see a more detailed breakdown of the steps involved and the states of the Factor tables when computing a query then turn on debugging:

```java
ve.turnOnDebugOutput();
```

That will log to the console the state of all the Factor tables for each elimination.


## MCMC

To run the MCMC algorithm, it is very similar to Variable Elimination. Simply create a MCMC Object and pass in the number of iterations per MCMC, N and the number of times MCMC should be run, M. Here MCMC is setup to run for 100 iterations, and will be repeated 1000 times and an average taken.

```java
MCMC mc = new MCMC(100, 1000);
```

To set an evidence observation before computing a query, simply use the following command stating the variable observed, and the actual observed value.

```java
mc.setEvidenceObservation(VariableType.S, true);
```

To actually compute a query, simply call the following method:

```java
double result = mc.computeQuery(VariableType.M);
```

That computes the probability, given any evidence supplied, of M being `true`. To know the probability of M being `false`, simply take `1 - result`.
