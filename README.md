# Zeugma

A parametric fuzzer that uses call tree information to select crossover points.

## Requirements

* OpenJDK 9+
* [Apache Maven](https://maven.apache.org/) 3.6.0+

## Setup

1. Ensure that some version of OpenJDK 9+ is installed.
2. Set the JAVA_HOME environmental variable to the path to this installation.
   For example, on Linux, run `export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64`.
3. Clone or download this repository.

## Building

In the root directory of this project, run:

```
mvn -DskipTests install
```

## Running a Fuzzing Campaign

After this project has been built, run:

```
mvn -pl :zeugma-evaluation-subjects 
meringue:fuzz meringue:analyze 
-P<SUBJECT>,<FUZZER> 
-Dmeringue.outputDirectory=<O> 
-Dmeringue.duration=<D>
```

Where:

* \<SUBJECT\> is the fuzzing target: ant, bcel, closure, maven, nashorn, rhino, or tomcat.
* \<FUZZER\> is the fuzzer to be used: zeugma-none, zeugma-linked, zeugma-one_point, or zeugma-two_point.
* \<O\> is the path of the directory to which the output files should be written.
* \<D\> is the maximum amount of time to execute the fuzzing campaign for specified in the ISO-8601 duration
  format (e.g., "P2DT3H4M" represents 2 days, 3 hours, and 4 minutes).

## Computing Heritability Metrics

After this project has been built, run:

```
mvn -pl :zeugma-evaluation-heritability 
dependency:properties exec:instrument exec:compute
-Dheritability.corporaDir=<C>
-Dheritability.outputFile=<O>
```

Where:

* \<C\> is the path of the directory to scan for fuzzing campaign corpora.
* \<O\> path of file to which the results should be written in CSV format.

## License

This software release is licensed under the BSD 3-Clause License.

Copyright (c) 2023, Katherine Hough and Jonathan Bell.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

## Acknowledgements

Zeugma makes use of the following libraries:

* [ASM](http://asm.ow2.org/license.html), (c) INRIA, France
  Telecom, [license](http://asm.ow2.org/license.html)