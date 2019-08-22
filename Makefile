docker-clean:
	@echo deleting containers
	docker rm `docker ps -q -f status=exited`
	@echo "Delete all untagged/dangling (<none>) images"
	# remove untagged or all images
	docker rmi `docker images -q -f dangling=true`

dockerRun:
	@echo "------- Starting elasticsearch cluster -------"
	docker-compose up --force-recreate -d elasticsearch elasticsearch2
	@echo "------- Elasticsearch cluster is up -------"
	@echo "------- Starting controller -------"
	docker-compose up --force-recreate -d controller
	# wait until it installs controller and ES
	sleep 600
	@echo "------- Controller started -------"
	# uncomment docker exec  and sleep lines to enable port 9200
	## bash into the controller controller, change props to enable port 9200
	#docker exec controller /bin/bash -c "sed -i s/ad.es.node.http.enabled=false/ad.es.node.http.enabled=true/g events-service/processor/conf/events-service-api-store.properties"
	## restart ES to make the changes reflect
	#docker exec controller /bin/bash -c "pa/platform-admin/bin/platform-admin.sh submit-job --platform-name AppDynamicsPlatform --service events-service --job restart-cluster"
	#sleep 60
	@echo ------- Starting machine agent -------
	docker-compose up --force-recreate -d --build machine
	@echo ------- Machine agent started -------

dockerStop:
	## stop and remove all containers
	sleep 60
	@echo ------- Stop and remove containers, images, networks and volumes -------
	docker-compose down --rmi all -v
	@echo ------- Done -------

sleep:
	@echo ------- Waiting for 5 minutes to read the metrics -------
	sleep 300
	@echo ------- Wait finished -------