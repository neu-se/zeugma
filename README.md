# Zeugma

A parametric fuzzer with crossover.

## Requirements

* Java Development Kit 9+
* [Apache Maven](https://maven.apache.org/) 3.6.0+

## Building

1. Ensure that some version of OpenJDK 9+ is installed.
2. Set the JAVA_HOME environmental variable to the path to this installation. For example, on linux,
   run `export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64`.
3. Clone or download this repository.
4. In the root directory for this project, run `mvn -DskipTests install`.

## Running a Fuzzing Campaign

After this project has been built, run:

```
mvn -pl :zeugma-experiments meringue:fuzz meringue:analyze -P<SUBJECT>,<FRAMEWORK>
```

Where:

* \<FRAMEWORK\> is one of the following: "zest" or "zeugma".
* \<SUBJECT\> is one of the following: "ant", "bcel", "closure", "maven", "nashorn", "rhino", or "tomcat".

## Computing Heritability Metrics

After this project has been built, run:

```
mvn -pl :zeugma-experiments -Pheritability -Dheritability.corporaDir=<C> -Dheritability.outputFile=<O> verify
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
* [Apache Harmony](https://harmony.apache.org), (c) The Apache Software
  Foundation, [license](http://www.apache.org/licenses/LICENSE-2.0)