# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Changed
- Updated `org.ow2.asm:asm` from `9.5` to `9.6`

## [0.2.6]
### Changed
- Updated `org.ow2.asm:asm` from `9.3` to `9.5`
- Updated `com.google.code.gson:gson` from `2.9` to `2.10.1`
- Updated override of `org.yaml:snakeyaml` transitive dependency from `1.30` to `1.33`

## [0.2.0]
### Changed
- (GH-28) Record files which failed to parse in a report, instead of failing the entire analysis
- Updated `com.google.guava:guava` from `31.0-jre` to `31.1-jre`
- Updated `org.ow2.asm:asm` from `9.2` to `9.3`
- Updated `org.slf4j:slf4j-api` from `1.7.32` to `1.7.36`
- Updated `org.slf4j:slf4j-simple` from `1.7.32` to `1.7.36`
- Updated `org.testng:testng` from `7.4.0` to `7.5`
- Updated override of `org.yaml:snakeyaml` transitive dependency from `1.29` to `1.30`

## [0.1.7]
### Changed
- Updated `com.google.guava:guava` from `30.1.1-jre` to `31.0-jre`

## [0.1.6]
### Changed
- Updated `com.google.code.gson:gson` from `2.8.7` to `2.8.8`

## [0.1.5]
### Changed
- Updated `org.ow2.asm:asm` from `9.1` to `9.2`

## [0.1.4]
### Changed
- Updated `com.google.code.gson:gson` from `2.8.6` to `2.8.7`
- Updated `org.slf4j:slf4j-api` and associated libraries from `1.7.30` to `1.7.31`
- Updated override of transitive dependency on `org.yaml:snakeyaml` from `1.28` to `1.29`

## [0.1.3]
### Changed
- Updated `com.google.guava:guava` from `30.0-jre` to `30.1.1-jre`
- Updated `org.ow2.asm:asm` from `9.0` to `9.1`
- Updated `org.testng:testng` from `7.3.0` to `7.4.0`
- Updated override of transitive dependency on `org.yaml:snakeyaml` from `1.26` to `1.28`

## [0.1.2]
### Changed
- Updated `com.google.guava:guava` and `org.ow2.asm:asm` dependencies to latest versions (`30.0-jre` and `9.0`, respectively)

## [0.1.1]
### Changed
- (GH-22) Added automatic cleanup of temporary files

## [0.1.0]
### Added
- Ability to analyze built source for external method calls, outputting to a formatted report
