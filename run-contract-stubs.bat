@echo off
setlocal enabledelayedexpansion

set "MVN_HOME=X:\.m2"
set "SC_CONTRACT_DOCKER_VERSION=4.2.0"

if not defined STUB_GROUP set "STUB_GROUP=com.tn.service"
if not defined STUB_ARTIFACT set "STUB_ARTIFACT=tn-user-service"
if not defined STUB_VERSION set "STUB_VERSION=1.0.00-SNAPSHOT"
set "STUB_PORT=9876"

REM Determine STUB_RUNNER_MODE from first argument or default to REMOTE
if "%~1"=="" (
    if not defined STUB_RUNNER_MODE set "STUB_RUNNER_MODE=REMOTE"
) else (
    set "STUB_RUNNER_MODE=%~1"
)

set "STUBRUNNER_REPOSITORY_ROOT="
if not defined STUBRUNNER_PORT set "STUBRUNNER_PORT=8083"
set "STUBRUNNER_IDS=%STUB_GROUP%:%STUB_ARTIFACT%:%STUB_VERSION%:stubs:%STUB_PORT%"

if /I "%STUB_RUNNER_MODE%"=="LOCAL" (
    echo Running in LOCAL mode.
    docker run --rm ^
        -v %MVN_HOME%:/home/scc/.m2 ^
        -e STUBRUNNER_STUBS_MODE=LOCAL ^
        -e STUBRUNNER_IDS=%STUBRUNNER_IDS% ^
        -e SERVER_PORT=%STUBRUNNER_PORT% ^
        -p %STUBRUNNER_PORT%:%STUBRUNNER_PORT% ^
        -p %STUB_PORT%:%STUB_PORT% ^
        springcloud/spring-cloud-contract-stub-runner:%SC_CONTRACT_DOCKER_VERSION%
) else if /I "%STUB_RUNNER_MODE%"=="REMOTE" (
    echo Running in REMOTE mode.
    docker run --rm ^
        -e STUBRUNNER_REPOSITORY_ROOT=%STUBRUNNER_REPOSITORY_ROOT% ^
        -e STUBRUNNER_IDS=%STUBRUNNER_IDS% ^
        -e SERVER_PORT=%STUBRUNNER_PORT% ^
        -p %STUBRUNNER_PORT%:%STUBRUNNER_PORT% ^
        -p %STUB_PORT%:%STUB_PORT% ^
        springcloud/spring-cloud-contract-stub-runner:%SC_CONTRACT_DOCKER_VERSION%
) else (
    echo Unknown mode: %STUB_RUNNER_MODE%.
)
endlocal
