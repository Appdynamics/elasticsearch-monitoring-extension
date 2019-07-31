package AE_ElasticsearchMonitoringExtension.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

object elasticsearchmonitoringextensionci : GitVcsRoot({
    uuid = "78e9d2db-c19a-4f6e-9998-acc41145b986"
    name = "elasticsearchmonitoringextensionci"
    url = "ssh://git@bitbucket.corp.appdynamics.com:7999/ae/elasticsearch-monitoring-extension.git"
    branch = "refs/heads/3.0.0"
    authMethod = uploadedKey {
        uploadedKey = "TaamCity BitBucket Key"
    }
})
