package AE_ElasticsearchMonitoringExtension.buildTypes

import AE_ElasticsearchMonitoringExtension.publishCommitStatus
import AE_ElasticsearchMonitoringExtension.triggerAfter
import AE_ElasticsearchMonitoringExtension.vcsRoots.AE_ElasticsearchMonitoringExtension
import AE_ElasticsearchMonitoringExtension.withDefaults
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2018_2.FailureAction
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object AE_ElasticsearchMonitoringExtension_IntegrationTests : BuildType({
    uuid = "e62c4c88-f680-4954-ad5a-7e9b6c060d47"
    name = "Run Integration Tests"

    withDefaults()

    steps {
        exec {
            path = "make"
            arguments = "dockerRun sleep"
        }
        maven {
            goals = "clean verify -DskipITs=false"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.JDK_18%"
        }
        exec {
            path = "make"
            arguments = "dockerStop"
        }
        exec {
            executionMode = BuildStep.ExecutionMode.ALWAYS
            path = "make"
            arguments = "dockerClean"
        }
    }

    triggers {
        vcs {
        }
    }

    artifactRules = """
        /opt/buildAgent/work/machine-agent-logs => target/
""".trimIndent()

    dependencies {
        dependency(AE_ElasticsearchMonitoringExtension_Build) {
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
            artifacts {
                artifactRules = """
                +:target/ElasticsearchMonitor-*.zip => target/
            """.trimIndent()
            }
        }
    }

    publishCommitStatus()

    triggerAfter(AE_ElasticsearchMonitoringExtension_Build)
})
