# CI and the self-hosted runner

Three workflows run on pushes to `main` and on pull requests. Each runs only when its part of
the tree changes.

| Workflow | Runs when these change | What it does |
|----------|------------------------|--------------|
| Shared   | `shared/` | Shared tests on the Android host and the iOS simulator, lint |
| Android  | `android/`, `shared/` | Builds the app, unit tests, lint |
| iOS      | `ios/`, `shared/` | Generates the Xcode project, builds, unit tests |

## Which machine runs a job

This is a public repository, and a self-hosted runner executes whatever code the workflow
checks out. So:

- **Pushes, and pull requests from branches in this repository,** run on the self-hosted Mac
  (labels `self-hosted`, `macOS`, `ARM64`). Only people with write access can create these.
- **Pull requests from forks** run on GitHub-hosted runners. They never reach the Mac.

The `runs-on` expression at the top of each workflow makes that choice. Do not replace it with
a plain `self-hosted`. The repository is also set to require a maintainer's approval before any
workflow runs for an outside contributor (Settings > Actions > General).

If no self-hosted runner is online, trusted jobs wait in the queue until one is.

## Setting up a runner

On an Apple silicon Mac with the tools from the README installed:

1. In the repository on GitHub, go to Settings > Actions > Runners > New self-hosted runner,
   choose macOS / ARM64, and follow the download and `./config.sh` steps it shows. Accept the
   default labels. The registration token on that page is a secret and expires in an hour.
2. Tell the runner where the Android SDK is. Jobs do not load your shell profile. In the
   runner's directory:

       echo "ANDROID_HOME=$HOME/Library/Android/sdk" >> .env

3. Start it. Either run it in a terminal when you want CI to run:

       ./run.sh

   or install it as a background service that starts at login:

       ./svc.sh install && ./svc.sh start

The runner needs Xcode with an iOS simulator runtime, the Android SDK, and xcodegen. The
workflows install their own JDK.

## Removing a runner

    ./svc.sh stop && ./svc.sh uninstall     # if installed as a service
    ./config.sh remove --token <token from the Runners settings page>
