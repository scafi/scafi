import config from 'semantic-release-preconfigured-conventional-commits' with {type: 'json'}

const publishCmd = `
git tag -a -f v\${nextRelease.version} v\${nextRelease.version} -F CHANGELOG.md  || exit 1
export CI_COMMIT_TAG="true"
sbt ci-release || exit 2
`
config.plugins.push(
    [
        "@semantic-release/exec",
        {
            "publishCmd": publishCmd,
        }
    ],
    "@semantic-release/github",
    "@semantic-release/git",
)
config.tagFormat = "v${version}"

export default config