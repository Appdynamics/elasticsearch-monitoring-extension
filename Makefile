dockerRun:
	@echo "------- Starting elasticsearch cluster -------"
	docker-compose up -d --force-recreate elasticsearch elasticsearch2
	@echo "------- Elasticsearch cluster is up -------"
	@echo "------- Starting controller -------"
	docker-compose up -d --force-recreate controller
# wait until it installs controller and ES
	sleep 600
	@echo "------- Controller started -------"
	@echo ------- Starting machine agent -------
	docker-compose up --force-recreate -d --build machine
	@echo ------- Machine agent started -------

dockerStop:
# stop and remove all containers
	sleep 60
	@echo ------- Stop and remove containers, images, networks and volumes -------
	docker-compose down --rmi all -v --remove-orphans
	docker rmi dtr.corp.appdynamics.com/appdynamics/machine-agent:latest
	docker rmi alpine
	@echo ------- Done -------

sleep:
	@echo ------- Waiting for 5 minutes to read the metrics -------
	sleep 300
	@echo ------- Wait finished -------

workbenchTest: ##test workbench mode
	@echo "Creating docker container for workbench"
	docker build -t 'workbench:latest' --no-cache -f Dockerfile_WorkBench .
	docker run --name workbench -d workbench
	@echo "Done"
# wait 60 seconds for workbench to report metrics
	sleep 60
	@echo "Checking /api/metric-paths"
	@out=$$(docker exec workbench /bin/sh -c "curl -s -w '\n%{http_code}\n' localhost:9090/api/metric-paths"); \
	printf "*****/api/metric-path returned*****\n%s\n**********\n" "$$out"; \
	code=$$(echo "$$out"|tail -1); \
	[ "$$code" = "200" ] || { echo "Failure: code=$$code"; exit 1; }; \
	[ "$$(echo "$$out"|grep ".*HeartBeat.*")" = "Custom Metrics|Elasticsearch|Server1|HeartBeat" ] || { echo "Hear Beat metric not found"; exit 1; }
	@echo "Workbench Tested successfully"
	@echo "Stopping docker container workbench"
	docker stop workbench
	docker rm workbench
	docker rmi dtr.corp.appdynamics.com/appdynamics/machine-agent:latest
	docker rmi alpine

dockerClean: ##Clean any left over containers, images, networks and volumes
	@if [[ -n "`docker ps -q`" ]]; then \
	docker stop `docker ps -q`; \
	fi
	docker rm -f `docker ps -a -q` || echo 0
	docker system prune -f -a --volumes
