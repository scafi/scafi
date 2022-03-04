var workflowCmd = '::set-output name=has-released::'
var publishCmd = `
git tag -a -f v\${nextRelease.version} v\${nextRelease.version} -F CHANGELOG.md
git push --force origin v\${nextRelease.version} || exit 6
sbt ci-release
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
