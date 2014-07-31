
grails.project.work.dir = "target"
grails.project.fork = false

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {

    inherits("global")

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
    }

    plugins {
        build ':release:3.0.1', {
            export = false
        }
    }
}
