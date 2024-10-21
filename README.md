# Jenkins JCasC Queue Job Plugin

This Jenkins plugin enables queueing jobs from the [JCasC](https://www.jenkins.io/projects/jcasc/) config using [Job DSL](https://plugins.jenkins.io/job-dsl/).  
Job DSL on its own can already queue jobs, but since JCasC runs before the Jenkins Queue is initialized any jobs queued during this initialization phase are discarded.  
This plugin defers jobs queued by Job DSL during initialization until after Jenkins has fully loaded.

### Usage

To queue a job via JCasC simply use the following example snippet:
```yaml
jobs:
  - script: |
      queue("my_job")
```

### Development

Starting a development Jenkins instance with this plugin: `mvn hpi:run`

Building the plugin: `mvn package`
