# Method Use Analyzer

![GitHub Actions](https://github.com/blackducksoftware/method-use-analyzer/workflows/Java%20CI/badge.svg?branch=master) [![Black Duck Security Risk](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/method-use-analyzer/branches/master/badge-risk.svg)](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/method-use-analyzer/branches/master) [![Changelog validated by Chronicler](https://chronicler.starchartlabs.org/images/changelog-chronicler-success.png)](https://chronicler.starchartlabs.org/) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The method use analyzer library evaluates a Java project and produces a report of the method calls made to Java code not present within the project

This library is currently in BETA, and will proceed to GA with the following guidelines:
- BETA: Some automated testing is in place for basic functionality, greater consideration is given to incompatible API changes
- Initial GA: Majority of functionality is tested, library will follow semantic versioning guidelines from this point in regards to API compatiblity

## Use

To use this library, it must be added as a dependency reference, and then invoked within the consuming project:

### Dependency Reference

This library is available in Maven Central under the GAV `com.blackducksoftware:method-analyzer-core:<version>`

### Analysis Execution

The primary use of the method analyzer is to find external method calls from a given set of class files, and create a report of them. This can be done via:

```
MethodUseAnalyzer analyzer = new MethodUseAnalyzer();
Path outputReportFile = analyzer.analyze(sourceDirectoryPath, outputDirectoryPath, projectName);
```

Where `sourceDirectoryPath` is the directory containing (either directly, or recursively) the Java `*.class` files to analyze, `outputDirectoryPath` is the directory to save the report to, and `projectName` is an optional human-readable label to associate with the analyzed files in generated reports. A custom name for the report file may be specified via the `MethodUseAnalyzer.analyze(Path, Path, String, String)` function, in place of `MethodUseAnalyzer.analyze(Path, Path, String)`

### Output

The output of the method use analyzer is a report file, whose formatting is documented [here](./docs/REPORT_FORMAT.md)

## Contributing

Information for how to contribute to this library can be found in [the contribution guidelines](./docs/CONTRIBUTING.md)

## Reporting Vulnerabilities

If you discover a security vulnerability, contact the development team by e-mail at `oss@blackduck.com`

## Legal

This library is distributed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0). The only requirement for use is inclusion of the following line within your NOTICES file:

```
method-use-analyzer
Copyright 2020 Black Duck Software, Inc.

This product includes software developed at
Black Duck Software, Inc. (http://www.blackduck.com/).
```

The requirement for a copy of the license being included in distributions is fulfilled by a copy of the [LICENSE](./LICENSE) file being included in constructed JAR archives

