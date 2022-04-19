
Cryptopolitics Selection
------------------------

![Software under MIT License](https://opensource.org/licenses/MIT)

This is version 1.1 of the Cryptopolitics Selection microservice.
It's made public so that anybody can replay Cryptopolitics talon transitions.

It is written in Java 17 with Micronaut, using Gradle as build manager.

More documentation can be found in subdirectory [/docs](docs) 

### Compilation

If you have Java 17 installed, you can compile it using

```shell
./gradlew build
```

Otherwise, you can use Docker to build this service in a container

```shell
docker build -t "cryptopolitics-selection:1.1" .
```

### Execution

The Micronaut microservice can be run straight from Gradle

```shell
export MICRONAUT_ENVIRONMENTS=dev
./gradlew run
```

or using Docker

```shell
docker run -e MICRONAUT_ENVIRONMENTS=dev "cryptopolitics-selection:1.1"
```

### Usage

To verify a transition document, send it to the `/talon/checkTransformations` 
endpoint, that will return either a `200` status if it computes the same
results by evaluating the embedded transformations starting from the 
talonBefore embedded state and embedded seed value, or a `409` if the
results don't match.

You can also use the `/talon/applyTransformations` to get the results and
end talon state from evaluating a list of transformations on a talon state.
The seedGeneratorName used in the talon will be used to generate the random
seeds used during the evaluation. It must reference a generator described 
in the microservice configuration `talon.generators`.

Finally, you can query the `/talon/seedGenerator` endpoint to check how the
provable random seeds generator works. Again the generator name must match
one described in the microservice configuration.
