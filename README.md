To reset repo:
`git fetch origin && git switch main && git reset --hard origin/main && git clean -fdx && (cd demos-coghealth-ehr-web && npm i)`

To run locally:
Ensure docker is running. Then run:
`bash ./start.sh`

To stop locally:
`bash ./stop.sh