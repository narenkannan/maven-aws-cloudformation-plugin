![Under Construction](http://www.heatcontrols.co.in/Page%20Under%20Construction.jpg)

# Maven plugin for creating AWS Cloudformation Stacks using Cloudformation Template




## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

#### Quickly Configuring the AWS CLI

For general use, the aws configure command is the fastest way to set up your AWS CLI installation. The following example shows sample values. Replace them with your own values as described in the following sections.

```
aws configure
AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
Default region name [None]: us-west-2
Default output format [None]: json

```

When you enter this command, the AWS CLI prompts you for four pieces of information (access key, secret access key, AWS Region, and output format). These are described in the following sections. The AWS CLI stores this information in a profile (a collection of settings) named default. The information in the default profile is used any time you run an AWS CLI command that doesn't explicitly specify a profile to use.

### Installing

Add the below section to your pom.xml file.

```
 <build>
    <plugins>
      <plugin>
        <groupId>com.nila.rana</groupId>
        <artifactId>cft-maven-plugin</artifactId>
        <version><!-- VERSION --></version>
      </plugin>
    </plugins>
  </build>
```

To run the plugin

```
mvn cft:create-stack
```

## Built With

* [JAVA 8](https://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html) - The JAVA framework used
* [Maven 3.6.3](https://maven.apache.org/) - Dependency Management
* [AWS CLI 2.0](https://maven.apache.org/) - AWS Command Line Interface


## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/narenkannan/4f58e604f3f3d0e9caa4980e9fa849b6) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/narenkannan/maven-aws-cloudformation-plugin/tags). 

## Authors

* ** Naren Kannan ** - [NILA-RANA](https://github.com/narenkannan)

See also the list of [contributors](https://github.com/narenkannan/maven-aws-cloudformation-plugin/graphs/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
