run:
	docker-compose up

reload:
	docker-compose down
	docker-compose up

load-data:
	docker exec -i comp30860_frontend_project-mysqldb-1 mysql -uroot -ppassword example < fill.sql