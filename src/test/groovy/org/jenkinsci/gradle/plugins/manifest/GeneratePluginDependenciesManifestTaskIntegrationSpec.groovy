package org.jenkinsci.gradle.plugins.manifest

import org.gradle.testkit.runner.TaskOutcome
import org.jenkinsci.gradle.plugins.jpi.IntegrationSpec
import org.jenkinsci.gradle.plugins.jpi.TestDataGenerator
import org.jenkinsci.gradle.plugins.jpi.TestSupport
import spock.lang.Unroll

import static org.jenkinsci.gradle.plugins.jpi.TestSupport.ant
import static org.jenkinsci.gradle.plugins.jpi.TestSupport.log4jApi

class GeneratePluginDependenciesManifestTaskIntegrationSpec extends IntegrationSpec {
    private final String projectName = TestDataGenerator.generateName()
    private final String taskName = GeneratePluginDependenciesManifestTask.NAME
    private final String taskPath = ':' + taskName
    private static final String MIN_BUILD_FILE = """\
            plugins {
                id 'org.jenkins-ci.jpi'
            }
            """.stripIndent()
    private static final String BUILD_FILE = """\
            $MIN_BUILD_FILE
            jenkinsPlugin {
                jenkinsVersion.set('${TestSupport.RECENT_JENKINS_VERSION}')
            }
            """.stripIndent()
    private File build

    def setup() {
        File settings = projectDir.newFile('settings.gradle')
        settings << """rootProject.name = \"$projectName\""""
        build = projectDir.newFile('build.gradle')
    }

    @Unroll
    def 'should rerun only if #config plugin dependencies change'(String config, String before, String after, TaskOutcome secondRun) {
        given:
        build.text = """\
            $BUILD_FILE
            java {
                registerFeature('ant') {
                    usingSourceSet(sourceSets.main)
                }
            }
            dependencies {
                $config $before
            }
            """.stripIndent()

        when:
        def result = gradleRunner()
                .withArguments(taskName)
                .build()

        then:
        result.task(taskPath).outcome == TaskOutcome.SUCCESS

        when:
        build.text = """\
            $BUILD_FILE
            java {
                registerFeature('ant') {
                    usingSourceSet(sourceSets.main)
                }
            }
            dependencies {
                $config $after
            }
            """.stripIndent()
        def rerunResult = gradleRunner()
                .withArguments(taskName)
                .build()

        then:
        rerunResult.task(taskPath).outcome == secondRun

        where:
        config              | before             | after              | secondRun
        'api'               | ant('1.10')        | ant('1.11')        | TaskOutcome.SUCCESS
        'implementation'    | ant('1.10')        | ant('1.11')        | TaskOutcome.SUCCESS
        'runtimeOnly'       | ant('1.10')        | ant('1.11')        | TaskOutcome.SUCCESS
        'antApi'            | ant('1.10')        | ant('1.11')        | TaskOutcome.SUCCESS
        'antImplementation' | ant('1.10')        | ant('1.11')        | TaskOutcome.SUCCESS
        'antRuntimeOnly'    | ant('1.10')        | ant('1.11')        | TaskOutcome.SUCCESS
        // non-plugin changes shouldn't force this to rerun
        'api'               | log4jApi('2.13.0') | log4jApi('2.14.0') | TaskOutcome.UP_TO_DATE
        'implementation'    | log4jApi('2.13.0') | log4jApi('2.14.0') | TaskOutcome.UP_TO_DATE
        'runtimeOnly'       | log4jApi('2.13.0') | log4jApi('2.14.0') | TaskOutcome.UP_TO_DATE
        'antApi'            | log4jApi('2.13.0') | log4jApi('2.14.0') | TaskOutcome.UP_TO_DATE
        'antImplementation' | log4jApi('2.13.0') | log4jApi('2.14.0') | TaskOutcome.UP_TO_DATE
        'antRuntimeOnly'    | log4jApi('2.13.0') | log4jApi('2.14.0') | TaskOutcome.UP_TO_DATE
    }

}
