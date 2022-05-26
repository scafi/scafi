var workflowCmd = '::set-output name=has-released::'
//CI_COMMIT_TAG to avoid snapshots
var publishCmd = `
git tag -a -f v\${nextRelease.version} v\${nextRelease.version} -F CHANGELOG.md  || exit 1
export CI_COMMIT_TAG="true" || exit 2
sbt ci-release || exit 3
git push --force origin v\${nextRelease.version} || exit 4
echo '${workflowCmd}true' 
`
console.log(`${workflowCmd}false`)
var config = require('semantic-release-preconfigured-conventional-commits');
config.tagFormat = 'v${version}'
config.plugins.push(
    ["@semantic-release/exec", {
        "publishCmd": publishCmd,
    }],
    ["@semantic-release/github", {
        "assets": [
            { "path": "build/shadow/*-all.jar" },
        ]
    }],
    "@semantic-release/git",
)
module.exports = config
