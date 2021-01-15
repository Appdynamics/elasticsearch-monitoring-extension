package AE_ElasticsearchMonitoringExtension.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object AE_ElasticsearchMonitoringExtension : GitVcsRoot({
    uuid = "5afaf19d-251b-4ed8-baef-6295232941e2"
    id("AE_ElasticsearchMonitoringExtension")
    name = "AE_ElasticsearchMonitoringExtension"
    url = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/elasticsearch-monitoring-extension.git"
    pushUrl = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/elasticsearch-monitoring-extension.git"
    authMethod = uploadedKey {
        uploadedKey = "TeamCity BitBucket Key"
    }
    agentCleanPolicy = AgentCleanPolicy.ALWAYS
    branchSpec = """
    +:refs/heads/(master)
    +:refs/(pull-requests/*)/from
    """.trimIndent()
})
